package com.fot.ams.ui.admin;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.DepartmentDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Course;
import com.fot.ams.model.Department;
import com.fot.ams.model.Timetable;
import com.fot.ams.model.User;
import com.fot.ams.service.TimetableService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;
import java.util.List;

/**
 * Administrative panel for managing faculty lecture and practical timetables.
 */
public class TimetableManagementPanel extends JPanel {
    private final TimetableService timetableService;
    private final DepartmentDAO departmentDAO;
    private final CourseDAO courseDAO;
    private final UserDAO userDAO;

    private JTable timetableTable;
    private DefaultTableModel tableModel;
    private JComboBox<DeptFilterItem> deptFilterCombo;

    // Form inputs
    private JTextField txtTimetableId;
    private JComboBox<DeptFilterItem> cmbDepartment;
    private JComboBox<CourseComboItem> cmbCourse;
    private JComboBox<String> cmbDay;
    private JTextField txtStartTime;
    private JTextField txtEndTime;
    private JTextField txtRoom;
    private JComboBox<LecturerComboItem> cmbLecturer;
    private JComboBox<String> cmbSessionType;
    private JSpinner spinSemester;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    private static class DeptFilterItem {
        final int id;
        final String name;

        DeptFilterItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class CourseComboItem {
        final int id;
        final String code;
        final String name;

        CourseComboItem(int id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return code + " - " + name;
        }
    }

    private static class LecturerComboItem {
        final int id;
        final String name;

        LecturerComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public TimetableManagementPanel() {
        this.timetableService = new TimetableService();
        this.departmentDAO = new DepartmentDAO();
        this.courseDAO = new CourseDAO();
        this.userDAO = new UserDAO();
        initComponents();
        loadDropdowns();
        loadTimetables(0);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel filterLabel = new JLabel("Filter Department:");
        filterLabel.setFont(ModernTheme.FONT_BOLD);
        deptFilterCombo = new JComboBox<>();
        deptFilterCombo.setFont(ModernTheme.FONT_REGULAR);
        deptFilterCombo.addActionListener(e -> {
            DeptFilterItem sel = (DeptFilterItem) deptFilterCombo.getSelectedItem();
            if (sel != null) {
                loadTimetables(sel.id);
            }
        });

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Timetable");
        btnRefresh.addActionListener(e -> {
            DeptFilterItem sel = (DeptFilterItem) deptFilterCombo.getSelectedItem();
            loadTimetables(sel != null ? sel.id : 0);
        });

        topBar.add(filterLabel);
        topBar.add(deptFilterCombo);
        topBar.add(btnRefresh);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(650);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"ID", "Day", "Time Slot", "Course", "Type", "Room", "Lecturer", "Dept"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        ModernTheme.styleTable(timetableTable);
        timetableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && timetableTable.getSelectedRow() != -1) {
                populateForm();
            }
        });

        tablePanel.add(new JScrollPane(timetableTable), BorderLayout.CENTER);

        // Form Panel
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BorderLayout());
        formCard.setPreferredSize(new Dimension(360, 500));

        JLabel formHeader = new JLabel("Schedule Slot Configuration");
        formHeader.setFont(ModernTheme.FONT_SUBTITLE);
        formHeader.setForeground(ModernTheme.PRIMARY);
        formHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Color.WHITE);

        txtTimetableId = new JTextField();
        txtTimetableId.setVisible(false);

        cmbDepartment = new JComboBox<>();
        cmbCourse = new JComboBox<>();
        cmbDay = new JComboBox<>(new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"});
        txtStartTime = new JTextField("08:00:00");
        txtEndTime = new JTextField("10:00:00");
        txtRoom = new JTextField("Lecture Theatre 01");
        cmbLecturer = new JComboBox<>();
        cmbSessionType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL"});
        spinSemester = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));

        fields.add(createRow("Department:", cmbDepartment));
        fields.add(createRow("Course Module:", cmbCourse));
        fields.add(createRow("Day of Week:", cmbDay));
        fields.add(createRow("Start Time (HH:MM:SS):", txtStartTime));
        fields.add(createRow("End Time (HH:MM:SS):", txtEndTime));
        fields.add(createRow("Room / Hall / Lab:", txtRoom));
        fields.add(createRow("Lecturer in Charge:", cmbLecturer));
        fields.add(createRow("Session Type:", cmbSessionType));
        fields.add(createRow("Semester:", spinSemester));

        formCard.add(new JScrollPane(fields), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnAdd = ModernTheme.createPrimaryButton("Add Slot");
        btnUpdate = ModernTheme.createSuccessButton("Update Slot");
        btnDelete = ModernTheme.createDangerButton("Delete Slot");
        btnClear = ModernTheme.createSecondaryButton("Clear Form");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(formCard);

        add(topBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
    }

    private JPanel createRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_MUTED);

        comp.setFont(ModernTheme.FONT_REGULAR);
        if (comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(200, 26));
            comp.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(2, 6, 2, 6)));
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(2, 0, 4, 0));
        return p;
    }

    private void loadDropdowns() {
        deptFilterCombo.removeAllItems();
        deptFilterCombo.addItem(new DeptFilterItem(0, "All Departments"));

        cmbDepartment.removeAllItems();
        List<Department> departments = departmentDAO.getAllDepartments();
        for (Department d : departments) {
            DeptFilterItem item = new DeptFilterItem(d.getDepartmentId(), d.getDepartmentCode() + " - " + d.getDepartmentName());
            deptFilterCombo.addItem(item);
            cmbDepartment.addItem(item);
        }

        cmbCourse.removeAllItems();
        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            cmbCourse.addItem(new CourseComboItem(c.getCourseId(), c.getCourseCode(), c.getCourseName()));
        }

        cmbLecturer.removeAllItems();
        cmbLecturer.addItem(new LecturerComboItem(0, "-- None / TBA --"));
        List<User> lecturers = userDAO.getUsersByRole(2);
        for (User u : lecturers) {
            cmbLecturer.addItem(new LecturerComboItem(u.getUserId(), u.getFullName()));
        }
    }

    public void loadTimetables(int deptId) {
        tableModel.setRowCount(0);
        List<Timetable> list;
        if (deptId == 0) {
            list = timetableService.getAllTimetables();
        } else {
            list = timetableService.getTimetablesByDepartment(deptId);
        }

        for (Timetable t : list) {
            String timeSlot = DateUtil.formatTime(t.getStartTime()) + " - " + DateUtil.formatTime(t.getEndTime());
            tableModel.addRow(new Object[]{
                t.getTimetableId(),
                t.getDayOfWeek(),
                timeSlot,
                t.getCourseCode() + " (" + t.getCourseName() + ")",
                t.getSessionType(),
                t.getRoom(),
                t.getLecturerName() != null ? t.getLecturerName() : "TBA",
                t.getDepartmentCode() != null ? t.getDepartmentCode() : "-"
            });
        }
    }

    private void populateForm() {
        int row = timetableTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableModel.getValueAt(row, 0);
        List<Timetable> list = timetableService.getAllTimetables();
        for (Timetable t : list) {
            if (t.getTimetableId() == id) {
                txtTimetableId.setText(String.valueOf(t.getTimetableId()));
                cmbDay.setSelectedItem(t.getDayOfWeek());
                txtStartTime.setText(t.getStartTime().toString());
                txtEndTime.setText(t.getEndTime().toString());
                txtRoom.setText(t.getRoom());
                cmbSessionType.setSelectedItem(t.getSessionType());
                spinSemester.setValue(t.getSemester());

                for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
                    if (cmbDepartment.getItemAt(i).id == t.getDepartmentId()) {
                        cmbDepartment.setSelectedIndex(i);
                        break;
                    }
                }

                for (int i = 0; i < cmbCourse.getItemCount(); i++) {
                    if (cmbCourse.getItemAt(i).id == t.getCourseId()) {
                        cmbCourse.setSelectedIndex(i);
                        break;
                    }
                }

                if (t.getLecturerId() != null) {
                    for (int i = 0; i < cmbLecturer.getItemCount(); i++) {
                        if (cmbLecturer.getItemAt(i).id == t.getLecturerId()) {
                            cmbLecturer.setSelectedIndex(i);
                            break;
                        }
                    }
                } else {
                    cmbLecturer.setSelectedIndex(0);
                }
                break;
            }
        }
    }

    private void clearForm() {
        timetableTable.clearSelection();
        txtTimetableId.setText("");
        txtStartTime.setText("08:00:00");
        txtEndTime.setText("10:00:00");
        txtRoom.setText("");
        if (cmbDay.getItemCount() > 0) cmbDay.setSelectedIndex(0);
        if (cmbSessionType.getItemCount() > 0) cmbSessionType.setSelectedIndex(0);
    }

    private void handleAdd() {
        try {
            DeptFilterItem dept = (DeptFilterItem) cmbDepartment.getSelectedItem();
            CourseComboItem course = (CourseComboItem) cmbCourse.getSelectedItem();
            LecturerComboItem lec = (LecturerComboItem) cmbLecturer.getSelectedItem();

            if (dept == null) throw new ValidationException("Please select a department.");
            if (course == null) throw new ValidationException("Please select a course module.");

            Time start = DateUtil.parseTime(txtStartTime.getText().trim());
            Time end = DateUtil.parseTime(txtEndTime.getText().trim());
            if (start == null || end == null) throw new ValidationException("Please enter valid times in HH:MM:SS format.");

            Timetable tt = new Timetable(
                0,
                dept.id,
                course.id,
                (String) cmbDay.getSelectedItem(),
                start,
                end,
                txtRoom.getText().trim(),
                (lec != null && lec.id > 0) ? lec.id : null,
                (String) cmbSessionType.getSelectedItem(),
                (int) spinSemester.getValue(),
                "2024/2025"
            );

            timetableService.addTimetable(tt);
            JOptionPane.showMessageDialog(this, "Timetable slot added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            DeptFilterItem sel = (DeptFilterItem) deptFilterCombo.getSelectedItem();
            loadTimetables(sel != null ? sel.id : 0);
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (txtTimetableId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a timetable slot to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtTimetableId.getText());
            DeptFilterItem dept = (DeptFilterItem) cmbDepartment.getSelectedItem();
            CourseComboItem course = (CourseComboItem) cmbCourse.getSelectedItem();
            LecturerComboItem lec = (LecturerComboItem) cmbLecturer.getSelectedItem();

            if (dept == null) throw new ValidationException("Please select a department.");
            if (course == null) throw new ValidationException("Please select a course module.");

            Time start = DateUtil.parseTime(txtStartTime.getText().trim());
            Time end = DateUtil.parseTime(txtEndTime.getText().trim());
            if (start == null || end == null) throw new ValidationException("Please enter valid times in HH:MM:SS format.");

            Timetable tt = new Timetable(
                id,
                dept.id,
                course.id,
                (String) cmbDay.getSelectedItem(),
                start,
                end,
                txtRoom.getText().trim(),
                (lec != null && lec.id > 0) ? lec.id : null,
                (String) cmbSessionType.getSelectedItem(),
                (int) spinSemester.getValue(),
                "2024/2025"
            );

            timetableService.updateTimetable(tt);
            JOptionPane.showMessageDialog(this, "Timetable slot updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            DeptFilterItem sel = (DeptFilterItem) deptFilterCombo.getSelectedItem();
            loadTimetables(sel != null ? sel.id : 0);
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (txtTimetableId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a slot to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtTimetableId.getText());
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this timetable entry?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                timetableService.deleteTimetable(id);
                JOptionPane.showMessageDialog(this, "Timetable entry deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                DeptFilterItem sel = (DeptFilterItem) deptFilterCombo.getSelectedItem();
                loadTimetables(sel != null ? sel.id : 0);
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting timetable slot: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
