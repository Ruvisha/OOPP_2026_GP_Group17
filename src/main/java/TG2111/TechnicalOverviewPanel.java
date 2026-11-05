package com.fot.ams.ui.technical;

import com.fot.ams.dao.RoleAnalyticsDAO;
import com.fot.ams.ui.admin.AdminCharts;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

/**
 * Modern Technical Operations Overview Panel.
 * Displays real-time laboratory, session attendance, and medical certificate telemetry.
 */
public class TechnicalOverviewPanel extends JPanel {

    public interface TechnicalNavDelegate {
        void navigateTo(String tabKey);
    }

    private final RoleAnalyticsDAO analyticsDAO;
    private final TechnicalNavDelegate navDelegate;

    private JLabel lblSessions;
    private JLabel lblMedicals;
    private JLabel lblEligible;

    private AdminCharts.BarChartPanel theoryVsPracticalChart;
    private AdminCharts.DonutChartPanel eligibilityChart;
    private AdminCharts.DonutChartPanel medicalStatusChart;
    private AdminCharts.TrendLineChartPanel monthlyAttendanceChart;

    public TechnicalOverviewPanel(TechnicalNavDelegate navDelegate) {
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

        // 2. Summary Stat Cards (3 Cards)
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
        JLabel title = new JLabel("Technical Officer Operational Shortcuts");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton btnLogAtt = createBtn("Log Attendance", "check", e -> {
            if (navDelegate != null) navDelegate.navigateTo("ATTENDANCE");
        });
        JButton btnReviewMed = createBtn("Review Medicals", "audit", e -> {
            if (navDelegate != null) navDelegate.navigateTo("MEDICAL");
        });
        JButton btnLabSched = createBtn("Lab Timetables", "calendar", e -> {
            if (navDelegate != null) navDelegate.navigateTo("TIMETABLE");
        });

        right.add(btnLogAtt);
        right.add(btnReviewMed);
        right.add(btnLabSched);

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
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblSessions = new JLabel("0");
        lblMedicals = new JLabel("0");
        lblEligible = new JLabel("0");

        row.add(createCard("Sessions Logged", lblSessions, "Theory & Practical attendance sessions", ModernTheme.ACCENT, "check"));
        row.add(createCard("Medical Certificates", lblMedicals, "Submitted student health filings", ModernTheme.WARNING, "audit"));
        row.add(createCard("Exam Attendance Eligibility", lblEligible, "Student modules meeting 80% threshold", ModernTheme.SUCCESS, "students"));

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
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

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

        theoryVsPracticalChart = new AdminCharts.BarChartPanel("Student Attendance: Theory vs Practical", ModernTheme.ACCENT);
        eligibilityChart = new AdminCharts.DonutChartPanel("Attendance Eligibility (80% Bar)");
        medicalStatusChart = new AdminCharts.DonutChartPanel("Medical Certificate Approval Status");
        monthlyAttendanceChart = new AdminCharts.TrendLineChartPanel("Monthly Attendance Volume Trend");

        grid.add(theoryVsPracticalChart);
        grid.add(eligibilityChart);
        grid.add(medicalStatusChart);
        grid.add(monthlyAttendanceChart);

        return grid;
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            RoleAnalyticsDAO.TechnicalSummary sum = analyticsDAO.getTechnicalSummary();
            lblSessions.setText(String.valueOf(sum.totalSessions));
            lblMedicals.setText(sum.medicalLogged + " (" + sum.medicalPending + " Pending)");
            lblEligible.setText(sum.eligibleCount + " / " + sum.totalStudentCourses + " (" +
                (sum.totalStudentCourses > 0 ? (int)(sum.eligibleCount * 100.0 / sum.totalStudentCourses) : 0) + "%)");

            // Charts
            Map<String, Integer> theoryVsPrac = analyticsDAO.getAttendanceTheoryVsPractical();
            theoryVsPracticalChart.setData(theoryVsPrac);

            Map<String, Integer> elig = analyticsDAO.getAttendanceEligibilityDistribution();
            eligibilityChart.setData(elig);

            Map<String, Integer> medStatus = analyticsDAO.getMedicalStatusDistribution();
            medicalStatusChart.setData(medStatus);

            Map<String, Integer> trend = analyticsDAO.getMonthlyAttendanceTrend();
            monthlyAttendanceChart.setData(trend);
        });
    }
}
