package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations.
 * Demonstrates Polymorphism and DAO pattern by instantiating proper User subclasses.
 */
public class UserDAO {

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int roleId = rs.getInt("role_id");
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        String profilePic = rs.getString("profile_pic");
        Integer departmentId = (Integer) rs.getObject("department_id");
        String departmentName = rs.getString("department_name");
        Timestamp createdAt = rs.getTimestamp("created_at");
        String status = rs.getString("status");

        User user;
        switch (roleId) {
            case 1: // Admin
                Admin admin = new Admin(userId, username, passwordHash, fullName, email, phone, address,
                    departmentId, rs.getString("designation"));
                user = admin;
                break;
            case 2: // Lecturer
                Lecturer lec = new Lecturer(userId, username, passwordHash, fullName, email, phone, address,
                    departmentId, rs.getString("designation"), rs.getString("office_room"));
                user = lec;
                break;
            case 3: // Technical Officer
                TechnicalOfficer to = new TechnicalOfficer(userId, username, passwordHash, fullName, email, phone, address,
                    departmentId, rs.getString("designation"), rs.getString("assigned_lab"));
                user = to;
                break;
            case 4: // Undergraduate
                Undergraduate ug = new Undergraduate(userId, username, passwordHash, fullName, email, phone, address,
                    departmentId, rs.getString("student_reg_no"), rs.getString("academic_year"),
                    rs.getInt("current_semester"), rs.getString("student_type"));
                user = ug;
                break;
            default:
                throw new DatabaseException("Unknown role id: " + roleId);
        }

        user.setProfilePic(profilePic != null ? profilePic : "default_avatar.png");
        user.setDepartmentName(departmentName);
        user.setCreatedAt(createdAt);
        user.setStatus(status != null ? status : "ACTIVE");
        user.setRoleName(rs.getString("role_name"));

        return user;
    }

    private static final String BASE_SELECT =
        "SELECT u.*, r.role_name, d.department_name, d.department_code " +
        "FROM users u " +
        "JOIN roles r ON u.role_id = r.role_id " +
        "LEFT JOIN departments d ON u.department_id = d.department_id ";

    public User getUserByUsername(String username) {
        String sql = BASE_SELECT + "WHERE u.username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch user by username: " + e.getMessage(), e);
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = BASE_SELECT + "WHERE u.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch user by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY u.role_id, u.user_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all users: " + e.getMessage(), e);
        }
        return list;
    }

    public List<User> getUsersByRole(int roleId) {
        List<User> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE u.role_id = ? ORDER BY u.user_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch users by role: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, role_id, full_name, email, phone, " +
                     "address, profile_pic, department_id, status, student_reg_no, academic_year, " +
                     "current_semester, student_type, designation, office_room, assigned_lab) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setInt(3, user.getRoleId());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getAddress());
            stmt.setString(8, user.getProfilePic());
            if (user.getDepartmentId() != null) {
                stmt.setInt(9, user.getDepartmentId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            stmt.setString(10, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            if (user instanceof Undergraduate ug) {
                stmt.setString(11, ug.getStudentRegNo());
                stmt.setString(12, ug.getAcademicYear());
                stmt.setInt(13, ug.getCurrentSemester());
                stmt.setString(14, ug.getStudentType());
                stmt.setNull(15, Types.VARCHAR);
                stmt.setNull(16, Types.VARCHAR);
                stmt.setNull(17, Types.VARCHAR);
            } else if (user instanceof Lecturer lec) {
                stmt.setNull(11, Types.VARCHAR);
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.INTEGER);
                stmt.setNull(14, Types.VARCHAR);
                stmt.setString(15, lec.getDesignation());
                stmt.setString(16, lec.getOfficeRoom());
                stmt.setNull(17, Types.VARCHAR);
            } else if (user instanceof TechnicalOfficer to) {
                stmt.setNull(11, Types.VARCHAR);
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.INTEGER);
                stmt.setNull(14, Types.VARCHAR);
                stmt.setString(15, to.getDesignation());
                stmt.setNull(16, Types.VARCHAR);
                stmt.setString(17, to.getAssignedLab());
            } else if (user instanceof Admin admin) {
                stmt.setNull(11, Types.VARCHAR);
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.INTEGER);
                stmt.setNull(14, Types.VARCHAR);
                stmt.setString(15, admin.getDesignation());
                stmt.setNull(16, Types.VARCHAR);
                stmt.setNull(17, Types.VARCHAR);
            }

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert user: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, address = ?, " +
                     "department_id = ?, status = ?, student_reg_no = ?, academic_year = ?, " +
                     "current_semester = ?, student_type = ?, designation = ?, office_room = ?, assigned_lab = ? " +
                     "WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getAddress());
            if (user.getDepartmentId() != null) {
                stmt.setInt(5, user.getDepartmentId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setString(6, user.getStatus());

            if (user instanceof Undergraduate ug) {
                stmt.setString(7, ug.getStudentRegNo());
                stmt.setString(8, ug.getAcademicYear());
                stmt.setInt(9, ug.getCurrentSemester());
                stmt.setString(10, ug.getStudentType());
                stmt.setNull(11, Types.VARCHAR);
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.VARCHAR);
            } else if (user instanceof Lecturer lec) {
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.INTEGER);
                stmt.setNull(10, Types.VARCHAR);
                stmt.setString(11, lec.getDesignation());
                stmt.setString(12, lec.getOfficeRoom());
                stmt.setNull(13, Types.VARCHAR);
            } else if (user instanceof TechnicalOfficer to) {
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.INTEGER);
                stmt.setNull(10, Types.VARCHAR);
                stmt.setString(11, to.getDesignation());
                stmt.setNull(12, Types.VARCHAR);
                stmt.setString(13, to.getAssignedLab());
            } else if (user instanceof Admin admin) {
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.INTEGER);
                stmt.setNull(10, Types.VARCHAR);
                stmt.setString(11, admin.getDesignation());
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.VARCHAR);
            }

            stmt.setInt(14, user.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update password: " + e.getMessage(), e);
        }
    }

    public boolean updateContactDetails(int userId, String email, String phone, String address) {
        String sql = "UPDATE users SET email = ?, phone = ?, address = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setInt(4, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update contact details: " + e.getMessage(), e);
        }
    }

    public boolean updateProfilePicture(int userId, String profilePic) {
        String sql = "UPDATE users SET profile_pic = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profilePic);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update profile picture: " + e.getMessage(), e);
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    public boolean isUsernameTaken(String username, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND user_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check username uniqueness: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean isEmailTaken(String email, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check email uniqueness: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean isRegNoTaken(String regNo, int excludeUserId) {
        if (regNo == null || regNo.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM users WHERE student_reg_no = ? AND user_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, regNo);
            stmt.setInt(2, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check student reg no uniqueness: " + e.getMessage(), e);
        }
        return false;
    }
}
