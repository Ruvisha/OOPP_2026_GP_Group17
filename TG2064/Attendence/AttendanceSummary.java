package com.fot.ams.model;

/**
 * Model representing attendance summary and percentage calculations.
 * Supports:
 * - Theory attendance
 * - Practical attendance
 * - Combined attendance
 * - 5 attendance scenarios (>80%, =80%, <80% no med, >80% with med, <80% with med)
 */
public class AttendanceSummary {
    private int studentId;
    private String studentRegNo;
    private String studentName;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String sessionType; // THEORY, PRACTICAL, COMBINED
    private int totalSessions = 15;
    private int presentSessions;
    private int absentSessions;
    private int approvedMedicals;
    private double rawPercentage;
    private double effectivePercentage;
    private String statusCategory;
    private boolean eligible;

    public AttendanceSummary() {
    }

    public AttendanceSummary(int studentId, String studentRegNo, String studentName,
                             int courseId, String courseCode, String courseName,
                             String sessionType, int totalSessions, int presentSessions,
                             int approvedMedicals) {
        this.studentId = studentId;
        this.studentRegNo = studentRegNo;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.sessionType = sessionType;
        this.totalSessions = totalSessions > 0 ? totalSessions : 15;
        this.presentSessions = presentSessions;
        this.absentSessions = Math.max(0, this.totalSessions - presentSessions);
        this.approvedMedicals = approvedMedicals;
        calculateStatus();
    }

    public void calculateStatus() {
        if (totalSessions <= 0) {
            rawPercentage = 0.0;
            effectivePercentage = 0.0;
            statusCategory = "No Sessions";
            eligible = false;
            return;
        }

        rawPercentage = ((double) presentSessions / totalSessions) * 100.0;
        int effectivePresent = Math.min(totalSessions, presentSessions + approvedMedicals);
        effectivePercentage = ((double) effectivePresent / totalSessions) * 100.0;

        // Determine 5 required scenarios
        if (rawPercentage > 80.0) {
            statusCategory = "More than 80%";
            eligible = true;
        } else if (Math.abs(rawPercentage - 80.0) < 0.001) {
            statusCategory = "Exactly 80%";
            eligible = true;
        } else {
            // raw < 80%
            if (approvedMedicals > 0) {
                if (effectivePercentage >= 80.0) {
                    statusCategory = "More than 80% (With Medicals)";
                    eligible = true;
                } else {
                    statusCategory = "Less than 80% (With Medicals)";
                    eligible = false;
                }
            } else {
                statusCategory = "Less than 80% (Without Medicals)";
                eligible = false;
            }
        }
    }

    // Getters and Setters

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

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
        calculateStatus();
    }

    public int getPresentSessions() {
        return presentSessions;
    }

    public void setPresentSessions(int presentSessions) {
        this.presentSessions = presentSessions;
        this.absentSessions = Math.max(0, this.totalSessions - presentSessions);
        calculateStatus();
    }

    public int getAbsentSessions() {
        return absentSessions;
    }

    public void setAbsentSessions(int absentSessions) {
        this.absentSessions = absentSessions;
    }

    public int getApprovedMedicals() {
        return approvedMedicals;
    }

    public void setApprovedMedicals(int approvedMedicals) {
        this.approvedMedicals = approvedMedicals;
        calculateStatus();
    }

    public double getRawPercentage() {
        return rawPercentage;
    }

    public double getEffectivePercentage() {
        return effectivePercentage;
    }

    public String getStatusCategory() {
        return statusCategory;
    }

    public boolean isEligible() {
        return eligible;
    }
}
