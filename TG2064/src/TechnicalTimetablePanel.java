// ==========================================
// 18. UI TECHNICAL: TechnicalTimetablePanel.java
// ==========================================
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;

public class TechnicalTimetablePanel extends JPanel {
    private final TimetableService timetableService = new TimetableService();
    private DefaultTableModel model;

    public TechnicalTimetablePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Day", "Time Slot", "Course Code", "Course Name", "Type", "Room", "Lecturer" };
        model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(2, 6, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Manage Timetable Slot"));

        JComboBox<String> dayBox = new JComboBox<>(new String[] { "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" });
        JComboBox<String> courseBox = new JComboBox<>(new String[] { "ICT2132", "ICT2113", "ICT2122", "ICT2143" });
        JTextField startField = new JTextField("08:00:00");
        JTextField endField = new JTextField("10:00:00");
        JTextField roomField = new JTextField("Computer Lab 01");
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL" });
        JButton addBtn = new JButton("Add Slot");

        form.add(new JLabel("Day:")); form.add(dayBox);
        form.add(new JLabel("Course Code:")); form.add(courseBox);
        form.add(new JLabel("Start Time:")); form.add(startField);
        form.add(new JLabel("End Time:")); form.add(endField);
        form.add(new JLabel("Room:")); form.add(roomField);
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                Timetable t = new Timetable();
                t.setDayOfWeek((String) dayBox.getSelectedItem());
                t.setCourseCode((String) courseBox.getSelectedItem());
                t.setStartTime(Time.valueOf(startField.getText().trim()));
                t.setEndTime(Time.valueOf(endField.getText().trim()));
                t.setRoom(roomField.getText().trim());
                t.setSessionType((String) typeBox.getSelectedItem());

                timetableService.addSlot(t);
                loadData();
                JOptionPane.showMessageDialog(this, "Timetable slot added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Conflict / Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(form, BorderLayout.SOUTH);
        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            for (Timetable t : timetableService.getTimetable()) {
                model.addRow(new Object[]{ t.getTimetableId(), t.getDayOfWeek(), t.getTimeSlot(), t.getCourseCode(), t.getCourseName(), t.getSessionType(), t.getRoom(), t.getLecturer() });
            }
        } catch (Exception ignored) {}
    }
}
