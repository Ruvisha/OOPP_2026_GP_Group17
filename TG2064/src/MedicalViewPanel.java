// ==========================================
// 20. UI UNDERGRADUATE: MedicalViewPanel.java
// ==========================================
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MedicalViewPanel extends JPanel {
    private final MedicalService medicalService = new MedicalService();
    private DefaultTableModel model;

    public MedicalViewPanel(String studentRegNo) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Course", "Session Date", "Submission Date", "Reason", "Status" };
        model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        loadData(studentRegNo);
    }

    public void loadData(String studentRegNo) {
        model.setRowCount(0);
        try {
            for (Medical m : medicalService.getStudentMedicals(studentRegNo)) {
                model.addRow(new Object[]{ m.getMedicalId(), m.getCourseCode(), m.getSessionDate(), m.getSubmissionDate(), m.getReason(), m.getStatus() });
            }
        } catch (Exception ignored) {}
    }
}
