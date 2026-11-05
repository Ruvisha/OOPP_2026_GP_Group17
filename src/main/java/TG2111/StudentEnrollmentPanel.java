package com.fot.ams.ui.admin;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.dao.DepartmentDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.CourseEnrollment;
import com.fot.ams.model.Department;
import com.fot.ams.model.Undergraduate;
import com.fot.ams.model.User;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern University Administration panel for Students & Enrollments.
 * Displays undergraduate records, cohort breakdowns, and enrolled courses with full CRUD management.
 */
public class StudentEnrollmentPanel extends JPanel {

    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final CourseEnrollmentDAO enrollmentDAO;
    private final DepartmentDAO departmentDAO;

    private JTable studentTable;
    private DefaultTableModel studentModel;
    private JTable courseTable;
    private DefaultTableModel courseModel;

    private JComboBox<String> cmbDeptFilter;
    private JComboBox<String> cmbTypeFilter;
    private JTextField txtSearch;
    private JLabel lblSelectedStudent;
    private JButton btnEnroll;
    private JButton btnRemoveEnrollment;

    private Undergraduate selectedStudent;

    public StudentEnrollmentPanel() {
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();
        this.departmentDAO = new DepartmentDAO();

        setLayout(new BorderLayout(16, 16));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(18, 20, 20, 20));

        initComponents();
        loadStudents();
    }

    private void initComponents() {
        // Top Filter Bar
        JPanel topBar = ModernTheme.createCardPanel(10);
        topBar.setLayout(new BorderLayout(15, 0));
        topBar.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter By:", ModernIcons.get("search", 15, ModernTheme.TEXT_MUTED), SwingConstants.LEFT);
        lblFilter.setFont(ModernTheme.FONT_BOLD);
        lblFilter.setForeground(ModernTheme.TEXT_SECONDARY);

        cmbDeptFilter = new JComboBox<>(new String[]{"All Departments", "ICT", "BST", "ET"});
        cmbDeptFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbDeptFilter.addActionListener(e -> filterStudents());

        cmbTypeFilter = new JComboBox<>(new String[]{"All Student Types", "REGULAR", "REPEAT", "BATCH_MISSED"});
        cmbTypeFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbTypeFilter.addActionListener(e -> filterStudents());

        txtSearch = new JTextField(16);
        txtSearch.setFont(ModernTheme.FONT_REGULAR);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search Reg No / Name...");
        txtSearch.addActionListener(e -> filterStudents());

        JButton btnSearch = ModernTheme.createSecondaryButton("Search");
        btnSearch.setIcon(ModernIcons.get("search", 14, ModernTheme.TEXT_DARK));
        btnSearch.addActionListener(e -> filterStudents());

        JButton btnReset = ModernTheme.createSecondaryButton("Reset");
        btnReset.setIcon(ModernIcons.get("refresh", 14, ModernTheme.TEXT_DARK));
        btnReset.addActionListener(e -> {
            cmbDeptFilter.setSelectedIndex(0);
            cmbTypeFilter.setSelectedIndex(0);
            txtSearch.setText("");
            loadStudents();
        });

        leftFilters.add(lblFilter);
        leftFilters.add(cmbDeptFilter);
        leftFilters.add(cmbTypeFilter);
        leftFilters.add(txtSearch);
        leftFilters.add(btnSearch);
        leftFilters.add(btnReset);

        topBar.add(leftFilters, BorderLayout.CENTER);
        add(topBar, BorderLayout.NORTH);

        // Center SplitPane: Left Table (Students), Right Details (Enrollments)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.62);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);

        // Students Table Panel
        JPanel leftPanel = ModernTheme.createCardPanel(10);
        leftPanel.setLayout(new BorderLayout(0, 10));

        JLabel leftTitle = new JLabel("Enrolled Undergraduates Directory", ModernIcons.get("students", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        leftTitle.setFont(ModernTheme.FONT_SUBTITLE);
        leftTitle.setForeground(ModernTheme.TEXT_DARK);
        leftPanel.add(leftTitle, BorderLayout.NORTH);

        String[] cols = {"User ID", "Reg No", "Full Name", "Department", "Academic Year", "Semester", "Type"};
        studentModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        studentTable = new JTable(studentModel);
        ModernTheme.styleTable(studentTable);
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleStudentSelected();
            }
        });

        JScrollPane studentScroll = new JScrollPane(studentTable);
        studentScroll.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        leftPanel.add(studentScroll, BorderLayout.CENTER);

        // Right Enrollments Panel
        JPanel rightPanel = ModernTheme.createCardPanel(10);
        rightPanel.setLayout(new BorderLayout(0, 12));

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setOpaque(false);

        lblSelectedStudent = new JLabel("Select a student to view course enrollments");
        lblSelectedStudent.setFont(ModernTheme.FONT_BOLD);
        lblSelectedStudent.setForeground(ModernTheme.PRIMARY);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightActions.setOpaque(false);

        btnEnroll = ModernTheme.createPrimaryButton("+ Enroll in Course");
        btnEnroll.setEnabled(false);
        btnEnroll.addActionListener(e -> showEnrollDialog());

        btnRemoveEnrollment = ModernTheme.createDangerButton("Drop Course");
        btnRemoveEnrollment.setEnabled(false);
        btnRemoveEnrollment.addActionListener(e -> handleDropCourse());

        rightActions.add(btnEnroll);
        rightActions.add(btnRemoveEnrollment);

        rightHeader.add(lblSelectedStudent, BorderLayout.WEST);
        rightHeader.add(rightActions, BorderLayout.EAST);
        rightPanel.add(rightHeader, BorderLayout.NORTH);

        String[] courseCols = {"Enroll ID", "Course Code", "Course Name", "Semester", "Status"};
        courseModel = new DefaultTableModel(courseCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        courseTable = new JTable(courseModel);
        ModernTheme.styleTable(courseTable);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            btnRemoveEnrollment.setEnabled(courseTable.getSelectedRow() >= 0);
        });

        JScrollPane courseScroll = new JScrollPane(courseTable);
        courseScroll.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        rightPanel.add(courseScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    public void loadStudents() {
        studentModel.setRowCount(0);
        try {
            List<User> users = userDAO.getUsersByRole(4); // Undergraduates
            for (User u : users) {
                if (u instanceof Undergraduate ug) {
                    studentModel.addRow(new Object[]{
                        ug.getUserId(),
                        ug.getStudentRegNo() != null ? ug.getStudentRegNo() : "-",
                        ug.getFullName(),
                        ug.getDepartmentName() != null ? ug.getDepartmentName() : "General",
                        ug.getAcademicYear() != null ? ug.getAcademicYear() : "2024/2025",
                        "Sem " + ug.getCurrentSemester(),
                        ug.getStudentType() != null ? ug.getStudentType() : "REGULAR"
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("[StudentEnrollmentPanel] Load students error: " + e.getMessage());
        }
    }

    private void filterStudents() {
        String dept = (String) cmbDeptFilter.getSelectedItem();
        String type = (String) cmbTypeFilter.getSelectedItem();
        String query = txtSearch.getText().trim().toLowerCase();

        studentModel.setRowCount(0);
        List<User> users = userDAO.getUsersByRole(4);
        for (User u : users) {
            if (u instanceof Undergraduate ug) {
                boolean matchDept = dept == null || dept.startsWith("All") ||
                    (ug.getDepartmentName() != null && ug.getDepartmentName().contains(dept));
                boolean matchType = type == null || type.startsWith("All") ||
                    (ug.getStudentType() != null && ug.getStudentType().equalsIgnoreCase(type));
                boolean matchSearch = query.isEmpty() ||
                    (ug.getFullName() != null && ug.getFullName().toLowerCase().contains(query)) ||
                    (ug.getStudentRegNo() != null && ug.getStudentRegNo().toLowerCase().contains(query));

                if (matchDept && matchType && matchSearch) {
                    studentModel.addRow(new Object[]{
                        ug.getUserId(),
                        ug.getStudentRegNo() != null ? ug.getStudentRegNo() : "-",
                        ug.getFullName(),
                        ug.getDepartmentName() != null ? ug.getDepartmentName() : "General",
                        ug.getAcademicYear() != null ? ug.getAcademicYear() : "2024/2025",
                        "Sem " + ug.getCurrentSemester(),
                        ug.getStudentType() != null ? ug.getStudentType() : "REGULAR"
                    });
                }
            }
        }
    }

    private void handleStudentSelected() {
        int row = studentTable.getSelectedRow();
        if (row < 0) {
            selectedStudent = null;
            lblSelectedStudent.setText("Select a student to view course enrollments");
            btnEnroll.setEnabled(false);
            btnRemoveEnrollment.setEnabled(false);
            courseModel.setRowCount(0);
            return;
        }

        int userId = (int) studentModel.getValueAt(row, 0);
        User u = userDAO.getUserById(userId);
        if (u instanceof Undergraduate ug) {
            selectedStudent = ug;
            lblSelectedStudent.setText(ug.getFullName() + " (" + ug.getStudentRegNo() + ")");
            btnEnroll.setEnabled(true);
            loadStudentCourses(ug.getUserId());
        }
    }

    private void loadStudentCourses(int studentId) {
        courseModel.setRowCount(0);
        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(studentId);
        for (CourseEnrollment ce : enrollments) {
            courseModel.addRow(new Object[]{
                ce.getEnrollmentId(),
                ce.getCourseCode() != null ? ce.getCourseCode() : ("CRS-" + ce.getCourseId()),
                ce.getCourseName() != null ? ce.getCourseName() : "Degree Course",
                "Sem " + ce.getSemester(),
                ce.isRepeat() ? "REPEAT" : "REGULAR"
            });
        }
    }

    private void showEnrollDialog() {
        if (selectedStudent == null) return;

        List<Course> allCourses = courseDAO.getAllCourses();
        if (allCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No courses available in the curriculum.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<Course> cmbCourses = new JComboBox<>(allCourses.toArray(new Course[0]));
        cmbCourses.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course c) {
                    setText(c.getCourseCode() + " - " + c.getCourseName() + " (" + c.getCredits() + " cr)");
                }
                return this;
            }
        });

        JTextField txtYear = new JTextField("2024/2025");
        JSpinner spinSem = new JSpinner(new SpinnerNumberModel(selectedStudent.getCurrentSemester(), 1, 8, 1));
        JCheckBox chkRepeat = new JCheckBox("Repeat Attempt");

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Select Course:"));
        form.add(cmbCourses);
        form.add(new JLabel("Academic Year:"));
        form.add(txtYear);
        form.add(new JLabel("Semester:"));
        form.add(spinSem);
        form.add(new JLabel("Attempt Type:"));
        form.add(chkRepeat);

        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Enroll Student: " + selectedStudent.getFullName(),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            Course selectedCrs = (Course) cmbCourses.getSelectedItem();
            if (selectedCrs != null) {
                boolean success = enrollmentDAO.enrollStudent(
                    selectedStudent.getUserId(),
                    selectedCrs.getCourseId(),
                    txtYear.getText().trim(),
                    (Integer) spinSem.getValue(),
                    chkRepeat.isSelected()
                );
                if (success) {
                    JOptionPane.showMessageDialog(this, "Student enrolled successfully in " + selectedCrs.getCourseCode() + "!", "Enrollment Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    loadStudentCourses(selectedStudent.getUserId());
                } else {
                    JOptionPane.showMessageDialog(this, "Student is already enrolled in this course for this semester.", "Enrollment Notice", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private void handleDropCourse() {
        int row = courseTable.getSelectedRow();
        if (row < 0 || selectedStudent == null) return;

        int enrollId = (int) courseModel.getValueAt(row, 0);
        String courseCode = (String) courseModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to drop course " + courseCode + " for " + selectedStudent.getFullName() + "?",
            "Confirm Course Drop",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean dropped = enrollmentDAO.deleteEnrollment(enrollId);
            if (dropped) {
                JOptionPane.showMessageDialog(this, "Enrollment dropped successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadStudentCourses(selectedStudent.getUserId());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to drop enrollment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
