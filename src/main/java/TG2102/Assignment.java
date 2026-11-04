package com.fot.ams.model;

import java.sql.Timestamp;

/**
 * Represents a Course Assignment entity for the TG2102 module.
 */
public class Assignment {
    private int id;
    private int courseId;
    private int lecturerId;
    private Integer evaluationId;
    private String title;
    private String instructions;
    private double maxMarks = 100.0;
    private Timestamp dueDate;
    private String status = "DRAFT"; // DRAFT, PUBLISHED, CLOSED
    private boolean marksReleased = false;
    private String filePath;
    private Timestamp createdAt;

    // Join display fields
    private String courseCode;
    private String courseName;
    private String lecturerName;
    private int submissionCount;

    public Assignment() {
    }

    public Assignment(int id, int courseId, int lecturerId, String title,
                      String instructions, double maxMarks, Timestamp dueDate, String status) {
        this.id = id;
        this.courseId = courseId;
        this.lecturerId = lecturerId;
        this.title = title;
        this.instructions = instructions;
        this.maxMarks = maxMarks;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getLecturerId() { return lecturerId; }
    public void setLecturerId(int lecturerId) { this.lecturerId = lecturerId; }

    public Integer getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Integer evaluationId) { this.evaluationId = evaluationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }

    public Timestamp getDueDate() { return dueDate; }
    public void setDueDate(Timestamp dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isMarksReleased() { return marksReleased; }
    public void setMarksReleased(boolean marksReleased) { this.marksReleased = marksReleased; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    public int getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(int submissionCount) { this.submissionCount = submissionCount; }

    public boolean isPublished() {
        return "PUBLISHED".equalsIgnoreCase(status);
    }

    public boolean isClosed() {
        return "CLOSED".equalsIgnoreCase(status);
    }

    public boolean isPastDue() {
        return dueDate != null && System.currentTimeMillis() > dueDate.getTime();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean hasAttachment() {
        return filePath != null && !filePath.trim().isEmpty();
    }

    public String getAttachmentFileName() {
        if (!hasAttachment())
            return "";
        return new java.io.File(filePath).getName();
    }

    @Override
    public String toString() {
        return title + " (" + (courseCode != null ? courseCode : "Course " + courseId) + ")";
    }
}
