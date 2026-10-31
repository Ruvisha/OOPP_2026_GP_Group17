package com.fot.ams.ui.technical;

import com.fot.ams.dao.DepartmentDAO;
import com.fot.ams.model.Department;
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
 * Panel enabling Technical Officers to view department timetables and laboratory schedules.
 */
public class TechnicalTimetablePanel extends JPanel {
    private final TimetableService timetableService;
    private final DepartmentDAO departmentDAO;

    private JComboBox<DeptItem> cmbDept;
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    private static class DeptItem {
        final int id;
        final String name;

        DeptItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public TechnicalTimetablePanel() {
        this.timetableService = new TimetableService();
        this.departmentDAO = new DepartmentDAO();
        initComponents();
        loadDepartments();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lbl = new JLabel("Department Timetable:");
        lbl.setFont(ModernTheme.FONT_BOLD);
        cmbDept = new JComboBox<>();
        cmbDept.addActionListener(e -> loadTimetable());

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Timetable");
        btnRefresh.addActionListener(e -> loadTimetable());

        topBar.add(lbl);
        topBar.add(cmbDept);
        topBar.add(btnRefresh);

        String[] cols = {"Day", "Time Slot", "Course Code", "Course Title", "Type", "Assigned Hall / Lab", "Lecturer in Charge"};
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

        add(topBar, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void loadDepartments() {
        cmbDept.removeAllItems();
        List<Department> list = departmentDAO.getAllDepartments();
        for (Department d : list) {
            cmbDept.addItem(new DeptItem(d.getDepartmentId(), d.getDepartmentCode() + " - " + d.getDepartmentName()));
        }

        User cur = SessionManager.getCurrentUser();
        if (cur != null && cur.getDepartmentId() != null) {
            for (int i = 0; i < cmbDept.getItemCount(); i++) {
                if (cmbDept.getItemAt(i).id == cur.getDepartmentId()) {
                    cmbDept.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public void loadTimetable() {
        tableModel.setRowCount(0);
        DeptItem dept = (DeptItem) cmbDept.getSelectedItem();
        if (dept == null) return;

        List<Timetable> list = timetableService.getTimetablesByDepartment(dept.id);
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
