package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEnrollment(rs));
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
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student enrollments: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean enrollStudent(int studentId, int courseId, String academicYear, int semester, boolean isRepeat) {
        String sql = "INSERT INTO course_enrollment (student_id, course_id, academic_year, semester, is_repeat) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE is_repeat = VALUES(is_repeat)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.setString(3, academicYear);
            stmt.setInt(4, semester);
            stmt.setBoolean(5, isRepeat);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to enroll student in course: " + e.getMessage(), e);
        }
    }

    public boolean isStudentEnrolled(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM course_enrollment WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check enrollment: " + e.getMessage(), e);
        }
        return false;
    }
}
