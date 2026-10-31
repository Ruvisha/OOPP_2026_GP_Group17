package com.fot.ams.model;

/**
 * Represents an assessment evaluation component for a course.
 */
public class Evaluation {
    private int evaluationId;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String evaluationName;
    private String evaluationType; // QUIZ, ASSIGNMENT, MID_EXAM, FINAL_THEORY, FINAL_PRACTICAL, CA_AGGREGATE
    private double maxMarks = 100.0;
    private double weightagePercent;
    private int semester = 1;
    private String academicYear = "2024/2025";

    public Evaluation() {
    }

    public Evaluation(int evaluationId, int courseId, String evaluationName,
                      String evaluationType, double maxMarks, double weightagePercent,
                      int semester, String academicYear) {
        this.evaluationId = evaluationId;
        this.courseId = courseId;
        this.evaluationName = evaluationName;
        this.evaluationType = evaluationType;
        this.maxMarks = maxMarks;
        this.weightagePercent = weightagePercent;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    public int getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(int evaluationId) {
        this.evaluationId = evaluationId;
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

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getWeightagePercent() {
        return weightagePercent;
    }

    public void setWeightagePercent(double weightagePercent) {
        this.weightagePercent = weightagePercent;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    // CA components are combined separately from the final examination components.
    public boolean isCaComponent() {
        return "QUIZ".equalsIgnoreCase(evaluationType) ||
               "ASSIGNMENT".equalsIgnoreCase(evaluationType) ||
               "MID_EXAM".equalsIgnoreCase(evaluationType);
    }

    public boolean isFinalExamComponent() {
        return "FINAL_THEORY".equalsIgnoreCase(evaluationType) ||
               "FINAL_PRACTICAL".equalsIgnoreCase(evaluationType);
    }

    @Override
    public String toString() {
        return evaluationName + " (" + weightagePercent + "%)";
    }
}
