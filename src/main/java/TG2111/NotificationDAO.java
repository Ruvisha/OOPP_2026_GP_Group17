package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User Notifications.
 * Ensures user-specific read/unread tracking persisted across sessions.
 */
public class NotificationDAO {

    public static class NotificationItem {
        public final int noticeId;
        public final String title;
        public final String content;
        public final Date publishedDate;
        public final String targetRole;
        public final boolean isRead;
        public final Timestamp readAt;

        public NotificationItem(int noticeId, String title, String content, Date publishedDate, String targetRole, boolean isRead, Timestamp readAt) {
            this.noticeId = noticeId;
            this.title = title;
            this.content = content;
            this.publishedDate = publishedDate;
            this.targetRole = targetRole;
            this.isRead = isRead;
            this.readAt = readAt;
        }
    }

    public NotificationDAO() {
        ensureNotificationTable();
    }

    private void ensureNotificationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `user_notifications` (" +
                     "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                     "  `user_id` INT NOT NULL," +
                     "  `notice_id` INT NOT NULL," +
                     "  `is_read` TINYINT(1) NOT NULL DEFAULT 0," +
                     "  `read_at` TIMESTAMP NULL DEFAULT NULL," +
                     "  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                     "  UNIQUE KEY `uq_user_notice` (`user_id`, `notice_id`)," +
                     "  KEY `idx_un_user` (`user_id`)," +
                     "  KEY `idx_un_notice` (`notice_id`)" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception ignored) {
        }
    }

    private boolean isRoleMatch(String targetRole, String userRole) {
        if (targetRole == null || targetRole.trim().isEmpty() || "ALL".equalsIgnoreCase(targetRole)) {
            return true;
        }
        if (userRole == null) return false;
        String t = targetRole.trim().toUpperCase();
        String u = userRole.trim().toUpperCase();

        if (t.contains(u) || u.contains(t)) return true;
        if (u.contains("ADMIN") && t.contains("ADMIN")) return true;
        if (u.contains("LECTURER") && t.contains("LECTURER")) return true;
        if (u.contains("TECH") && t.contains("TECH")) return true;
        if (u.contains("UNDERGRADUATE") && t.contains("UNDERGRADUATE")) return true;
        if (u.contains("STUDENT") && (t.contains("STUDENT") || t.contains("UNDERGRADUATE"))) return true;

        return false;
    }

    public void syncUserNotifications(int userId, String userRole) {
        if (userId <= 0) return;
        String fetchNoticesSql = "SELECT notice_id, target_role FROM notices WHERE status = 'ACTIVE'";
        String insertSql = "INSERT IGNORE INTO user_notifications (user_id, notice_id, is_read) VALUES (?, ?, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement fetchStmt = conn.prepareStatement(fetchNoticesSql);
             ResultSet rs = fetchStmt.executeQuery();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            while (rs.next()) {
                int noticeId = rs.getInt("notice_id");
                String targetRole = rs.getString("target_role");
                if (isRoleMatch(targetRole, userRole)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, noticeId);
                    insertStmt.addBatch();
                }
            }
            insertStmt.executeBatch();
        } catch (Exception e) {
            System.err.println("[NotificationDAO] Sync error: " + e.getMessage());
        }
    }

    public void distributeNoticeNotifications(int noticeId, String targetRole) {
        if (noticeId <= 0) return;
        String usersSql = "SELECT u.user_id, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.status = 'ACTIVE'";
        String insertSql = "INSERT IGNORE INTO user_notifications (user_id, notice_id, is_read) VALUES (?, ?, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement userStmt = conn.prepareStatement(usersSql);
             ResultSet rs = userStmt.executeQuery();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String roleName = rs.getString("role_name");
                if (isRoleMatch(targetRole, roleName)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, noticeId);
                    insertStmt.addBatch();
                }
            }
            insertStmt.executeBatch();
        } catch (Exception e) {
            System.err.println("[NotificationDAO] Distribute notice notifications error: " + e.getMessage());
        }
    }

    public int getUnreadCount(int userId, String userRole) {
        syncUserNotifications(userId, userRole);
        String sql = "SELECT COUNT(*) FROM user_notifications un " +
                     "JOIN notices n ON un.notice_id = n.notice_id " +
                     "WHERE un.user_id = ? AND un.is_read = 0 AND n.status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            // Fallback: count active notices matching role
            return countActiveNoticesMatchingRole(userRole);
        }
        return 0;
    }

    private int countActiveNoticesMatchingRole(String userRole) {
        String sql = "SELECT target_role FROM notices WHERE status = 'ACTIVE'";
        int count = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (isRoleMatch(rs.getString("target_role"), userRole)) {
                    count++;
                }
            }
        } catch (Exception ignored) {
        }
        return count;
    }

    public List<NotificationItem> getUserNotifications(int userId, String userRole) {
        syncUserNotifications(userId, userRole);
        List<NotificationItem> list = new ArrayList<>();
        String sql = "SELECT n.notice_id, n.title, n.content, n.published_date, n.target_role, " +
                     "COALESCE(un.is_read, 0) AS is_read, un.read_at " +
                     "FROM notices n " +
                     "LEFT JOIN user_notifications un ON n.notice_id = un.notice_id AND un.user_id = ? " +
                     "WHERE n.status = 'ACTIVE' " +
                     "ORDER BY is_read ASC, n.published_date DESC, n.notice_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String target = rs.getString("target_role");
                    if (isRoleMatch(target, userRole)) {
                        list.add(new NotificationItem(
                            rs.getInt("notice_id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getDate("published_date"),
                            target,
                            rs.getInt("is_read") == 1,
                            rs.getTimestamp("read_at")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[NotificationDAO] Fetch error: " + e.getMessage());
        }
        return list;
    }

    public boolean markAsRead(int userId, int noticeId) {
        String sql = "INSERT INTO user_notifications (user_id, notice_id, is_read, read_at) " +
                     "VALUES (?, ?, 1, NOW()) " +
                     "ON DUPLICATE KEY UPDATE is_read = 1, read_at = NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, noticeId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean markAllAsRead(int userId, String userRole) {
        syncUserNotifications(userId, userRole);
        String sql = "UPDATE user_notifications SET is_read = 1, read_at = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
