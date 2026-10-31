package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Evaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for course assessment evaluations.
 */
public class EvaluationDAO {

    private Evaluation mapResultSetToEvaluation(ResultSet rs) throws SQLException {
        Evaluation eval = new Evaluation(
            rs.getInt("evaluation_id"),
            rs.getInt("course_id"),
            rs.getString("evaluation_name"),
            rs.getString("evaluation_type"),
            rs.getDouble("max_marks"),
            rs.getDouble("weightage_percent"),
            rs.getInt("semester"),
            rs.getString("academic_year")
        );
        eval.setCourseCode(rs.getString("course_code"));
        eval.setCourseName(rs.getString("course_name"));
        return eval;
    }

    private static final String BASE_SELECT =
        "SELECT e.*, c.course_code, c.course_name " +
        "FROM evaluations e " +
        "JOIN courses c ON e.course_id = c.course_id ";

    // Include course details so callers can display evaluations without another query.
    public List<Evaluation> getEvaluationsByCourse(int courseId) {
        List<Evaluation> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE e.course_id = ? ORDER BY e.evaluation_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEvaluation(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch evaluations by course: " + e.getMessage(), e);
        }
        return list;
    }

    public Evaluation getEvaluationById(int evaluationId) {
        String sql = BASE_SELECT + "WHERE e.evaluation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, evaluationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvaluation(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch evaluation by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean addEvaluation(Evaluation eval) {
        String sql = "INSERT INTO evaluations (course_id, evaluation_name, evaluation_type, " +
                     "max_marks, weightage_percent, semester, academic_year) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, eval.getCourseId());
            stmt.setString(2, eval.getEvaluationName());
            stmt.setString(3, eval.getEvaluationType());
            stmt.setDouble(4, eval.getMaxMarks());
            stmt.setDouble(5, eval.getWeightagePercent());
            stmt.setInt(6, eval.getSemester());
            stmt.setString(7, eval.getAcademicYear());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                // Return the database-generated ID to the model that was just saved.
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        eval.setEvaluationId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add evaluation: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateEvaluation(Evaluation eval) {
        String sql = "UPDATE evaluations SET evaluation_name = ?, evaluation_type = ?, " +
                     "max_marks = ?, weightage_percent = ?, semester = ?, academic_year = ? " +
                     "WHERE evaluation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, eval.getEvaluationName());
            stmt.setString(2, eval.getEvaluationType());
            stmt.setDouble(3, eval.getMaxMarks());
            stmt.setDouble(4, eval.getWeightagePercent());
            stmt.setInt(5, eval.getSemester());
            stmt.setString(6, eval.getAcademicYear());
            stmt.setInt(7, eval.getEvaluationId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update evaluation: " + e.getMessage(), e);
        }
    }

    public boolean deleteEvaluation(int evaluationId) {
        String sql = "DELETE FROM evaluations WHERE evaluation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, evaluationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete evaluation: " + e.getMessage(), e);
        }
    }
}
