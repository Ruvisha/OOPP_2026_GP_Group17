package com.fot.ams.ui.lecturer;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.AttendanceSummary;
import com.fot.ams.model.Course;
import com.fot.ams.model.Medical;
import com.fot.ams.model.Undergraduate;
import com.fot.ams.model.User;
import com.fot.ams.service.AttendanceService;
import com.fot.ams.service.GPAService;
import com.fot.ams.service.MedicalService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel enabling Lecturers to review student profiles, attendance summaries, medical certificates, and GPA.
 */
public class StudentDetailsPanel extends JPanel {
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final AttendanceService attendanceService;
    private final MedicalService medicalService;
    private final GPAService gpaService;

    private JComboBox<CourseComboItem> cmbCourse;
    private JComboBox<String> cmbSessionType;
    private JTable studentTable;
    private DefaultTableModel tableModel;

    // Bottom Details Card
    private JLabel lblSelectedStudent;
    private JLabel lblAttendanceStats;
    private JLabel lblGpaStats;
    private DefaultTableModel medTableModel;
    private JTable medTable;

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

    public StudentDetailsPanel() {
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
        this.attendanceService = new AttendanceService();
        this.medicalService = new MedicalService();
        this.gpaService = new GPAService();

        initComponents();
        loadCourses();
        loadStudentList();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lCourse = new JLabel("Course Module:");
        lCourse.setFont(ModernTheme.FONT_BOLD);
        cmbCourse = new JComboBox<>();
        cmbCourse.addActionListener(e -> loadStudentList());

        JLabel lType = new JLabel("Attendance Component:");
        lType.setFont(ModernTheme.FONT_BOLD);
        cmbSessionType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL", "COMBINED"});
        cmbSessionType.addActionListener(e -> loadStudentList());

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Overview");
        btnRefresh.addActionListener(e -> loadStudentList());

        topBar.add(lCourse);
        topBar.add(cmbCourse);
        topBar.add(lType);
        topBar.add(cmbSessionType);
        topBar.add(btnRefresh);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.58);
        splitPane.setDividerLocation(350);

        // Top Table: Students with attendance percentage & GPA
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"Student ID", "Reg No", "Full Name", "Student Type", "Attendance %", "Attendance Status", "CGPA", "Academic Standing"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        ModernTheme.styleTable(studentTable);
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentTable.getSelectedRow() != -1) {
                displayStudentMedicalRecords();
            }
        });

        tablePanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // Bottom Detail Card (Medical history and specific details)
        JPanel detailCard = ModernTheme.createCardPanel();
        detailCard.setLayout(new BorderLayout(10, 10));

        JPanel detailHeader = new JPanel(new GridLayout(2, 1, 4, 4));
        detailHeader.setOpaque(false);

        lblSelectedStudent = new JLabel("Select an undergraduate from the table to view verified medical records & detailed statistics.");
        lblSelectedStudent.setFont(ModernTheme.FONT_BOLD);
        lblSelectedStudent.setForeground(ModernTheme.PRIMARY);

        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        statsRow.setOpaque(false);
        lblAttendanceStats = new JLabel("Attendance: -");
        lblAttendanceStats.setFont(ModernTheme.FONT_REGULAR);
        lblGpaStats = new JLabel("GPA: -");
        lblGpaStats.setFont(ModernTheme.FONT_REGULAR);
        statsRow.add(lblAttendanceStats);
        statsRow.add(lblGpaStats);

        detailHeader.add(lblSelectedStudent);
        detailHeader.add(statsRow);
        detailCard.add(detailHeader, BorderLayout.NORTH);

        // Medical Sub-table
        String[] medCols = {"Medical ID", "Course", "Session Type", "Session Date", "Reason", "Document", "Status", "Reviewer Remarks"};
        medTableModel = new DefaultTableModel(medCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        medTable = new JTable(medTableModel);
        ModernTheme.styleTable(medTable);

        detailCard.add(new JScrollPane(medTable), BorderLayout.CENTER);

        splitPane.setTopComponent(tablePanel);
        splitPane.setBottomComponent(detailCard);

        add(topBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadCourses() {
        cmbCourse.removeAllItems();
        List<Course> list = courseDAO.getAllCourses();
        for (Course c : list) {
            cmbCourse.addItem(new CourseComboItem(c.getCourseId(), c.getCourseCode(), c.getCourseName()));
        }
        if (cmbCourse.getItemCount() > 0) {
            cmbCourse.setSelectedIndex(0);
        }
    }

    private void loadStudentList() {
        tableModel.setRowCount(0);
        CourseComboItem course = (CourseComboItem) cmbCourse.getSelectedItem();
        String sessionType = (String) cmbSessionType.getSelectedItem();
        if (course == null) return;

        List<User> students = userDAO.getUsersByRole(4);
        for (User u : students) {
            if (u instanceof Undergraduate ug) {
                AttendanceSummary summary = attendanceService.getStudentAttendanceSummary(ug.getUserId(), course.id, sessionType);
                double cgpa = gpaService.calculateCgpaForStudent(ug.getUserId());
                String classification = gpaService.getClassification(cgpa);

                tableModel.addRow(new Object[]{
                    ug.getUserId(),
                    ug.getStudentRegNo(),
                    ug.getFullName(),
                    ug.getStudentType(),
                    String.format("%.1f%%", summary.getEffectivePercentage()),
                    summary.getStatusCategory(),
                    cgpa > 0.0 ? String.format("%.2f", cgpa) : "N/A",
                    classification
                });
            }
        }
    }

    private void displayStudentMedicalRecords() {
        medTableModel.setRowCount(0);
        int row = studentTable.getSelectedRow();
        if (row == -1) return;

        int studentId = (int) tableModel.getValueAt(row, 0);
        String regNo = (String) tableModel.getValueAt(row, 1);
        String name = (String) tableModel.getValueAt(row, 2);
        String attStr = (String) tableModel.getValueAt(row, 4);
        String attStatus = (String) tableModel.getValueAt(row, 5);
        String cgpaStr = (String) tableModel.getValueAt(row, 6);

        lblSelectedStudent.setText("Selected Student: " + regNo + " - " + name);
        lblAttendanceStats.setText("Effective Attendance: " + attStr + " (" + attStatus + ")");
        lblGpaStats.setText("CGPA: " + cgpaStr);

        List<Medical> medicals = medicalService.getMedicalsByStudent(studentId);
        for (Medical m : medicals) {
            medTableModel.addRow(new Object[]{
                m.getMedicalId(),
                m.getCourseCode(),
                m.getSessionType(),
                DateUtil.formatDate(m.getSessionDate()),
                m.getReason(),
                m.getMedicalDocPath() != null ? m.getMedicalDocPath() : "-",
                m.getStatus(),
                m.getRemarks() != null ? m.getRemarks() : "-"
            });
        }
    }
}
