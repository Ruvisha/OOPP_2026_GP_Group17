package com.fot.ams.model;

import java.sql.Timestamp;

/**
 * Represents a mark scored by a student in an evaluation.
 * All marks are strictly validated to be out of 100 (0 <= mark <= 100).
 */
public class Mark {
    private int markId;
    private int evaluationId;
    private String evaluationName;
    private String evaluationType;
    private double weightagePercent;
    private int studentId;
    private String studentRegNo;
    private String studentName;
    private double marksObtained;
    private Integer enteredBy;
    private String entererName;
    private Timestamp updatedAt;

    public Mark() {
    }

    public Mark(int markId, int evaluationId, int studentId, double marksObtained, Integer enteredBy) {
        this.markId = markId;
        this.evaluationId = evaluationId;
        this.studentId = studentId;
        this.marksObtained = marksObtained;
        this.enteredBy = enteredBy;
    }

    public int getMarkId() {
        return markId;
    }

    public void setMarkId(int markId) {
        this.markId = markId;
    }

    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getEvaluationName() {
        return evaluationName;
    }

    public void setEvaluationName(String evaluationName) {
        this.evaluationName = evaluationName;
    }

    public String getEvaluationType() {
        return evaluationType;
    }

    public void setEvaluationType(String evaluationType) {
        this.evaluationType = evaluationType;
    }

    public double getWeightagePercent() {
        return weightagePercent;
    }

    public void setWeightagePercent(double weightagePercent) {
        this.weightagePercent = weightagePercent;
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

    public double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(double marksObtained) {
        this.marksObtained = marksObtained;
    }

    public Integer getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(Integer enteredBy) {
        this.enteredBy = enteredBy;
    }

    public String getEntererName() {
        return entererName;
    }

    public void setEntererName(String entererName) {
        this.entererName = entererName;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
