package com.fot.ams.model;

/**
 * Represents an Administrator user.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Admin extends User {
    private String designation;

    public Admin() {
        super();
        setRoleName("Admin");
        setRoleId(1);
    }

    public Admin(int userId, String username, String passwordHash, String fullName,
                 String email, String phone, String address, Integer departmentId, String designation) {
        super(userId, username, passwordHash, 1, "Admin", fullName, email, phone, address, departmentId);
        this.designation = designation;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    @Override
    public String getRoleTitle() {
        return "System Administrator";
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Control Portal - Faculty of Technology";
    }
}
