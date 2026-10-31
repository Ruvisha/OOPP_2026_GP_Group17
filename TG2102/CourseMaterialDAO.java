package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.CourseMaterial;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course Materials.
 */
public class CourseMaterialDAO {

    private CourseMaterial mapResultSetToMaterial(ResultSet rs) throws SQLException {
        CourseMaterial material = new CourseMaterial(
            rs.getInt("material_id"),
            rs.getInt("course_id"),
            rs.getInt("lecturer_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("file_path")
        );
        material.setCourseCode(rs.getString("course_code"));
        material.setCourseName(rs.getString("course_name"));
        material.setLecturerName(rs.getString("lecturer_name"));
        material.setUploadedAt(rs.getTimestamp("uploaded_at"));
        return material;
    }

    private static final String BASE_SELECT =
        "SELECT cm.*, c.course_code, c.course_name, u.full_name AS lecturer_name " +
        "FROM course_materials cm " +
        "JOIN courses c ON cm.course_id = c.course_id " +
        "JOIN users u ON cm.lecturer_id = u.user_id ";

    public List<CourseMaterial> getAllMaterials() {
        List<CourseMaterial> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY cm.uploaded_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToMaterial(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all course materials: " + e.getMessage(), e);
        }
        return list;
    }

    public List<CourseMaterial> getMaterialsByCourse(int courseId) {
        List<CourseMaterial> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE cm.course_id = ? ORDER BY cm.uploaded_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMaterial(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch course materials by course: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addMaterial(CourseMaterial material) {
        String sql = "INSERT INTO course_materials (course_id, lecturer_id, title, description, file_path) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, material.getCourseId());
            stmt.setInt(2, material.getLecturerId());
            stmt.setString(3, material.getTitle());
            stmt.setString(4, material.getDescription());
            stmt.setString(5, material.getFilePath());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        material.setMaterialId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add course material: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateMaterial(CourseMaterial material) {
        String sql = "UPDATE course_materials SET course_id = ?, title = ?, description = ?, file_path = ? " +
                     "WHERE material_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, material.getCourseId());
            stmt.setString(2, material.getTitle());
            stmt.setString(3, material.getDescription());
            stmt.setString(4, material.getFilePath());
            stmt.setInt(5, material.getMaterialId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update course material: " + e.getMessage(), e);
        }
    }

    public boolean deleteMaterial(int materialId) {
        String sql = "DELETE FROM course_materials WHERE material_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, materialId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete course material: " + e.getMessage(), e);
        }
    }
}
