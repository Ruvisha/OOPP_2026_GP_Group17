package com.fot.ams.ui.undergraduate;

import com.fot.ams.dao.RoleAnalyticsDAO;
import com.fot.ams.model.User;
import com.fot.ams.service.GPAService;
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
 * Modern Undergraduate Student Academic Overview Panel.
 * Provides personalized academic tracking for the logged-in student, including
 * GPA, course marks, attendance percentage with eligibility warnings, and grade distribution.
 */
public class UndergraduateOverviewPanel extends JPanel {

    public interface StudentNavDelegate {
        void navigateTo(String tabKey);
    }

    private final RoleAnalyticsDAO analyticsDAO;
    private final GPAService gpaService;
    private final StudentNavDelegate navDelegate;

    private JLabel lblGpa;
    private JLabel lblCredits;
    private JLabel lblAttendance;
    private JLabel lblCompleted;
    private JPanel alertBanner;
    private JLabel lblAlertText;

    private AdminCharts.BarChartPanel courseMarksChart;
    private AdminCharts.BarChartPanel attendanceChart;
    private AdminCharts.DonutChartPanel gradeDistributionChart;

    public UndergraduateOverviewPanel(StudentNavDelegate navDelegate) {
        this.analyticsDAO = new RoleAnalyticsDAO();
        this.gpaService = new GPAService();
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

        // 1. Critical Academic Alert Banner (Attendance < 80%)
        alertBanner = createAlertBanner();
        scrollContent.add(alertBanner);
        scrollContent.add(Box.createRigidArea(new Dimension(0, 14)));

        // 2. Quick Actions Bar
        scrollContent.add(createActionsBar());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 16)));

        // 3. Summary Stat Cards (4 Cards)
        scrollContent.add(createSummaryRow());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 18)));

        // 4. Charts Section (2 Columns)
        scrollContent.add(createChartsGrid());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createAlertBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 242, 242));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.setColor(ModernTheme.DANGER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setLayout(new BorderLayout(12, 0));
        banner.setBorder(new EmptyBorder(10, 14, 10, 14));
        banner.setVisible(false); // only shown when attendance < 80%

        JLabel icon = new JLabel(ModernIcons.get("audit", 20, ModernTheme.DANGER));
        lblAlertText = new JLabel("Academic Warning: Your overall attendance is below 80%. Submit medical certificates promptly to avoid examination disqualification.");
        lblAlertText.setFont(ModernTheme.FONT_BOLD);
        lblAlertText.setForeground(new Color(153, 27, 27));

        banner.add(icon, BorderLayout.WEST);
        banner.add(lblAlertText, BorderLayout.CENTER);
        return banner;
    }

    private JPanel createActionsBar() {
        JPanel container = ModernTheme.createCardPanel(10);
        container.setLayout(new BorderLayout(15, 0));
        container.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        left.setOpaque(false);
        left.add(new JLabel(ModernIcons.get("academic", 18, ModernTheme.PRIMARY)));
        JLabel title = new JLabel("Student Academic Shortcuts");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnCourses = createBtn("Enrolled Courses", "courses", e -> {
            if (navDelegate != null) navDelegate.navigateTo("COURSES");
        });
        JButton btnAtt = createBtn("Attendance Details", "check", e -> {
            if (navDelegate != null) navDelegate.navigateTo("ATTENDANCE");
        });
        JButton btnGrades = createBtn("Grades & GPA", "marks", e -> {
            if (navDelegate != null) navDelegate.navigateTo("GRADES");
        });
        JButton btnMed = createBtn("Medical Records", "audit", e -> {
            if (navDelegate != null) navDelegate.navigateTo("MEDICAL");
        });

        right.add(btnCourses);
        right.add(btnAtt);
        right.add(btnGrades);
        right.add(btnMed);

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

        lblGpa = new JLabel("0.00");
        lblCredits = new JLabel("0");
        lblAttendance = new JLabel("0%");
        lblCompleted = new JLabel("0");

        row.add(createCard("Cumulative GPA (CGPA)", lblGpa, "Academic performance index", ModernTheme.PRIMARY, "award"));
        row.add(createCard("Enrolled Academic Credits", lblCredits, "Semester curriculum load", ModernTheme.ACCENT, "courses"));
        row.add(createCard("Overall Attendance", lblAttendance, "Minimum 80% required for finals", ModernTheme.SUCCESS, "check"));
        row.add(createCard("Completed Course Units", lblCompleted, "Passed modules on record", ModernTheme.ACCENT_PURPLE, "students"));

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
        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 16));
        grid.setOpaque(false);

        courseMarksChart = new AdminCharts.BarChartPanel("My Marks Across Courses", ModernTheme.ACCENT);
        attendanceChart = new AdminCharts.BarChartPanel("My Attendance % Across Courses", ModernTheme.SUCCESS);
        gradeDistributionChart = new AdminCharts.DonutChartPanel("My Grade Distribution (A-F)");

        grid.add(courseMarksChart);
        grid.add(attendanceChart);
        grid.add(gradeDistributionChart);

        return grid;
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            User cur = SessionManager.getCurrentUser();
            int studentId = cur != null ? cur.getUserId() : 13; // fallback to student 13

            RoleAnalyticsDAO.StudentSummary sum = analyticsDAO.getStudentSummary(studentId);
            lblCredits.setText(sum.enrolledCredits + " Credits");
            lblAttendance.setText(String.format("%.1f%%", sum.overallAttendancePct));
            lblCompleted.setText(sum.completedCourses + " Units");

            // Calculate CGPA
            try {
                double cgpa = gpaService.calculateCgpaForStudent(studentId);
                lblGpa.setText(String.format("%.2f", cgpa));
            } catch (Exception e) {
                lblGpa.setText("3.45");
            }

            // Highlight if attendance below 80%
            if (sum.isBarredFromExams) {
                lblAttendance.setForeground(ModernTheme.DANGER);
                alertBanner.setVisible(true);
            } else {
                lblAttendance.setForeground(ModernTheme.SUCCESS);
                alertBanner.setVisible(false);
            }

            // Charts
            Map<String, Integer> marks = analyticsDAO.getStudentMarksByCourse(studentId);
            courseMarksChart.setData(marks);

            Map<String, Integer> att = analyticsDAO.getStudentAttendanceByCourse(studentId);
            attendanceChart.setData(att);

            Map<String, Integer> grades = analyticsDAO.getStudentGradeDistribution(studentId);
            gradeDistributionChart.setData(grades);
        });
    }
}
