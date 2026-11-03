package com.fot.ams.model;

import java.sql.Timestamp;

/**
 * Represents a student's attempt at an Online Quiz.
 */
public class QuizAttempt {
    private int id;
    private int quizId;
    private int studentId;
    private Timestamp startedAt;
    private Timestamp submittedAt;
    private double score;
    private double totalMarks;
    private String status = "IN_PROGRESS"; // IN_PROGRESS, SUBMITTED, GRADED
    private Integer gradedBy;
    private Timestamp gradedAt;

    // Join display fields
    private String studentRegNo;
    private String studentName;
    private String quizTitle;
    private String courseCode;
    private String courseName;

    public QuizAttempt() {
    }

    public QuizAttempt(int id, int quizId, int studentId, Timestamp startedAt,
                       Timestamp submittedAt, double score, double totalMarks, String status) {
        this.id = id;
        this.quizId = quizId;
        this.studentId = studentId;
        this.startedAt = startedAt;
        this.submittedAt = submittedAt;
        this.score = score;
        this.totalMarks = totalMarks;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(double totalMarks) { this.totalMarks = totalMarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getGradedBy() { return gradedBy; }
    public void setGradedBy(Integer gradedBy) { this.gradedBy = gradedBy; }

    public Timestamp getGradedAt() { return gradedAt; }
    public void setGradedAt(Timestamp gradedAt) { this.gradedAt = gradedAt; }

    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public boolean isCompleted() {
        return "SUBMITTED".equalsIgnoreCase(status) || "GRADED".equalsIgnoreCase(status);
    }
}
