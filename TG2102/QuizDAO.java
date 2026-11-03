package com.fot.ams.dao;

import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Quiz;
import com.fot.ams.model.QuizAnswer;
import com.fot.ams.model.QuizAttempt;
import com.fot.ams.model.QuizQuestion;
import com.fot.ams.tg2102.TG2102DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Online Quiz Management in TG2102.
 * Fully integrates with MySQL database 'faculty_academic_management'
 * and syncs results to the central 'evaluations' and 'marks' tables.
 */
public class QuizDAO {

    public QuizDAO() {
    }

    /**
     * Non-destructive schema verification and initialization.
     */
    public void ensureSchema() {
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
        } catch (SQLException e) {
            System.err.println("[QuizDAO] Schema init notice: " + e.getMessage());
        }
    }

    private void ensureSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS quizzes (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "course_id INT NOT NULL," +
                    "lecturer_id INT NOT NULL," +
                    "evaluation_id INT NULL," +
                    "title VARCHAR(200) NOT NULL," +
                    "duration_minutes INT NOT NULL DEFAULT 30," +
                    "start_time DATETIME NULL," +
                    "end_time DATETIME NULL," +
                    "max_attempts INT NOT NULL DEFAULT 1," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'," +
                    "marks_released TINYINT(1) NOT NULL DEFAULT 0," +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "KEY idx_course (course_id)," +
                    "KEY idx_lecturer (lecturer_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS quiz_questions (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "quiz_id INT NOT NULL," +
                    "question_text TEXT NOT NULL," +
                    "question_type VARCHAR(30) NOT NULL," +
                    "option_a TEXT NULL," +
                    "option_b TEXT NULL," +
                    "option_c TEXT NULL," +
                    "option_d TEXT NULL," +
                    "correct_answer TEXT NULL," +
                    "marks DECIMAL(8,2) NOT NULL DEFAULT 1.00," +
                    "KEY idx_quiz (quiz_id)," +
                    "CONSTRAINT fk_q_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS quiz_attempts (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "quiz_id INT NOT NULL," +
                    "student_id INT NOT NULL," +
                    "started_at DATETIME NOT NULL," +
                    "submitted_at DATETIME NULL," +
                    "score DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                    "total_marks DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                    "status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'," +
                    "graded_by INT NULL," +
                    "graded_at DATETIME NULL," +
                    "UNIQUE KEY uq_one_attempt (quiz_id, student_id)," +
                    "KEY idx_student (student_id)," +
                    "CONSTRAINT fk_att_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS quiz_answers (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "attempt_id INT NOT NULL," +
                    "question_id INT NOT NULL," +
                    "answer_text TEXT NULL," +
                    "awarded_marks DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                    "feedback TEXT NULL," +
                    "UNIQUE KEY uq_att_q (attempt_id, question_id)," +
                    "KEY idx_q (question_id)," +
                    "CONSTRAINT fk_ans_att FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE," +
                    "CONSTRAINT fk_ans_q FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }

        // Add missing columns if tables already existed from an older version
        addColumnIfNotExists(conn, "quizzes", "evaluation_id", "INT NULL AFTER lecturer_id");
        addColumnIfNotExists(conn, "quizzes", "start_time", "DATETIME NULL AFTER duration_minutes");
        addColumnIfNotExists(conn, "quizzes", "end_time", "DATETIME NULL AFTER start_time");
        addColumnIfNotExists(conn, "quizzes", "marks_released", "TINYINT(1) NOT NULL DEFAULT 0 AFTER status");
        addColumnIfNotExists(conn, "quizzes", "file_path", "VARCHAR(1000) NULL AFTER marks_released");
        addColumnIfNotExists(conn, "quiz_answers", "feedback", "TEXT NULL AFTER awarded_marks");
        addColumnIfNotExists(conn, "quiz_attempts", "graded_by", "INT NULL AFTER status");
        addColumnIfNotExists(conn, "quiz_attempts", "graded_at", "DATETIME NULL AFTER graded_by");
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

    private Quiz mapResultSetToQuiz(ResultSet rs) throws SQLException {
        Quiz q = new Quiz();
        q.setId(rs.getInt("id"));
        q.setCourseId(rs.getInt("course_id"));
        q.setLecturerId(rs.getInt("lecturer_id"));
        int evalId = rs.getInt("evaluation_id");
        if (!rs.wasNull()) q.setEvaluationId(evalId);
        q.setTitle(rs.getString("title"));
        q.setDurationMinutes(rs.getInt("duration_minutes"));
        q.setStartTime(rs.getTimestamp("start_time"));
        q.setEndTime(rs.getTimestamp("end_time"));
        q.setMaxAttempts(rs.getInt("max_attempts"));
        q.setStatus(rs.getString("status"));
        q.setMarksReleased(rs.getBoolean("marks_released"));
        q.setFilePath(rs.getString("file_path"));
        q.setCreatedAt(rs.getTimestamp("created_at"));

        // Join fields
        q.setCourseCode(rs.getString("course_code"));
        q.setCourseName(rs.getString("course_name"));
        q.setLecturerName(rs.getString("lecturer_name"));
        q.setQuestionCount(rs.getInt("question_count"));
        q.setTotalMarks(rs.getDouble("total_quiz_marks"));
        return q;
    }

    private static final String BASE_QUIZ_SELECT =
            "SELECT q.*, c.course_code, c.course_name, u.full_name AS lecturer_name, " +
            "  (SELECT COUNT(*) FROM quiz_questions qq WHERE qq.quiz_id = q.id) AS question_count, " +
            "  (SELECT COALESCE(SUM(qq.marks), 0.0) FROM quiz_questions qq WHERE qq.quiz_id = q.id) AS total_quiz_marks " +
            "FROM quizzes q " +
            "JOIN courses c ON q.course_id = c.course_id " +
            "JOIN users u ON q.lecturer_id = u.user_id ";

    public List<Quiz> getQuizzesByCourse(int courseId) {
        List<Quiz> list = new ArrayList<>();
        String sql = BASE_QUIZ_SELECT + "WHERE q.course_id = ? ORDER BY q.created_at DESC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToQuiz(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load quizzes for course: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Quiz> getQuizzesByLecturer(int lecturerId) {
        List<Quiz> list = new ArrayList<>();
        String sql = BASE_QUIZ_SELECT + "WHERE q.lecturer_id = ? ORDER BY q.created_at DESC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToQuiz(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load lecturer quizzes: " + e.getMessage(), e);
        }
        return list;
    }

    public Quiz getQuizById(int quizId) {
        String sql = BASE_QUIZ_SELECT + "WHERE q.id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapResultSetToQuiz(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch quiz: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Creates or updates a matching evaluation entry in the central 'evaluations' table.
     */
    private int syncEvaluation(Connection conn, int courseId, String title, double maxMarks) throws SQLException {
        // Check if matching evaluation exists
        String findSql = "SELECT evaluation_id FROM evaluations WHERE course_id = ? AND evaluation_name = ? AND evaluation_type = 'QUIZ'";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setInt(1, courseId);
            ps.setString(2, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int evalId = rs.getInt(1);
                    // Update max_marks
                    try (PreparedStatement upd = conn.prepareStatement("UPDATE evaluations SET max_marks = ? WHERE evaluation_id = ?")) {
                        upd.setDouble(1, maxMarks > 0 ? maxMarks : 100.0);
                        upd.setInt(2, evalId);
                        upd.executeUpdate();
                    }
                    return evalId;
                }
            }
        }

        // Get course semester
        int semester = 1;
        try (PreparedStatement psCourse = conn.prepareStatement("SELECT semester FROM courses WHERE course_id = ?")) {
            psCourse.setInt(1, courseId);
            try (ResultSet rs = psCourse.executeQuery()) {
                if (rs.next()) semester = rs.getInt(1);
            }
        }

        String insertSql = "INSERT INTO evaluations (course_id, evaluation_name, evaluation_type, max_marks, weightage_percent, semester, academic_year) " +
                "VALUES (?, ?, 'QUIZ', ?, 10.0, ?, '2024/2025')";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            ps.setString(2, title);
            ps.setDouble(3, maxMarks > 0 ? maxMarks : 100.0);
            ps.setInt(4, semester);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return 0;
    }

    public boolean createQuiz(Quiz quiz) {
        String sql = "INSERT INTO quizzes (course_id, lecturer_id, evaluation_id, title, duration_minutes, " +
                "start_time, end_time, max_attempts, status, marks_released, file_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            conn.setAutoCommit(false);
            try {
                int evalId = syncEvaluation(conn, quiz.getCourseId(), quiz.getTitle(), 100.0);
                quiz.setEvaluationId(evalId > 0 ? evalId : null);

                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, quiz.getCourseId());
                    ps.setInt(2, quiz.getLecturerId());
                    if (quiz.getEvaluationId() != null) ps.setInt(3, quiz.getEvaluationId());
                    else ps.setNull(3, Types.INTEGER);
                    ps.setString(4, quiz.getTitle());
                    ps.setInt(5, quiz.getDurationMinutes());
                    if (quiz.getStartTime() != null) ps.setTimestamp(6, quiz.getStartTime());
                    else ps.setNull(6, Types.TIMESTAMP);
                    if (quiz.getEndTime() != null) ps.setTimestamp(7, quiz.getEndTime());
                    else ps.setNull(7, Types.TIMESTAMP);
                    ps.setInt(8, quiz.getMaxAttempts());
                    ps.setString(9, quiz.getStatus() != null ? quiz.getStatus() : "DRAFT");
                    ps.setBoolean(10, quiz.isMarksReleased());
                    ps.setString(11, quiz.getFilePath());

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) quiz.setId(keys.getInt(1));
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
            throw new DatabaseException("Failed to create quiz: " + e.getMessage(), e);
        }
    }

    public boolean updateQuiz(Quiz quiz) {
        String sql = "UPDATE quizzes SET title = ?, duration_minutes = ?, start_time = ?, end_time = ?, " +
                "max_attempts = ?, status = ?, file_path = ? WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, quiz.getTitle());
                ps.setInt(2, quiz.getDurationMinutes());
                if (quiz.getStartTime() != null) ps.setTimestamp(3, quiz.getStartTime());
                else ps.setNull(3, Types.TIMESTAMP);
                if (quiz.getEndTime() != null) ps.setTimestamp(4, quiz.getEndTime());
                else ps.setNull(4, Types.TIMESTAMP);
                ps.setInt(5, quiz.getMaxAttempts());
                ps.setString(6, quiz.getStatus());
                ps.setString(7, quiz.getFilePath());
                ps.setInt(8, quiz.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update quiz: " + e.getMessage(), e);
        }
    }

    public boolean updateQuizStatus(int quizId, String status) {
        String sql = "UPDATE quizzes SET status = ? WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, quizId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update quiz status: " + e.getMessage(), e);
        }
    }

    public boolean setMarksReleased(int quizId, boolean released) {
        String sql = "UPDATE quizzes SET marks_released = ? WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setBoolean(1, released);
                    ps.setInt(2, quizId);
                    ps.executeUpdate();
                }

                // If releasing marks, sync all completed attempts to central marks table
                if (released) {
                    syncAllCompletedAttemptsToMarks(conn, quizId);
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to change marks release status: " + e.getMessage(), e);
        }
    }

    private void syncAllCompletedAttemptsToMarks(Connection conn, int quizId) throws SQLException {
        String findAttempts = "SELECT a.student_id, a.score, q.evaluation_id, q.lecturer_id " +
                "FROM quiz_attempts a " +
                "JOIN quizzes q ON a.quiz_id = q.id " +
                "WHERE a.quiz_id = ? AND a.status IN ('SUBMITTED', 'GRADED')";
        try (PreparedStatement ps = conn.prepareStatement(findAttempts)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    double score = rs.getDouble("score");
                    int evalId = rs.getInt("evaluation_id");
                    int enterer = rs.getInt("lecturer_id");
                    if (evalId > 0) {
                        saveOrUpdateCentralMark(conn, evalId, studentId, score, enterer);
                    }
                }
            }
        }
    }

    public void saveOrUpdateCentralMark(Connection conn, int evaluationId, int studentId, double marksObtained, Integer enteredBy) throws SQLException {
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

    public boolean deleteQuiz(int quizId) {
        // Prevent deleting if student attempts already exist
        String checkAttempts = "SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(checkAttempts)) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new DatabaseException("Cannot delete this quiz because students have already attempted it. Close or unpublish it instead.");
                    }
                }
            }

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM quizzes WHERE id = ?")) {
                del.setInt(1, quizId);
                return del.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete quiz: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // Question Management
    // ==========================================

    public List<QuizQuestion> getQuestionsByQuiz(int quizId) {
        List<QuizQuestion> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_questions WHERE quiz_id = ? ORDER BY id ASC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        QuizQuestion q = new QuizQuestion(
                                rs.getInt("id"),
                                rs.getInt("quiz_id"),
                                rs.getString("question_text"),
                                rs.getString("question_type"),
                                rs.getString("option_a"),
                                rs.getString("option_b"),
                                rs.getString("option_c"),
                                rs.getString("option_d"),
                                rs.getString("correct_answer"),
                                rs.getDouble("marks")
                        );
                        list.add(q);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load quiz questions: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addQuestion(QuizQuestion q) {
        String sql = "INSERT INTO quiz_questions (quiz_id, question_text, question_type, " +
                "option_a, option_b, option_c, option_d, correct_answer, marks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, q.getQuizId());
                ps.setString(2, q.getQuestionText());
                ps.setString(3, q.getQuestionType());
                ps.setString(4, q.getOptionA());
                ps.setString(5, q.getOptionB());
                ps.setString(6, q.getOptionC());
                ps.setString(7, q.getOptionD());
                ps.setString(8, q.getCorrectAnswer());
                ps.setDouble(9, q.getMarks());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) q.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add question: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateQuestion(QuizQuestion q) {
        String sql = "UPDATE quiz_questions SET question_text = ?, question_type = ?, option_a = ?, " +
                "option_b = ?, option_c = ?, option_d = ?, correct_answer = ?, marks = ? WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, q.getQuestionText());
                ps.setString(2, q.getQuestionType());
                ps.setString(3, q.getOptionA());
                ps.setString(4, q.getOptionB());
                ps.setString(5, q.getOptionC());
                ps.setString(6, q.getOptionD());
                ps.setString(7, q.getCorrectAnswer());
                ps.setDouble(8, q.getMarks());
                ps.setInt(9, q.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update question: " + e.getMessage(), e);
        }
    }

    public boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM quiz_questions WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, questionId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete question: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // Student Quiz Taking & Submissions
    // ==========================================

    /**
     * Checks if the student has already submitted an attempt for this quiz.
     */
    public boolean hasStudentAttempted(int quizId, int studentId) {
        String sql = "SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id = ? AND student_id = ? AND status IN ('SUBMITTED', 'GRADED')";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quizId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking student quiz attempt: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves an in-progress attempt or starts a new one safely.
     */
    public QuizAttempt startAttempt(int quizId, int studentId) {
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);

            // 1. Verify student enrollment in the quiz's course
            String checkEnrollment = "SELECT COUNT(*) FROM course_enrollment ce " +
                    "JOIN quizzes q ON ce.course_id = q.course_id " +
                    "WHERE q.id = ? AND ce.student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkEnrollment)) {
                ps.setInt(1, quizId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new DatabaseException("You are not registered in the course for this quiz.");
                    }
                }
            }

            // 2. Check if already attempted
            String checkExisting = "SELECT * FROM quiz_attempts WHERE quiz_id = ? AND student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkExisting)) {
                ps.setInt(1, quizId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String st = rs.getString("status");
                        if ("SUBMITTED".equalsIgnoreCase(st) || "GRADED".equalsIgnoreCase(st)) {
                            throw new DatabaseException("You have already completed this quiz. Multiple attempts are not permitted.");
                        }
                        // Resume in-progress attempt
                        return mapAttempt(rs);
                    }
                }
            }

            // 3. Compute total quiz marks
            double totalMarks = 0.0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(marks), 0.0) FROM quiz_questions WHERE quiz_id = ?")) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalMarks = rs.getDouble(1);
                }
            }

            // 4. Create new attempt
            String insertSql = "INSERT INTO quiz_attempts (quiz_id, student_id, started_at, score, total_marks, status) " +
                    "VALUES (?, ?, CURRENT_TIMESTAMP, 0.00, ?, 'IN_PROGRESS')";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, quizId);
                ps.setInt(2, studentId);
                ps.setDouble(3, totalMarks);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        QuizAttempt att = new QuizAttempt();
                        att.setId(keys.getInt(1));
                        att.setQuizId(quizId);
                        att.setStudentId(studentId);
                        att.setStartedAt(new Timestamp(System.currentTimeMillis()));
                        att.setTotalMarks(totalMarks);
                        att.setStatus("IN_PROGRESS");
                        return att;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to start quiz attempt: " + e.getMessage(), e);
        }
        return null;
    }

    private QuizAttempt mapAttempt(ResultSet rs) throws SQLException {
        QuizAttempt att = new QuizAttempt();
        att.setId(rs.getInt("id"));
        att.setQuizId(rs.getInt("quiz_id"));
        att.setStudentId(rs.getInt("student_id"));
        att.setStartedAt(rs.getTimestamp("started_at"));
        att.setSubmittedAt(rs.getTimestamp("submitted_at"));
        att.setScore(rs.getDouble("score"));
        att.setTotalMarks(rs.getDouble("total_marks"));
        att.setStatus(rs.getString("status"));
        return att;
    }

    public QuizAttempt getAttemptById(int attemptId) {
        String sql = "SELECT * FROM quiz_attempts WHERE id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, attemptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapAttempt(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch attempt by ID: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Saves student's answer for a single question in an attempt.
     */
    public boolean saveAnswer(int attemptId, int questionId, String answerText) {
        String sql = "INSERT INTO quiz_answers (attempt_id, question_id, answer_text, awarded_marks) " +
                "VALUES (?, ?, ?, 0.00) " +
                "ON DUPLICATE KEY UPDATE answer_text = VALUES(answer_text)";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, attemptId);
                ps.setInt(2, questionId);
                ps.setString(3, answerText);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save answer: " + e.getMessage(), e);
        }
    }

    /**
     * Submits the quiz attempt. Evaluates MCQ and True/False automatically,
     * sums the score, sets attempt status to SUBMITTED or GRADED, and stores all answers safely in MySQL.
     */
    public boolean submitAttempt(int attemptId, int studentId) {
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            conn.setAutoCommit(false);
            try {
                // 1. Fetch questions and student answers
                List<QuizQuestion> questions = new ArrayList<>();
                int quizId = 0;
                String getQuizQuery = "SELECT a.quiz_id FROM quiz_attempts a WHERE a.id = ? AND a.student_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(getQuizQuery)) {
                    ps.setInt(1, attemptId);
                    ps.setInt(2, studentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) quizId = rs.getInt("quiz_id");
                        else throw new DatabaseException("Attempt not found or unauthorized.");
                    }
                }

                String qSql = "SELECT * FROM quiz_questions WHERE quiz_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(qSql)) {
                    ps.setInt(1, quizId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            questions.add(new QuizQuestion(
                                    rs.getInt("id"),
                                    rs.getInt("quiz_id"),
                                    rs.getString("question_text"),
                                    rs.getString("question_type"),
                                    rs.getString("option_a"),
                                    rs.getString("option_b"),
                                    rs.getString("option_c"),
                                    rs.getString("option_d"),
                                    rs.getString("correct_answer"),
                                    rs.getDouble("marks")
                            ));
                        }
                    }
                }

                double totalScore = 0.0;
                double totalPossible = 0.0;
                boolean hasShortAnswer = false;

                String ansSql = "SELECT answer_text FROM quiz_answers WHERE attempt_id = ? AND question_id = ?";
                String updAnsSql = "UPDATE quiz_answers SET awarded_marks = ? WHERE attempt_id = ? AND question_id = ?";

                for (QuizQuestion q : questions) {
                    totalPossible += q.getMarks();
                    String answer = "";
                    try (PreparedStatement ps = conn.prepareStatement(ansSql)) {
                        ps.setInt(1, attemptId);
                        ps.setInt(2, q.getId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) answer = rs.getString(1);
                        }
                    }

                    double awarded = 0.0;
                    if (q.isMultipleChoice()) {
                        // Compare selected option (e.g. "A", "B", "C", "D" or matching text)
                        if (answer != null && q.getCorrectAnswer() != null) {
                            String a = answer.trim();
                            String ca = q.getCorrectAnswer().trim();
                            if (a.equalsIgnoreCase(ca)) {
                                awarded = q.getMarks();
                            } else {
                                // Also check if student selected full option text
                                if ("A".equalsIgnoreCase(ca) && a.equalsIgnoreCase(q.getOptionA())) awarded = q.getMarks();
                                else if ("B".equalsIgnoreCase(ca) && a.equalsIgnoreCase(q.getOptionB())) awarded = q.getMarks();
                                else if ("C".equalsIgnoreCase(ca) && a.equalsIgnoreCase(q.getOptionC())) awarded = q.getMarks();
                                else if ("D".equalsIgnoreCase(ca) && a.equalsIgnoreCase(q.getOptionD())) awarded = q.getMarks();
                            }
                        }
                    } else if (q.isTrueFalse()) {
                        if (answer != null && q.getCorrectAnswer() != null &&
                                answer.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                            awarded = q.getMarks();
                        }
                    } else if (q.isShortAnswer()) {
                        hasShortAnswer = true;
                        // Objective check if lecturer set exact text or defer to manual grading
                        if (answer != null && q.getCorrectAnswer() != null && !q.getCorrectAnswer().trim().isEmpty() &&
                                answer.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                            awarded = q.getMarks();
                        }
                    }

                    totalScore += awarded;
                    try (PreparedStatement psUpd = conn.prepareStatement(updAnsSql)) {
                        psUpd.setDouble(1, awarded);
                        psUpd.setInt(2, attemptId);
                        psUpd.setInt(3, q.getId());
                        psUpd.executeUpdate();
                    }
                }

                String finalStatus = hasShortAnswer ? "SUBMITTED" : "GRADED";
                String updAttempt = "UPDATE quiz_attempts SET submitted_at = CURRENT_TIMESTAMP, " +
                        "score = ?, total_marks = ?, status = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updAttempt)) {
                    ps.setDouble(1, totalScore);
                    ps.setDouble(2, totalPossible);
                    ps.setString(3, finalStatus);
                    ps.setInt(4, attemptId);
                    ps.executeUpdate();
                }

                // If marks are already released for this quiz, sync to marks table right away
                Quiz quiz = getQuizById(quizId);
                if (quiz != null && quiz.isMarksReleased() && quiz.getEvaluationId() != null) {
                    saveOrUpdateCentralMark(conn, quiz.getEvaluationId(), studentId, totalScore, quiz.getLecturerId());
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to submit quiz: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // Lecturer Review & Grading
    // ==========================================

    public List<QuizAttempt> getAttemptsByQuiz(int quizId) {
        List<QuizAttempt> list = new ArrayList<>();
        String sql = "SELECT a.*, u.student_reg_no, u.full_name AS student_name, " +
                "q.title AS quiz_title, c.course_code, c.course_name " +
                "FROM quiz_attempts a " +
                "JOIN users u ON a.student_id = u.user_id " +
                "JOIN quizzes q ON a.quiz_id = q.id " +
                "JOIN courses c ON q.course_id = c.course_id " +
                "WHERE a.quiz_id = ? ORDER BY a.submitted_at DESC, a.started_at DESC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        QuizAttempt att = mapAttempt(rs);
                        att.setStudentRegNo(rs.getString("student_reg_no"));
                        att.setStudentName(rs.getString("student_name"));
                        att.setQuizTitle(rs.getString("quiz_title"));
                        att.setCourseCode(rs.getString("course_code"));
                        att.setCourseName(rs.getString("course_name"));
                        list.add(att);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load quiz attempts: " + e.getMessage(), e);
        }
        return list;
    }

    public List<QuizAnswer> getAnswersByAttempt(int attemptId) {
        List<QuizAnswer> list = new ArrayList<>();
        String sql = "SELECT ans.*, q.question_text, q.question_type, q.option_a, q.option_b, " +
                "q.option_c, q.option_d, q.correct_answer, q.marks AS max_marks " +
                "FROM quiz_answers ans " +
                "JOIN quiz_questions q ON ans.question_id = q.id " +
                "WHERE ans.attempt_id = ? ORDER BY q.id ASC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, attemptId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        QuizAnswer a = new QuizAnswer();
                        a.setId(rs.getInt("id"));
                        a.setAttemptId(rs.getInt("attempt_id"));
                        a.setQuestionId(rs.getInt("question_id"));
                        a.setAnswerText(rs.getString("answer_text"));
                        a.setAwardedMarks(rs.getDouble("awarded_marks"));
                        a.setFeedback(rs.getString("feedback"));
                        a.setQuestionText(rs.getString("question_text"));
                        a.setQuestionType(rs.getString("question_type"));
                        a.setOptionA(rs.getString("option_a"));
                        a.setOptionB(rs.getString("option_b"));
                        a.setOptionC(rs.getString("option_c"));
                        a.setOptionD(rs.getString("option_d"));
                        a.setCorrectAnswer(rs.getString("correct_answer"));
                        a.setMaxMarks(rs.getDouble("max_marks"));
                        list.add(a);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load attempt answers: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Manually grades a short-answer or disputed question, updates attempt score and marks table.
     */
    public boolean gradeAnswer(int answerId, double awardedMarks, String feedback, int lecturerId) {
        String getAnswerInfo = "SELECT a.attempt_id, a.question_id, q.marks AS max_marks " +
                "FROM quiz_answers a " +
                "JOIN quiz_questions q ON a.question_id = q.id " +
                "WHERE a.id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            conn.setAutoCommit(false);
            try {
                int attemptId = 0;
                double maxMarks = 0.0;
                try (PreparedStatement ps = conn.prepareStatement(getAnswerInfo)) {
                    ps.setInt(1, answerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            attemptId = rs.getInt("attempt_id");
                            maxMarks = rs.getDouble("max_marks");
                        } else {
                            throw new DatabaseException("Answer record not found.");
                        }
                    }
                }

                if (awardedMarks < 0 || awardedMarks > maxMarks) {
                    throw new DatabaseException("Awarded marks must be between 0 and " + maxMarks);
                }

                // Update answer
                String updAns = "UPDATE quiz_answers SET awarded_marks = ?, feedback = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updAns)) {
                    ps.setDouble(1, awardedMarks);
                    ps.setString(2, feedback);
                    ps.setInt(3, answerId);
                    ps.executeUpdate();
                }

                // Recalculate attempt score
                double totalScore = 0.0;
                String sumSql = "SELECT COALESCE(SUM(awarded_marks), 0.0) FROM quiz_answers WHERE attempt_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sumSql)) {
                    ps.setInt(1, attemptId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) totalScore = rs.getDouble(1);
                    }
                }

                String updAtt = "UPDATE quiz_attempts SET score = ?, status = 'GRADED', graded_by = ?, graded_at = CURRENT_TIMESTAMP WHERE id = ?";
                int studentId = 0;
                int quizId = 0;
                try (PreparedStatement ps = conn.prepareStatement(updAtt)) {
                    ps.setDouble(1, totalScore);
                    ps.setInt(2, lecturerId);
                    ps.setInt(3, attemptId);
                    ps.executeUpdate();
                }

                // Retrieve studentId and quizId for sync
                String getAttempt = "SELECT student_id, quiz_id FROM quiz_attempts WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(getAttempt)) {
                    ps.setInt(1, attemptId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            studentId = rs.getInt("student_id");
                            quizId = rs.getInt("quiz_id");
                        }
                    }
                }

                Quiz quiz = getQuizById(quizId);
                if (quiz != null && quiz.isMarksReleased() && quiz.getEvaluationId() != null) {
                    saveOrUpdateCentralMark(conn, quiz.getEvaluationId(), studentId, totalScore, lecturerId);
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to grade question: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves student attempt for a given quiz if exists.
     */
    public QuizAttempt getStudentAttempt(int quizId, int studentId) {
        String sql = "SELECT * FROM quiz_attempts WHERE quiz_id = ? AND student_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSchema(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quizId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapAttempt(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student attempt: " + e.getMessage(), e);
        }
        return null;
    }
}
