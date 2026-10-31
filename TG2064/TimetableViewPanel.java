package com.fot.ams.ui.undergraduate;

import com.fot.ams.model.Timetable;
import com.fot.ams.model.User;
import com.fot.ams.service.TimetableService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel enabling Undergraduates to inspect their weekly academic lecture & practical schedule.
 */
public class TimetableViewPanel extends JPanel {
    private final TimetableService timetableService;
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    public TimetableViewPanel() {
        this.timetableService = new TimetableService();
        initComponents();
        loadTimetable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Department Academic Timetable (Semester I)");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Timetable");
        btnRefresh.addActionListener(e -> loadTimetable());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {
            "Day of Week", "Time Slot", "Course Code", "Course Title",
            "Session Type", "Assigned Lecture Room / Lab", "Lecturer in Charge"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        ModernTheme.styleTable(timetableTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(timetableTable), BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    public void loadTimetable() {
        tableModel.setRowCount(0);
        User cur = SessionManager.getCurrentUser();
        int deptId = (cur != null && cur.getDepartmentId() != null) ? cur.getDepartmentId() : 1;

        List<Timetable> list = timetableService.getTimetablesByDepartment(deptId);
        for (Timetable t : list) {
            String timeSlot = DateUtil.formatTime(t.getStartTime()) + " - " + DateUtil.formatTime(t.getEndTime());
            tableModel.addRow(new Object[]{
                t.getDayOfWeek(),
                timeSlot,
                t.getCourseCode(),
                t.getCourseName(),
                t.getSessionType(),
                t.getRoom(),
                t.getLecturerName() != null ? t.getLecturerName() : "TBA"
            });
        }
    }
}
