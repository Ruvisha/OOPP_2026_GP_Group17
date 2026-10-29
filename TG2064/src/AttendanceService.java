// ==========================================
// 11. SERVICE: AttendanceService.java
// ==========================================
import java.sql.SQLException;
import java.util.List;

public class AttendanceService {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MedicalDAO medicalDAO = new MedicalDAO();

    public boolean recordAttendance(Attendance attendance) throws SQLException {
        return attendanceDAO.saveOrUpdateAttendance(attendance);
    }

    public List<Attendance> getAllAttendance() throws SQLException {
        return attendanceDAO.getAllAttendance();
    }

    public List<Attendance> getStudentAttendance(String studentRegNo) throws SQLException {
        return attendanceDAO.getAttendanceByStudent(studentRegNo);
    }



    public AttendanceSummary getSummaryForCourse(String studentRegNo, String courseCode) throws SQLException {
        List<Attendance> attendances = attendanceDAO.getAttendanceByStudent(studentRegNo);
        List<Medical> medicals = medicalDAO.getMedicalsByStudent(studentRegNo);

        int total = 0;
        int present = 0;
        for (Attendance a : attendances) {
            if (a.getCourseCode().equalsIgnoreCase(courseCode)) {
                total++;
                if ("PRESENT".equalsIgnoreCase(a.getStatus())) {
                    present++;
                }
            }
        }

        int approvedMedicals = 0;
        for (Medical m : medicals) {
            if (m.getCourseCode().equalsIgnoreCase(courseCode) && "APPROVED".equalsIgnoreCase(m.getStatus())) {
                approvedMedicals++;
            }
        }

        return new AttendanceSummary(courseCode, total, present, approvedMedicals);
    }
}
