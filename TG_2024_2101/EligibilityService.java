package com.fot.ams.service;

import com.fot.ams.dao.AttendanceDAO;
import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.dao.MarkDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.AttendanceSummary;
import com.fot.ams.model.Course;
import com.fot.ams.model.CourseEnrollment;
import com.fot.ams.model.EligibilityRecord;
import com.fot.ams.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Service calculating and evaluating final examination eligibility.
 * Evaluates:
 * 1. CA Eligibility: CA >= 40%
 * 2. Attendance Eligibility: Attendance >= 80% (including approved medicals)
 * 3. Combined Overall Eligibility
 */
public class EligibilityService {
    private final MarkDAO markDAO;
    private final AttendanceDAO attendanceDAO;
    private final CourseDAO courseDAO;
    private final UserDAO userDAO;
    private final CourseEnrollmentDAO enrollmentDAO;

    public EligibilityService() {
        this.markDAO = new MarkDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();
        this.userDAO = new UserDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();
    }

    public EligibilityService(MarkDAO markDAO, AttendanceDAO attendanceDAO, CourseDAO courseDAO,
                              UserDAO userDAO, CourseEnrollmentDAO enrollmentDAO) {
        this.markDAO = markDAO;
        this.attendanceDAO = attendanceDAO;
        this.courseDAO = courseDAO;
        this.userDAO = userDAO;
        this.enrollmentDAO = enrollmentDAO;
    }

    /**
     * Evaluates individual eligibility for a student in a course.
     */
    public EligibilityRecord getStudentEligibility(int studentId, int courseId) {
        User user = userDAO.getUserById(studentId);
        Course course = courseDAO.getCourseById(courseId);

        String regNo = user != null ? user.getUsername() : "";
        String studentName = user != null ? user.getFullName() : "";
        String courseCode = course != null ? course.getCourseCode() : "";
        String courseName = course != null ? course.getCourseName() : "";

        double caMark = markDAO.calculateCaMark(studentId, courseId);
        AttendanceSummary attSummary = attendanceDAO.getStudentAttendanceSummary(studentId, courseId, "THEORY");
        double attPct = attSummary != null ? attSummary.getEffectivePercentage() : 0.0;

        return new EligibilityRecord(
            studentId, regNo, studentName,
            courseId, courseCode, courseName,
            caMark, attPct
        );
    }

    /**
     * Evaluates batch eligibility for all students enrolled in a course.
     */
    public List<EligibilityRecord> getBatchEligibility(int courseId) {
        List<EligibilityRecord> list = new ArrayList<>();
        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByCourse(courseId);

        for (CourseEnrollment ce : enrollments) {
            list.add(getStudentEligibility(ce.getStudentId(), courseId));
        }
        return list;
    }
}
