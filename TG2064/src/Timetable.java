// ==========================================
// 6. MODEL: Timetable.java
// ==========================================
import java.sql.Time;

public class Timetable {
    private int timetableId;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;
    private String timeSlot;
    private String courseCode;
    private String courseName;
    private String sessionType;
    private String room;
    private String lecturer;

    public Timetable() {}

    public Timetable(int timetableId, String dayOfWeek, String timeSlot, String courseCode, String courseName, String sessionType, String room, String lecturer) {
        this.timetableId = timetableId;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.sessionType = sessionType;
        this.room = room;
        this.lecturer = lecturer;
    }

    public int getTimetableId() { return timetableId; }
    public void setTimetableId(int timetableId) { this.timetableId = timetableId; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }
    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getLecturer() { return lecturer; }
    public void setLecturer(String lecturer) { this.lecturer = lecturer; }
}
