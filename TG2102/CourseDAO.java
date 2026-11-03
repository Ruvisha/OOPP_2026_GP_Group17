package com.fot.ams.dao;

import com.fot.ams.tg2102.TG2102DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course operations.
 */
public class CourseDAO {

    /** Lecturer choice shown in the course management form. */
    public static class LecturerOption {
        private final int id;
        private final String name;

        public LecturerOption(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
    }

    /** Adds TG2102-owned course schema extensions without touching shared project files. */
    private void ensureCourseLecturerTable(Connection conn) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'courses' AND COLUMN_NAME = 'study_year'")) {
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (Statement alter = conn.createStatement()) {
                        alter.executeUpdate("ALTER TABLE courses ADD COLUMN study_year TINYINT NOT NULL DEFAULT 1 AFTER semester");
                    }
                }
            }
        }
        String sql = "CREATE TABLE IF NOT EXISTS course_lecturers (" +
                "course_id INT NOT NULL PRIMARY KEY," +
                "lecturer_id INT NOT NULL," +
                "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "CONSTRAINT fk_course_lecturer_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE," +
                "CONSTRAINT fk_course_lecturer_user FOREIGN KEY (lecturer_id) REFERENCES users(user_id) ON DELETE RESTRICT" +
                ") ENGINE=InnoDB";
        try (Statement st = conn.createStatement()) { st.executeUpdate(sql); }
    }

    public List<LecturerOption> getLecturersByDepartment(int departmentId) {
        List<LecturerOption> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name FROM users u " +
                "WHERE u.role_id = 2 AND u.department_id = ? AND u.status = 'ACTIVE' " +
                "ORDER BY u.full_name";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, departmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(new LecturerOption(rs.getInt("user_id"), rs.getString("full_name")));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load department lecturers: " + e.getMessage(), e);
        }
        return list;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course(
                rs.getInt("course_id"),
                rs.getString("course_code"),
                rs.getString("course_name"),
                rs.getInt("credits"),
                rs.getInt("semester"),
                rs.getInt("department_id"),
                rs.getBoolean("has_theory"),
                rs.getBoolean("has_practical"),
                rs.getString("description")
        );
        course.setStudyYear(rs.getInt("study_year"));
        course.setDepartmentName(rs.getString("department_name"));
        course.setDepartmentCode(rs.getString("department_code"));
        course.setLecturerId(rs.getInt("lecturer_id"));
        course.setLecturerName(rs.getString("lecturer_name"));
        return course;
    }

    private static final String BASE_SELECT =
            "SELECT c.*, d.department_name, d.department_code, cl.lecturer_id, u.full_name AS lecturer_name " +
                    "FROM courses c " +
                    "JOIN departments d ON c.department_id = d.department_id " +
                    "LEFT JOIN course_lecturers cl ON cl.course_id = c.course_id " +
                    "LEFT JOIN users u ON u.user_id = cl.lecturer_id ";

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.study_year, c.semester, c.course_code";

        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    list.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all courses: " + e.getMessage(), e);
        }
        return list;
    }

    public Course getCourseById(int courseId) {
        String sql = BASE_SELECT + "WHERE c.course_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, courseId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToCourse(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch course by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Course> getCoursesByDepartment(int departmentId) {
        List<Course> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.department_id = ? ORDER BY c.study_year, c.semester, c.course_code";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, departmentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToCourse(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch courses by department: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Course> getCoursesForStudent(int studentId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, d.department_name, d.department_code, cl.lecturer_id, u.full_name AS lecturer_name " +
                "FROM courses c " +
                "JOIN departments d ON c.department_id = d.department_id " +
                "LEFT JOIN course_lecturers cl ON cl.course_id = c.course_id " +
                "LEFT JOIN users u ON u.user_id = cl.lecturer_id " +
                "JOIN course_enrollment e ON c.course_id = e.course_id " +
                "WHERE e.student_id = ? " +
                "ORDER BY c.study_year, c.semester, c.course_code";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToCourse(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student enrolled courses: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (course_code, course_name, credits, semester, study_year, " +
                "department_id, has_theory, has_practical, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, course.getCourseCode());
                    stmt.setString(2, course.getCourseName());
                    stmt.setInt(3, course.getCredits());
                    stmt.setInt(4, course.getSemester());
                    stmt.setInt(5, course.getStudyYear());
                    stmt.setInt(6, course.getDepartmentId());
                    stmt.setBoolean(7, course.isHasTheory());
                    stmt.setBoolean(8, course.isHasPractical());
                    stmt.setString(9, course.getDescription());
                    if (stmt.executeUpdate() == 0) { conn.rollback(); return false; }
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) { conn.rollback(); return false; }
                        course.setCourseId(keys.getInt(1));
                    }
                }
                saveCourseLecturer(conn, course);
                conn.commit();
                return true;
            } catch (SQLException ex) { conn.rollback(); throw ex; }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add course: " + e.getMessage(), e);
        }
    }

    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, credits = ?, " +
                "semester = ?, study_year = ?, department_id = ?, has_theory = ?, has_practical = ?, description = ? WHERE course_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, course.getCourseCode());
                    stmt.setString(2, course.getCourseName());
                    stmt.setInt(3, course.getCredits());
                    stmt.setInt(4, course.getSemester());
                    stmt.setInt(5, course.getStudyYear());
                    stmt.setInt(6, course.getDepartmentId());
                    stmt.setBoolean(7, course.isHasTheory());
                    stmt.setBoolean(8, course.isHasPractical());
                    stmt.setString(9, course.getDescription());
                    stmt.setInt(10, course.getCourseId());
                    if (stmt.executeUpdate() == 0) { conn.rollback(); return false; }
                }
                saveCourseLecturer(conn, course);
                conn.commit();
                return true;
            } catch (SQLException ex) { conn.rollback(); throw ex; }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update course: " + e.getMessage(), e);
        }
    }

    private void saveCourseLecturer(Connection conn, Course course) throws SQLException {
        if (course.getLecturerId() <= 0) throw new SQLException("Please select a lecturer for this department.");
        String check = "SELECT COUNT(*) FROM users WHERE user_id = ? AND role_id = 2 AND department_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, course.getLecturerId());
            ps.setInt(2, course.getDepartmentId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) == 0) throw new SQLException("Selected lecturer does not belong to the selected course department.");
            }
        }
        String upsert = "INSERT INTO course_lecturers (course_id, lecturer_id) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE lecturer_id = VALUES(lecturer_id)";
        try (PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setInt(1, course.getCourseId());
            ps.setInt(2, course.getLecturerId());
            ps.executeUpdate();
        }
    }

    /** Courses assigned to the currently logged-in lecturer only. */
    public List<Course> getCoursesByLecturer(int lecturerId) {
        List<Course> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE cl.lecturer_id = ? ORDER BY c.study_year, c.semester, c.course_code";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, lecturerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load lecturer courses: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean isCourseAssignedToLecturer(int courseId, int lecturerId) {
        String sql = "SELECT COUNT(*) FROM course_lecturers WHERE course_id = ? AND lecturer_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, courseId); ps.setInt(2, lecturerId);
                try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to verify lecturer course access: " + e.getMessage(), e);
        }
    }

    public List<Course> getCoursesFiltered(int departmentId, int studyYear, int semester) {
        List<Course> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        if (departmentId > 0) sql.append("AND c.department_id = ? ");
        if (studyYear > 0) sql.append("AND c.study_year = ? ");
        if (semester > 0) sql.append("AND c.semester = ? ");
        sql.append("ORDER BY c.study_year, c.semester, c.course_code");
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureCourseLecturerTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int i=1;
                if (departmentId > 0) ps.setInt(i++, departmentId);
                if (studyYear > 0) ps.setInt(i++, studyYear);
                if (semester > 0) ps.setInt(i++, semester);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapResultSetToCourse(rs)); }
            }
        } catch (SQLException e) { throw new DatabaseException("Failed to filter courses: " + e.getMessage(), e); }
        return list;
    }

    /**
     * Generates a course code with exactly four digits after the department code.
     * Format: DEPT + Y + S + NN (study year, semester, two-digit sequence).
     * Example: ICT2101 = ICT, Year 2, Semester 1, Course 01.
     * Credits are deliberately not part of the course code.
     */
    public String generateCourseCode(int departmentId, int year, int semester, int credits) {
        if (year < 1 || year > 4) throw new IllegalArgumentException("Study year must be between 1 and 4.");
        if (semester < 1 || semester > 2) throw new IllegalArgumentException("Semester must be 1 or 2.");

        String deptCode = null;
        String prefixSql = "SELECT department_code FROM departments WHERE department_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(prefixSql)) {
                ps.setInt(1, departmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) deptCode = rs.getString(1);
                }
            }
            if (deptCode == null || deptCode.trim().isEmpty()) {
                throw new SQLException("Invalid department selected.");
            }

            String prefix = deptCode.trim().toUpperCase(java.util.Locale.ROOT) + year + semester;
            String query = "SELECT course_code FROM courses WHERE course_code LIKE ?";
            int next = 1;
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, prefix + "__");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String code = rs.getString(1);
                        if (code != null && code.matches(java.util.regex.Pattern.quote(prefix) + "\\d{2}")) {
                            int sequence = Integer.parseInt(code.substring(prefix.length()));
                            next = Math.max(next, sequence + 1);
                        }
                    }
                }
            }
            if (next > 99) throw new SQLException("No more two-digit course numbers are available for this year and semester.");
            return prefix + String.format(java.util.Locale.ROOT, "%02d", next);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate course code: " + e.getMessage(), e);
        }
    }

    public boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete course: " + e.getMessage(), e);
        }
    }

    public boolean isCourseCodeTaken(String code, int excludeCourseId) {
        String sql = "SELECT COUNT(*) FROM courses WHERE course_code = ? AND course_id != ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            stmt.setInt(2, excludeCourseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check course code uniqueness: " + e.getMessage(), e);
        }
        return false;
    }
}
