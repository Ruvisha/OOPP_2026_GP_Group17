package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Grade records.
 */
public class GradeDAO {

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade(
            rs.getInt("grade_id"),
            rs.getInt("student_id"),
            rs.getInt("course_id"),
            rs.getString("academic_year"),
            rs.getDouble("ca_mark"),
            rs.getDouble("final_exam_mark"),
            rs.getDouble("total_mark"),
            rs.getString("grade_letter"),
            rs.getDouble("grade_point"),
            rs.getBoolean("is_repeat")
        );
        grade.setStudentRegNo(rs.getString("student_reg_no"));
        grade.setStudentName(rs.getString("student_name"));
        grade.setCourseCode(rs.getString("course_code"));
        grade.setCourseName(rs.getString("course_name"));
        grade.setCredits(rs.getInt("credits"));
        grade.setCalculatedAt(rs.getTimestamp("calculated_at"));
        return grade;
    }

    private static final String BASE_SELECT =
        "SELECT g.*, u.student_reg_no, u.full_name AS student_name, " +
        "c.course_code, c.course_name, c.credits " +
        "FROM grades g " +
        "JOIN users u ON g.student_id = u.user_id " +
        "JOIN courses c ON g.course_id = c.course_id ";

    public List<Grade> getAllGrades() {
        List<Grade> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY c.course_code, u.student_reg_no";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToGrade(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all grades: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Grade> getGradesByStudent(int studentId) {
        List<Grade> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE g.student_id = ? ORDER BY c.course_code";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGrade(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student grades: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Grade> getGradesByCourse(int courseId) {
        List<Grade> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE g.course_id = ? ORDER BY u.student_reg_no";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGrade(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch course grades: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean saveOrUpdateGrade(Grade grade) {
        // The unique student/course/year key lets recalculation replace an existing grade.
        String sql = "INSERT INTO grades (student_id, course_id, academic_year, ca_mark, " +
                     "final_exam_mark, total_mark, grade_letter, grade_point, is_repeat) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE ca_mark = VALUES(ca_mark), " +
                     "final_exam_mark = VALUES(final_exam_mark), total_mark = VALUES(total_mark), " +
                     "grade_letter = VALUES(grade_letter), grade_point = VALUES(grade_point), " +
                     "is_repeat = VALUES(is_repeat), calculated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, grade.getStudentId());
            stmt.setInt(2, grade.getCourseId());
            stmt.setString(3, grade.getAcademicYear());
            stmt.setDouble(4, grade.getCaMark());
            stmt.setDouble(5, grade.getFinalExamMark());
            stmt.setDouble(6, grade.getTotalMark());
            stmt.setString(7, grade.getGradeLetter());
            stmt.setDouble(8, grade.getGradePoint());
            stmt.setBoolean(9, grade.isRepeat());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save grade: " + e.getMessage(), e);
        }
    }
}
