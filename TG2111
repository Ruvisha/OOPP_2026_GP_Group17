package com.fot.ams.model;

import java.sql.Timestamp;

/**
 * Abstract base class representing a system user.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class User {
    private int userId;
    private String username;
    private String passwordHash;
    private int roleId;
    private String roleName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String profilePic = "default_avatar.png";
    private Integer departmentId;
    private String departmentName;
    private Timestamp createdAt;
    private String status = "ACTIVE";

    public User() {
    }

    public User(int userId, String username, String passwordHash, int roleId, String roleName,
                String fullName, String email, String phone, String address, Integer departmentId) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.roleName = roleName;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.departmentId = departmentId;
    }

    /**
     * Polymorphic method implemented by each subclass to return role title.
     */
    public abstract String getRoleTitle();

    /**
     * Polymorphic method to return dashboard title.
     */
    public abstract String getDashboardTitle();

    // Getters and Setters (Encapsulation)

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
