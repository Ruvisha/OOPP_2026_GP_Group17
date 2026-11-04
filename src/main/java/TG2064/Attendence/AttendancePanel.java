package com.fot.ams.ui.technical;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Attendance;
import com.fot.ams.model.AttendanceSummary;
import com.fot.ams.model.Course;
import com.fot.ams.model.CourseEnrollment;
import com.fot.ams.model.User;
import com.fot.ams.service.AttendanceService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Attendance Management Panel for Technical Officers.
 * Supports:
 * - Theory, Practical, and Combined attendance
 * - Batch attendance recording for all 15 sessions
 * - Percentage calculation and evaluation of 5 attendance scenarios.
 */
public class AttendancePanel extends JPanel {
    private final AttendanceService attendanceService;
    private final CourseDAO courseDAO;
    private final CourseEnrollmentDAO enrollmentDAO;

    private JTabbedPane tabbedPane;

    // Tab 1: Record Batch Attendance for a session
    private JComboBox<CourseComboItem> cmbRecordCourse;
    private JComboBox<String> cmbRecordType;
    private JSpinner spinSessionNo;
    private JTextField txtSessionDate;
    private JTable recordTable;
    private DefaultTableModel recordTableModel;

    // Tab 2: Batch Attendance Summary
    private JComboBox<CourseComboItem> cmbSummaryCourse;
    private JComboBox<String> cmbSummaryType;
    private JTable summaryTable;
    private DefaultTableModel summaryTableModel;

    // Tab 3: Individual Student Session Breakdown
    private JComboBox<CourseComboItem> cmbIndivCourse;
    private JComboBox<StudentComboItem> cmbIndivStudent;
    private JComboBox<String> cmbIndivType;
    private JTable indivTable;
    private DefaultTableModel indivTableModel;
    private JLabel lblIndivSummaryCard;

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

    private static class StudentComboItem {
        final int id;
        final String regNo;
        final String name;

        StudentComboItem(int id, String regNo, String name) {
            this.id = id;
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public String toString() {
            return regNo + " - " + name;
        }
    }

    public AttendancePanel() {
        this.attendanceService = new AttendanceService();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();

        initComponents();
        loadCourses();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_BOLD);

        tabbedPane.addTab("Take / Update Session Attendance", createRecordTab());
        tabbedPane.addTab("Whole Batch Attendance Summary (15 Sessions)", createBatchSummaryTab());
        tabbedPane.addTab("Individual Student Attendance Analysis", createIndividualTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------------
    // TAB 1: RECORD SESSION ATTENDANCE
    // ------------------------------------------------------------------------
    private JPanel createRecordTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernTheme.BG_MAIN);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lCourse = new JLabel("Course:");
        lCourse.setFont(ModernTheme.FONT_BOLD);
        cmbRecordCourse = new JComboBox<>();
        cmbRecordCourse.addActionListener(e -> loadSessionStudents());

        JLabel lType = new JLabel("Type:");
        lType.setFont(ModernTheme.FONT_BOLD);
        cmbRecordType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL"});
        cmbRecordType.addActionListener(e -> loadSessionStudents());

        JLabel lSession = new JLabel("Session (1-15):");
        lSession.setFont(ModernTheme.FONT_BOLD);
        spinSessionNo = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        spinSessionNo.addChangeListener(e -> loadSessionStudents());

        JLabel lDate = new JLabel("Date:");
        lDate.setFont(ModernTheme.FONT_BOLD);
        txtSessionDate = new JTextField(DateUtil.formatDate(DateUtil.nowSqlDate()), 10);

        JButton btnLoad = ModernTheme.createSecondaryButton("Load Students");
        btnLoad.addActionListener(e -> loadSessionStudents());

        topBar.add(lCourse);
        topBar.add(cmbRecordCourse);
        topBar.add(lType);
        topBar.add(cmbRecordType);
        topBar.add(lSession);
        topBar.add(spinSessionNo);
        topBar.add(lDate);
        topBar.add(txtSessionDate);
        topBar.add(btnLoad);

        // Table with status checkboxes
        String[] cols = {"Student ID", "Reg No", "Full Name", "Student Type", "Attendance Status (Double-Click)"};
        recordTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4; // status column editable
            }
        };

        recordTable = new JTable(recordTableModel);
        ModernTheme.styleTable(recordTable);

        // Status Combo editor for column 4
        JComboBox<String> statusEditor = new JComboBox<>(new String[]{"PRESENT", "ABSENT"});
        recordTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusEditor));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(recordTable), BorderLayout.CENTER);

        // Bottom action bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JButton btnMarkAllPresent = ModernTheme.createSecondaryButton("Mark All Present");
        btnMarkAllPresent.addActionListener(e -> {
            for (int i = 0; i < recordTableModel.getRowCount(); i++) {
                recordTableModel.setValueAt("PRESENT", i, 4);
            }
        });

        JButton btnSaveAttendance = ModernTheme.createPrimaryButton("Save & Commit Session Attendance");
        btnSaveAttendance.addActionListener(e -> handleSaveSessionAttendance());

        bottomBar.add(btnMarkAllPresent);
        bottomBar.add(btnSaveAttendance);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    // ------------------------------------------------------------------------
    // TAB 2: BATCH ATTENDANCE SUMMARY
    // ------------------------------------------------------------------------
    private JPanel createBatchSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernTheme.BG_MAIN);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lCourse = new JLabel("Course:");
        lCourse.setFont(ModernTheme.FONT_BOLD);
        cmbSummaryCourse = new JComboBox<>();
        cmbSummaryCourse.addActionListener(e -> loadBatchSummary());

        JLabel lType = new JLabel("Component:");
        lType.setFont(ModernTheme.FONT_BOLD);
        cmbSummaryType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL", "COMBINED"});
        cmbSummaryType.addActionListener(e -> loadBatchSummary());

        JButton btnRefresh = ModernTheme.createSecondaryButton("Generate Batch Summary");
        btnRefresh.addActionListener(e -> loadBatchSummary());

        topBar.add(lCourse);
        topBar.add(cmbSummaryCourse);
        topBar.add(lType);
        topBar.add(cmbSummaryType);
        topBar.add(btnRefresh);

        String[] cols = {
            "Reg No", "Student Name", "Total Sessions", "Present", "Absent",
            "Approved Med", "Raw %", "Effective %", "Attendance Status Scenario", "Exam Eligibility"
        };

        summaryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        summaryTable = new JTable(summaryTableModel);
        ModernTheme.styleTable(summaryTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(summaryTable), BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "Attendance Scenarios Evaluated: " +
            "1) >80% | 2) Exactly 80% | 3) <80% without medical | 4) >80% with approved medical | 5) <80% with approved medical"
        );
        footer.setFont(ModernTheme.FONT_SMALL);
        footer.setForeground(ModernTheme.PRIMARY);
        footer.setBorder(new EmptyBorder(8, 12, 8, 12));
        tablePanel.add(footer, BorderLayout.SOUTH);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------------
    // TAB 3: INDIVIDUAL STUDENT BREAKDOWN
    // ------------------------------------------------------------------------
    private JPanel createIndividualTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernTheme.BG_MAIN);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lCourse = new JLabel("Course:");
        lCourse.setFont(ModernTheme.FONT_BOLD);
        cmbIndivCourse = new JComboBox<>();
        cmbIndivCourse.addActionListener(e -> {
            loadStudentsForIndivDropdown();
            loadIndividualStudentData();
        });

        JLabel lStudent = new JLabel("Undergraduate:");
        lStudent.setFont(ModernTheme.FONT_BOLD);
        cmbIndivStudent = new JComboBox<>();
        cmbIndivStudent.addActionListener(e -> loadIndividualStudentData());

        JLabel lType = new JLabel("Type:");
        lType.setFont(ModernTheme.FONT_BOLD);
        cmbIndivType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL", "COMBINED"});
        cmbIndivType.addActionListener(e -> loadIndividualStudentData());

        JButton btnRefresh = ModernTheme.createSecondaryButton("Analyze Record");
        btnRefresh.addActionListener(e -> loadIndividualStudentData());

        topBar.add(lCourse);
        topBar.add(cmbIndivCourse);
        topBar.add(lStudent);
        topBar.add(cmbIndivStudent);
        topBar.add(lType);
        topBar.add(cmbIndivType);
        topBar.add(btnRefresh);

        // Summary Card Top
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BorderLayout());
        lblIndivSummaryCard = new JLabel("Select an undergraduate to view attendance percentage & session breakdown.");
        lblIndivSummaryCard.setFont(ModernTheme.FONT_BOLD);
        lblIndivSummaryCard.setForeground(ModernTheme.PRIMARY);
        card.add(lblIndivSummaryCard, BorderLayout.CENTER);

        String[] cols = {"Session #", "Session Date", "Type", "Hours", "Status", "Recorded By"};
        indivTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        indivTable = new JTable(indivTableModel);
        ModernTheme.styleTable(indivTable);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(card, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(indivTable), BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void loadCourses() {
        cmbRecordCourse.removeAllItems();
        cmbSummaryCourse.removeAllItems();
        cmbIndivCourse.removeAllItems();

        List<Course> list = courseDAO.getAllCourses();
        for (Course c : list) {
            CourseComboItem item = new CourseComboItem(c.getCourseId(), c.getCourseCode(), c.getCourseName());
            cmbRecordCourse.addItem(item);
            cmbSummaryCourse.addItem(item);
            cmbIndivCourse.addItem(item);
        }

        if (cmbRecordCourse.getItemCount() > 0) {
            cmbRecordCourse.setSelectedIndex(0);
        }
    }

    private void loadSessionStudents() {
        recordTableModel.setRowCount(0);
        CourseComboItem course = (CourseComboItem) cmbRecordCourse.getSelectedItem();
        String sessionType = (String) cmbRecordType.getSelectedItem();
        int sessionNo = (int) spinSessionNo.getValue();
        if (course == null) return;

        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByCourse(course.id);
        List<Attendance> existing = attendanceService.getAttendanceByCourseAndSession(course.id, sessionType, sessionNo);

        for (CourseEnrollment ce : enrollments) {
            String status = "PRESENT"; // Default
            for (Attendance a : existing) {
                if (a.getStudentId() == ce.getStudentId()) {
                    status = a.getStatus();
                    break;
                }
            }

            recordTableModel.addRow(new Object[]{
                ce.getStudentId(),
                ce.getStudentRegNo(),
                ce.getStudentName(),
                ce.isRepeat() ? "REPEAT" : "REGULAR",
                status
            });
        }
    }

    private void handleSaveSessionAttendance() {
        CourseComboItem course = (CourseComboItem) cmbRecordCourse.getSelectedItem();
        String sessionType = (String) cmbRecordType.getSelectedItem();
        int sessionNo = (int) spinSessionNo.getValue();
        if (course == null) return;

        Date sessionDate = DateUtil.parseDate(txtSessionDate.getText().trim());
        if (sessionDate == null) {
            JOptionPane.showMessageDialog(this, "Please enter a valid session date (YYYY-MM-DD).", "Date Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User cur = SessionManager.getCurrentUser();
        Integer recorderId = cur != null ? cur.getUserId() : 7;

        List<Attendance> list = new ArrayList<>();
        for (int i = 0; i < recordTableModel.getRowCount(); i++) {
            int studentId = (int) recordTableModel.getValueAt(i, 0);
            String status = (String) recordTableModel.getValueAt(i, 4);

            Attendance a = new Attendance(
                0, studentId, course.id, sessionDate,
                sessionType, sessionNo, 2, status, recorderId
            );
            list.add(a);
        }

        try {
            attendanceService.batchRecordAttendance(list);
            JOptionPane.showMessageDialog(this, "Attendance saved for " + list.size() + " students successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSessionStudents();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBatchSummary() {
        summaryTableModel.setRowCount(0);
        CourseComboItem course = (CourseComboItem) cmbSummaryCourse.getSelectedItem();
        String sessionType = (String) cmbSummaryType.getSelectedItem();
        if (course == null) return;

        List<AttendanceSummary> summaries = attendanceService.getBatchAttendanceSummary(course.id, sessionType);
        for (AttendanceSummary s : summaries) {
            summaryTableModel.addRow(new Object[]{
                s.getStudentRegNo(),
                s.getStudentName(),
                s.getTotalSessions(),
                s.getPresentSessions(),
                s.getAbsentSessions(),
                s.getApprovedMedicals(),
                String.format("%.1f%%", s.getRawPercentage()),
                String.format("%.1f%%", s.getEffectivePercentage()),
                s.getStatusCategory(),
                s.isEligible() ? "ELIGIBLE" : "NOT ELIGIBLE"
            });
        }
    }

    private void loadStudentsForIndivDropdown() {
        cmbIndivStudent.removeAllItems();
        CourseComboItem course = (CourseComboItem) cmbIndivCourse.getSelectedItem();
        if (course == null) return;

        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByCourse(course.id);
        for (CourseEnrollment ce : enrollments) {
            cmbIndivStudent.addItem(new StudentComboItem(ce.getStudentId(), ce.getStudentRegNo(), ce.getStudentName()));
        }
    }

    private void loadIndividualStudentData() {
        indivTableModel.setRowCount(0);
        CourseComboItem course = (CourseComboItem) cmbIndivCourse.getSelectedItem();
        StudentComboItem student = (StudentComboItem) cmbIndivStudent.getSelectedItem();
        String sessionType = (String) cmbIndivType.getSelectedItem();
        if (course == null || student == null) return;

        AttendanceSummary s = attendanceService.getStudentAttendanceSummary(student.id, course.id, sessionType);
        lblIndivSummaryCard.setText(String.format(
            "<html><b>%s (%s)</b> | Total: %d | Present: %d | Absent: %d | Approved Medicals: %d | " +
            "Raw: <b>%.1f%%</b> | Effective (With Medicals): <b>%.1f%%</b> | Status: <b><font color='%s'>%s</font></b></html>",
            student.name, student.regNo, s.getTotalSessions(), s.getPresentSessions(), s.getAbsentSessions(),
            s.getApprovedMedicals(), s.getRawPercentage(), s.getEffectivePercentage(),
            s.isEligible() ? "#16A34A" : "#DC2626", s.getStatusCategory()
        ));

        List<Attendance> records = attendanceService.getAttendanceByStudentAndCourse(student.id, course.id, sessionType);
        for (Attendance a : records) {
            indivTableModel.addRow(new Object[]{
                a.getSessionNumber(),
                DateUtil.formatDate(a.getSessionDate()),
                a.getSessionType(),
                a.getHours() + " hrs",
                a.getStatus(),
                a.getRecorderName() != null ? a.getRecorderName() : "Technical Officer"
            });
        }
    }
}
