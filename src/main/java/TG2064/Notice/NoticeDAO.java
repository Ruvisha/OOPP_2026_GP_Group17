package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.DatabaseException;
import com.fot.ams.model.Notice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Notice operations.
 */
public class NoticeDAO {

    private Notice mapResultSetToNotice(ResultSet rs) throws SQLException {
        Notice notice = new Notice(
            rs.getInt("notice_id"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getDate("published_date"),
            (Integer) rs.getObject("created_by"),
            rs.getString("target_role"),
            rs.getString("status")
        );
        notice.setCreatorName(rs.getString("creator_name"));
        notice.setCreatedAt(rs.getTimestamp("created_at"));
        return notice;
    }

    private static final String BASE_SELECT =
        "SELECT n.*, u.full_name AS creator_name " +
        "FROM notices n " +
        "LEFT JOIN users u ON n.created_by = u.user_id ";

    public List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY n.published_date DESC, n.notice_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToNotice(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all notices: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Notice> getActiveNotices() {
        List<Notice> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE n.status = 'ACTIVE' ORDER BY n.published_date DESC, n.notice_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToNotice(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch active notices: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addNotice(Notice notice) {
        String sql = "INSERT INTO notices (title, content, published_date, created_by, target_role, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, notice.getTitle());
            stmt.setString(2, notice.getContent());
            stmt.setDate(3, notice.getPublishedDate());
            if (notice.getCreatedBy() != null) {
                stmt.setInt(4, notice.getCreatedBy());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, notice.getTargetRole() != null ? notice.getTargetRole() : "ALL");
            stmt.setString(6, notice.getStatus() != null ? notice.getStatus() : "ACTIVE");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        notice.setNoticeId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add notice: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateNotice(Notice notice) {
        String sql = "UPDATE notices SET title = ?, content = ?, published_date = ?, " +
                     "target_role = ?, status = ? WHERE notice_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notice.getTitle());
            stmt.setString(2, notice.getContent());
            stmt.setDate(3, notice.getPublishedDate());
            stmt.setString(4, notice.getTargetRole());
            stmt.setString(5, notice.getStatus());
            stmt.setInt(6, notice.getNoticeId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update notice: " + e.getMessage(), e);
        }
    }

    public boolean deleteNotice(int noticeId) {
        String sql = "DELETE FROM notices WHERE notice_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noticeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete notice: " + e.getMessage(), e);
        }
    }
}
