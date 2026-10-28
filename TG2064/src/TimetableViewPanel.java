// ==========================================
// 22. UI UNDERGRADUATE: TimetableViewPanel.java
// ==========================================
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TimetableViewPanel extends JPanel {
    private final TimetableService timetableService = new TimetableService();
    private DefaultTableModel model;

    public TimetableViewPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "Day", "Time Slot", "Course Code", "Course Name", "Type", "Room / Hall", "Lecturer" };
        model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            for (Timetable t : timetableService.getTimetable()) {
                model.addRow(new Object[]{ t.getDayOfWeek(), t.getTimeSlot(), t.getCourseCode(), t.getCourseName(), t.getSessionType(), t.getRoom(), t.getLecturer() });
            }
        } catch (Exception ignored) {}
    }
}
