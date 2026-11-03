package com.fot.ams.model;

/**
 * Model representing eligibility of a student to sit for the final examination.
 * Evaluates CA criteria (CA >= 40%) and Attendance criteria (Attendance >= 80%).
 */
public class EligibilityRecord {
    private int studentId;
    private String studentRegNo;
    private String studentName;
    private int courseId;
    private String courseCode;
    private String courseName;
    private double caMark;
    private boolean caEligible;
    private double attendancePercentage;
    private boolean attendanceEligible;
    private boolean overallEligible;
    private String remarks;

    public EligibilityRecord() {
    }

    public EligibilityRecord(int studentId, String studentRegNo, String studentName,
                             int courseId, String courseCode, String courseName,
                             double caMark, double attendancePercentage) {
        this.studentId = studentId;
        this.studentRegNo = studentRegNo;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.caMark = caMark;
        this.attendancePercentage = attendancePercentage;
        evaluate();
    }

    public void evaluate() {
        // Final-exam eligibility requires both the CA mark and attendance thresholds.
        this.caEligible = (caMark >= 40.0);
        this.attendanceEligible = (attendancePercentage >= 80.0);
        this.overallEligible = (caEligible && attendanceEligible);

        if (overallEligible) {
            this.remarks = "Eligible to sit for final examination";
        } else if (!caEligible && !attendanceEligible) {
            this.remarks = "Not Eligible: CA < 40% AND Attendance < 80%";
        } else if (!caEligible) {
            this.remarks = "Not Eligible: CA < 40%";
        } else {
            this.remarks = "Not Eligible: Attendance < 80%";
        }
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentRegNo() {
        return studentRegNo;
    }

    public void setStudentRegNo(String studentRegNo) {
        this.studentRegNo = studentRegNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getCaMark() {
        return caMark;
    }

    public void setCaMark(double caMark) {
        this.caMark = caMark;
        evaluate();
    }

    public boolean isCaEligible() {
        return caEligible;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
        evaluate();
    }

    public boolean isAttendanceEligible() {
        return attendanceEligible;
    }

    public boolean isOverallEligible() {
        return overallEligible;
    }

    public String getRemarks() {
        return remarks;
    }
}
