package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Attendance;
import com.fot.ams.model.AttendanceSummary;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Attendance operations.
 * Implements individual and batch attendance recording and percentage calculation.
 */
public class AttendanceDAO {

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance att = new Attendance(
            rs.getInt("attendance_id"),
            rs.getInt("student_id"),
            rs.getInt("course_id"),
            rs.getDate("session_date"),
            rs.getString("session_type"),
            rs.getInt("session_number"),
            rs.getInt("hours"),
            rs.getString("status"),
            (Integer) rs.getObject("recorded_by")
        );
        att.setStudentRegNo(rs.getString("student_reg_no"));
        att.setStudentName(rs.getString("student_name"));
        att.setCourseCode(rs.getString("course_code"));
        att.setCourseName(rs.getString("course_name"));
        att.setRecorderName(rs.getString("recorder_name"));
        att.setRecordedAt(rs.getTimestamp("recorded_at"));
        return att;
    }

    private static final String BASE_SELECT =
        "SELECT a.*, u.student_reg_no, u.full_name AS student_name, c.course_code, c.course_name, " +
        "rec.full_name AS recorder_name " +
        "FROM attendance a " +
        "JOIN users u ON a.student_id = u.user_id " +
        "JOIN courses c ON a.course_id = c.course_id " +
        "LEFT JOIN users rec ON a.recorded_by = rec.user_id ";

    public boolean recordAttendance(Attendance att) {
        String sql = "INSERT INTO attendance (student_id, course_id, session_date, session_type, " +
                     "session_number, hours, status, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE session_date = VALUES(session_date), " +
                     "status = VALUES(status), recorded_by = VALUES(recorded_by), recorded_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, att.getStudentId());
            stmt.setInt(2, att.getCourseId());
            stmt.setDate(3, att.getSessionDate());
            stmt.setString(4, att.getSessionType());
            stmt.setInt(5, att.getSessionNumber());
            stmt.setInt(6, att.getHours());
            stmt.setString(7, att.getStatus());
            if (att.getRecordedBy() != null) {
                stmt.setInt(8, att.getRecordedBy());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to record attendance: " + e.getMessage(), e);
        }
    }

    public boolean batchRecordAttendance(List<Attendance> attendances) {
        String sql = "INSERT INTO attendance (student_id, course_id, session_date, session_type, " +
                     "session_number, hours, status, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE session_date = VALUES(session_date), " +
                     "status = VALUES(status), recorded_by = VALUES(recorded_by), recorded_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Attendance att : attendances) {
                    stmt.setInt(1, att.getStudentId());
                    stmt.setInt(2, att.getCourseId());
                    stmt.setDate(3, att.getSessionDate());
                    stmt.setString(4, att.getSessionType());
                    stmt.setInt(5, att.getSessionNumber());
                    stmt.setInt(6, att.getHours());
                    stmt.setString(7, att.getStatus());
                    if (att.getRecordedBy() != null) {
                        stmt.setInt(8, att.getRecordedBy());
                    } else {
                        stmt.setNull(8, Types.INTEGER);
                    }
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to execute batch attendance: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during batch attendance: " + e.getMessage(), e);
        }
    }

    public List<Attendance> getAttendanceByStudentAndCourse(int studentId, int courseId, String sessionType) {
        List<Attendance> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(BASE_SELECT);
        sb.append("WHERE a.student_id = ? AND a.course_id = ? ");
        if (sessionType != null && !sessionType.equalsIgnoreCase("COMBINED")) {
            sb.append("AND a.session_type = ? ");
        }
        sb.append("ORDER BY a.session_type, a.session_number");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sb.toString())) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            if (sessionType != null && !sessionType.equalsIgnoreCase("COMBINED")) {
                stmt.setString(3, sessionType);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch student attendance: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Attendance> getAttendanceByCourseAndSession(int courseId, String sessionType, int sessionNumber) {
        List<Attendance> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE a.course_id = ? AND a.session_type = ? AND a.session_number = ? ORDER BY u.student_reg_no";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            stmt.setString(2, sessionType);
            stmt.setInt(3, sessionNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch session attendance: " + e.getMessage(), e);
        }
        return list;
    }

    public AttendanceSummary getStudentAttendanceSummary(int studentId, int courseId, String sessionType) {
        int totalExpectedSessions = "COMBINED".equalsIgnoreCase(sessionType) ? 30 : 15;

        String attSql;
        if ("COMBINED".equalsIgnoreCase(sessionType)) {
            attSql = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND course_id = ? AND status = 'PRESENT'";
        } else {
            attSql = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND course_id = ? AND session_type = ? AND status = 'PRESENT'";
        }

        String medSql;
        if ("COMBINED".equalsIgnoreCase(sessionType)) {
            medSql = "SELECT COUNT(*) FROM medical WHERE student_id = ? AND course_id = ? AND status = 'APPROVED'";
        } else {
            medSql = "SELECT COUNT(*) FROM medical WHERE student_id = ? AND course_id = ? AND (session_type = ? OR session_type = 'BOTH') AND status = 'APPROVED'";
        }

        String studentInfoSql = "SELECT u.student_reg_no, u.full_name, c.course_code, c.course_name " +
                               "FROM users u CROSS JOIN courses c " +
                               "WHERE u.user_id = ? AND c.course_id = ?";

        int presentCount = 0;
        int approvedMedCount = 0;
        String regNo = "";
        String studentName = "";
        String courseCode = "";
        String courseName = "";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Student & Course info
            try (PreparedStatement stmt = conn.prepareStatement(studentInfoSql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        regNo = rs.getString("student_reg_no");
                        studentName = rs.getString("full_name");
                        courseCode = rs.getString("course_code");
                        courseName = rs.getString("course_name");
                    }
                }
            }

            // Present sessions
            try (PreparedStatement stmt = conn.prepareStatement(attSql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                if (!"COMBINED".equalsIgnoreCase(sessionType)) {
                    stmt.setString(3, sessionType);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        presentCount = rs.getInt(1);
                    }
                }
            }

            // Approved medicals
            try (PreparedStatement stmt = conn.prepareStatement(medSql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                if (!"COMBINED".equalsIgnoreCase(sessionType)) {
                    stmt.setString(3, sessionType);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        approvedMedCount = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate attendance summary: " + e.getMessage(), e);
        }

        return new AttendanceSummary(
            studentId, regNo, studentName,
            courseId, courseCode, courseName,
            sessionType, totalExpectedSessions,
            presentCount, approvedMedCount
        );
    }

    public List<AttendanceSummary> getBatchAttendanceSummary(int courseId, String sessionType) {
        List<AttendanceSummary> summaries = new ArrayList<>();
        String sql = "SELECT ce.student_id FROM course_enrollment ce " +
                     "JOIN users u ON ce.student_id = u.user_id " +
                     "WHERE ce.course_id = ? ORDER BY u.student_reg_no";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    summaries.add(getStudentAttendanceSummary(studentId, courseId, sessionType));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch batch attendance summary: " + e.getMessage(), e);
        }
        return summaries;
    }
}
