package com.fot.ams.ui.undergraduate;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel enabling Undergraduates to track their lecture & practical attendance,
 * view percentages, and check exam eligibility across the 5 attendance scenarios.
 */
public class AttendanceViewPanel extends JPanel {
    private final AttendanceService attendanceService;
    private final CourseDAO courseDAO;
    private final CourseEnrollmentDAO enrollmentDAO;

    private JComboBox<CourseComboItem> cmbCourse;
    private JComboBox<String> cmbSessionType;

    // Stat metric labels
    private JLabel lblTotalSessions;
    private JLabel lblPresentSessions;
    private JLabel lblAbsentSessions;
    private JLabel lblApprovedMedicals;
    private JLabel lblRawPercentage;
    private JLabel lblEffectivePercentage;
    private JLabel lblStatusScenario;
    private JLabel lblEligibilityBadge;

    private JTable sessionTable;
    private DefaultTableModel sessionTableModel;

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

    public AttendanceViewPanel() {
        this.attendanceService = new AttendanceService();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();

        initComponents();
        loadStudentCourses();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lCourse = new JLabel("Enrolled Course Module:");
        lCourse.setFont(ModernTheme.FONT_BOLD);
        cmbCourse = new JComboBox<>();
        cmbCourse.addActionListener(e -> loadAttendanceDetails());

        JLabel lType = new JLabel("Component:");
        lType.setFont(ModernTheme.FONT_BOLD);
        cmbSessionType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL", "COMBINED"});
        cmbSessionType.addActionListener(e -> loadAttendanceDetails());

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Attendance");
        btnRefresh.addActionListener(e -> loadAttendanceDetails());

        topBar.add(lCourse);
        topBar.add(cmbCourse);
        topBar.add(lType);
        topBar.add(cmbSessionType);
        topBar.add(btnRefresh);

        // Center Content: Top Cards + Bottom Session Table
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        // Top Metrics Grid
        JPanel metricsGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        metricsGrid.setOpaque(false);

        lblTotalSessions = new JLabel("15", SwingConstants.CENTER);
        lblPresentSessions = new JLabel("0", SwingConstants.CENTER);
        lblAbsentSessions = new JLabel("0", SwingConstants.CENTER);
        lblApprovedMedicals = new JLabel("0", SwingConstants.CENTER);
        lblRawPercentage = new JLabel("0.0%", SwingConstants.CENTER);
        lblEffectivePercentage = new JLabel("0.0%", SwingConstants.CENTER);
        lblStatusScenario = new JLabel("-", SwingConstants.CENTER);
        lblEligibilityBadge = new JLabel("-", SwingConstants.CENTER);

        metricsGrid.add(createMetricBox("Total Sessions", lblTotalSessions, ModernTheme.TEXT_DARK));
        metricsGrid.add(createMetricBox("Present Sessions", lblPresentSessions, ModernTheme.SUCCESS));
        metricsGrid.add(createMetricBox("Absent Sessions", lblAbsentSessions, ModernTheme.DANGER));
        metricsGrid.add(createMetricBox("Approved Medicals", lblApprovedMedicals, ModernTheme.WARNING));
        metricsGrid.add(createMetricBox("Raw Attendance", lblRawPercentage, ModernTheme.PRIMARY));
        metricsGrid.add(createMetricBox("Effective Attendance (Medicals)", lblEffectivePercentage, new Color(5, 150, 105)));
        metricsGrid.add(createMetricBox("Attendance Scenario Status", lblStatusScenario, new Color(79, 70, 229)));
        metricsGrid.add(createMetricBox("Exam Attendance Eligibility", lblEligibilityBadge, ModernTheme.SUCCESS));

        centerPanel.add(metricsGrid, BorderLayout.NORTH);

        // Bottom Table: Session details
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"Session #", "Session Date", "Type", "Duration", "Status", "Attended / Verified"};
        sessionTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        sessionTable = new JTable(sessionTableModel);
        ModernTheme.styleTable(sessionTable);

        tablePanel.add(new JScrollPane(sessionTable), BorderLayout.CENTER);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricBox(String title, JLabel valueLabel, Color valueColor) {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(10, 12, 10, 12)));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(ModernTheme.FONT_SMALL);
        lblTitle.setForeground(ModernTheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valueLabel.setForeground(valueColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadStudentCourses() {
        cmbCourse.removeAllItems();
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(cur.getUserId());
        for (CourseEnrollment ce : enrollments) {
            Course c = courseDAO.getCourseById(ce.getCourseId());
            if (c != null) {
                cmbCourse.addItem(new CourseComboItem(c.getCourseId(), c.getCourseCode(), c.getCourseName()));
            }
        }

        if (cmbCourse.getItemCount() > 0) {
            cmbCourse.setSelectedIndex(0);
            loadAttendanceDetails();
        }
    }

    public void loadAttendanceDetails() {
        sessionTableModel.setRowCount(0);
        User cur = SessionManager.getCurrentUser();
        CourseComboItem course = (CourseComboItem) cmbCourse.getSelectedItem();
        String sessionType = (String) cmbSessionType.getSelectedItem();
        if (cur == null || course == null) return;

        AttendanceSummary summary = attendanceService.getStudentAttendanceSummary(cur.getUserId(), course.id, sessionType);

        lblTotalSessions.setText(String.valueOf(summary.getTotalSessions()));
        lblPresentSessions.setText(String.valueOf(summary.getPresentSessions()));
        lblAbsentSessions.setText(String.valueOf(summary.getAbsentSessions()));
        lblApprovedMedicals.setText(String.valueOf(summary.getApprovedMedicals()));
        lblRawPercentage.setText(String.format("%.1f%%", summary.getRawPercentage()));
        lblEffectivePercentage.setText(String.format("%.1f%%", summary.getEffectivePercentage()));
        lblStatusScenario.setText(summary.getStatusCategory());

        if (summary.isEligible()) {
            lblEligibilityBadge.setText("ELIGIBLE (\u2265 80%)");
            lblEligibilityBadge.setForeground(ModernTheme.SUCCESS);
        } else {
            lblEligibilityBadge.setText("INELIGIBLE (< 80%)");
            lblEligibilityBadge.setForeground(ModernTheme.DANGER);
        }

        List<Attendance> records = attendanceService.getAttendanceByStudentAndCourse(cur.getUserId(), course.id, sessionType);
        for (Attendance a : records) {
            sessionTableModel.addRow(new Object[]{
                "Session " + a.getSessionNumber(),
                DateUtil.formatDate(a.getSessionDate()),
                a.getSessionType(),
                a.getHours() + " Hours",
                a.getStatus(),
                "PRESENT".equalsIgnoreCase(a.getStatus()) ? "\u2714 Present" : "\u2718 Absent"
            });
        }
    }
}
