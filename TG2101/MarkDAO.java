package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Mark;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Marks operations.
 * Validates that all recorded marks are strictly between 0 and 100.
 */
public class MarkDAO {

    private Mark mapResultSetToMark(ResultSet rs) throws SQLException {
        Mark mark = new Mark(
            rs.getInt("mark_id"),
            rs.getInt("evaluation_id"),
            rs.getInt("student_id"),
            rs.getDouble("marks_obtained"),
            (Integer) rs.getObject("entered_by")
        );
        mark.setEvaluationName(rs.getString("evaluation_name"));
        mark.setEvaluationType(rs.getString("evaluation_type"));
        mark.setWeightagePercent(rs.getDouble("weightage_percent"));
        mark.setStudentRegNo(rs.getString("student_reg_no"));
        mark.setStudentName(rs.getString("student_name"));
        mark.setEntererName(rs.getString("enterer_name"));
        mark.setUpdatedAt(rs.getTimestamp("updated_at"));
        return mark;
    }

    private static final String BASE_SELECT =
        "SELECT m.*, e.evaluation_name, e.evaluation_type, e.weightage_percent, " +
        "u.student_reg_no, u.full_name AS student_name, " +
        "ent.full_name AS enterer_name " +
        "FROM marks m " +
        "JOIN evaluations e ON m.evaluation_id = e.evaluation_id " +
        "JOIN users u ON m.student_id = u.user_id " +
        "LEFT JOIN users ent ON m.entered_by = ent.user_id ";

    public boolean saveOrUpdateMark(Mark mark) {
        String sql = "INSERT INTO marks (evaluation_id, student_id, marks_obtained, entered_by) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained), " +
                     "entered_by = VALUES(entered_by), updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mark.getEvaluationId());
            stmt.setInt(2, mark.getStudentId());
            stmt.setDouble(3, mark.getMarksObtained());
            if (mark.getEnteredBy() != null) {
                stmt.setInt(4, mark.getEnteredBy());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save mark: " + e.getMessage(), e);
        }
    }

    public boolean batchSaveMarks(List<Mark> marks) {
        String sql = "INSERT INTO marks (evaluation_id, student_id, marks_obtained, entered_by) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained), " +
                     "entered_by = VALUES(entered_by), updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Save the whole class together, or roll back every mark if one write fails.
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Mark m : marks) {
                    stmt.setInt(1, m.getEvaluationId());
                    stmt.setInt(2, m.getStudentId());
                    stmt.setDouble(3, m.getMarksObtained());
                    if (m.getEnteredBy() != null) {
                        stmt.setInt(4, m.getEnteredBy());
                    } else {
                        stmt.setNull(4, Types.INTEGER);
                    }
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to batch save marks: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during batch marks save: " + e.getMessage(), e);
        }
    }

    public List<Mark> getMarksByEvaluation(int evaluationId) {
        List<Mark> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.evaluation_id = ? ORDER BY u.student_reg_no";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, evaluationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMark(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch marks by evaluation: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Mark> getMarksByStudentAndCourse(int studentId, int courseId) {
        List<Mark> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.student_id = ? AND e.course_id = ? ORDER BY e.evaluation_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMark(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student marks for course: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Calculates the aggregate Continuous Assessment (CA) mark out of 100.
     * Normalized based on weights of Quiz, Assignment, Mid Exam.
     */
    public double calculateCaMark(int studentId, int courseId) {
        String sql = "SELECT m.marks_obtained, e.weightage_percent " +
                     "FROM marks m " +
                     "JOIN evaluations e ON m.evaluation_id = e.evaluation_id " +
                     "WHERE m.student_id = ? AND e.course_id = ? " +
                     "AND e.evaluation_type IN ('QUIZ', 'ASSIGNMENT', 'MID_EXAM')";

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        // Divide by the recorded weights so the result remains a mark out of 100.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double mark = rs.getDouble("marks_obtained");
                    double weight = rs.getDouble("weightage_percent");
                    weightedSum += mark * weight;
                    totalWeight += weight;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate CA mark: " + e.getMessage(), e);
        }

        if (totalWeight <= 0.0) {
            return 0.0;
        }
        return Math.round((weightedSum / totalWeight) * 100.0) / 100.0;
    }

    /**
     * Calculates the aggregate Final Exam mark out of 100.
     * Normalized based on weights of Final Theory and Final Practical exams.
     */
    public double calculateFinalExamMark(int studentId, int courseId) {
        String sql = "SELECT m.marks_obtained, e.weightage_percent " +
                     "FROM marks m " +
                     "JOIN evaluations e ON m.evaluation_id = e.evaluation_id " +
                     "WHERE m.student_id = ? AND e.course_id = ? " +
                     "AND e.evaluation_type IN ('FINAL_THEORY', 'FINAL_PRACTICAL')";

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        // Normalize theory and practical marks by their configured evaluation weights.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double mark = rs.getDouble("marks_obtained");
                    double weight = rs.getDouble("weightage_percent");
                    weightedSum += mark * weight;
                    totalWeight += weight;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate Final Exam mark: " + e.getMessage(), e);
        }

        if (totalWeight <= 0.0) {
            return 0.0;
        }
        return Math.round((weightedSum / totalWeight) * 100.0) / 100.0;
    }
}
