package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Role operations.
 */
public class RoleDAO {

    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT role_id, role_name, description FROM roles ORDER BY role_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Role role = new Role(
                    rs.getInt("role_id"),
                    rs.getString("role_name"),
                    rs.getString("description")
                );
                roles.add(role);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch roles: " + e.getMessage(), e);
        }
        return roles;
    }

    public Role getRoleById(int roleId) {
        String sql = "SELECT role_id, role_name, description FROM roles WHERE role_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Role(
                        rs.getInt("role_id"),
                        rs.getString("role_name"),
                        rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch role by ID: " + e.getMessage(), e);
        }
        return null;
    }
}
