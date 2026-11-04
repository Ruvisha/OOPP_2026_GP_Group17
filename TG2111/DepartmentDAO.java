package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Department operations.
 */
public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT department_id, department_code, department_name, description FROM departments ORDER BY department_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Department dept = new Department(
                    rs.getInt("department_id"),
                    rs.getString("department_code"),
                    rs.getString("department_name"),
                    rs.getString("description")
                );
                departments.add(dept);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch departments: " + e.getMessage(), e);
        }
        return departments;
    }

    public Department getDepartmentById(int departmentId) {
        String sql = "SELECT department_id, department_code, department_name, description FROM departments WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Department(
                        rs.getInt("department_id"),
                        rs.getString("department_code"),
                        rs.getString("department_name"),
                        rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch department by ID: " + e.getMessage(), e);
        }
        return null;
    }
}
