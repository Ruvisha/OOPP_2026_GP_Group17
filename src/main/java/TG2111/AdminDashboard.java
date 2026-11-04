package com.fot.ams.ui.admin;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.NoticeDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.User;
import com.fot.ams.ui.LoginFrame;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Date;

/**
 * Main Administrative Dashboard Frame.
 */
public class AdminDashboard extends JFrame {
    private JPanel contentContainer;
    private CardLayout cardLayout;

    private UserManagementPanel userPanel;
    private CourseManagementPanel coursePanel;
    private MarksOverviewPanel marksPanel;
    private NoticeManagementPanel noticePanel;
    private TimetableManagementPanel timetablePanel;
    private JPanel overviewPanel;

    private JButton btnOverview;
    private JButton btnUsers;
    private JButton btnCourses;
    private JButton btnMarks;
    private JButton btnNotices;
    private JButton btnTimetables;

    public AdminDashboard() {
        initComponents();
    }

    private void initComponents() {
        User currentUser = SessionManager.getCurrentUser();
        String title = currentUser != null ? currentUser.getDashboardTitle() : "Administrator Control Portal";
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ModernTheme.BG_MAIN);

        // Sidebar Panel
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 750));
        sidebar.setBackground(ModernTheme.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel brandTitle = new JLabel("FOT - AMS");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSubtitle = new JLabel("University of Ruhuna");
        brandSubtitle.setFont(ModernTheme.FONT_SMALL);
        brandSubtitle.setForeground(new Color(148, 163, 184));
        brandSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userRoleLabel = new JLabel("Role: Administrator");
        userRoleLabel.setFont(ModernTheme.FONT_SMALL);
        userRoleLabel.setForeground(new Color(96, 165, 250));
        userRoleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(brandTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(brandSubtitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(userRoleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));

        btnOverview = createNavButton("Dashboard Overview");
        btnUsers = createNavButton("Manage Users");
        btnCourses = createNavButton("Manage Courses");
        btnMarks = createNavButton("Marks & Grades");
        btnNotices = createNavButton("Manage Notices");
        btnTimetables = createNavButton("Manage Timetables");

        sidebar.add(btnOverview);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnUsers);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnCourses);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnMarks);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnNotices);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnTimetables);

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = ModernTheme.createDangerButton("Logout from Portal");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(210, 38));
        btnLogout.addActionListener(e -> handleLogout());
        sidebar.add(btnLogout);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setPreferredSize(new Dimension(960, 60));
        headerPanel.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(10, 25, 10, 25)));

        JLabel welcomeLabel = new JLabel("Welcome, " + (currentUser != null ? currentUser.getFullName() : "Administrator"));
        welcomeLabel.setFont(ModernTheme.FONT_SUBTITLE);
        welcomeLabel.setForeground(ModernTheme.TEXT_DARK);

        JLabel dateLabel = new JLabel(DateUtil.formatDisplayDate(new Date()));
        dateLabel.setFont(ModernTheme.FONT_REGULAR);
        dateLabel.setForeground(ModernTheme.TEXT_MUTED);

        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        // Main Center Content Area (CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(ModernTheme.BG_MAIN);

        overviewPanel = createOverviewPanel();
        userPanel = new UserManagementPanel();
        coursePanel = new CourseManagementPanel();
        marksPanel = new MarksOverviewPanel();
        noticePanel = new NoticeManagementPanel();
        timetablePanel = new TimetableManagementPanel();

        contentContainer.add(overviewPanel, "OVERVIEW");
        contentContainer.add(userPanel, "USERS");
        contentContainer.add(coursePanel, "COURSES");
        contentContainer.add(marksPanel, "MARKS");
        contentContainer.add(noticePanel, "NOTICES");
        contentContainer.add(timetablePanel, "TIMETABLES");

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);
        rootPanel.add(contentContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);

        // Nav Handlers
        btnOverview.addActionListener(e -> switchCard("OVERVIEW", btnOverview));
        btnUsers.addActionListener(e -> {
            userPanel.loadUsers(0);
            switchCard("USERS", btnUsers);
        });
        btnCourses.addActionListener(e -> {
            coursePanel.loadCourses();
            switchCard("COURSES", btnCourses);
        });
        btnMarks.addActionListener(e -> switchCard("MARKS", btnMarks));
        btnNotices.addActionListener(e -> {
            noticePanel.loadNotices();
            switchCard("NOTICES", btnNotices);
        });
        btnTimetables.addActionListener(e -> {
            timetablePanel.loadTimetables(0);
            switchCard("TIMETABLES", btnTimetables);
        });

        // Set active button
        setActiveNav(btnOverview);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(ModernTheme.FONT_BOLD);
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(ModernTheme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(210, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void setActiveNav(JButton active) {
        JButton[] buttons = {btnOverview, btnUsers, btnCourses, btnMarks, btnNotices, btnTimetables};
        for (JButton b : buttons) {
            if (b == active) {
                b.setBackground(ModernTheme.BG_SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
                b.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, ModernTheme.ACCENT),
                    new EmptyBorder(10, 11, 10, 15)
                ));
            } else {
                b.setBackground(ModernTheme.BG_SIDEBAR);
                b.setForeground(new Color(203, 213, 225));
                b.setBorder(new EmptyBorder(10, 15, 10, 15));
            }
        }
    }

    private void switchCard(String name, JButton btn) {
        cardLayout.show(contentContainer, name);
        setActiveNav(btn);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Top Statistics Cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setOpaque(false);

        UserDAO userDAO = new UserDAO();
        CourseDAO courseDAO = new CourseDAO();
        NoticeDAO noticeDAO = new NoticeDAO();

        int userCount = userDAO.getAllUsers().size();
        int studentCount = userDAO.getUsersByRole(4).size();
        int courseCount = courseDAO.getAllCourses().size();
        int noticeCount = noticeDAO.getActiveNotices().size();

        statsRow.add(createStatCard("Total System Users", String.valueOf(userCount), new Color(37, 99, 235)));
        statsRow.add(createStatCard("Enrolled Students", String.valueOf(studentCount), new Color(16, 185, 129)));
        statsRow.add(createStatCard("Active Courses", String.valueOf(courseCount), new Color(245, 158, 11)));
        statsRow.add(createStatCard("Active Notices", String.valueOf(noticeCount), new Color(139, 92, 246)));

        // System information card
        JPanel infoCard = ModernTheme.createCardPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));

        JLabel infoTitle = new JLabel("System Modules & Administrative Responsibilities");
        infoTitle.setFont(ModernTheme.FONT_SUBTITLE);
        infoTitle.setForeground(ModernTheme.PRIMARY);

        JTextArea infoText = new JTextArea(
            "\n• Module 01 - User & Admin Management:\n" +
            "  Create, update, view, and delete system user profiles (Admin, Lecturer, Technical Officer, Undergraduate).\n\n" +
            "• Module 02 - Course Management:\n" +
            "  Define and update curriculum course units, credits, semester offerings, and department mappings.\n\n" +
            "• Module 04 - Communication & Timetables:\n" +
            "  Publish faculty announcements and maintain weekly lecture and lab session allocations for all departments.\n\n" +
            "Undergraduate cohort includes Regular, Repeat, and Batch-Missed students according to University of Ruhuna guidelines."
        );
        infoText.setFont(ModernTheme.FONT_REGULAR);
        infoText.setEditable(false);
        infoText.setOpaque(false);
        infoText.setWrapStyleWord(true);
        infoText.setLineWrap(true);

        infoCard.add(infoTitle);
        infoCard.add(infoText);

        panel.add(statsRow, BorderLayout.NORTH);
        panel.add(infoCard, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(ModernTheme.FONT_BOLD);
        lblTitle.setForeground(ModernTheme.TEXT_MUTED);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblVal.setForeground(ModernTheme.TEXT_DARK);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out from the Administrative portal?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
