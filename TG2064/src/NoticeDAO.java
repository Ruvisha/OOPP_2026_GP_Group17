// ==========================================
// 9. DAO: NoticeDAO.java
// ==========================================
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {
    public boolean addNotice(Notice notice, int authorId) throws SQLException {
        String sql = "INSERT INTO notices (title, content, published_date, created_by, target_role, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notice.getTitle());
            stmt.setString(2, notice.getContent());
            stmt.setDate(3, notice.getPublishedDate());
            stmt.setInt(4, authorId);
            stmt.setString(5, notice.getTargetRole());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Notice> getNoticesByAudience(String role) throws SQLException {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.notice_id, n.title, n.content, n.published_date, n.target_role, COALESCE(u.full_name, 'Administration') AS posted_by, n.status " +
                "FROM notices n LEFT JOIN users u ON n.created_by = u.user_id " +
                "WHERE n.status = 'ACTIVE' AND (n.target_role = 'ALL' OR n.target_role = ?) " +
                "ORDER BY n.published_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notice(
                            rs.getInt("notice_id"), rs.getString("title"), rs.getString("content"),
                            rs.getDate("published_date"), rs.getString("target_role"), rs.getString("posted_by"), rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }
}