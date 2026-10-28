// ==========================================
// 13. SERVICE: NoticeService.java
// ==========================================
import java.sql.SQLException;
import java.util.List;

public class NoticeService {
    private final NoticeDAO noticeDAO = new NoticeDAO();

    public boolean createNotice(Notice notice, int authorId) throws SQLException {
        return noticeDAO.addNotice(notice, authorId);
    }

    public List<Notice> getNoticesForUserRole(String role) throws SQLException {
        return noticeDAO.getNoticesByAudience(role);
    }
}
