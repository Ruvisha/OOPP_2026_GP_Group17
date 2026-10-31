package com.fot.ams.service;

import com.fot.ams.dao.AttendanceDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Attendance;
import com.fot.ams.model.AttendanceSummary;
import com.fot.ams.util.ValidationUtil;

import java.util.List;

/**
 * Service managing attendance records, summaries, and percentages.
 */
public class AttendanceService {
    private final AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }

    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    public boolean recordAttendance(Attendance att) throws ValidationException {
        ValidationUtil.validatePositive(att.getStudentId(), "Student");
        ValidationUtil.validatePositive(att.getCourseId(), "Course");
        ValidationUtil.validateNotEmpty(att.getSessionType(), "Session Type");
        if (att.getSessionNumber() < 1 || att.getSessionNumber() > 15) {
            throw new ValidationException("Session number must be between 1 and 15.");
        }
        return attendanceDAO.recordAttendance(att);
    }

    public boolean batchRecordAttendance(List<Attendance> attendances) {
        if (attendances == null || attendances.isEmpty()) return false;
        return attendanceDAO.batchRecordAttendance(attendances);
    }

    public List<Attendance> getAttendanceByStudentAndCourse(int studentId, int courseId, String sessionType) {
        return attendanceDAO.getAttendanceByStudentAndCourse(studentId, courseId, sessionType);
    }

    public List<Attendance> getAttendanceByCourseAndSession(int courseId, String sessionType, int sessionNumber) {
        return attendanceDAO.getAttendanceByCourseAndSession(courseId, sessionType, sessionNumber);
    }

    public AttendanceSummary getStudentAttendanceSummary(int studentId, int courseId, String sessionType) {
        return attendanceDAO.getStudentAttendanceSummary(studentId, courseId, sessionType);
    }

    public List<AttendanceSummary> getBatchAttendanceSummary(int courseId, String sessionType) {
        return attendanceDAO.getBatchAttendanceSummary(courseId, sessionType);
    }
}
