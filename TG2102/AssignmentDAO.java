package com.fot.ams.dao;

import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Assignment;
import com.fot.ams.model.AssignmentSubmission;
import com.fot.ams.tg2102.TG2102DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Assignment Management in TG2102.
 * Fully integrates with MySQL database 'faculty_academic_management'
 * and syncs results to the central 'evaluations' and 'marks' tables.
 */
public class AssignmentDAO {

    public AssignmentDAO() {
    }

    public void ensureSchema() {
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] Schema init notice: " + e.getMessage());
        }
    }

    private void ensureSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS assignments (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "course_id INT NOT NULL," +
                    "lecturer_id INT NOT NULL," +
                    "evaluation_id INT NULL," +
                    "title VARCHAR(200) NOT NULL," +
                    "instructions TEXT NULL," +
                    "max_marks DECIMAL(8,2) NOT NULL DEFAULT 100.00," +
                    "due_date DATETIME NULL," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'," +
                    "marks_released TINYINT(1) NOT NULL DEFAULT 0," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "KEY idx_course (course_id)," +
                    "KEY idx_lecturer (lecturer_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS assignment_submissions (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "assignment_id INT NOT NULL," +
                    "student_id INT NOT NULL," +
                    "file_path VARCHAR(1000) NOT NULL," +
                    "submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED'," +
                    "marks DECIMAL(10,2) NULL," +
                    "feedback TEXT NULL," +
                    "graded_by INT NULL," +
                    "graded_at DATETIME NULL," +
                    "UNIQUE KEY uq_assignment_student (assignment_id, student_id)," +
                    "KEY idx_student (student_id)," +
                    "CONSTRAINT fk_sub_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }

        addColumnIfNotExists(conn, "assignments", "evaluation_id", "INT NULL AFTER lecturer_id");
        addColumnIfNotExists(conn, "assignments", "max_marks", "DECIMAL(8,2) NOT NULL DEFAULT 100.00 AFTER instructions");
        addColumnIfNotExists(conn, "assignments", "marks_released", "TINYINT(1) NOT NULL DEFAULT 0 AFTER status");
        addColumnIfNotExists(conn, "assignments", "file_path", "VARCHAR(1000) NULL AFTER marks_released");
        addColumnIfNotExists(conn, "assignment_submissions", "graded_by", "INT NULL AFTER feedback");
        addColumnIfNotExists(conn, "assignment_submissions", "graded_at", "DATETIME NULL AFTER graded_by");
    }

    private void addColumnIfNotExists(Connection conn, String table, String column, String definition) {
        String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (Statement alt = conn.createStatement()) {
                        alt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                    }
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private Assignment mapResultSetToAssignment(ResultSet rs) throws SQLException {
        Assignment a = new Assignment();
        a.setId(rs.getInt("id"));
        a.setCourseId(rs.getInt("course_id"));
        a.setLecturerId(rs.getInt("lecturer_id"));
        int evalId = rs.getInt("evaluation_id");
        if (!rs.wasNull()) a.setEvaluationId(evalId);
        a.setTitle(rs.getString("title"));
        a.setInstructions(rs.getString("instructions"));
        a.setMaxMarks(rs.getDouble("max_marks"));
        a.setDueDate(rs.getTimestamp("due_date"));
        a.setStatus(rs.getString("status"));
        a.setMarksReleased(rs.getBoolean("marks_released"));
        a.setFilePath(rs.getString("file_path"));
        a.setCreatedAt(rs.getTimestamp("created_at"));

        a.setCourseCode(rs.getString("course_code"));
        a.setCourseName(rs.getString("course_name"));
        a.setLecturerName(rs.getString("lecturer_name"));
        a.setSubmissionCount(rs.getInt("submission_count"));
        return a;
    }

    private static final String BASE_ASSIGNMENT_SELECT =
            "SELECT a.*, c.course_code, c.course_name, u.full_name AS lecturer_name, " +
            "  (SELECT COUNT(*) FROM assignment_submissions sub WHERE sub.assignment_id = a.id) AS submission_count " +
            "FROM assignments a " +
            "JOIN courses c ON a.course_id = c.course_id " +
            "JOIN users u ON a.lecturer_id = u.user_id ";

    public List<Assignment> getAssignmentsByCourse(int courseId) {
        List<Assignment> list = new ArrayList<>();
        String sql = BASE_ASSIGNMENT_SELECT + "WHERE a.course_id = ? ORDER BY a.created_at DESC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToAssignment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load course assignments: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Assignment> getAssignmentsByLecturer(int lecturerId) {
        List<Assignment> list = new ArrayList<>();
        String sql = BASE_ASSIGNMENT_SELECT + "WHERE a.lecturer_id = ? ORDER BY a.created_at DESC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToAssignment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load lecturer assignments: " + e.getMessage(), e);
        }
        return list;
    }

    public Assignment getAssignmentById(int assignmentId) {
        String sql = BASE_ASSIGNMENT_SELECT + "WHERE a.id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToAssignment(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch assignment: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Integrates with central 'evaluations' table for assessment alignment.
     */
    private int syncEvaluation(Connection conn, int courseId, String title, double maxMarks) throws SQLException {
        String findSql = "SELECT evaluation_id FROM evaluations WHERE course_id = ? AND evaluation_name = ? AND evaluation_type = 'ASSIGNMENT'";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setInt(1, courseId);
            ps.setString(2, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int evalId = rs.getInt(1);
                    try (PreparedStatement upd = conn.prepareStatement("UPDATE evaluations SET max_marks = ? WHERE evaluation_id = ?")) {
                        upd.setDouble(1, maxMarks);
                        upd.setInt(2, evalId);
                        upd.executeUpdate();
                    }
                    return evalId;
                }
            }
        }

        int semester = 1;
        try (PreparedStatement psCourse = conn.prepareStatement("SELECT semester FROM courses WHERE course_id = ?")) {
            psCourse.setInt(1, courseId);
            try (ResultSet rs = psCourse.executeQuery()) {
                if (rs.next()) semester = rs.getInt(1);
            }
        }

        String insertSql = "INSERT INTO evaluations (course_id, evaluation_name, evaluation_type, max_marks, weightage_percent, semester, academic_year) " +
                "VALUES (?, ?, 'ASSIGNMENT', ?, 20.0, ?, '2024/2025')";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            ps.setString(2, title);
            ps.setDouble(3, maxMarks);
            ps.setInt(4, semester);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return 0;
    }

    public boolean createAssignment(Assignment assignment) {
        String sql = "INSERT INTO assignments (course_id, lecturer_id, evaluation_id, title, instructions, " +
                "max_marks, due_date, status, marks_released, file_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            conn.setAutoCommit(false);
            try {
                int evalId = syncEvaluation(conn, assignment.getCourseId(), assignment.getTitle(), assignment.getMaxMarks());
                assignment.setEvaluationId(evalId > 0 ? evalId : null);

                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, assignment.getCourseId());
                    ps.setInt(2, assignment.getLecturerId());
                    if (assignment.getEvaluationId() != null) ps.setInt(3, assignment.getEvaluationId());
                    else ps.setNull(3, Types.INTEGER);
                    ps.setString(4, assignment.getTitle());
                    ps.setString(5, assignment.getInstructions());
                    ps.setDouble(6, assignment.getMaxMarks());
                    if (assignment.getDueDate() != null) ps.setTimestamp(7, assignment.getDueDate());
                    else ps.setNull(7, Types.TIMESTAMP);
                    ps.setString(8, assignment.getStatus() != null ? assignment.getStatus() : "DRAFT");
                    ps.setBoolean(9, assignment.isMarksReleased());
                    ps.setString(10, assignment.getFilePath());

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) assignment.setId(keys.getInt(1));
                        }
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create assignment: " + e.getMessage(), e);
        }
    }

    public boolean updateAssignment(Assignment assignment) {
        String sql = "UPDATE assignments SET title = ?, instructions = ?, max_marks = ?, " +
                "due_date = ?, status = ?, file_path = ? WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, assignment.getTitle());
                ps.setString(2, assignment.getInstructions());
                ps.setDouble(3, assignment.getMaxMarks());
                if (assignment.getDueDate() != null) ps.setTimestamp(4, assignment.getDueDate());
                else ps.setNull(4, Types.TIMESTAMP);
                ps.setString(5, assignment.getStatus());
                ps.setString(6, assignment.getFilePath());
                ps.setInt(7, assignment.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update assignment: " + e.getMessage(), e);
        }
    }

    public boolean updateAssignmentStatus(int assignmentId, String status) {
        String sql = "UPDATE assignments SET status = ? WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, assignmentId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update assignment status: " + e.getMessage(), e);
        }
    }

    public boolean setMarksReleased(int assignmentId, boolean released) {
        String sql = "UPDATE assignments SET marks_released = ? WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setBoolean(1, released);
                    ps.setInt(2, assignmentId);
                    ps.executeUpdate();
                }

                if (released) {
                    syncAllGradedSubmissionsToMarks(conn, assignmentId);
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to set assignment marks release: " + e.getMessage(), e);
        }
    }

    private void syncAllGradedSubmissionsToMarks(Connection conn, int assignmentId) throws SQLException {
        String sql = "SELECT s.student_id, s.marks, a.evaluation_id, a.lecturer_id " +
                "FROM assignment_submissions s " +
                "JOIN assignments a ON s.assignment_id = a.id " +
                "WHERE s.assignment_id = ? AND s.status = 'GRADED' AND s.marks IS NOT NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    double marks = rs.getDouble("marks");
                    int evalId = rs.getInt("evaluation_id");
                    int enterer = rs.getInt("lecturer_id");
                    if (evalId > 0) {
                        saveOrUpdateCentralMark(conn, evalId, studentId, marks, enterer);
                    }
                }
            }
        }
    }

    private void saveOrUpdateCentralMark(Connection conn, int evaluationId, int studentId, double marksObtained, Integer enteredBy) throws SQLException {
        String sql = "INSERT INTO marks (evaluation_id, student_id, marks_obtained, entered_by) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained), " +
                "entered_by = VALUES(entered_by), updated_at = CURRENT_TIMESTAMP";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evaluationId);
            ps.setInt(2, studentId);
            ps.setDouble(3, marksObtained);
            if (enteredBy != null) ps.setInt(4, enteredBy);
            else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public boolean deleteAssignment(int assignmentId) {
        String checkSubmissions = "SELECT COUNT(*) FROM assignment_submissions WHERE assignment_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(checkSubmissions)) {
                ps.setInt(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new DatabaseException("Cannot delete this assignment because students have already submitted work. Close or unpublish it instead.");
                    }
                }
            }

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM assignments WHERE id = ?")) {
                del.setInt(1, assignmentId);
                return del.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete assignment: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // Student Submissions & Lecturer Grading
    // ==========================================

    public List<AssignmentSubmission> getSubmissionsByAssignment(int assignmentId) {
        List<AssignmentSubmission> list = new ArrayList<>();
        String sql = "SELECT s.*, u.student_reg_no, u.full_name AS student_name, " +
                "a.title AS assignment_title, a.max_marks, a.marks_released, c.course_code " +
                "FROM assignment_submissions s " +
                "JOIN users u ON s.student_id = u.user_id " +
                "JOIN assignments a ON s.assignment_id = a.id " +
                "JOIN courses c ON a.course_id = c.course_id " +
                "WHERE s.assignment_id = ? ORDER BY s.submitted_at DESC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        AssignmentSubmission sub = new AssignmentSubmission();
                        sub.setId(rs.getInt("id"));
                        sub.setAssignmentId(rs.getInt("assignment_id"));
                        sub.setStudentId(rs.getInt("student_id"));
                        sub.setFilePath(rs.getString("file_path"));
                        sub.setSubmittedAt(rs.getTimestamp("submitted_at"));
                        sub.setStatus(rs.getString("status"));
                        double m = rs.getDouble("marks");
                        if (!rs.wasNull()) sub.setMarks(m);
                        sub.setFeedback(rs.getString("feedback"));
                        sub.setGradedBy((Integer) rs.getObject("graded_by"));
                        sub.setGradedAt(rs.getTimestamp("graded_at"));

                        sub.setStudentRegNo(rs.getString("student_reg_no"));
                        sub.setStudentName(rs.getString("student_name"));
                        sub.setAssignmentTitle(rs.getString("assignment_title"));
                        sub.setMaxMarks(rs.getDouble("max_marks"));
                        sub.setMarksReleased(rs.getBoolean("marks_released"));
                        sub.setCourseCode(rs.getString("course_code"));
                        list.add(sub);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load assignment submissions: " + e.getMessage(), e);
        }
        return list;
    }

    public AssignmentSubmission getSubmissionByStudent(int assignmentId, int studentId) {
        String sql = "SELECT s.*, a.max_marks, a.marks_released " +
                "FROM assignment_submissions s " +
                "JOIN assignments a ON s.assignment_id = a.id " +
                "WHERE s.assignment_id = ? AND s.student_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, assignmentId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        AssignmentSubmission sub = new AssignmentSubmission();
                        sub.setId(rs.getInt("id"));
                        sub.setAssignmentId(rs.getInt("assignment_id"));
                        sub.setStudentId(rs.getInt("student_id"));
                        sub.setFilePath(rs.getString("file_path"));
                        sub.setSubmittedAt(rs.getTimestamp("submitted_at"));
                        sub.setStatus(rs.getString("status"));
                        double m = rs.getDouble("marks");
                        if (!rs.wasNull()) sub.setMarks(m);
                        sub.setFeedback(rs.getString("feedback"));
                        sub.setMaxMarks(rs.getDouble("max_marks"));
                        sub.setMarksReleased(rs.getBoolean("marks_released"));
                        return sub;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load student submission: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean submitAssignment(int assignmentId, int studentId, String filePath) {
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);

            // 1. Verify student enrollment
            String checkEnrollment = "SELECT COUNT(*) FROM course_enrollment ce " +
                    "JOIN assignments a ON ce.course_id = a.course_id " +
                    "WHERE a.id = ? AND ce.student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkEnrollment)) {
                ps.setInt(1, assignmentId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new DatabaseException("You are not registered in the course for this assignment.");
                    }
                }
            }

            // 2. Verify assignment status and deadline
            Assignment assignment = getAssignmentById(assignmentId);
            if (assignment == null) throw new DatabaseException("Assignment not found.");
            if (!assignment.isPublished()) throw new DatabaseException("This assignment is currently not accepting submissions.");
            if (assignment.isClosed()) throw new DatabaseException("This assignment has been closed by the lecturer.");

            String upsertSql = "INSERT INTO assignment_submissions (assignment_id, student_id, file_path, submitted_at, status) " +
                    "VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'SUBMITTED') " +
                    "ON DUPLICATE KEY UPDATE file_path = VALUES(file_path), submitted_at = CURRENT_TIMESTAMP, status = 'SUBMITTED'";
            try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
                ps.setInt(1, assignmentId);
                ps.setInt(2, studentId);
                ps.setString(3, filePath);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save assignment submission: " + e.getMessage(), e);
        }
    }

    public boolean gradeSubmission(int submissionId, double marks, String feedback, int lecturerId) {
        String getInfo = "SELECT s.assignment_id, s.student_id, a.max_marks, a.evaluation_id, a.marks_released " +
                "FROM assignment_submissions s " +
                "JOIN assignments a ON s.assignment_id = a.id " +
                "WHERE s.id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            conn.setAutoCommit(false);
            try {
                double maxMarks = 100.0;
                int evalId = 0;
                int studentId = 0;
                boolean marksReleased = false;

                try (PreparedStatement ps = conn.prepareStatement(getInfo)) {
                    ps.setInt(1, submissionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maxMarks = rs.getDouble("max_marks");
                            evalId = rs.getInt("evaluation_id");
                            studentId = rs.getInt("student_id");
                            marksReleased = rs.getBoolean("marks_released");
                        } else {
                            throw new DatabaseException("Submission not found.");
                        }
                    }
                }

                if (marks < 0 || marks > maxMarks) {
                    throw new DatabaseException("Marks must be between 0 and " + maxMarks);
                }

                String updSql = "UPDATE assignment_submissions SET marks = ?, feedback = ?, " +
                        "status = 'GRADED', graded_by = ?, graded_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updSql)) {
                    ps.setDouble(1, marks);
                    ps.setString(2, feedback);
                    ps.setInt(3, lecturerId);
                    ps.setInt(4, submissionId);
                    ps.executeUpdate();
                }

                if (marksReleased && evalId > 0) {
                    saveOrUpdateCentralMark(conn, evalId, studentId, marks, lecturerId);
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to grade submission: " + e.getMessage(), e);
        }
    }
}
