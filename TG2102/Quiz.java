package com.fot.ams.model;

import java.sql.Timestamp;

/**
 * Represents an Online Quiz entity for the TG2102 module.
 */
public class Quiz {
    private int id;
    private int courseId;
    private int lecturerId;
    private Integer evaluationId;
    private String title;
    private int durationMinutes = 30;
    private Timestamp startTime;
    private Timestamp endTime;
    private int maxAttempts = 1;
    private String status = "DRAFT"; // DRAFT, PUBLISHED, CLOSED
    private boolean marksReleased = false;
    private String filePath;
    private Timestamp createdAt;

    // Informational fields from joins
    private String courseCode;
    private String courseName;
    private String lecturerName;
    private int questionCount;
    private double totalMarks;

    public Quiz() {
    }

    public Quiz(int id, int courseId, int lecturerId, String title, int durationMinutes,
            Timestamp startTime, Timestamp endTime, int maxAttempts, String status) {
        this.id = id;
        this.courseId = courseId;
        this.lecturerId = lecturerId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxAttempts = maxAttempts;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(int lecturerId) {
        this.lecturerId = lecturerId;
    }

    public Integer getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Integer evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isMarksReleased() {
        return marksReleased;
    }

    public void setMarksReleased(boolean marksReleased) {
        this.marksReleased = marksReleased;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(double totalMarks) {
        this.totalMarks = totalMarks;
    }

    public boolean isPublished() {
        return "PUBLISHED".equalsIgnoreCase(status);
    }

    public boolean isClosed() {
        return "CLOSED".equalsIgnoreCase(status);
    }

    public boolean isAvailableNow() {
        if (!isPublished())
            return false;
        long now = System.currentTimeMillis();
        if (startTime != null && now < startTime.getTime())
            return false;
        if (endTime != null && now > endTime.getTime())
            return false;
        return true;
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
