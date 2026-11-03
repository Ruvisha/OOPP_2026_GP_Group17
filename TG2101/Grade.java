package com.fot.ams.model;

import java.sql.Timestamp;

/**
 * Represents the final grade record for a student in a course.
 */
public class Grade {
    private int gradeId;
    private int studentId;
    private String studentRegNo;
    private String studentName;
    private int courseId;
    private String courseCode;
    private String courseName;
    private int credits = 2;
    private String academicYear = "2024/2025";
    private double caMark;
    private double finalExamMark;
    private double totalMark;
    private String gradeLetter;
    private double gradePoint;
    private boolean isRepeat;
    private Timestamp calculatedAt;

    public Grade() {
    }

    public Grade(int gradeId, int studentId, int courseId, String academicYear,
                 double caMark, double finalExamMark, double totalMark,
                 String gradeLetter, double gradePoint, boolean isRepeat) {
        this.gradeId = gradeId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.academicYear = academicYear;
        this.caMark = caMark;
        this.finalExamMark = finalExamMark;
        this.totalMark = totalMark;
        this.gradeLetter = gradeLetter;
        this.gradePoint = gradePoint;
        this.isRepeat = isRepeat;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public double getCaMark() {
        return caMark;
    }

    public void setCaMark(double caMark) {
        this.caMark = caMark;
    }

    public double getFinalExamMark() {
        return finalExamMark;
    }

    public void setFinalExamMark(double finalExamMark) {
        this.finalExamMark = finalExamMark;
    }

    public double getTotalMark() {
        return totalMark;
    }

    public void setTotalMark(double totalMark) {
        this.totalMark = totalMark;
    }

    public String getGradeLetter() {
        return gradeLetter;
    }

    public void setGradeLetter(String gradeLetter) {
        this.gradeLetter = gradeLetter;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(double gradePoint) {
        this.gradePoint = gradePoint;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    public Timestamp getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Timestamp calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
