// ==========================================
// 3. MODEL: AttendanceSummary.java
// ==========================================
public class AttendanceSummary {
    private String courseCode;
    private int totalSessions;
    private int presentSessions;
    private int approvedMedicalSessions;

    public AttendanceSummary(String courseCode, int totalSessions, int presentSessions, int approvedMedicalSessions) {
        this.courseCode = courseCode;
        this.totalSessions = totalSessions;
        this.presentSessions = presentSessions;
        this.approvedMedicalSessions = approvedMedicalSessions;
    }

    public double getEffectivePercentage() {
        if (totalSessions == 0) return 100.0;
        int effectivePresent = presentSessions + approvedMedicalSessions;
        return ((double) effectivePresent / totalSessions) * 100.0;
    }

    public String getCourseCode() { return courseCode; }
    public int getTotalSessions() { return totalSessions; }
    public int getPresentSessions() { return presentSessions; }
    public int getApprovedMedicalSessions() { return approvedMedicalSessions; }
}
