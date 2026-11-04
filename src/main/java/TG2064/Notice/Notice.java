package com.fot.ams.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents an announcement or official notice.
 */
public class Notice {
    private int noticeId;
    private String title;
    private String content;
    private Date publishedDate;
    private Integer createdBy;
    private String creatorName;
    private String targetRole = "ALL";
    private String status = "ACTIVE"; // ACTIVE, ARCHIVED
    private Timestamp createdAt;

    public Notice() {
    }

    public Notice(int noticeId, String title, String content, Date publishedDate,
                  Integer createdBy, String targetRole, String status) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.publishedDate = publishedDate;
        this.createdBy = createdBy;
        this.targetRole = targetRole;
        this.status = status;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title;
    }
}
