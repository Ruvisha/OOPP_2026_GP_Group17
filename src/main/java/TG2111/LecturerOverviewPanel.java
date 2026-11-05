package com.fot.ams.ui.lecturer;

import com.fot.ams.dao.RoleAnalyticsDAO;
import com.fot.ams.model.User;
import com.fot.ams.ui.admin.AdminCharts;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

/**
 * Modern Lecturer Academic Overview Panel.
 * Visualizes student performance, course attendance, and grade distributions
 * restricted strictly to the courses the logged-in lecturer is assigned to teach.
 */
public class LecturerOverviewPanel extends JPanel {

    public interface LecturerNavDelegate {
        void navigateTo(String tabKey);
    }

    private final RoleAnalyticsDAO analyticsDAO;
    private final LecturerNavDelegate navDelegate;

    private JLabel lblCourses;
    private JLabel lblStudents;
    private JLabel lblEvaluations;
    private JLabel lblPending;

    private AdminCharts.BarChartPanel performanceByCourseChart;
    private AdminCharts.BarChartPanel attendanceByCourseChart;
    private AdminCharts.DonutChartPanel gradeDistributionChart;
    private AdminCharts.TrendLineChartPanel assessmentTrendChart;

    public LecturerOverviewPanel(LecturerNavDelegate navDelegate) {
        this.analyticsDAO = new RoleAnalyticsDAO();
        this.navDelegate = navDelegate;

        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_MAIN);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(ModernTheme.BG_MAIN);
        scrollContent.setBorder(new EmptyBorder(16, 20, 24, 20));

        // 1. Quick Actions Bar
        scrollContent.add(createActionsBar());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 16)));

        // 2. Summary Stat Cards (4 Cards)
        scrollContent.add(createSummaryRow());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 18)));

        // 3. 2x2 Charts Grid
        scrollContent.add(createChartsGrid());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createActionsBar() {
        JPanel container = ModernTheme.createCardPanel(10);
        container.setLayout(new BorderLayout(15, 0));
        container.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        left.setOpaque(false);
        left.add(new JLabel(ModernIcons.get("settings", 18, ModernTheme.PRIMARY)));
        JLabel title = new JLabel("Lecturer Academic Quick Actions");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnMat = createBtn("Upload Material", "courses", e -> {
            if (navDelegate != null) navDelegate.navigateTo("MATERIALS");
        });
        JButton btnMarks = createBtn("Enter Assessment Marks", "marks", e -> {
            if (navDelegate != null) navDelegate.navigateTo("MARKS");
        });
        JButton btnStud = createBtn("Review Undergraduates", "students", e -> {
            if (navDelegate != null) navDelegate.navigateTo("STUDENTS");
        });

        right.add(btnMat);
        right.add(btnMarks);
        right.add(btnStud);

        container.add(left, BorderLayout.WEST);
        container.add(right, BorderLayout.EAST);
        return container;
    }

    private JButton createBtn(String text, String icon, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text, ModernIcons.get(icon, 14, ModernTheme.PRIMARY));
        btn.setFont(ModernTheme.FONT_SMALL);
        btn.setForeground(ModernTheme.PRIMARY);
        btn.setBackground(ModernTheme.PRIMARY_LIGHT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(new Color(199, 210, 254), 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        btn.addActionListener(al);
        return btn;
    }

    private JPanel createSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblCourses = new JLabel("0");
        lblStudents = new JLabel("0");
        lblEvaluations = new JLabel("0");
        lblPending = new JLabel("0");

        row.add(createCard("My Assigned Courses", lblCourses, "Curriculum modules taught", ModernTheme.ACCENT, "courses"));
        row.add(createCard("Enrolled Students", lblStudents, "Undergraduates in my cohorts", ModernTheme.SUCCESS, "students"));
        row.add(createCard("Evaluations & CAs", lblEvaluations, "Quizzes, lab tests & projects", ModernTheme.WARNING, "marks"));
        row.add(createCard("Pending Submissions", lblPending, "Submissions awaiting grading", ModernTheme.ACCENT_PURPLE, "audit"));

        return row;
    }

    private JPanel createCard(String title, JLabel valLbl, String subtitle, Color accent, String iconName) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight() - 1, 6, 6));
                g2.setColor(ModernTheme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(ModernTheme.FONT_SMALL);
        lblT.setForeground(ModernTheme.TEXT_SECONDARY);

        valLbl.setFont(new Font(ModernTheme.getFontFamily(), Font.BOLD, 24));
        valLbl.setForeground(ModernTheme.TEXT_DARK);

        JLabel lblS = new JLabel(subtitle);
        lblS.setFont(ModernTheme.FONT_CAPTION);
        lblS.setForeground(ModernTheme.TEXT_MUTED);

        textStack.add(lblT);
        textStack.add(Box.createRigidArea(new Dimension(0, 4)));
        textStack.add(valLbl);
        textStack.add(Box.createRigidArea(new Dimension(0, 3)));
        textStack.add(lblS);

        card.add(textStack, BorderLayout.CENTER);
        return card;
    }

    private JPanel createChartsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        performanceByCourseChart = new AdminCharts.BarChartPanel("Average Marks by Assigned Course", ModernTheme.ACCENT);
        attendanceByCourseChart = new AdminCharts.BarChartPanel("Attendance Percentage by Course", ModernTheme.SUCCESS);
        gradeDistributionChart = new AdminCharts.DonutChartPanel("Student Grade Distribution (A-F)");
        assessmentTrendChart = new AdminCharts.TrendLineChartPanel("Academic Performance Across Assessments");

        grid.add(performanceByCourseChart);
        grid.add(attendanceByCourseChart);
        grid.add(gradeDistributionChart);
        grid.add(assessmentTrendChart);

        return grid;
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            User cur = SessionManager.getCurrentUser();
            int lecturerId = cur != null ? cur.getUserId() : 2; // fallback to lecturer id 2

            RoleAnalyticsDAO.LecturerSummary sum = analyticsDAO.getLecturerSummary(lecturerId);
            lblCourses.setText(String.valueOf(sum.assignedCourses));
            lblStudents.setText(String.valueOf(sum.enrolledStudents));
            lblEvaluations.setText(String.valueOf(sum.evaluationsCount));
            lblPending.setText(String.valueOf(sum.pendingAssessments));

            // Authorized charts
            Map<String, Integer> perf = analyticsDAO.getLecturerStudentPerformanceByCourse(lecturerId);
            performanceByCourseChart.setData(perf);

            Map<String, Integer> att = analyticsDAO.getLecturerAttendanceByCourse(lecturerId);
            attendanceByCourseChart.setData(att);

            Map<String, Integer> grades = analyticsDAO.getLecturerGradeDistribution(lecturerId);
            gradeDistributionChart.setData(grades);

            Map<String, Integer> trend = analyticsDAO.getLecturerPerformanceTrend(lecturerId);
            assessmentTrendChart.setData(trend);
        });
    }
}
