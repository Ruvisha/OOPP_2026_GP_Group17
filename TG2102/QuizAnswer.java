package com.fot.ams.model;

/**
 * Represents a student's answer to a specific Quiz question.
 */
public class QuizAnswer {
    private int id;
    private int attemptId;
    private int questionId;
    private String answerText;
    private double awardedMarks;
    private String feedback;

    // Join fields from QuizQuestion
    private String questionText;
    private String questionType;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private double maxMarks;

    public QuizAnswer() {
    }

    public QuizAnswer(int id, int attemptId, int questionId, String answerText, double awardedMarks) {
        this.id = id;
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.answerText = answerText;
        this.awardedMarks = awardedMarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public double getAwardedMarks() { return awardedMarks; }
    public void setAwardedMarks(double awardedMarks) { this.awardedMarks = awardedMarks; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }
}
