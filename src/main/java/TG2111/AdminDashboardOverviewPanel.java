package com.fot.ams.ui.admin;

import com.fot.ams.dao.AdminAnalyticsDAO;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Modern University Administration Overview Panel.
 * Features real-time statistics cards, interactive charts, quick action shortcuts,
 * and a live recent system activities feed.
 */
public class AdminDashboardOverviewPanel extends JPanel {

    public interface NavigationDelegate {
        void navigateTo(String tabKey);
        void triggerQuickAction(String actionKey);
    }

    private final AdminAnalyticsDAO analyticsDAO;
    private final NavigationDelegate navigationDelegate;

    // Stat card labels
    private JLabel lblTotalUsers;
    private JLabel lblEnrolledStudents;
    private JLabel lblActiveCourses;
    private JLabel lblActiveNotices;

    // Charts
    private AdminCharts.DonutChartPanel roleDonutChart;
    private AdminCharts.BarChartPanel courseDeptChart;
    private AdminCharts.BarChartPanel studentEnrollChart;
    private AdminCharts.TrendLineChartPanel registrationTrendChart;

    // Recent activity list container
    private JPanel activitiesListPanel;

    public AdminDashboardOverviewPanel(NavigationDelegate delegate) {
        this.analyticsDAO = new AdminAnalyticsDAO();
        this.navigationDelegate = delegate;

        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_MAIN);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(ModernTheme.BG_MAIN);
        scrollContent.setBorder(new EmptyBorder(20, 24, 28, 24));

        // 1. Overview Header & Quick Actions Bar
        scrollContent.add(createQuickActionsBar());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 18)));

        // 2. Statistics Cards Row
        scrollContent.add(createStatisticsRow());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. Visualization Charts Grid (2x2)
        scrollContent.add(createChartsGrid());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 20)));

        // 4. Recent Activities Section
        scrollContent.add(createRecentActivitiesSection());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(ModernTheme.BG_MAIN);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createQuickActionsBar() {
        JPanel container = ModernTheme.createCardPanel(10);
        container.setLayout(new BorderLayout(15, 0));
        container.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        leftTitle.setOpaque(false);
        JLabel icon = new JLabel(ModernIcons.get("settings", 20, ModernTheme.PRIMARY));
        JLabel title = new JLabel("Quick Administrative Actions");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        leftTitle.add(icon);
        leftTitle.add(title);

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsRow.setOpaque(false);

        JButton btnAddUser = createQuickBtn("Add User", "user", e -> {
            if (navigationDelegate != null) navigationDelegate.triggerQuickAction("ADD_USER");
        });
        JButton btnAddStudent = createQuickBtn("Add Student", "students", e -> {
            if (navigationDelegate != null) navigationDelegate.triggerQuickAction("ADD_STUDENT");
        });
        JButton btnAddCourse = createQuickBtn("Add Course", "courses", e -> {
            if (navigationDelegate != null) navigationDelegate.triggerQuickAction("ADD_COURSE");
        });
        JButton btnPublishNotice = createQuickBtn("Publish Notice", "notices", e -> {
            if (navigationDelegate != null) navigationDelegate.triggerQuickAction("PUBLISH_NOTICE");
        });
        JButton btnGenReport = createQuickBtn("Generate Report", "reports", e -> {
            if (navigationDelegate != null) navigationDelegate.triggerQuickAction("GEN_REPORT");
        });

        actionsRow.add(btnAddUser);
        actionsRow.add(btnAddStudent);
        actionsRow.add(btnAddCourse);
        actionsRow.add(btnPublishNotice);
        actionsRow.add(btnGenReport);

        container.add(leftTitle, BorderLayout.WEST);
        container.add(actionsRow, BorderLayout.EAST);
        return container;
    }

    private JButton createQuickBtn(String text, String iconName, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text, ModernIcons.get(iconName, 15, ModernTheme.PRIMARY));
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

    private JPanel createStatisticsRow() {
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        lblTotalUsers = new JLabel("0");
        lblEnrolledStudents = new JLabel("0");
        lblActiveCourses = new JLabel("0");
        lblActiveNotices = new JLabel("0");

        statsRow.add(createStatCard(
            "Total System Users",
            lblTotalUsers,
            "All registered staff & students",
            ModernTheme.ACCENT,
            "users"
        ));

        statsRow.add(createStatCard(
            "Enrolled Students",
            lblEnrolledStudents,
            "Active undergraduate cohorts",
            ModernTheme.SUCCESS,
            "students"
        ));

        statsRow.add(createStatCard(
            "Active Courses",
            lblActiveCourses,
            "Curriculum degree modules",
            ModernTheme.WARNING,
            "courses"
        ));

        statsRow.add(createStatCard(
            "Active Notices",
            lblActiveNotices,
            "Current faculty bulletins",
            ModernTheme.ACCENT_PURPLE,
            "notices"
        ));

        return statsRow;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle, Color accentColor, String iconName) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(15, 23, 42, 6));
                g2.fill(new RoundRectangle2D.Float(1, 2, getWidth() - 2, getHeight() - 2, 12, 12));

                // Background
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

                // Left Accent bar
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight() - 1, 6, 6));

                // Border
                g2.setColor(ModernTheme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 18));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(ModernTheme.FONT_SMALL);
        lblTitle.setForeground(ModernTheme.TEXT_SECONDARY);

        valueLabel.setFont(new Font(ModernTheme.getFontFamily(), Font.BOLD, 26));
        valueLabel.setForeground(ModernTheme.TEXT_DARK);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(ModernTheme.FONT_CAPTION);
        lblSub.setForeground(ModernTheme.TEXT_MUTED);

        content.add(lblTitle);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(valueLabel);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(lblSub);

        // Icon badge on right
        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 26));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(42, 42));
        iconBadge.setLayout(new GridBagLayout());
        iconBadge.add(new JLabel(ModernIcons.get(iconName, 22, accentColor)));

        card.add(content, BorderLayout.CENTER);
        card.add(iconBadge, BorderLayout.EAST);
        return card;
    }

    private JPanel createChartsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);

        roleDonutChart = new AdminCharts.DonutChartPanel("User Distribution by Role");
        courseDeptChart = new AdminCharts.BarChartPanel("Course Units by Department", ModernTheme.ACCENT);
        studentEnrollChart = new AdminCharts.BarChartPanel("Student Enrollment Breakdown", ModernTheme.SUCCESS);
        registrationTrendChart = new AdminCharts.TrendLineChartPanel("Monthly Registration Trends");

        grid.add(roleDonutChart);
        grid.add(courseDeptChart);
        grid.add(studentEnrollChart);
        grid.add(registrationTrendChart);

        return grid;
    }

    private JPanel createRecentActivitiesSection() {
        JPanel card = ModernTheme.createCardPanel(12);
        card.setLayout(new BorderLayout(0, 12));

        // Header with title and refresh button
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(new JLabel(ModernIcons.get("audit", 20, ModernTheme.PRIMARY)));
        JLabel title = new JLabel("Recent System Activities");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        titlePanel.add(title);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Data");
        btnRefresh.setIcon(ModernIcons.get("refresh", 14, ModernTheme.TEXT_DARK));
        btnRefresh.addActionListener(e -> loadData());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);

        activitiesListPanel = new JPanel();
        activitiesListPanel.setLayout(new BoxLayout(activitiesListPanel, BoxLayout.Y_AXIS));
        activitiesListPanel.setOpaque(false);

        card.add(header, BorderLayout.NORTH);
        card.add(activitiesListPanel, BorderLayout.CENTER);

        return card;
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            // Load stats
            AdminAnalyticsDAO.StatOverview stats = analyticsDAO.getOverviewStats();
            lblTotalUsers.setText(String.valueOf(stats.totalUsers));
            lblEnrolledStudents.setText(String.valueOf(stats.enrolledStudents));
            lblActiveCourses.setText(String.valueOf(stats.activeCourses));
            lblActiveNotices.setText(String.valueOf(stats.activeNotices));

            // Load charts
            Map<String, Integer> roles = analyticsDAO.getUserRoleDistribution();
            roleDonutChart.setData(roles);

            Map<String, Integer> deptCourses = analyticsDAO.getCourseDepartmentDistribution();
            courseDeptChart.setData(deptCourses);

            Map<String, Integer> studentTypes = analyticsDAO.getStudentEnrollmentAnalytics();
            studentEnrollChart.setData(studentTypes);

            Map<String, Integer> trends = analyticsDAO.getMonthlyRegistrationTrends();
            registrationTrendChart.setData(trends);

            // Load recent activities
            List<AdminAnalyticsDAO.ActivityItem> activities = analyticsDAO.getRecentActivities(8);
            activitiesListPanel.removeAll();

            if (activities.isEmpty()) {
                activitiesListPanel.add(ModernTheme.createEmptyState(
                    "No Recent Activities Recorded",
                    "New user registrations, course changes, and notices will appear here.",
                    "info"
                ));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                for (AdminAnalyticsDAO.ActivityItem item : activities) {
                    activitiesListPanel.add(createActivityRow(item, sdf));
                    activitiesListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }
            }

            activitiesListPanel.revalidate();
            activitiesListPanel.repaint();
        });
    }

    private JPanel createActivityRow(AdminAnalyticsDAO.ActivityItem item, SimpleDateFormat sdf) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setBackground(new Color(248, 250, 252));
        row.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));

        // Category icon badge
        Color badgeColor;
        String iconName;
        switch (item.type) {
            case "USER":
                badgeColor = ModernTheme.ACCENT;
                iconName = "user";
                break;
            case "COURSE":
                badgeColor = ModernTheme.WARNING;
                iconName = "courses";
                break;
            case "NOTICE":
                badgeColor = ModernTheme.ACCENT_PURPLE;
                iconName = "notices";
                break;
            case "ENROLLMENT":
                badgeColor = ModernTheme.SUCCESS;
                iconName = "students";
                break;
            default:
                badgeColor = ModernTheme.TEXT_MUTED;
                iconName = "info";
                break;
        }

        JLabel icon = new JLabel(ModernIcons.get(iconName, 18, badgeColor));

        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(false);

        JLabel titleLbl = new JLabel(item.title);
        titleLbl.setFont(ModernTheme.FONT_BOLD);
        titleLbl.setForeground(ModernTheme.TEXT_DARK);

        JLabel descLbl = new JLabel(item.description);
        descLbl.setFont(ModernTheme.FONT_SMALL);
        descLbl.setForeground(ModernTheme.TEXT_SECONDARY);

        details.add(titleLbl);
        details.add(Box.createRigidArea(new Dimension(0, 2)));
        details.add(descLbl);

        // Timestamp
        String timeStr = item.timestamp != null ? sdf.format(item.timestamp) : "Recently";
        JLabel timeLbl = new JLabel(timeStr);
        timeLbl.setFont(ModernTheme.FONT_CAPTION);
        timeLbl.setForeground(ModernTheme.TEXT_MUTED);

        row.add(icon, BorderLayout.WEST);
        row.add(details, BorderLayout.CENTER);
        row.add(timeLbl, BorderLayout.EAST);
        return row;
    }
}
