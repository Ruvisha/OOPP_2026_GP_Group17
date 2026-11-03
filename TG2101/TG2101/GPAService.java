package com.fot.ams.service;

import com.fot.ams.dao.GradeDAO;
import com.fot.ams.model.Grade;

import java.util.List;

/**
 * Service calculating SGPA (Semester Grade Point Average) and
 * CGPA (Cumulative Grade Point Average) for students.
 */
public class GPAService {
    private final GradeDAO gradeDAO;

    public GPAService() {
        this.gradeDAO = new GradeDAO();
    }

    public GPAService(GradeDAO gradeDAO) {
        this.gradeDAO = gradeDAO;
    }

    /**
     * Calculates Semester GPA (SGPA) for a list of grades.
     * Formula: sum(credit * grade_point) / sum(credit)
     */
    public double calculateSgpa(List<Grade> semesterGrades) {
        if (semesterGrades == null || semesterGrades.isEmpty()) {
            return 0.0;
        }

        double totalPoints = 0.0;
        int totalCredits = 0;

        for (Grade g : semesterGrades) {
            // Use the model's default credit value when a grade has no valid credit count.
            int cred = g.getCredits() > 0 ? g.getCredits() : 2;
            totalPoints += cred * g.getGradePoint();
            totalCredits += cred;
        }

        if (totalCredits == 0) return 0.0;
        return Math.round((totalPoints / totalCredits) * 100.0) / 100.0;
    }

    /**
     * Calculates Cumulative GPA (CGPA) for a student across all completed grades.
     */
    public double calculateCgpaForStudent(int studentId) {
        List<Grade> grades = gradeDAO.getGradesByStudent(studentId);
        return calculateSgpa(grades);
    }

    /**
     * Determines the degree classification from CGPA using the project's class thresholds.
     */
    public String getClassification(double cgpa) {
        if (cgpa >= 3.70) {
            return "First Class Honours";
        } else if (cgpa >= 3.30) {
            return "Second Class (Upper Division) Honours";
        } else if (cgpa >= 3.00) {
            return "Second Class (Lower Division) Honours";
        } else if (cgpa >= 2.00) {
            return "General Pass";
        } else {
            return "Academic Probation / Incomplete";
        }
    }
}
