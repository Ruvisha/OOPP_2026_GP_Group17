package com.fot.ams.service;

import com.fot.ams.dao.AssignmentDAO;
import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.dao.QuizDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.*;
import com.fot.ams.util.SessionManager;
import com.fot.ams.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Service orchestrating Online Quizzes and Assignment Management in TG2102.
 * Enforces business logic, authentication constraints, role validation,
 * and safe file management.
 */
public class AssessmentService {

    private final QuizDAO quizDAO;
    private final AssignmentDAO assignmentDAO;
    private final CourseDAO courseDAO;
    private final CourseEnrollmentDAO enrollmentDAO;

    public AssessmentService() {
        this.quizDAO = new QuizDAO();
        this.assignmentDAO = new AssignmentDAO();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();
    }

    // ==========================================
    // Lecturer Authorization & Course Access
    // ==========================================

    public List<Course> getAuthorizedCoursesForLecturer(int lecturerId) {
        return courseDAO.getCoursesByLecturer(lecturerId);
    }

    public List<Course> getEnrolledCoursesForStudent(int studentId) {
        return courseDAO.getCoursesForStudent(studentId);
    }

    // ==========================================
    // Online Quiz Operations (Lecturer)
    // ==========================================

    public List<Quiz> getQuizzesByCourse(int courseId) {
        return quizDAO.getQuizzesByCourse(courseId);
    }

    public List<Quiz> getQuizzesByLecturer(int lecturerId) {
        return quizDAO.getQuizzesByLecturer(lecturerId);
    }

    public Quiz getQuizById(int quizId) {
        return quizDAO.getQuizById(quizId);
    }

    public boolean createQuiz(Quiz quiz) throws ValidationException {
        ValidationUtil.validateNotEmpty(quiz.getTitle(), "Quiz Title");
        ValidationUtil.validatePositive(quiz.getCourseId(), "Course");
        if (quiz.getDurationMinutes() <= 0) {
            throw new ValidationException("Quiz duration must be greater than zero.");
        }

        if (quiz.getStartTime() != null && quiz.getEndTime() != null) {
            if (!quiz.getEndTime().after(quiz.getStartTime())) {
                throw new ValidationException("Quiz closing date & time must be after opening date & time.");
            }
        }

        if ("PUBLISHED".equalsIgnoreCase(quiz.getStatus())) {
            validateQuizAvailability(quiz);
        }

        return quizDAO.createQuiz(quiz);
    }

    public boolean updateQuiz(Quiz quiz) throws ValidationException {
        ValidationUtil.validateNotEmpty(quiz.getTitle(), "Quiz Title");
        if (quiz.getDurationMinutes() <= 0) {
            throw new ValidationException("Quiz duration must be greater than zero.");
        }

        if (quiz.getStartTime() != null && quiz.getEndTime() != null) {
            if (!quiz.getEndTime().after(quiz.getStartTime())) {
                throw new ValidationException("Quiz closing date & time must be after opening date & time.");
            }
        }

        if ("PUBLISHED".equalsIgnoreCase(quiz.getStatus())) {
            validateQuizAvailability(quiz);
        }

        return quizDAO.updateQuiz(quiz);
    }

    public void validateQuizAvailability(Quiz quiz) throws ValidationException {
        if (quiz.getStartTime() == null) {
            throw new ValidationException("Opening date & time must be set before publishing the quiz.");
        }
        if (quiz.getEndTime() == null) {
            throw new ValidationException("Closing date & time must be set before publishing the quiz.");
        }
        if (!quiz.getEndTime().after(quiz.getStartTime())) {
            throw new ValidationException("Quiz closing date & time must be after opening date & time.");
        }
        if (quiz.getEndTime().getTime() <= System.currentTimeMillis()) {
            throw new ValidationException(
                    "Cannot publish quiz: Closing date and time has already passed. Please set a future closing date and time.");
        }
        long windowMinutes = (quiz.getEndTime().getTime() - quiz.getStartTime().getTime()) / (60 * 1000);
        if (windowMinutes < quiz.getDurationMinutes()) {
            throw new ValidationException("Quiz availability window (" + windowMinutes
                    + " mins) must be at least as long as quiz duration (" + quiz.getDurationMinutes() + " mins).");
        }
    }

    public boolean publishQuiz(int quizId) throws ValidationException {
        Quiz q = quizDAO.getQuizById(quizId);
        if (q != null) {
            validateQuizAvailability(q);
        }
        return quizDAO.updateQuizStatus(quizId, "PUBLISHED");
    }

    public boolean unpublishQuiz(int quizId) {
        return quizDAO.updateQuizStatus(quizId, "DRAFT");
    }

    public boolean closeQuiz(int quizId) {
        return quizDAO.updateQuizStatus(quizId, "CLOSED");
    }

    public boolean setQuizMarksReleased(int quizId, boolean released) {
        return quizDAO.setMarksReleased(quizId, released);
    }

    public boolean deleteQuiz(int quizId) {
        return quizDAO.deleteQuiz(quizId);
    }

    // Questions
    public List<QuizQuestion> getQuestionsByQuiz(int quizId) {
        return quizDAO.getQuestionsByQuiz(quizId);
    }

    public boolean addQuestion(QuizQuestion q) throws ValidationException {
        ValidationUtil.validateNotEmpty(q.getQuestionText(), "Question Text");
        ValidationUtil.validateNotEmpty(q.getQuestionType(), "Question Type");
        if (q.getMarks() <= 0)
            throw new ValidationException("Question marks must be greater than 0.");

        if (q.isMultipleChoice()) {
            ValidationUtil.validateNotEmpty(q.getOptionA(), "Option A");
            ValidationUtil.validateNotEmpty(q.getOptionB(), "Option B");
            ValidationUtil.validateNotEmpty(q.getCorrectAnswer(), "Correct Answer");
        } else if (q.isTrueFalse()) {
            ValidationUtil.validateNotEmpty(q.getCorrectAnswer(), "Correct Answer (True or False)");
        }

        return quizDAO.addQuestion(q);
    }

    public boolean updateQuestion(QuizQuestion q) throws ValidationException {
        ValidationUtil.validateNotEmpty(q.getQuestionText(), "Question Text");
        if (q.getMarks() <= 0)
            throw new ValidationException("Question marks must be greater than 0.");
        return quizDAO.updateQuestion(q);
    }

    public boolean deleteQuestion(int questionId) {
        return quizDAO.deleteQuestion(questionId);
    }

    // Attempts and Grading
    public List<QuizAttempt> getAttemptsByQuiz(int quizId) {
        return quizDAO.getAttemptsByQuiz(quizId);
    }

    public List<QuizAnswer> getAnswersByAttempt(int attemptId) {
        return quizDAO.getAnswersByAttempt(attemptId);
    }

    public boolean gradeAnswer(int answerId, double awardedMarks, String feedback, int lecturerId)
            throws ValidationException {
        if (awardedMarks < 0)
            throw new ValidationException("Awarded marks cannot be negative.");
        return quizDAO.gradeAnswer(answerId, awardedMarks, feedback, lecturerId);
    }

    // ==========================================
    // Online Quiz Operations (Student)
    // ==========================================

    public boolean hasStudentAttemptedQuiz(int quizId, int studentId) {
        return quizDAO.hasStudentAttempted(quizId, studentId);
    }

    public QuizAttempt getStudentAttempt(int quizId, int studentId) {
        return quizDAO.getStudentAttempt(quizId, studentId);
    }

    public QuizAttempt startQuizAttempt(int quizId, int studentId) throws ValidationException {
        Quiz quiz = quizDAO.getQuizById(quizId);
        if (quiz == null)
            throw new ValidationException("Quiz does not exist.");
        if (!quiz.isPublished())
            throw new ValidationException("This quiz is not currently published.");
        if (quiz.isClosed())
            throw new ValidationException("This quiz has closed.");

        long now = System.currentTimeMillis();
        if (quiz.getStartTime() != null && now < quiz.getStartTime().getTime()) {
            throw new ValidationException("This quiz will open on " + quiz.getStartTime() + ".");
        }
        if (quiz.getEndTime() != null && now > quiz.getEndTime().getTime()) {
            throw new ValidationException("The deadline for this quiz has expired.");
        }

        if (quizDAO.hasStudentAttempted(quizId, studentId)) {
            throw new ValidationException("You have already submitted an attempt for this quiz.");
        }

        return quizDAO.startAttempt(quizId, studentId);
    }

    public boolean saveStudentAnswer(int attemptId, int questionId, String answerText) {
        return quizDAO.saveAnswer(attemptId, questionId, answerText);
    }

    public boolean submitQuizAttempt(int attemptId, int studentId) {
        return quizDAO.submitAttempt(attemptId, studentId);
    }

    // ==========================================
    // Assignment Operations (Lecturer)
    // ==========================================

    public List<Assignment> getAssignmentsByCourse(int courseId) {
        return assignmentDAO.getAssignmentsByCourse(courseId);
    }

    public List<Assignment> getAssignmentsByLecturer(int lecturerId) {
        return assignmentDAO.getAssignmentsByLecturer(lecturerId);
    }

    public Assignment getAssignmentById(int assignmentId) {
        return assignmentDAO.getAssignmentById(assignmentId);
    }

    public boolean createAssignment(Assignment assignment) throws ValidationException {
        ValidationUtil.validateNotEmpty(assignment.getTitle(), "Assignment Title");
        ValidationUtil.validatePositive(assignment.getCourseId(), "Course");
        if (assignment.getMaxMarks() <= 0)
            throw new ValidationException("Maximum marks must be greater than 0.");

        if ("PUBLISHED".equalsIgnoreCase(assignment.getStatus())) {
            validateAssignmentAvailability(assignment);
        }
        return assignmentDAO.createAssignment(assignment);
    }

    public boolean updateAssignment(Assignment assignment) throws ValidationException {
        ValidationUtil.validateNotEmpty(assignment.getTitle(), "Assignment Title");
        if (assignment.getMaxMarks() <= 0)
            throw new ValidationException("Maximum marks must be greater than 0.");

        if ("PUBLISHED".equalsIgnoreCase(assignment.getStatus())) {
            validateAssignmentAvailability(assignment);
        }
        return assignmentDAO.updateAssignment(assignment);
    }

    public void validateAssignmentAvailability(Assignment assignment) throws ValidationException {
        if (assignment.getDueDate() == null) {
            throw new ValidationException("Deadline Date & Time must be set before publishing the assignment.");
        }
        if (assignment.getDueDate().getTime() <= System.currentTimeMillis()) {
            throw new ValidationException("Cannot publish assignment: Deadline must be a valid future date and time.");
        }
    }

    public boolean publishAssignment(int assignmentId) throws ValidationException {
        Assignment a = assignmentDAO.getAssignmentById(assignmentId);
        if (a != null) {
            validateAssignmentAvailability(a);
        }
        return assignmentDAO.updateAssignmentStatus(assignmentId, "PUBLISHED");
    }

    public boolean closeAssignment(int assignmentId) {
        return assignmentDAO.updateAssignmentStatus(assignmentId, "CLOSED");
    }

    public boolean updateAssignmentStatus(int assignmentId, String status) {
        return assignmentDAO.updateAssignmentStatus(assignmentId, status);
    }

    public boolean setAssignmentMarksReleased(int assignmentId, boolean released) {
        return assignmentDAO.setMarksReleased(assignmentId, released);
    }

    public boolean deleteAssignment(int assignmentId) {
        return assignmentDAO.deleteAssignment(assignmentId);
    }

    public List<AssignmentSubmission> getSubmissionsByAssignment(int assignmentId) {
        return assignmentDAO.getSubmissionsByAssignment(assignmentId);
    }

    public boolean gradeAssignmentSubmission(int submissionId, double marks, String feedback, int lecturerId)
            throws ValidationException {
        if (marks < 0)
            throw new ValidationException("Marks cannot be negative.");
        return assignmentDAO.gradeSubmission(submissionId, marks, feedback, lecturerId);
    }

    // ==========================================
    // Assignment Operations (Student)
    // ==========================================

    public AssignmentSubmission getStudentSubmission(int assignmentId, int studentId) {
        return assignmentDAO.getSubmissionByStudent(assignmentId, studentId);
    }

    public boolean submitAssignmentFile(int assignmentId, int studentId, File sourceFile) throws ValidationException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new ValidationException("Please select a valid file to upload.");
        }

        Assignment assignment = assignmentDAO.getAssignmentById(assignmentId);
        if (assignment == null)
            throw new ValidationException("Assignment not found.");
        if (!assignment.isPublished())
            throw new ValidationException("This assignment is currently not active.");
        if (assignment.isClosed())
            throw new ValidationException("This assignment is closed.");

        // Safe file storage
        try {
            Path targetDir = Paths.get("uploads", "assignments");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String origName = sourceFile.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String safeFileName = "std_" + studentId + "_asg_" + assignmentId + "_"
                    + UUID.randomUUID().toString().substring(0, 8) + "_" + origName;
            Path destPath = targetDir.resolve(safeFileName);
            Files.copy(sourceFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            return assignmentDAO.submitAssignment(assignmentId, studentId, destPath.toString());
        } catch (IOException e) {
            throw new ValidationException("Failed to safely store the uploaded file: " + e.getMessage());
        }
    }

    public String saveQuizQuestionPaperFile(int lecturerId, File pdfFile) throws ValidationException {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new ValidationException("Please select a valid PDF file.");
        }
        if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
            throw new ValidationException("Selected file must be a PDF document (*.pdf).");
        }
        try {
            Path targetDir = Paths.get("uploads", "quizzes");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            String origName = pdfFile.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String safeFileName = "quiz_qp_lec_" + lecturerId + "_"
                    + UUID.randomUUID().toString().substring(0, 8) + "_" + origName;
            Path destPath = targetDir.resolve(safeFileName);
            Files.copy(pdfFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            return destPath.toString();
        } catch (IOException e) {
            throw new ValidationException("Failed to store the uploaded quiz PDF: " + e.getMessage());
        }
    }

    public String saveAssignmentBriefFile(int lecturerId, File pdfFile) throws ValidationException {
        if (pdfFile == null || !pdfFile.exists()) {
            throw new ValidationException("Please select a valid PDF file.");
        }
        if (!pdfFile.getName().toLowerCase().endsWith(".pdf")) {
            throw new ValidationException("Selected file must be a PDF document (*.pdf).");
        }
        try {
            Path targetDir = Paths.get("uploads", "assignments", "briefs");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            String origName = pdfFile.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String safeFileName = "asg_brief_lec_" + lecturerId + "_"
                    + UUID.randomUUID().toString().substring(0, 8) + "_" + origName;
            Path destPath = targetDir.resolve(safeFileName);
            Files.copy(pdfFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            return destPath.toString();
        } catch (IOException e) {
            throw new ValidationException("Failed to store the uploaded assignment PDF: " + e.getMessage());
        }
    }
}
