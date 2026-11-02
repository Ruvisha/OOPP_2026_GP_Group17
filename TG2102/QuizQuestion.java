package com.fot.ams.model;

/**
 * Represents a Question in an Online Quiz.
 * Supports MULTIPLE_CHOICE, TRUE_FALSE, and SHORT_ANSWER.
 */
public class QuizQuestion {
    public static final String TYPE_MCQ = "MULTIPLE_CHOICE";
    public static final String TYPE_TRUE_FALSE = "TRUE_FALSE";
    public static final String TYPE_SHORT_ANSWER = "SHORT_ANSWER";

    private int id;
    private int quizId;
    private String questionText;
    private String questionType = TYPE_MCQ;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private double marks = 1.0;

    public QuizQuestion() {
    }

    public QuizQuestion(int id, int quizId, String questionText, String questionType,
                        String optionA, String optionB, String optionC, String optionD,
                        String correctAnswer, double marks) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

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

    public double getMarks() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }

    public boolean isMultipleChoice() {
        return TYPE_MCQ.equalsIgnoreCase(questionType);
    }

    public boolean isTrueFalse() {
        return TYPE_TRUE_FALSE.equalsIgnoreCase(questionType);
    }

    public boolean isShortAnswer() {
        return TYPE_SHORT_ANSWER.equalsIgnoreCase(questionType);
    }

    @Override
    public String toString() {
        return "[" + questionType + "] " + (questionText != null && questionText.length() > 50
                ? questionText.substring(0, 47) + "..." : questionText);
    }
}
