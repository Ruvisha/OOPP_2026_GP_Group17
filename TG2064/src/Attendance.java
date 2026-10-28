// ==========================================
// 2. MODEL: Attendance.java
// ==========================================
import java.sql.Date;

public class Attendance {
    private int attendanceId;
    private int studentId;
    private String studentRegNo;
    private String studentName;
    private int courseId;
    private String courseCode;
    private Date sessionDate;
    private String sessionType;
    private int sessionNumber;
    private String status;
    private int recordedBy;

    public Attendance() {}

    public Attendance(int attendanceId, String studentRegNo, String studentName, String courseCode, Date sessionDate, String sessionType, int sessionNumber, String status) {
        this.attendanceId = attendanceId;
        this.studentRegNo = studentRegNo;
        this.studentName = studentName;
        this.courseCode = courseCode;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.sessionNumber = sessionNumber;
        this.status = status;
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }
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
    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getRecordedBy() { return recordedBy; }
    public void setRecordedBy(int recordedBy) { this.recordedBy = recordedBy; }
}
