package com.fot.ams.ui.admin;

import com.fot.ams.dao.AdminAnalyticsDAO;
import com.fot.ams.dao.NoticeDAO;
import com.fot.ams.dao.NotificationDAO;
import com.fot.ams.model.Notice;
import com.fot.ams.model.User;
import com.fot.ams.ui.LoginFrame;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.ui.common.NotificationDialog;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Modern University Administration Dashboard for Faculty of Technology Academic Management System.
 * University of Ruhuna - Group 17.
 */
public class AdminDashboard extends JFrame implements AdminDashboardOverviewPanel.NavigationDelegate {

    private JPanel contentContainer;
    private CardLayout cardLayout;

    // Existing Panels
    private UserManagementPanel userPanel;
    private CourseManagementPanel coursePanel;
    private MarksOverviewPanel marksPanel;
    private NoticeManagementPanel noticePanel;
    private TimetableManagementPanel timetablePanel;
    private JPanel overviewPanel;

    // Upgraded & New Enterprise Panels
    private StudentEnrollmentPanel studentPanel;
    private ReportsAnalyticsPanel reportsPanel;
    private AuditLogsPanel auditPanel;
    private SystemSettingsPanel settingsPanel;
    private AdminProfilePanel profilePanel;

    // Sidebar Navigation Buttons - MAIN MENU
    private JButton btnOverview;
    private JButton btnUsers;
    private JButton btnCourses;
    private JButton btnStudents;
    private JButton btnMarks;
    private JButton btnNotices;
    private JButton btnTimetables;

    // Sidebar Navigation Buttons - SYSTEM
    private JButton btnReports;
    private JButton btnAudit;
    private JButton btnSettings;
    private JButton btnProfile;
    private JButton btnLogout;

    // Top Bar Components
    private JTextField txtGlobalSearch;
    private JLabel lblNotificationBadge;
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final AdminAnalyticsDAO analyticsDAO = new AdminAnalyticsDAO();

    public AdminDashboard() {
        initComponents();
    }

    private void initComponents() {
        User currentUser = SessionManager.getCurrentUser();
        String title = currentUser != null ? currentUser.getDashboardTitle() : "Faculty of Technology - Administrator Portal";
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ModernTheme.BG_MAIN);

        // Build Left Modern Sidebar
        JPanel sidebar = createSidebar();

        // Build Top Navigation Bar
        JPanel headerPanel = createTopNavigationBar(currentUser);

        // Build Main Center Content Area (CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(ModernTheme.BG_MAIN);

        overviewPanel = new AdminDashboardOverviewPanel(this);
        userPanel = new UserManagementPanel();
        coursePanel = new CourseManagementPanel();
        studentPanel = new StudentEnrollmentPanel();
        marksPanel = new MarksOverviewPanel();
        noticePanel = new NoticeManagementPanel();
        timetablePanel = new TimetableManagementPanel();
        reportsPanel = new ReportsAnalyticsPanel();
        auditPanel = new AuditLogsPanel();
        settingsPanel = new SystemSettingsPanel();
        profilePanel = new AdminProfilePanel();

        contentContainer.add(overviewPanel, "OVERVIEW");
        contentContainer.add(userPanel, "USERS");
        contentContainer.add(coursePanel, "COURSES");
        contentContainer.add(studentPanel, "STUDENTS");
        contentContainer.add(marksPanel, "MARKS");
        contentContainer.add(noticePanel, "NOTICES");
        contentContainer.add(timetablePanel, "TIMETABLES");
        contentContainer.add(reportsPanel, "REPORTS");
        contentContainer.add(auditPanel, "AUDIT");
        contentContainer.add(settingsPanel, "SETTINGS");
        contentContainer.add(profilePanel, "PROFILE");

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(headerPanel, BorderLayout.NORTH);
        rootPanel.add(contentContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);

        // Bind Nav Handlers
        btnOverview.addActionListener(e -> {
            ((AdminDashboardOverviewPanel) overviewPanel).loadData();
            switchCard("OVERVIEW", btnOverview);
        });
        btnUsers.addActionListener(e -> {
            userPanel.loadUsers(0);
            switchCard("USERS", btnUsers);
        });
        btnCourses.addActionListener(e -> {
            coursePanel.loadCourses();
            switchCard("COURSES", btnCourses);
        });
        btnStudents.addActionListener(e -> {
            studentPanel.loadStudents();
            switchCard("STUDENTS", btnStudents);
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
        btnReports.addActionListener(e -> switchCard("REPORTS", btnReports));
        btnAudit.addActionListener(e -> {
            auditPanel.loadAuditLogs();
            switchCard("AUDIT", btnAudit);
        });
        btnSettings.addActionListener(e -> switchCard("SETTINGS", btnSettings));
        btnProfile.addActionListener(e -> switchCard("PROFILE", btnProfile));
        btnLogout.addActionListener(e -> handleLogout());

        // Default initial active tab
        setActiveNav(btnOverview);
        refreshNotificationBadge();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, 820));
        sidebar.setBackground(ModernTheme.NAVY);
        sidebar.setLayout(new BorderLayout());

        // Stationary Menu Content (strictly non-scrolling)
        JPanel menuContent = new JPanel();
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));
        menuContent.setBackground(ModernTheme.NAVY);
        menuContent.setBorder(new EmptyBorder(14, 14, 8, 14));

        // 1. Branding Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandTitle = new JLabel("FOT - AMS", ModernIcons.get("academic", 22, ModernTheme.ACCENT), SwingConstants.CENTER);
        brandTitle.setFont(new Font(ModernTheme.getFontFamily(), Font.BOLD, 20));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSubtitle = new JLabel("University of Ruhuna");
        brandSubtitle.setFont(ModernTheme.FONT_SMALL);
        brandSubtitle.setForeground(new Color(148, 163, 184));
        brandSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userRoleLabel = ModernTheme.createBadge("ADMIN CONTROL PORTAL", new Color(30, 58, 138), new Color(147, 197, 253));
        userRoleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        brandPanel.add(brandSubtitle);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        brandPanel.add(userRoleLabel);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        menuContent.add(brandPanel);

        // 2. MAIN MENU Section
        menuContent.add(createSectionHeader("MAIN MENU"));
        menuContent.add(Box.createRigidArea(new Dimension(0, 3)));

        btnOverview = createNavButton("Dashboard Overview", "dashboard");
        btnUsers = createNavButton("Manage Users", "users");
        btnCourses = createNavButton("Manage Courses", "courses");
        btnStudents = createNavButton("Students & Enrollments", "students");
        btnMarks = createNavButton("Marks & Grades", "marks");
        btnNotices = createNavButton("Manage Notices", "notices");
        btnTimetables = createNavButton("Manage Timetables", "timetables");

        menuContent.add(btnOverview);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnUsers);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnCourses);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnStudents);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnMarks);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnNotices);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnTimetables);

        // 3. SYSTEM Section
        menuContent.add(Box.createRigidArea(new Dimension(0, 8)));
        menuContent.add(createSectionHeader("SYSTEM"));
        menuContent.add(Box.createRigidArea(new Dimension(0, 3)));

        btnReports = createNavButton("Reports & Analytics", "reports");
        btnAudit = createNavButton("Audit Logs", "audit");
        btnSettings = createNavButton("System Settings", "settings");
        btnProfile = createNavButton("Admin Profile", "user");

        menuContent.add(btnReports);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnAudit);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnSettings);
        menuContent.add(Box.createRigidArea(new Dimension(0, 2)));
        menuContent.add(btnProfile);

        // Stationary center - strictly NO scrollbars
        sidebar.add(menuContent, BorderLayout.CENTER);

        // Bottom Logout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ModernTheme.NAVY);
        bottomPanel.setBorder(new EmptyBorder(8, 14, 12, 14));

        btnLogout = new JButton("Logout from Portal", ModernIcons.get("logout", 16, new Color(252, 165, 165))) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogout.setContentAreaFilled(false);
        btnLogout.setOpaque(false);
        btnLogout.setFont(ModernTheme.FONT_BOLD);
        btnLogout.setBackground(new Color(153, 27, 27)); // Deep Red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnLogout.setMaximumSize(new Dimension(232, 36));
        btnLogout.setPreferredSize(new Dimension(232, 36));

        bottomPanel.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JLabel createSectionHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(ModernTheme.FONT_CAPTION);
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBorder(new EmptyBorder(4, 10, 2, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton createNavButton(String text, String iconName) {
        JButton btn = new JButton(text, ModernIcons.get(iconName, 16, new Color(148, 163, 184))) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFont(ModernTheme.FONT_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(ModernTheme.NAVY);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(232, 34));
        btn.setPreferredSize(new Dimension(232, 34));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.getBackground() != ModernTheme.BG_SIDEBAR_ACTIVE) {
                    btn.setBackground(ModernTheme.BG_SIDEBAR_HOVER);
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground() != ModernTheme.BG_SIDEBAR_ACTIVE) {
                    btn.setBackground(ModernTheme.NAVY);
                    btn.setForeground(new Color(203, 213, 225));
                }
            }
        });

        return btn;
    }

    private void setActiveNav(JButton active) {
        JButton[] buttons = {
            btnOverview, btnUsers, btnCourses, btnStudents,
            btnMarks, btnNotices, btnTimetables,
            btnReports, btnAudit, btnSettings, btnProfile
        };

        for (JButton b : buttons) {
            if (b == null) continue;
            if (b == active) {
                b.setBackground(ModernTheme.BG_SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
                b.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, ModernTheme.ACCENT),
                    new EmptyBorder(6, 8, 6, 12)
                ));
            } else {
                b.setBackground(ModernTheme.NAVY);
                b.setForeground(new Color(203, 213, 225));
                b.setBorder(new EmptyBorder(6, 12, 6, 12));
            }
        }
    }

    private void switchCard(String name, JButton btn) {
        cardLayout.show(contentContainer, name);
        setActiveNav(btn);
    }

    @Override
    public void navigateTo(String tabKey) {
        switch (tabKey) {
            case "USERS":
                userPanel.loadUsers(0);
                switchCard("USERS", btnUsers);
                break;
            case "COURSES":
                coursePanel.loadCourses();
                switchCard("COURSES", btnCourses);
                break;
            case "STUDENTS":
                studentPanel.loadStudents();
                switchCard("STUDENTS", btnStudents);
                break;
            case "MARKS":
                switchCard("MARKS", btnMarks);
                break;
            case "NOTICES":
                noticePanel.loadNotices();
                switchCard("NOTICES", btnNotices);
                break;
            case "REPORTS":
                switchCard("REPORTS", btnReports);
                break;
        }
    }

    @Override
    public void triggerQuickAction(String actionKey) {
        switch (actionKey) {
            case "ADD_USER":
                switchCard("USERS", btnUsers);
                break;
            case "ADD_STUDENT":
                switchCard("STUDENTS", btnStudents);
                break;
            case "ADD_COURSE":
                switchCard("COURSES", btnCourses);
                break;
            case "PUBLISH_NOTICE":
                switchCard("NOTICES", btnNotices);
                break;
            case "GEN_REPORT":
                switchCard("REPORTS", btnReports);
                break;
        }
    }

    private JPanel createTopNavigationBar(User currentUser) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(1000, 64));
        header.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
            new EmptyBorder(10, 24, 10, 24)
        ));

        // Left: Greeting with Admin Name
        JPanel leftWelcome = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftWelcome.setOpaque(false);

        String adminName = currentUser != null ? currentUser.getFullName() : "Administrator";
        JLabel welcomeTitle = new JLabel("Welcome, " + adminName);
        welcomeTitle.setFont(ModernTheme.FONT_SUBTITLE);
        welcomeTitle.setForeground(ModernTheme.TEXT_DARK);

        JLabel welcomeSub = new JLabel("System Administrator • Faculty of Technology");
        welcomeSub.setFont(ModernTheme.FONT_SMALL);
        welcomeSub.setForeground(ModernTheme.TEXT_MUTED);

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);
        textStack.add(welcomeTitle);
        textStack.add(Box.createRigidArea(new Dimension(0, 2)));
        textStack.add(welcomeSub);

        leftWelcome.add(textStack);

        // Center / Right Controls
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightControls.setOpaque(false);

        // 1. Global Search Box
        JPanel searchBox = new JPanel(new BorderLayout(6, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(ModernTheme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(280, 36));
        searchBox.setBorder(new EmptyBorder(0, 10, 0, 10));

        JLabel searchIcon = new JLabel(ModernIcons.get("search", 15, ModernTheme.TEXT_MUTED));
        txtGlobalSearch = new JTextField();
        txtGlobalSearch.setFont(ModernTheme.FONT_REGULAR);
        txtGlobalSearch.setBorder(null);
        txtGlobalSearch.setOpaque(false);
        txtGlobalSearch.putClientProperty("JTextField.placeholderText", "Global search users, courses...");
        txtGlobalSearch.addActionListener(e -> performGlobalSearch());

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(txtGlobalSearch, BorderLayout.CENTER);

        // 2. Current Date Display
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy");
        String dateFormatted = sdf.format(new Date());
        JLabel dateBadge = new JLabel(dateFormatted, ModernIcons.get("calendar", 16, ModernTheme.PRIMARY), SwingConstants.LEFT);
        dateBadge.setFont(ModernTheme.FONT_SMALL);
        dateBadge.setForeground(ModernTheme.TEXT_SECONDARY);
        dateBadge.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));

        // 3. Notification Button with live badge
        JPanel notifContainer = new JPanel(null);
        notifContainer.setOpaque(false);
        notifContainer.setPreferredSize(new Dimension(42, 36));

        JButton btnNotif = new JButton(ModernIcons.get("notices", 19, ModernTheme.TEXT_DARK));
        btnNotif.setBounds(0, 0, 36, 36);
        btnNotif.setContentAreaFilled(false);
        btnNotif.setFocusPainted(false);
        btnNotif.setBorder(new LineBorder(ModernTheme.BORDER, 1, true));
        btnNotif.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotif.setToolTipText("Official Faculty Notifications");
        btnNotif.addActionListener(e -> NotificationDialog.showDialog(this, this::refreshNotificationBadge));

        lblNotificationBadge = new JLabel("0") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ModernTheme.DANGER);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblNotificationBadge.setFont(new Font(ModernTheme.getFontFamily(), Font.BOLD, 9));
        lblNotificationBadge.setForeground(Color.WHITE);
        lblNotificationBadge.setHorizontalAlignment(SwingConstants.CENTER);
        lblNotificationBadge.setBounds(20, 0, 16, 16);
        lblNotificationBadge.setVisible(false);

        notifContainer.add(lblNotificationBadge);
        notifContainer.add(btnNotif);

        // 4. Admin Profile Avatar & Quick Dropdown Button
        JButton btnProfileQuick = new JButton(ModernIcons.get("profile", 18, ModernTheme.PRIMARY));
        btnProfileQuick.setText("Profile");
        btnProfileQuick.setFont(ModernTheme.FONT_SMALL);
        btnProfileQuick.setForeground(ModernTheme.TEXT_DARK);
        btnProfileQuick.setBackground(Color.WHITE);
        btnProfileQuick.setFocusPainted(false);
        btnProfileQuick.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProfileQuick.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        btnProfileQuick.addActionListener(e -> switchCard("PROFILE", btnProfile));

        rightControls.add(searchBox);
        rightControls.add(dateBadge);
        rightControls.add(notifContainer);
        rightControls.add(btnProfileQuick);

        header.add(leftWelcome, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    private void performGlobalSearch() {
        String query = txtGlobalSearch.getText().trim();
        if (query.length() < 2) {
            JOptionPane.showMessageDialog(this, "Please enter at least 2 characters to search.", "Search Query", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<AdminAnalyticsDAO.SearchResult> results = analyticsDAO.searchGlobal(query);
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records matching '" + query + "' were found.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show modern search results dialog
        JDialog dialog = new JDialog(this, "Search Results for '" + query + "' (" + results.size() + " matches)", true);
        dialog.setSize(620, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        DefaultListModel<AdminAnalyticsDAO.SearchResult> listModel = new DefaultListModel<>();
        for (AdminAnalyticsDAO.SearchResult sr : results) {
            listModel.addElement(sr);
        }

        JList<AdminAnalyticsDAO.SearchResult> resultList = new JList<>(listModel);
        resultList.setFont(ModernTheme.FONT_REGULAR);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBorder(new EmptyBorder(8, 12, 8, 12));
                row.setBackground(isSelected ? ModernTheme.PRIMARY_LIGHT : Color.WHITE);

                if (value instanceof AdminAnalyticsDAO.SearchResult sr) {
                    JLabel catBadge = ModernTheme.createBadge(sr.category.toUpperCase(), ModernTheme.PRIMARY_LIGHT, ModernTheme.PRIMARY);
                    JLabel titleLbl = new JLabel(sr.title);
                    titleLbl.setFont(ModernTheme.FONT_BOLD);
                    titleLbl.setForeground(ModernTheme.TEXT_DARK);

                    JLabel subLbl = new JLabel(sr.subtitle);
                    subLbl.setFont(ModernTheme.FONT_SMALL);
                    subLbl.setForeground(ModernTheme.TEXT_MUTED);

                    JPanel textP = new JPanel();
                    textP.setLayout(new BoxLayout(textP, BoxLayout.Y_AXIS));
                    textP.setOpaque(false);
                    textP.add(titleLbl);
                    textP.add(subLbl);

                    row.add(catBadge, BorderLayout.WEST);
                    row.add(textP, BorderLayout.CENTER);
                }
                return row;
            }
        });

        JScrollPane scroll = new JScrollPane(resultList);
        scroll.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        JButton btnJump = ModernTheme.createPrimaryButton("Jump to Module");
        btnJump.addActionListener(e -> {
            AdminAnalyticsDAO.SearchResult selected = resultList.getSelectedValue();
            if (selected != null) {
                dialog.dispose();
                if ("User".equalsIgnoreCase(selected.category)) {
                    switchCard("USERS", btnUsers);
                } else if ("Course".equalsIgnoreCase(selected.category)) {
                    switchCard("COURSES", btnCourses);
                } else if ("Notice".equalsIgnoreCase(selected.category)) {
                    switchCard("NOTICES", btnNotices);
                }
            }
        });

        JButton btnClose = ModernTheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());

        btnBar.add(btnJump);
        btnBar.add(btnClose);
        panel.add(btnBar, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showNotificationsPopup() {
        List<Notice> notices = noticeDAO.getActiveNotices();
        JDialog dialog = new JDialog(this, "Active Faculty Announcements & Bulletins", true);
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(ModernTheme.BG_MAIN);

        if (notices.isEmpty()) {
            listContainer.add(ModernTheme.createEmptyState("No Active Notices", "All official bulletins have expired or been archived.", "notices"));
        } else {
            for (Notice n : notices) {
                JPanel card = ModernTheme.createCardPanel(8);
                card.setLayout(new BorderLayout(8, 6));

                JLabel titleLbl = new JLabel(n.getTitle(), ModernIcons.get("notices", 16, ModernTheme.PRIMARY), SwingConstants.LEFT);
                titleLbl.setFont(ModernTheme.FONT_BOLD);
                titleLbl.setForeground(ModernTheme.TEXT_DARK);

                JLabel dateLbl = new JLabel("Published: " + n.getPublishedDate() + " • Target: " + n.getTargetRole());
                dateLbl.setFont(ModernTheme.FONT_SMALL);
                dateLbl.setForeground(ModernTheme.TEXT_MUTED);

                JTextArea contentArea = new JTextArea(n.getContent());
                contentArea.setFont(ModernTheme.FONT_REGULAR);
                contentArea.setForeground(ModernTheme.TEXT_SECONDARY);
                contentArea.setLineWrap(true);
                contentArea.setWrapStyleWord(true);
                contentArea.setEditable(false);
                contentArea.setOpaque(false);

                card.add(titleLbl, BorderLayout.NORTH);
                card.add(contentArea, BorderLayout.CENTER);
                card.add(dateLbl, BorderLayout.SOUTH);

                listContainer.add(card);
                listContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnClose = ModernTheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        btm.add(btnClose);
        panel.add(btm, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    public void refreshNotificationBadge() {
        int unread = 0;
        try {
            User u = SessionManager.getCurrentUser();
            if (u != null) {
                unread = new NotificationDAO().getUnreadCount(u.getUserId(), u.getRoleName());
            }
        } catch (Exception ignored) {
        }
        if (lblNotificationBadge != null) {
            lblNotificationBadge.setText(String.valueOf(unread));
            lblNotificationBadge.setVisible(unread > 0);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out from the Administrative portal?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
