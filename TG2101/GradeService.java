package com.fot.ams.service;

import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.dao.GradeDAO;
import com.fot.ams.dao.MarkDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.CourseEnrollment;
import com.fot.ams.model.Grade;
import com.fot.ams.model.User;

import java.util.List;

/**
 * Service managing grade calculations and records according to UGC Commission Circular No. 12-2024.
 * Centralized grading criteria ensuring consistent calculations across all modules.
 */
public class GradeService {
    private final GradeDAO gradeDAO;
    private final MarkDAO markDAO;
    private final CourseEnrollmentDAO enrollmentDAO;
    private final UserDAO userDAO;

    public GradeService() {
        this.gradeDAO = new GradeDAO();
        this.markDAO = new MarkDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();
        this.userDAO = new UserDAO();
    }

    public GradeService(GradeDAO gradeDAO, MarkDAO markDAO, CourseEnrollmentDAO enrollmentDAO, UserDAO userDAO) {
        this.gradeDAO = gradeDAO;
        this.markDAO = markDAO;
        this.enrollmentDAO = enrollmentDAO;
        this.userDAO = userDAO;
    }

    /**
     * Determines grade letter from total mark based on UGC Commission Circular No. 12-2024.
     */
    public static String calculateGradeLetter(double totalMark, boolean isRepeat) {
        // Check from the highest range downward so each total maps to one grade.
        String letter;
        if (totalMark >= 85.0) {
            letter = "A+";
        } else if (totalMark >= 80.0) {
            letter = "A";
        } else if (totalMark >= 75.0) {
            letter = "A-";
        } else if (totalMark >= 70.0) {
            letter = "B+";
        } else if (totalMark >= 65.0) {
            letter = "B";
        } else if (totalMark >= 60.0) {
            letter = "B-";
        } else if (totalMark >= 55.0) {
            letter = "C+";
        } else if (totalMark >= 50.0) {
            letter = "C";
        } else if (totalMark >= 45.0) {
            letter = "C-";
        } else if (totalMark >= 40.0) {
            letter = "D+";
        } else if (totalMark >= 35.0) {
            letter = "D";
        } else {
            letter = "E";
        }

        // Standard Sri Lankan university repeat rule: Repeat pass capped at C
        if (isRepeat && (letter.startsWith("A") || letter.startsWith("B") || letter.equals("C+"))) {
            letter = "C";
        }

        return letter;
    }

    /**
     * Determines Grade Point (GP) from Grade Letter.
     */
    public static double calculateGradePoint(String gradeLetter) {
        if (gradeLetter == null) return 0.0;
        return switch (gradeLetter.trim().toUpperCase()) {
            case "A+", "A" -> 4.00;
            case "A-" -> 3.70;
            case "B+" -> 3.30;
            case "B" -> 3.00;
            case "B-" -> 2.70;
            case "C+" -> 2.30;
            case "C" -> 2.00;
            case "C-" -> 1.70;
            case "D+" -> 1.30;
            case "D" -> 1.00;
            default -> 0.00; // "E" or invalid
        };
    }

    /**
     * Calculates and saves the grade for a student in a course.
     */
    public Grade calculateAndSaveGrade(int studentId, int courseId, String academicYear) {
        double caMark = markDAO.calculateCaMark(studentId, courseId);
        double finalExamMark = markDAO.calculateFinalExamMark(studentId, courseId);

        // Combine the normalized component marks using the course's standard 40/60 split.
        double totalMark;
        String gradeLetter;
        double gradePoint;

        User user = userDAO.getUserById(studentId);
        // Check studentType field (REPEAT/REGULAR/BATCH_MISSED) — not roleTitle which is always "Undergraduate"
        boolean isRepeat = user instanceof com.fot.ams.model.Undergraduate
                && "REPEAT".equalsIgnoreCase(((com.fot.ams.model.Undergraduate) user).getStudentType());

        // Check if student satisfied CA >= 40% eligibility
        if (caMark < 40.0) {
            totalMark = caMark * 0.40 + finalExamMark * 0.60;
            gradeLetter = "E"; // Ineligible due to CA failure
            gradePoint = 0.00;
        } else {
            totalMark = Math.round((caMark * 0.40 + finalExamMark * 0.60) * 10.0) / 10.0;
            gradeLetter = calculateGradeLetter(totalMark, isRepeat);
            gradePoint = calculateGradePoint(gradeLetter);
        }

        Grade grade = new Grade(
            0, studentId, courseId, academicYear != null ? academicYear : "2024/2025",
            caMark, finalExamMark, totalMark, gradeLetter, gradePoint, isRepeat
        );

        gradeDAO.saveOrUpdateGrade(grade);
        return grade;
    }

    /**
     * Calculates grades for all students enrolled in a course.
     */
    public void calculateGradesForCourse(int courseId, String academicYear) {
        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByCourse(courseId);
        for (CourseEnrollment ce : enrollments) {
            calculateAndSaveGrade(ce.getStudentId(), courseId, academicYear);
        }
    }

    public List<Grade> getGradesByStudent(int studentId) {
        return gradeDAO.getGradesByStudent(studentId);
    }

    public List<Grade> getGradesByCourse(int courseId) {
        return gradeDAO.getGradesByCourse(courseId);
    }

    public List<Grade> getAllGrades() {
        return gradeDAO.getAllGrades();
    }
}
