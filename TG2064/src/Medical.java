// ==========================================
// 4. MODEL: Medical.java
// ==========================================
import java.sql.Date;

public class Medical {
    private int medicalId;
    private int studentId;
    private String studentRegNo;
    private String studentName;
    private int courseId;
    private String courseCode;
    private String sessionType;
    private Date sessionDate;
    private Date submissionDate;
    private String reason;
    private String status;
    private Integer reviewedBy;

    public Medical() {}

    public Medical(int medicalId, String studentRegNo, String studentName, String courseCode, String sessionType, Date sessionDate, Date submissionDate, String reason, String status) {
        this.medicalId = medicalId;
        this.studentRegNo = studentRegNo;
        this.studentName = studentName;
        this.courseCode = courseCode;
        this.sessionType = sessionType;
        this.sessionDate = sessionDate;
        this.submissionDate = submissionDate;
        this.reason = reason;
        this.status = status;
    }

    public int getMedicalId() { return medicalId; }
    public void setMedicalId(int medicalId) { this.medicalId = medicalId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }
    public Date getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(Date submissionDate) { this.submissionDate = submissionDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }
}
