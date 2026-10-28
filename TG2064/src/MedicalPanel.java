// ==========================================
// 16. UI TECHNICAL: MedicalPanel.java
// ==========================================
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;

public class MedicalPanel extends JPanel {
    private final MedicalService medicalService = new MedicalService();
    private DefaultTableModel model;
    private JTable table;

    public MedicalPanel(int currentUserId) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Reg No", "Student Name", "Course", "Session Type", "Session Date", "Submission Date", "Reason", "Status" };
        model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        topActions.add(approveBtn);
        topActions.add(rejectBtn);

        approveBtn.addActionListener(e -> updateStatus(currentUserId, true));
        rejectBtn.addActionListener(e -> updateStatus(currentUserId, false));

        add(topActions, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 5, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Log Student Medical Submission"));

        JTextField regField = new JTextField("TG/2024/2005");
        JComboBox<String> courseBox = new JComboBox<>(new String[] { "ICT2132", "ICT2113", "ICT2122", "ICT2143" });
        JTextField sDateField = new JTextField("2026-10-10");
        JTextField subDateField = new JTextField("2026-10-12");
        JTextField reasonField = new JTextField("Fever");
        JButton addBtn = new JButton("Log Medical");

        form.add(new JLabel("Reg No:")); form.add(regField);
        form.add(new JLabel("Course:")); form.add(courseBox);
        form.add(new JLabel("Session Date:")); form.add(sDateField);
        form.add(new JLabel("Submission Date:")); form.add(subDateField);
        form.add(new JLabel("Reason:")); form.add(reasonField);
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                Medical m = new Medical();
                m.setStudentRegNo(regField.getText().trim());
                m.setCourseCode((String) courseBox.getSelectedItem());
                m.setSessionType("THEORY");
                m.setSessionDate(Date.valueOf(sDateField.getText().trim()));
                m.setSubmissionDate(Date.valueOf(subDateField.getText().trim()));
                m.setReason(reasonField.getText().trim());

                medicalService.submitMedical(m);
                loadData();
                JOptionPane.showMessageDialog(this, "Medical logged!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error logging medical: " + ex.getMessage());
            }
        });

        add(form, BorderLayout.SOUTH);
        loadData();
    }

    private void updateStatus(int reviewerId, boolean approve) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a medical record first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try {
            if (approve) medicalService.approveMedical(id, reviewerId);
            else medicalService.rejectMedical(id, reviewerId);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            for (Medical m : medicalService.getAllMedicals()) {
                model.addRow(new Object[]{ m.getMedicalId(), m.getStudentRegNo(), m.getStudentName(), m.getCourseCode(), m.getSessionType(), m.getSessionDate(), m.getSubmissionDate(), m.getReason(), m.getStatus() });
            }
        } catch (Exception ignored) {}
    }
}
