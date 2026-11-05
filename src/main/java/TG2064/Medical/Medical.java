package com.fot.ams.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents a student medical certificate record.
 */
public class Medical {
    private int medicalId;
    private int studentId;
    private String studentRegNo;
    private String studentName;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String sessionType = "THEORY"; // THEORY, PRACTICAL, BOTH
    private String fromDate;
    private String toDate;
    private Date submissionDate;
    private String reason;
    private String medicalDocPath;
    private String status = "NOT_APPROVED"; // APPROVED, NOT_APPROVED
    private Integer reviewedBy;
    private String reviewerName;
    private Timestamp reviewedAt;
    private String remarks;

    public Medical() {
    }

    public Medical(int medicalId, int studentId, int courseId, String sessionType,
                   String fromDate, String toDate, Date submissionDate, String reason,
                   String medicalDocPath, String status) {
        this.medicalId = medicalId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.sessionType = sessionType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.submissionDate = submissionDate;
        this.reason = reason;
        this.medicalDocPath = medicalDocPath;
        this.status = status;
    }

    // Compatibility methods for file path handling
    public String getFilePath() {
        return medicalDocPath;
    }

    public void setFilePath(String filePath) {
        this.medicalDocPath = filePath;
    }

    public int getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(int medicalId) {
        this.medicalId = medicalId;
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

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMedicalDocPath() {
        return medicalDocPath;
    }

    public void setMedicalDocPath(String medicalDocPath) {
        this.medicalDocPath = medicalDocPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Timestamp getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Timestamp reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }
}