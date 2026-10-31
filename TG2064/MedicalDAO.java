package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Medical;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Medical Record management.
 */
public class MedicalDAO {

    private Medical mapResultSetToMedical(ResultSet rs) throws SQLException {
        Medical med = new Medical(
            rs.getInt("medical_id"),
            rs.getInt("student_id"),
            rs.getInt("course_id"),
            rs.getString("session_type"),
            rs.getDate("session_date"),
            rs.getDate("submission_date"),
            rs.getString("reason"),
            rs.getString("medical_doc_path"),
            rs.getString("status")
        );
        med.setStudentRegNo(rs.getString("student_reg_no"));
        med.setStudentName(rs.getString("student_name"));
        med.setCourseCode(rs.getString("course_code"));
        med.setCourseName(rs.getString("course_name"));
        med.setReviewedBy((Integer) rs.getObject("reviewed_by"));
        med.setReviewerName(rs.getString("reviewer_name"));
        med.setReviewedAt(rs.getTimestamp("reviewed_at"));
        med.setRemarks(rs.getString("remarks"));
        return med;
    }

    private static final String BASE_SELECT =
        "SELECT m.*, u.student_reg_no, u.full_name AS student_name, " +
        "c.course_code, c.course_name, rev.full_name AS reviewer_name " +
        "FROM medical m " +
        "JOIN users u ON m.student_id = u.user_id " +
        "JOIN courses c ON m.course_id = c.course_id " +
        "LEFT JOIN users rev ON m.reviewed_by = rev.user_id ";

    public List<Medical> getAllMedicals() {
        List<Medical> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY m.submission_date DESC, m.medical_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToMedical(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all medical records: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Medical> getMedicalsByStudent(int studentId) {
        List<Medical> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.student_id = ? ORDER BY m.submission_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedical(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student medical records: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Medical> getMedicalsByCourse(int courseId) {
        List<Medical> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.course_id = ? ORDER BY m.submission_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedical(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch course medical records: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addMedical(Medical med) {
        String sql = "INSERT INTO medical (student_id, course_id, session_type, session_date, " +
                     "submission_date, reason, medical_doc_path, status, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, med.getStudentId());
            stmt.setInt(2, med.getCourseId());
            stmt.setString(3, med.getSessionType());
            stmt.setDate(4, med.getSessionDate());
            stmt.setDate(5, med.getSubmissionDate());
            stmt.setString(6, med.getReason());
            stmt.setString(7, med.getMedicalDocPath());
            stmt.setString(8, med.getStatus() != null ? med.getStatus() : "NOT_APPROVED");
            stmt.setString(9, med.getRemarks());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        med.setMedicalId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add medical record: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateMedicalStatus(int medicalId, String status, int reviewerId, String remarks) {
        String sql = "UPDATE medical SET status = ?, reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP, remarks = ? " +
                     "WHERE medical_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, reviewerId);
            stmt.setString(3, remarks);
            stmt.setInt(4, medicalId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update medical status: " + e.getMessage(), e);
        }
    }

    public boolean deleteMedical(int medicalId) {
        String sql = "DELETE FROM medical WHERE medical_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete medical record: " + e.getMessage(), e);
        }
    }
}
