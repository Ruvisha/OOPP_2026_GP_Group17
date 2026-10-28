// ==========================================
// 15. UI TECHNICAL: AttendancePanel.java
// ==========================================
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;

public class AttendancePanel extends JPanel {
    private final AttendanceService attendanceService = new AttendanceService();
    private DefaultTableModel model;
    private JComboBox<String> courseBox;

    public AttendancePanel(int currentUserId) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Reg No", "Student Name", "Course", "Date", "Session Type", "Session #", "Status" };
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(2, 6, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Record Student Attendance (Technical Officer)"));

        JTextField regField = new JTextField("TG/2024/2001");
        courseBox = new JComboBox<>(new String[] { "ICT2132", "ICT2113", "ICT2122", "ICT2143" });
        JTextField dateField = new JTextField("2026-10-18");
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL" });
        JTextField sessionNumField = new JTextField("16");
        JComboBox<String> statusBox = new JComboBox<>(new String[] { "PRESENT", "ABSENT" });
        JButton addBtn = new JButton("Save Record");

        form.add(new JLabel("Reg No:")); form.add(regField);
        form.add(new JLabel("Course Code:")); form.add(courseBox);
        form.add(new JLabel("Date:")); form.add(dateField);
        form.add(new JLabel("Type / Session #:"));
        JPanel subGroup = new JPanel(new GridLayout(1, 2));
        subGroup.add(typeBox); subGroup.add(sessionNumField);
        form.add(subGroup);
        form.add(new JLabel("Status:")); form.add(statusBox);
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                Attendance att = new Attendance();
                att.setStudentRegNo(regField.getText().trim());
                att.setCourseCode((String) courseBox.getSelectedItem());
                att.setSessionDate(Date.valueOf(dateField.getText().trim()));
                att.setSessionType((String) typeBox.getSelectedItem());
                att.setSessionNumber(Integer.parseInt(sessionNumField.getText().trim()));
                att.setStatus((String) statusBox.getSelectedItem());
                att.setRecordedBy(currentUserId);

                attendanceService.recordAttendance(att);
                loadData();
                JOptionPane.showMessageDialog(this, "Attendance recorded successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(form, BorderLayout.SOUTH);
        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            for (Attendance a : attendanceService.getAllAttendance()) {
                model.addRow(new Object[]{ a.getAttendanceId(), a.getStudentRegNo(), a.getStudentName(), a.getCourseCode(), a.getSessionDate(), a.getSessionType(), a.getSessionNumber(), a.getStatus() });
            }
        } catch (Exception ignored) {}
    }
}
