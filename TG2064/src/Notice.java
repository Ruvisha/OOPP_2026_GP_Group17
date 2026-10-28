// ==========================================
// 5. MODEL: Notice.java
// ==========================================
import java.sql.Date;

public class Notice {
    private int noticeId;
    private String title;
    private String content;
    private Date publishedDate;
    private String targetRole;
    private String postedBy;
    private String status;

    public Notice() {}

    public Notice(int noticeId, String title, String content, Date publishedDate, String targetRole, String postedBy, String status) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.publishedDate = publishedDate;
        this.targetRole = targetRole;
        this.postedBy = postedBy;
        this.status = status;
    }

    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int noticeId) { this.noticeId = noticeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Date getPublishedDate() { return publishedDate; }
    public void setPublishedDate(Date publishedDate) { this.publishedDate = publishedDate; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
