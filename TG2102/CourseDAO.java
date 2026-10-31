package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course operations.
 */
public class CourseDAO {

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
        course.setDepartmentName(rs.getString("department_name"));
        course.setDepartmentCode(rs.getString("department_code"));
        return course;
    }

    private static final String BASE_SELECT =
        "SELECT c.*, d.department_name, d.department_code " +
        "FROM courses c " +
        "JOIN departments d ON c.department_id = d.department_id ";

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.semester, c.course_code";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all courses: " + e.getMessage(), e);
        }
        return list;
    }

    public Course getCourseById(int courseId) {
        String sql = BASE_SELECT + "WHERE c.course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch course by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Course> getCoursesByDepartment(int departmentId) {
        List<Course> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.department_id = ? ORDER BY c.semester, c.course_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch courses by department: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Course> getCoursesForStudent(int studentId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, d.department_name, d.department_code " +
                     "FROM courses c " +
                     "JOIN departments d ON c.department_id = d.department_id " +
                     "JOIN course_enrollment e ON c.course_id = e.course_id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY c.semester, c.course_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student enrolled courses: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (course_code, course_name, credits, semester, " +
                     "department_id, has_theory, has_practical, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getSemester());
            stmt.setInt(5, course.getDepartmentId());
            stmt.setBoolean(6, course.isHasTheory());
            stmt.setBoolean(7, course.isHasPractical());
            stmt.setString(8, course.getDescription());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        course.setCourseId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add course: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, credits = ?, " +
                     "semester = ?, department_id = ?, has_theory = ?, has_practical = ?, description = ? " +
                     "WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getSemester());
            stmt.setInt(5, course.getDepartmentId());
            stmt.setBoolean(6, course.isHasTheory());
            stmt.setBoolean(7, course.isHasPractical());
            stmt.setString(8, course.getDescription());
            stmt.setInt(9, course.getCourseId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update course: " + e.getMessage(), e);
        }
    }

    public boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete course: " + e.getMessage(), e);
        }
    }

    public boolean isCourseCodeTaken(String code, int excludeCourseId) {
        String sql = "SELECT COUNT(*) FROM courses WHERE course_code = ? AND course_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
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
