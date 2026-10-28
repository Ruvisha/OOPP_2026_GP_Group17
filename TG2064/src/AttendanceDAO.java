// ==========================================
// 7. DAO: AttendanceDAO.java
// ==========================================
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {
    public boolean saveOrUpdateAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by) " +
                "VALUES ((SELECT user_id FROM users WHERE student_reg_no = ? LIMIT 1), " +
                "(SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status), recorded_by = VALUES(recorded_by), session_date = VALUES(session_date)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, attendance.getStudentRegNo());
            stmt.setString(2, attendance.getCourseCode());
            stmt.setDate(3, attendance.getSessionDate());
            stmt.setString(4, attendance.getSessionType());
            stmt.setInt(5, attendance.getSessionNumber());
            stmt.setString(6, attendance.getStatus());
            stmt.setInt(7, attendance.getRecordedBy());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Attendance> getAllAttendance() throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.attendance_id, u.student_reg_no, u.full_name, c.course_code, a.session_date, a.session_type, a.session_number, a.status " +
                "FROM attendance a JOIN users u ON a.student_id = u.user_id JOIN courses c ON a.course_id = c.course_id " +
                "ORDER BY a.session_date DESC, a.attendance_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Attendance(
                        rs.getInt("attendance_id"), rs.getString("student_reg_no"), rs.getString("full_name"),
                        rs.getString("course_code"), rs.getDate("session_date"), rs.getString("session_type"),
                        rs.getInt("session_number"), rs.getString("status")
                ));
            }
        }
        return list;
    }

    public List<Attendance> getAttendanceByStudent(String studentRegNo) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.attendance_id, u.student_reg_no, u.full_name, c.course_code, a.session_date, a.session_type, a.session_number, a.status " +
                "FROM attendance a JOIN users u ON a.student_id = u.user_id JOIN courses c ON a.course_id = c.course_id " +
                "WHERE u.student_reg_no = ? ORDER BY a.session_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentRegNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getInt("attendance_id"), rs.getString("student_reg_no"), rs.getString("full_name"),
                            rs.getString("course_code"), rs.getDate("session_date"), rs.getString("session_type"),
                            rs.getInt("session_number"), rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }
}
