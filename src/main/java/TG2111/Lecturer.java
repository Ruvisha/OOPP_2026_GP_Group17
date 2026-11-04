package com.fot.ams.model;

/**
 * Represents a Lecturer user.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Lecturer extends User {
    private String designation;
    private String officeRoom;

    public Lecturer() {
        super();
        setRoleName("Lecturer");
        setRoleId(2);
    }

    public Lecturer(int userId, String username, String passwordHash, String fullName,
                    String email, String phone, String address, Integer departmentId,
                    String designation, String officeRoom) {
        super(userId, username, passwordHash, 2, "Lecturer", fullName, email, phone, address, departmentId);
        this.designation = designation;
        this.officeRoom = officeRoom;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getOfficeRoom() {
        return officeRoom;
    }

    public void setOfficeRoom(String officeRoom) {
        this.officeRoom = officeRoom;
    }

    @Override
    public String getRoleTitle() {
        return designation != null ? designation : "Lecturer";
    }

    @Override
    public String getDashboardTitle() {
        return "Lecturer Academic Portal - Faculty of Technology";
    }
}
