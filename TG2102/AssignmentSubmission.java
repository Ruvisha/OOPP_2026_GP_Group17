package com.fot.ams.model;

import java.sql.Timestamp;

/**
 * Represents a student's submission for an Assignment in TG2102.
 */
public class AssignmentSubmission {
    private int id;
    private int assignmentId;
    private int studentId;
    private String filePath;
    private Timestamp submittedAt;
    private String status = "SUBMITTED"; // SUBMITTED, GRADED
    private Double marks;
    private String feedback;
    private Integer gradedBy;
    private Timestamp gradedAt;

    // Join display fields
    private String studentRegNo;
    private String studentName;
    private String assignmentTitle;
    private String courseCode;
    private double maxMarks = 100.0;
    private boolean marksReleased = false;

    public AssignmentSubmission() {
    }

    public AssignmentSubmission(int id, int assignmentId, int studentId, String filePath,
                                Timestamp submittedAt, String status, Double marks, String feedback) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.filePath = filePath;
        this.submittedAt = submittedAt;
        this.status = status;
        this.marks = marks;
        this.feedback = feedback;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getMarks() { return marks; }
    public void setMarks(Double marks) { this.marks = marks; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Integer getGradedBy() { return gradedBy; }
    public void setGradedBy(Integer gradedBy) { this.gradedBy = gradedBy; }

    public Timestamp getGradedAt() { return gradedAt; }
    public void setGradedAt(Timestamp gradedAt) { this.gradedAt = gradedAt; }

    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }

    public boolean isMarksReleased() { return marksReleased; }
    public void setMarksReleased(boolean marksReleased) { this.marksReleased = marksReleased; }

    public boolean isGraded() {
        return "GRADED".equalsIgnoreCase(status) && marks != null;
    }

    public String getFileName() {
        if (filePath == null || filePath.isEmpty()) return "";
        int idx = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return idx >= 0 ? filePath.substring(idx + 1) : filePath;
    }
}
