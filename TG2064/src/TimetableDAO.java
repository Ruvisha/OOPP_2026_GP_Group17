// ==========================================
// 10. DAO: TimetableDAO.java
// ==========================================
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {
    public boolean checkConflict(String dayOfWeek, String room, Time startTime, Time endTime) throws SQLException {
        String sql = "SELECT COUNT(*) FROM timetables WHERE day_of_week = ? AND room = ? AND " +
                "((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?) OR (start_time >= ? AND end_time <= ?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dayOfWeek);
            stmt.setString(2, room);
            stmt.setTime(3, endTime); stmt.setTime(4, startTime);
            stmt.setTime(5, endTime); stmt.setTime(6, endTime);
            stmt.setTime(7, startTime); stmt.setTime(8, endTime);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean addTimetableSlot(Timetable t) throws SQLException {
        String sql = "INSERT INTO timetables (department_id, course_id, day_of_week, start_time, end_time, room, session_type, semester, academic_year) " +
                "VALUES (1, (SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, ?, ?, ?, 1, '2024/2025')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getCourseCode());
            stmt.setString(2, t.getDayOfWeek());
            stmt.setTime(3, t.getStartTime());
            stmt.setTime(4, t.getEndTime());
            stmt.setString(5, t.getRoom());
            stmt.setString(6, t.getSessionType());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Timetable> getAllTimetables() throws SQLException {
        List<Timetable> list = new ArrayList<>();
        String sql = "SELECT t.timetable_id, t.day_of_week, CONCAT(TIME_FORMAT(t.start_time, '%h:%i %p'), ' - ', TIME_FORMAT(t.end_time, '%h:%i %p')) AS time_slot, " +
                "c.course_code, c.course_name, t.session_type, t.room, COALESCE(u.full_name, 'Unassigned') AS lecturer " +
                "FROM timetables t JOIN courses c ON t.course_id = c.course_id LEFT JOIN users u ON t.lecturer_id = u.user_id " +
                "ORDER BY FIELD(t.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'), t.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Timetable(
                        rs.getInt("timetable_id"), rs.getString("day_of_week"), rs.getString("time_slot"),
                        rs.getString("course_code"), rs.getString("course_name"), rs.getString("session_type"),
                        rs.getString("room"), rs.getString("lecturer")
                ));
            }
        }
        return list;
    }
}
