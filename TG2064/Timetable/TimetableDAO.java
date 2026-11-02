package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Timetable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Timetable operations.
 */
public class TimetableDAO {

    private Timetable mapResultSetToTimetable(ResultSet rs) throws SQLException {
        Timetable tt = new Timetable(
            rs.getInt("timetable_id"),
            rs.getInt("department_id"),
            rs.getInt("course_id"),
            rs.getString("day_of_week"),
            rs.getTime("start_time"),
            rs.getTime("end_time"),
            rs.getString("room"),
            (Integer) rs.getObject("lecturer_id"),
            rs.getString("session_type"),
            rs.getInt("semester"),
            rs.getString("academic_year")
        );
        tt.setDepartmentCode(rs.getString("department_code"));
        tt.setDepartmentName(rs.getString("department_name"));
        tt.setCourseCode(rs.getString("course_code"));
        tt.setCourseName(rs.getString("course_name"));
        tt.setLecturerName(rs.getString("lecturer_name"));
        return tt;
    }

    private static final String BASE_SELECT =
        "SELECT tt.*, d.department_code, d.department_name, " +
        "c.course_code, c.course_name, lec.full_name AS lecturer_name " +
        "FROM timetables tt " +
        "JOIN departments d ON tt.department_id = d.department_id " +
        "JOIN courses c ON tt.course_id = c.course_id " +
        "LEFT JOIN users lec ON tt.lecturer_id = lec.user_id ";

    public List<Timetable> getAllTimetables() {
        List<Timetable> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY FIELD(tt.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'), tt.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTimetable(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all timetables: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Timetable> getTimetablesByDepartment(int departmentId) {
        List<Timetable> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE tt.department_id = ? " +
                     "ORDER BY FIELD(tt.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'), tt.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTimetable(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch timetables by department: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addTimetable(Timetable tt) {
        String sql = "INSERT INTO timetables (department_id, course_id, day_of_week, start_time, " +
                     "end_time, room, lecturer_id, session_type, semester, academic_year) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, tt.getDepartmentId());
            stmt.setInt(2, tt.getCourseId());
            stmt.setString(3, tt.getDayOfWeek());
            stmt.setTime(4, tt.getStartTime());
            stmt.setTime(5, tt.getEndTime());
            stmt.setString(6, tt.getRoom());
            if (tt.getLecturerId() != null) {
                stmt.setInt(7, tt.getLecturerId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setString(8, tt.getSessionType());
            stmt.setInt(9, tt.getSemester());
            stmt.setString(10, tt.getAcademicYear());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        tt.setTimetableId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add timetable: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateTimetable(Timetable tt) {
        String sql = "UPDATE timetables SET department_id = ?, course_id = ?, day_of_week = ?, " +
                     "start_time = ?, end_time = ?, room = ?, lecturer_id = ?, session_type = ?, " +
                     "semester = ?, academic_year = ? WHERE timetable_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tt.getDepartmentId());
            stmt.setInt(2, tt.getCourseId());
            stmt.setString(3, tt.getDayOfWeek());
            stmt.setTime(4, tt.getStartTime());
            stmt.setTime(5, tt.getEndTime());
            stmt.setString(6, tt.getRoom());
            if (tt.getLecturerId() != null) {
                stmt.setInt(7, tt.getLecturerId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setString(8, tt.getSessionType());
            stmt.setInt(9, tt.getSemester());
            stmt.setString(10, tt.getAcademicYear());
            stmt.setInt(11, tt.getTimetableId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update timetable: " + e.getMessage(), e);
        }
    }

    public boolean deleteTimetable(int timetableId) {
        String sql = "DELETE FROM timetables WHERE timetable_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, timetableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete timetable: " + e.getMessage(), e);
        }
    }
}
