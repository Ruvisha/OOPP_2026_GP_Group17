package com.fot.ams.dao;

import com.fot.ams.tg2102.TG2102DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.CourseEnrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student-Course Enrollment.
 */
public class CourseEnrollmentDAO {

    private CourseEnrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        CourseEnrollment ce = new CourseEnrollment(
            rs.getInt("enrollment_id"),
            rs.getInt("student_id"),
            rs.getInt("course_id"),
            rs.getString("academic_year"),
            rs.getInt("semester"),
            rs.getBoolean("is_repeat")
        );
        ce.setStudentRegNo(rs.getString("student_reg_no"));
        ce.setStudentName(rs.getString("student_name"));
        ce.setCourseCode(rs.getString("course_code"));
        ce.setCourseName(rs.getString("course_name"));
        ce.setEnrolledAt(rs.getTimestamp("enrolled_at"));
        return ce;
    }

    private static final String BASE_SELECT =
        "SELECT ce.*, u.student_reg_no, u.full_name AS student_name, c.course_code, c.course_name " +
        "FROM course_enrollment ce " +
        "JOIN users u ON ce.student_id = u.user_id " +
        "JOIN courses c ON ce.course_id = c.course_id ";

    public List<CourseEnrollment> getEnrollmentsByCourse(int courseId) {
        List<CourseEnrollment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE ce.course_id = ? ORDER BY u.student_reg_no";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSemesterAwareUniqueKey(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEnrollment(rs));
                }
            }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch course enrollments: " + e.getMessage(), e);
        }
        return list;
    }

    public List<CourseEnrollment> getEnrollmentsByStudent(int studentId) {
        List<CourseEnrollment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE ce.student_id = ? ORDER BY c.course_code";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSemesterAwareUniqueKey(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEnrollment(rs));
                }
            }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student enrollments: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Ensures the enrollment key includes semester. Existing duplicate rows are
     * detected before the old key is replaced; no enrollment data is deleted.
     */
    private void ensureSemesterAwareUniqueKey(Connection conn) throws SQLException {
        boolean oldKey = false;
        boolean newKey = false;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT INDEX_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'course_enrollment' AND NON_UNIQUE = 0 GROUP BY INDEX_NAME" );
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString(1);
                if ("uq_student_course_year".equalsIgnoreCase(name)) oldKey = true;
                if ("uq_student_course_year_semester".equalsIgnoreCase(name)) newKey = true;
            }
        }
        if (newKey) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM (SELECT student_id, course_id, academic_year, semester " +
                "FROM course_enrollment GROUP BY student_id, course_id, academic_year, semester HAVING COUNT(*) > 1) d");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Duplicate enrollment rows exist for the same student, course, academic year and semester. Resolve those duplicates before applying the semester-aware enrollment key.");
            }
        }
        try (Statement st = conn.createStatement()) {
            if (oldKey) st.executeUpdate("ALTER TABLE course_enrollment DROP INDEX uq_student_course_year");
            st.executeUpdate("ALTER TABLE course_enrollment ADD UNIQUE INDEX uq_student_course_year_semester (student_id, course_id, academic_year, semester)");
        }
    }

    public boolean enrollStudent(int studentId, int courseId, String academicYear, int semester, boolean isRepeat) {
        if (studentId <= 0 || courseId <= 0 || academicYear == null || academicYear.trim().isEmpty() || semester < 1 || semester > 2) {
            throw new IllegalArgumentException("Valid student, course, academic year and semester are required.");
        }
        String sql = "INSERT INTO course_enrollment (student_id, course_id, academic_year, semester, is_repeat) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureSemesterAwareUniqueKey(conn);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                stmt.setString(3, academicYear.trim());
                stmt.setInt(4, semester);
                stmt.setBoolean(5, isRepeat);
                return stmt.executeUpdate() == 1;
            }
        } catch (SQLIntegrityConstraintViolationException duplicate) {
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to enroll student in course: " + e.getMessage(), e);
        }
    }

    public boolean isStudentEnrolled(int studentId, int courseId, String academicYear, int semester) {
        String sql = "SELECT COUNT(*) FROM course_enrollment WHERE student_id = ? AND course_id = ? AND academic_year = ? AND semester = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.setString(3, academicYear);
            stmt.setInt(4, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check enrollment: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean deleteEnrollment(int enrollmentId) {
        String sql = "DELETE FROM course_enrollment WHERE enrollment_id = ?";
        try (Connection conn = TG2102DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DatabaseException("Failed to cancel enrollment: " + e.getMessage(), e); }
    }

    private void ensureAcademicYearsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS academic_years (" +
                "academic_year VARCHAR(10) NOT NULL PRIMARY KEY," +
                "is_active TINYINT(1) NOT NULL DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }

        String countSql = "SELECT COUNT(*) FROM academic_years";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO academic_years (academic_year, is_active) VALUES ('2024', 0), ('2025', 0), ('2026', 1), ('2027', 0), ('2028', 0)")) {
                    ps.executeUpdate();
                }
            }
        }
    }

    public List<String> getAvailableAcademicYears() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT academic_year FROM academic_years WHERE academic_year REGEXP '^[0-9]{4}$' ORDER BY academic_year ASC";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureAcademicYearsTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("academic_year"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CourseEnrollmentDAO] Error loading academic years: " + e.getMessage());
        }
        if (list.isEmpty()) {
            list.addAll(List.of("2024", "2025", "2026", "2027", "2028"));
        }
        return list;
    }

    public String getActiveAcademicYear() {
        String sql = "SELECT academic_year FROM academic_years WHERE is_active = 1 AND academic_year REGEXP '^[0-9]{4}$' LIMIT 1";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureAcademicYearsTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("academic_year");
            }
        } catch (SQLException ignored) {
        }
        return "2026";
    }

    public boolean addAcademicYear(String year, boolean isActive) {
        if (year == null || !year.matches("^\\d{4}$")) {
            throw new IllegalArgumentException("Academic year must be in simple 4-digit YYYY format (e.g. 2026).");
        }
        String sql = "INSERT INTO academic_years (academic_year, is_active) VALUES (?, ?) ON DUPLICATE KEY UPDATE is_active = VALUES(is_active)";
        try (Connection conn = TG2102DatabaseConnection.getConnection()) {
            ensureAcademicYearsTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, year);
                ps.setInt(2, isActive ? 1 : 0);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add academic year: " + e.getMessage(), e);
        }
    }
}
