// ==========================================
// 8. DAO: MedicalDAO.java
// ==========================================
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalDAO {
    public boolean addMedical(Medical medical) throws SQLException {
        String sql = "INSERT INTO medical (student_id, course_id, session_type, session_date, submission_date, reason, status) " +
                "VALUES ((SELECT user_id FROM users WHERE student_reg_no = ? LIMIT 1), " +
                "(SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, ?, ?, 'NOT_APPROVED')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, medical.getStudentRegNo());
            stmt.setString(2, medical.getCourseCode());
            stmt.setString(3, medical.getSessionType());
            stmt.setDate(4, medical.getSessionDate());
            stmt.setDate(5, medical.getSubmissionDate());
            stmt.setString(6, medical.getReason());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateMedicalStatus(int medicalId, String status, int reviewerId) throws SQLException {
        String sql = "UPDATE medical SET status = ?, reviewed_by = ?, reviewed_at = NOW() WHERE medical_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, reviewerId);
            stmt.setInt(3, medicalId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Medical> getAllMedicals() throws SQLException {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT m.medical_id, u.student_reg_no, u.full_name, c.course_code, m.session_type, m.session_date, m.submission_date, m.reason, m.status " +
                "FROM medical m JOIN users u ON m.student_id = u.user_id JOIN courses c ON m.course_id = c.course_id " +
                "ORDER BY m.submission_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Medical(
                        rs.getInt("medical_id"), rs.getString("student_reg_no"), rs.getString("full_name"),
                        rs.getString("course_code"), rs.getString("session_type"), rs.getDate("session_date"),
                        rs.getDate("submission_date"), rs.getString("reason"), rs.getString("status")
                ));
            }
        }
        return list;
    }

    public List<Medical> getMedicalsByStudent(String studentRegNo) throws SQLException {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT m.medical_id, u.student_reg_no, u.full_name, c.course_code, m.session_type, m.session_date, m.submission_date, m.reason, m.status " +
                "FROM medical m JOIN users u ON m.student_id = u.user_id JOIN courses c ON m.course_id = c.course_id " +
                "WHERE u.student_reg_no = ? ORDER BY m.submission_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentRegNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Medical(
                            rs.getInt("medical_id"), rs.getString("student_reg_no"), rs.getString("full_name"),
                            rs.getString("course_code"), rs.getString("session_type"), rs.getDate("session_date"),
                            rs.getDate("submission_date"), rs.getString("reason"), rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }
}
