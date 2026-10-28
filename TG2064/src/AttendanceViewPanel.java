// ==========================================
// 19. UI UNDERGRADUATE: AttendanceViewPanel.java
// ==========================================
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AttendanceViewPanel extends JPanel {
    private final AttendanceService attendanceService = new AttendanceService();
    private DefaultTableModel model;
    private JLabel summaryLabel;

    public AttendanceViewPanel(String studentRegNo) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        summaryLabel = new JLabel("Summary: Loading...");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(summaryLabel, BorderLayout.NORTH);

        String[] columns = { "ID", "Course Code", "Date", "Session Type", "Session #", "Status" };
        model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        loadData(studentRegNo);
    }

    public void loadData(String studentRegNo) {
        model.setRowCount(0);
        try {
            for (Attendance a : attendanceService.getStudentAttendance(studentRegNo)) {
                model.addRow(new Object[]{ a.getAttendanceId(), a.getCourseCode(), a.getSessionDate(), a.getSessionType(), a.getSessionNumber(), a.getStatus() });
            }
            AttendanceSummary summary = attendanceService.getSummaryForCourse(studentRegNo, "ICT2132");
            summaryLabel.setText(String.format("ICT2132 Effective Attendance Percentage: %.1f%% (Present: %d, Approved Medicals: %d, Total: %d)",
                    summary.getEffectivePercentage(), summary.getPresentSessions(), summary.getApprovedMedicalSessions(), summary.getTotalSessions()));
        } catch (Exception ignored) {}
    }
}
