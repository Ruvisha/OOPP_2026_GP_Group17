package com.fot.ams.model;

/**
 * Represents an Undergraduate student.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Undergraduate extends User {
    private String studentRegNo;
    private String academicYear = "2024/2025";
    private int currentSemester = 1;
    private String studentType = "REGULAR"; // REGULAR, REPEAT, BATCH_MISSED

    public Undergraduate() {
        super();
        setRoleName("Undergraduate");
        setRoleId(4);
    }

    public Undergraduate(int userId, String username, String passwordHash, String fullName,
                         String email, String phone, String address, Integer departmentId,
                         String studentRegNo, String academicYear, int currentSemester, String studentType) {
        super(userId, username, passwordHash, 4, "Undergraduate", fullName, email, phone, address, departmentId);
        this.studentRegNo = studentRegNo;
        this.academicYear = academicYear;
        this.currentSemester = currentSemester;
        this.studentType = studentType != null ? studentType : "REGULAR";
    }

    public String getStudentRegNo() {
        return studentRegNo;
    }

    public void setStudentRegNo(String studentRegNo) {
        this.studentRegNo = studentRegNo;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public int getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }

    public String getStudentType() {
        return studentType;
    }

    public void setStudentType(String studentType) {
        this.studentType = studentType;
    }

    public boolean isRepeat() {
        return "REPEAT".equalsIgnoreCase(studentType);
    }

    public boolean isBatchMissed() {
        return "BATCH_MISSED".equalsIgnoreCase(studentType);
    }

    @Override
    public String getRoleTitle() {
        return "Undergraduate (" + (studentRegNo != null ? studentRegNo : "") +
               (isRepeat() ? " - Repeat" : isBatchMissed() ? " - Batch Missed" : "") + ")";
    }

    @Override
    public String getDashboardTitle() {
        return "Student Academic Portal - Faculty of Technology";
    }
}
