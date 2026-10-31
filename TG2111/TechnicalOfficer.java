package com.fot.ams.model;

/**
 * Represents a Technical Officer user.
 * Demonstrates Inheritance and Polymorphism.
 */
public class TechnicalOfficer extends User {
    private String designation;
    private String assignedLab;

    public TechnicalOfficer() {
        super();
        setRoleName("Technical Officer");
        setRoleId(3);
    }

    public TechnicalOfficer(int userId, String username, String passwordHash, String fullName,
                            String email, String phone, String address, Integer departmentId,
                            String designation, String assignedLab) {
        super(userId, username, passwordHash, 3, "Technical Officer", fullName, email, phone, address, departmentId);
        this.designation = designation;
        this.assignedLab = assignedLab;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getAssignedLab() {
        return assignedLab;
    }

    public void setAssignedLab(String assignedLab) {
        this.assignedLab = assignedLab;
    }

    @Override
    public String getRoleTitle() {
        return "Technical Officer (" + (assignedLab != null ? assignedLab : "General Lab") + ")";
    }

    @Override
    public String getDashboardTitle() {
        return "Technical Operations Portal - Faculty of Technology";
    }
}
