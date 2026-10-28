import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class LMSGuiApplication extends JFrame {

    // Database Configuration (from database/database.sql:
    // faculty_academic_management)
    private static String dbHost = "localhost";
    private static String dbPort = "3307";
    private static String dbName = "faculty_academic_management";
    private static String dbUser = "root";
    private static String dbPassword = ""; // Update with your MySQL password if configured

    // Current Logged-in User Information (Default: Technical Officer Mr. Saman
    // Kumara)
    private static int currentUserId = 7;
    private static String currentUsername = "to_saman";
    private static String currentFullName = "Mr. Saman Kumara";
    private static String currentStaffId = "TO/ICT/01";
    private static String currentRole = "Technical Officer";
    private static String currentDesignation = "Technical Officer Gr. I";
    private static String currentAssignedLab = "Computer Lab 01";
    private static String currentEmail = "saman.to@fot.ruh.ac.lk";
    private static String currentPhone = "0714567801";
    private static String currentAddress = "No. 22, Hakmana Road, Matara";
    private static String currentDepartment = "Department of Information and Communication Technology (ICT)";
    private static String currentAcademicYear = "2024/2025";
    private static String currentSemester = "Level II Semester I";
    private static String currentStaffType = "TECHNICAL STAFF";
    private static String currentStatus = "ACTIVE";

    // Card Layout for main module switching
    private JPanel cardContainer;
    private CardLayout cardLayout;
    private String activeSection = "HOME";
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    // Sidebar User Display Labels
    private JLabel sidebarUserNameLabel;
    private JLabel sidebarUserRoleLabel;

    // Tables and Models for each module
    private DefaultTableModel attendanceModel;
    private DefaultTableModel medicalModel;
    private DefaultTableModel noticeModel;
    private DefaultTableModel timetableModel;
    private DefaultTableModel dashboardMedicalModel;
    private JTable dashboardMedicalTable;

    // Dashboard Metric Labels
    private JLabel pendingMedicalsLabel;
    private JLabel approvedMedicalsLabel;
    private JLabel attendanceRateLabel;
    private JLabel activeNoticesLabel;
    private JLabel dashboardWelcomeTitle;
    private JLabel dashboardWelcomeSub;

    // Profile Detail Labels
    private JLabel profFullNameVal;
    private JLabel profStaffIdVal;
    private JLabel profEmailVal;
    private JLabel profPhoneVal;
    private JLabel profAddressVal;
    private JLabel profDeptVal;
    private JLabel profDesignationVal;
    private JLabel profAssignedLabVal;
    private JLabel profRoleVal;
    private JLabel profStatusVal;
    private JLabel profAvatarLabel;
    private JLabel profHeaderNameLabel;
    private JLabel profHeaderSubLabel;

    // Form Dropdowns
    private JComboBox<String> attCourseBox;
    private JComboBox<String> medCourseBox;
    private JComboBox<String> ttCourseBox;

    public LMSGuiApplication() {
        // Set window properties
        setTitle("Learning Management System (LMS) - Faculty Academic Management");
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Apply Native System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());

        // Top Toolbar (Header bar without any Database Disconnected message)
        JPanel topBar = createTopToolbar();
        add(topBar, BorderLayout.NORTH);

        // Main Workspace: Left Sidebar navigation + Center Card Content
        JPanel mainWorkspace = new JPanel(new BorderLayout());

        // Left Navigation Sidebar (Home, Profile, Attendance, Medical, Notices,
        // Timetable + Log Out down at bottom)
        JPanel sidebar = createSidebarPanel();
        mainWorkspace.add(sidebar, BorderLayout.WEST);

        // Center Content Container (CardLayout)
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        // Register cards in menu order:
        // 1. Home (Dashboard with Medical Approval Guidelines & Stats)
        // 2. Profile (Technical Officer Details, placed upper to Attendance)
        // 3. Attendance
        // 4. Medical Records
        // 5. Notices
        // 6. Timetable
        cardContainer.add(createHomePanel(), "HOME");
        cardContainer.add(createProfilePanel(), "PROFILE");
        cardContainer.add(createAttendancePanel(), "ATTENDANCE");
        cardContainer.add(createMedicalPanel(), "MEDICAL");
        cardContainer.add(createNoticesPanel(), "NOTICES");
        cardContainer.add(createTimetablePanel(), "TIMETABLE");

        mainWorkspace.add(cardContainer, BorderLayout.CENTER);
        add(mainWorkspace, BorderLayout.CENTER);

        // Highlight Home as initially active
        switchSection("HOME");

        // Fetch data from database or fallback to seed data
        refreshAllData();
    }

    // ==========================================
    // TOP TOOLBAR (Clean Header - No Disconnected Banner)
    // ==========================================
    private JPanel createTopToolbar() {
        JPanel bar = new JPanel(new BorderLayout(15, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        bar.setBackground(new Color(248, 250, 252));

        // System Title Header
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);

        JLabel brandIcon = new JLabel("🏛");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel titleLabel = new JLabel("Faculty of Technology • University of Ruhuna");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(30, 41, 59));

        JLabel subLabel = new JLabel("|  Academic Management System (LMS) - Staff Portal");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(100, 116, 139));

        titlePanel.add(brandIcon);
        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);

        // Right Action Controls
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refreshAllData());

        JButton configBtn = new JButton("DB Settings");
        configBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        configBtn.setFocusPainted(false);
        configBtn.addActionListener(e -> showDbConfigDialog());

        rightControls.add(refreshBtn);
        rightControls.add(configBtn);

        bar.add(titlePanel, BorderLayout.WEST);
        bar.add(rightControls, BorderLayout.EAST);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
        return bar;
    }

    private void showDbConfigDialog() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        JTextField hostField = new JTextField(dbHost);
        JTextField portField = new JTextField(dbPort);
        JTextField nameField = new JTextField(dbName);
        JTextField userField = new JTextField(dbUser);
        JPasswordField passField = new JPasswordField(dbPassword);

        panel.add(new JLabel("MySQL Host:"));
        panel.add(hostField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel("Database Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "MySQL Database Connection Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            dbHost = hostField.getText().trim();
            dbPort = portField.getText().trim();
            dbName = nameField.getText().trim();
            dbUser = userField.getText().trim();
            dbPassword = new String(passField.getPassword());
            refreshAllData();
            if (testDbConnection()) {
                JOptionPane.showMessageDialog(this, "Connected successfully to MySQL database (" + dbName + ")!",
                        "Database Connected", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not connect with provided settings. Operating in Offline Demo Mode.",
                        "Connection Notice", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // ==========================================
    // LEFT NAVIGATION SIDEBAR WITH LOGOUT BUTTON AT BOTTOM
    // ==========================================
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(new Color(24, 32, 47));

        // 1. Sidebar Top Header: Logo & User Info
        JPanel topHeader = new JPanel();
        topHeader.setLayout(new BoxLayout(topHeader, BoxLayout.Y_AXIS));
        topHeader.setOpaque(false);
        topHeader.setBorder(BorderFactory.createEmptyBorder(20, 16, 16, 16));

        JLabel logoLabel = new JLabel("FOT - LMS");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel portalSub = new JLabel("Technical Officer Portal");
        portalSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        portalSub.setForeground(new Color(148, 163, 184));
        portalSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        topHeader.add(logoLabel);
        topHeader.add(portalSub);
        topHeader.add(Box.createVerticalStrut(16));

        // User profile badge in sidebar (Technical Officer)
        JPanel userBadge = new JPanel(new BorderLayout(10, 0));
        userBadge.setBackground(new Color(33, 44, 63));
        userBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        userBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userIcon = new JLabel("🛠️");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JPanel userTextPanel = new JPanel();
        userTextPanel.setLayout(new BoxLayout(userTextPanel, BoxLayout.Y_AXIS));
        userTextPanel.setOpaque(false);

        sidebarUserNameLabel = new JLabel(currentFullName);
        sidebarUserNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sidebarUserNameLabel.setForeground(Color.WHITE);

        sidebarUserRoleLabel = new JLabel(currentRole + " (" + currentAssignedLab + ")");
        sidebarUserRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sidebarUserRoleLabel.setForeground(new Color(148, 163, 184));

        userTextPanel.add(sidebarUserNameLabel);
        userTextPanel.add(sidebarUserRoleLabel);

        userBadge.add(userIcon, BorderLayout.WEST);
        userBadge.add(userTextPanel, BorderLayout.CENTER);

        topHeader.add(userBadge);

        sidebar.add(topHeader, BorderLayout.NORTH);

        // 2. Sidebar Navigation Items in Center
        // ORDER REQUIRED:
        // - Home (Dashboard)
        // - Profile (upper to Attendance)
        // - Attendance
        // - Medical Records
        // - Notices
        // - Timetable
        JPanel navMenu = new JPanel();
        navMenu.setLayout(new BoxLayout(navMenu, BoxLayout.Y_AXIS));
        navMenu.setOpaque(false);
        navMenu.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        navMenu.add(createNavButton("Home", "HOME", "🏠"));
        navMenu.add(Box.createVerticalStrut(4));
        navMenu.add(createNavButton("Profile", "PROFILE", "👤")); // Placed upper to Attendance
        navMenu.add(Box.createVerticalStrut(4));
        navMenu.add(createNavButton("Attendance", "ATTENDANCE", "📋"));
        navMenu.add(Box.createVerticalStrut(4));
        navMenu.add(createNavButton("Medical Records", "MEDICAL", "🏥"));
        navMenu.add(Box.createVerticalStrut(4));
        navMenu.add(createNavButton("Notices", "NOTICES", "📢"));
        navMenu.add(Box.createVerticalStrut(4));
        navMenu.add(createNavButton("Timetable", "TIMETABLE", "📅"));

        sidebar.add(navMenu, BorderLayout.CENTER);

        // 3. Sidebar Bottom Panel: Log Out Button placed down to the selection menu
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)),
                new EmptyBorder(14, 14, 18, 14)));

        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutBtn.setForeground(new Color(248, 113, 113));
        logoutBtn.setBackground(new Color(45, 26, 32));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(239, 68, 68), 1, true),
                new EmptyBorder(10, 14, 10, 14)));

        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(new Color(220, 38, 38));
                logoutBtn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(new Color(45, 26, 32));
                logoutBtn.setForeground(new Color(248, 113, 113));
            }
        });

        logoutBtn.addActionListener(e -> performLogout());

        bottomPanel.add(logoutBtn, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String label, String cardName, String icon) {
        JButton btn = new JButton(icon + "   " + label);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(24, 32, 47));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!cardName.equals(activeSection)) {
                    btn.setBackground(new Color(36, 48, 70));
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!cardName.equals(activeSection)) {
                    btn.setBackground(new Color(24, 32, 47));
                    btn.setForeground(new Color(203, 213, 225));
                }
            }
        });

        btn.addActionListener(e -> switchSection(cardName));
        navButtons.put(cardName, btn);
        return btn;
    }

    public void switchSection(String cardName) {
        activeSection = cardName;
        cardLayout.show(cardContainer, cardName);

        // Highlight active nav button
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton button = entry.getValue();
            if (entry.getKey().equals(cardName)) {
                button.setBackground(new Color(37, 99, 235)); // Vibrant blue
                button.setForeground(Color.WHITE);
                button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                button.setBackground(new Color(24, 32, 47));
                button.setForeground(new Color(203, 213, 225));
                button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        }

        // Refresh stats if switching to Home
        if ("HOME".equals(cardName)) {
            updateDashboardData();
        }
    }

    // ==========================================
    // LOG OUT FUNCTIONALITY
    // ==========================================
    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of the Technical Officer portal?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            showLoginDialog();
        }
    }

    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "LMS Portal - Sign In", true);
        loginDialog.setSize(440, 370);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 25, 20, 25));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("Faculty Academic Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(30, 41, 59));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in with staff/student credentials or select quick login");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(title);
        content.add(subtitle);
        content.add(Box.createVerticalStrut(18));

        JTextField usernameField = new JTextField(currentUsername);
        JPasswordField passwordField = new JPasswordField("to123");

        JPanel formGrid = new JPanel(new GridLayout(2, 2, 8, 10));
        formGrid.setOpaque(false);
        formGrid.add(new JLabel("Username / ID:"));
        formGrid.add(usernameField);
        formGrid.add(new JLabel("Password:"));
        formGrid.add(passwordField);

        content.add(formGrid);
        content.add(Box.createVerticalStrut(14));

        JLabel quickLabel = new JLabel("Quick Sign-in Options:");
        quickLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        quickLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(quickLabel);
        content.add(Box.createVerticalStrut(6));

        JPanel quickPanel = new JPanel(new GridLayout(2, 2, 6, 6));
        quickPanel.setOpaque(false);

        JButton qTechOfficer = new JButton("Technical Officer (Saman)");
        JButton qStudent = new JButton("Student (Dilshan)");
        JButton qLecturer = new JButton("Lecturer (Dr. Kamal)");
        JButton qAdmin = new JButton("System Admin");

        qTechOfficer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        qTechOfficer.setBackground(new Color(239, 246, 255));
        qStudent.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        qLecturer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        qAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        qTechOfficer.addActionListener(e -> {
            usernameField.setText("to_saman");
            passwordField.setText("to123");
        });
        qStudent.addActionListener(e -> {
            usernameField.setText("tg20242006");
            passwordField.setText("student123");
        });
        qLecturer.addActionListener(e -> {
            usernameField.setText("lec_kamal");
            passwordField.setText("lec123");
        });
        qAdmin.addActionListener(e -> {
            usernameField.setText("admin");
            passwordField.setText("admin123");
        });

        quickPanel.add(qTechOfficer);
        quickPanel.add(qStudent);
        quickPanel.add(qLecturer);
        quickPanel.add(qAdmin);
        content.add(quickPanel);

        loginDialog.add(content, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton signInBtn = new JButton("Sign In");
        signInBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        signInBtn.setBackground(new Color(37, 99, 235));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFocusPainted(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.setFocusPainted(false);

        signInBtn.addActionListener(e -> {
            String u = usernameField.getText().trim();
            handleUserLogin(u);
            loginDialog.dispose();
            switchSection("HOME");
            JOptionPane.showMessageDialog(this,
                    "Logged in successfully as " + currentFullName + " (" + currentRole + ")!",
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);
        });

        cancelBtn.addActionListener(e -> loginDialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(signInBtn);
        loginDialog.add(btnPanel, BorderLayout.SOUTH);

        loginDialog.setVisible(true);
    }

    private void handleUserLogin(String username) {
        if ("to_saman".equalsIgnoreCase(username) || username.toLowerCase().startsWith("to")) {
            setCurrentUser(7, "to_saman", "Mr. Saman Kumara", "TO/ICT/01", "Technical Officer",
                    "Technical Officer Gr. I", "Computer Lab 01",
                    "saman.to@fot.ruh.ac.lk", "0714567801", "No. 22, Hakmana Road, Matara",
                    "Department of Information and Communication Technology (ICT)", "2024/2025", "Level II Semester I",
                    "TECHNICAL STAFF", "ACTIVE");
        } else if ("tg20242006".equalsIgnoreCase(username)) {
            setCurrentUser(16, "tg20242006", "Dilshan Madushanka", "TG/2024/2006", "Undergraduate",
                    "Undergraduate Student", "Student Desk",
                    "dilshan@fot.ruh.ac.lk", "0761112006", "No. 55, Beach Road, Tangalle",
                    "Department of Information and Communication Technology (ICT)", "2024/2025", "Level II Semester I",
                    "REGULAR", "ACTIVE");
        } else if ("lec_kamal".equalsIgnoreCase(username)) {
            setCurrentUser(2, "lec_kamal", "Dr. Kamal Perera", "STAFF/ICT/02", "Lecturer",
                    "Senior Lecturer Gr. I", "FOT-ICT-301",
                    "kamal@ict.ruh.ac.lk", "0771234501", "No. 12, Galle Road, Matara",
                    "Department of Information and Communication Technology (ICT)", "2024/2025", "Level II Semester I",
                    "ACADEMIC STAFF", "ACTIVE");
        } else if ("admin".equalsIgnoreCase(username)) {
            setCurrentUser(1, "admin", "System Administrator", "ADMIN/01", "Admin",
                    "Senior Assistant Registrar", "Dean's Office",
                    "admin@fot.ruh.ac.lk", "0412223344", "Faculty of Technology, University of Ruhuna",
                    "Dean's Office / ICT Department", "2024/2025", "All Semesters",
                    "ADMINISTRATOR", "ACTIVE");
        } else {
            // Default to Technical Officer
            setCurrentUser(7, "to_saman", "Mr. Saman Kumara", "TO/ICT/01", "Technical Officer",
                    "Technical Officer Gr. I", "Computer Lab 01",
                    "saman.to@fot.ruh.ac.lk", "0714567801", "No. 22, Hakmana Road, Matara",
                    "Department of Information and Communication Technology (ICT)", "2024/2025", "Level II Semester I",
                    "TECHNICAL STAFF", "ACTIVE");
        }
    }

    private void setCurrentUser(int id, String uname, String fname, String staffId, String role,
            String desig, String lab, String email, String phone, String addr, String dept, String year, String sem,
            String staffType, String status) {
        currentUserId = id;
        currentUsername = uname;
        currentFullName = fname;
        currentStaffId = staffId;
        currentRole = role;
        currentDesignation = desig;
        currentAssignedLab = lab;
        currentEmail = email;
        currentPhone = phone;
        currentAddress = addr;
        currentDepartment = dept;
        currentAcademicYear = year;
        currentSemester = sem;
        currentStaffType = staffType;
        currentStatus = status;

        // Update sidebar
        if (sidebarUserNameLabel != null) {
            sidebarUserNameLabel.setText(currentFullName);
            sidebarUserRoleLabel.setText(currentRole + " (" + currentAssignedLab + ")");
        }

        // Update Profile panel values
        updateProfileDisplay();
        updateDashboardData();
    }

    // ==========================================
    // 1. HOME PANEL (Dashboard with Medical Approval Guidelines & Stats)
    // ==========================================
    private JPanel createHomePanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBorder(new EmptyBorder(16, 20, 20, 20));
        scrollContent.setBackground(new Color(245, 247, 250));

        // 1.1 Welcome Hero Card (Tailored for Technical Officer)
        JPanel welcomeHero = new JPanel(new BorderLayout(15, 8));
        welcomeHero.setBackground(Color.WHITE);
        welcomeHero.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(18, 20, 18, 20)));
        welcomeHero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        JPanel leftHero = new JPanel();
        leftHero.setLayout(new BoxLayout(leftHero, BoxLayout.Y_AXIS));
        leftHero.setOpaque(false);

        dashboardWelcomeTitle = new JLabel("Welcome back, " + currentFullName + " (" + currentRole + ")");
        dashboardWelcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dashboardWelcomeTitle.setForeground(new Color(30, 41, 59));

        dashboardWelcomeSub = new JLabel(
                "Technical Officer Portal • " + currentAssignedLab + " • Attendance & Medical Verification Center");
        dashboardWelcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dashboardWelcomeSub.setForeground(new Color(100, 116, 139));

        leftHero.add(dashboardWelcomeTitle);
        leftHero.add(Box.createVerticalStrut(4));
        leftHero.add(dashboardWelcomeSub);

        welcomeHero.add(leftHero, BorderLayout.WEST);
        scrollContent.add(welcomeHero);
        scrollContent.add(Box.createVerticalStrut(16));

        // 1.2 Metric Cards Grid (Essential Summary for Technical Officer)
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        pendingMedicalsLabel = new JLabel("1");
        approvedMedicalsLabel = new JLabel("3");
        attendanceRateLabel = new JLabel("80.0%");
        activeNoticesLabel = new JLabel("5");

        statsGrid.add(createMetricCard("Medicals to Verify & Approve", pendingMedicalsLabel, "Awaiting TO Action",
                new Color(245, 158, 11), new Color(254, 243, 199)));
        statsGrid.add(createMetricCard("Approved Medicals", approvedMedicalsLabel, "Absences Excused",
                new Color(16, 185, 129), new Color(209, 250, 229)));
        statsGrid.add(createMetricCard("Overall Attendance", attendanceRateLabel, "Student Exam Requirement >= 80%",
                new Color(37, 99, 235), new Color(219, 234, 254)));
        statsGrid.add(createMetricCard("Active Notices", activeNoticesLabel, "Announcements",
                new Color(139, 92, 246), new Color(243, 232, 255)));

        scrollContent.add(statsGrid);
        scrollContent.add(Box.createVerticalStrut(18));

        // 1.3 ESSENTIAL SECTION: HOW MEDICAL HAVE TO APPROVE (Official Guidelines)
        JPanel approvalGuideCard = createMedicalApprovalGuideCard();
        scrollContent.add(approvalGuideCard);
        scrollContent.add(Box.createVerticalStrut(18));

        // 1.4 Medical Records Approval Action Center Table
        JPanel tableCard = createDashboardMedicalTableCard();
        scrollContent.add(tableCard);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        return root;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, Color accentColor,
            Color bgTint) {
        JPanel card = new JPanel(new BorderLayout(8, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(new Color(148, 163, 184));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createMedicalApprovalGuideCard() {
        JPanel card = new JPanel(new BorderLayout(10, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 20, 18, 20)));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("📋 How Medical Certificates Must Be Approved (Technical Officer Guidelines)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(30, 41, 59));

        JLabel subtitle = new JLabel(
                "Official procedure and criteria for Technical Officers to verify student medicals and approve excused attendance");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        card.add(header, BorderLayout.NORTH);

        // Grid of 6 procedural guidelines
        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setOpaque(false);

        grid.add(createGuideBox("1. 7-Day Submission Window",
                "Students must submit the original medical certificate to the Technical Officer within 7 calendar days of returning to academic activities.",
                new Color(37, 99, 235), new Color(239, 246, 255)));

        grid.add(createGuideBox("2. Authorized Medical Officer",
                "Must be issued by a Government Hospital/DMO or University Medical Officer (UMO). Private chits must be endorsed by UMO.",
                new Color(16, 185, 129), new Color(236, 253, 245)));

        grid.add(createGuideBox("3. Verification by Tech Officer",
                "Technical Officer verifies the submission date against lecture dates, checks session logs, and marks status for review.",
                new Color(245, 158, 11), new Color(254, 243, 199)));

        grid.add(createGuideBox("4. Approval by Coordinator / Head",
                "Head of Department or Course Coordinator reviews reasons and grants APPROVED status in the academic system.",
                new Color(139, 92, 246), new Color(245, 243, 255)));

        grid.add(createGuideBox("5. 80% Attendance Excusal",
                "Once approved, absent hours are credited as medical excused, protecting student eligibility for final examinations.",
                new Color(14, 165, 233), new Color(240, 249, 255)));

        grid.add(createGuideBox("6. Late Submissions Policy",
                "Certificates submitted after 7 days without prior Dean's approval remain NOT_APPROVED and count as unexcused absences.",
                new Color(239, 68, 68), new Color(254, 242, 242)));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createGuideBox(String heading, String text, Color accentColor, Color bg) {
        JPanel box = new JPanel(new BorderLayout(6, 6));
        box.setBackground(bg);
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 80), 1,
                        true),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel headLabel = new JLabel(heading);
        headLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headLabel.setForeground(accentColor);

        JTextArea descArea = new JTextArea(text);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descArea.setForeground(new Color(51, 65, 85));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);

        box.add(headLabel, BorderLayout.NORTH);
        box.add(descArea, BorderLayout.CENTER);
        return box;
    }

    private JPanel createDashboardMedicalTableCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        // Header with title and Action Buttons
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("🏥 Medical Records & Approval Status Management (Technical Officer Action Center)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(30, 41, 59));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton approveBtn = new JButton("Verify & Approve");
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        approveBtn.setBackground(new Color(16, 185, 129));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFocusPainted(false);

        JButton rejectBtn = new JButton("Set Not Approved");
        rejectBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rejectBtn.setFocusPainted(false);

        JButton openMedTabBtn = new JButton("Go to Medical Form →");
        openMedTabBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        openMedTabBtn.setFocusPainted(false);

        actions.add(approveBtn);
        actions.add(rejectBtn);
        actions.add(openMedTabBtn);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Reg No", "Student Name", "Course", "Session Date", "Submission Date", "Reason",
                "Status" };
        dashboardMedicalModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dashboardMedicalTable = new JTable(dashboardMedicalModel);
        styleTable(dashboardMedicalTable);

        // Highlight Status Column
        dashboardMedicalTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = value != null ? value.toString() : "";
                setHorizontalAlignment(SwingConstants.CENTER);
                if ("APPROVED".equalsIgnoreCase(val)) {
                    setForeground(new Color(16, 185, 129));
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    setForeground(new Color(239, 68, 68));
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(dashboardMedicalTable);
        tableScroll.setPreferredSize(new Dimension(800, 180));
        card.add(tableScroll, BorderLayout.CENTER);

        // Button Listeners for Approval
        approveBtn.addActionListener(e -> {
            int selectedRow = dashboardMedicalTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a medical record from the table to approve.",
                        "No Record Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int medId = (int) dashboardMedicalModel.getValueAt(selectedRow, 0);
            updateMedicalApprovalStatus(medId, "APPROVED");
        });

        rejectBtn.addActionListener(e -> {
            int selectedRow = dashboardMedicalTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a medical record from the table.",
                        "No Record Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int medId = (int) dashboardMedicalModel.getValueAt(selectedRow, 0);
            updateMedicalApprovalStatus(medId, "NOT_APPROVED");
        });

        openMedTabBtn.addActionListener(e -> switchSection("MEDICAL"));

        return card;
    }

    private void updateMedicalApprovalStatus(int medicalId, String newStatus) {
        boolean dbUpdated = false;
        String sql = "UPDATE medical SET status = ?, reviewed_by = ?, reviewed_at = NOW() WHERE medical_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, currentUserId);
            dbUpdated = stmt.executeUpdate() > 0;
        } catch (SQLException ignored) {
        }

        // Update in-memory models (both dashboard table and main medical module table)
        for (int i = 0; i < dashboardMedicalModel.getRowCount(); i++) {
            if ((int) dashboardMedicalModel.getValueAt(i, 0) == medicalId) {
                dashboardMedicalModel.setValueAt(newStatus, i, 7);
                break;
            }
        }
        for (int i = 0; i < medicalModel.getRowCount(); i++) {
            if ((int) medicalModel.getValueAt(i, 0) == medicalId) {
                medicalModel.setValueAt(newStatus, i, 8);
                break;
            }
        }

        updateDashboardData();
        JOptionPane.showMessageDialog(this,
                "Medical record #" + medicalId + " status updated to " + newStatus + " by " + currentFullName + "!"
                        + (dbUpdated ? " (Saved to Database)" : " (Offline Mode Updated)"),
                "Approval Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateDashboardData() {
        int pending = 0;
        int approved = 0;

        if (dashboardMedicalModel != null) {
            dashboardMedicalModel.setRowCount(0);
            for (int i = 0; i < medicalModel.getRowCount(); i++) {
                int id = (int) medicalModel.getValueAt(i, 0);
                String reg = (String) medicalModel.getValueAt(i, 1);
                String name = (String) medicalModel.getValueAt(i, 2);
                String course = (String) medicalModel.getValueAt(i, 3);
                String sDate = String.valueOf(medicalModel.getValueAt(i, 5));
                String subDate = String.valueOf(medicalModel.getValueAt(i, 6));
                String reason = (String) medicalModel.getValueAt(i, 7);
                String status = (String) medicalModel.getValueAt(i, 8);

                dashboardMedicalModel.addRow(new Object[] { id, reg, name, course, sDate, subDate, reason, status });

                if ("APPROVED".equalsIgnoreCase(status)) {
                    approved++;
                } else {
                    pending++;
                }
            }
        }

        if (pendingMedicalsLabel != null) {
            pendingMedicalsLabel.setText(String.valueOf(pending));
        }
        if (approvedMedicalsLabel != null) {
            approvedMedicalsLabel.setText(String.valueOf(approved));
        }

        // Calculate attendance rate from attendanceModel
        if (attendanceRateLabel != null && attendanceModel != null) {
            int total = attendanceModel.getRowCount();
            int present = 0;
            for (int i = 0; i < total; i++) {
                String st = String.valueOf(attendanceModel.getValueAt(i, 7));
                if ("PRESENT".equalsIgnoreCase(st)) {
                    present++;
                }
            }
            if (total > 0) {
                double pct = ((double) present / total) * 100.0;
                attendanceRateLabel.setText(String.format("%.1f%%", pct));
            } else {
                attendanceRateLabel.setText("80.0%");
            }
        }

        if (activeNoticesLabel != null && noticeModel != null) {
            activeNoticesLabel.setText(String.valueOf(noticeModel.getRowCount()));
        }

        if (dashboardWelcomeTitle != null) {
            dashboardWelcomeTitle.setText("Welcome back, " + currentFullName + " (" + currentRole + ")");
        }
        if (dashboardWelcomeSub != null) {
            dashboardWelcomeSub.setText(
                    "Technical Officer Portal • " + currentAssignedLab + " • Attendance & Medical Verification Center");
        }
    }

    // ==========================================
    // 2. PROFILE PANEL (Technical Officer Details Front Page)
    // ==========================================
    private JPanel createProfilePanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBorder(new EmptyBorder(16, 24, 24, 24));
        scrollContent.setBackground(new Color(245, 247, 250));

        // 2.1 Identity Header Card (Technical Officer Badge)
        JPanel headerCard = new JPanel(new BorderLayout(20, 0));
        headerCard.setBackground(Color.WHITE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(18, 20, 18, 20)));
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        profAvatarLabel = new JLabel("🛠️");
        profAvatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));

        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setOpaque(false);

        profHeaderNameLabel = new JLabel(currentFullName);
        profHeaderNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        profHeaderNameLabel.setForeground(new Color(30, 41, 59));

        profHeaderSubLabel = new JLabel(
                currentRole + "  •  Staff ID: " + currentStaffId + "  •  " + currentAssignedLab);
        profHeaderSubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        profHeaderSubLabel.setForeground(new Color(100, 116, 139));

        nameBlock.add(profHeaderNameLabel);
        nameBlock.add(Box.createVerticalStrut(4));
        nameBlock.add(profHeaderSubLabel);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        badges.setOpaque(false);

        JLabel statusBadge = new JLabel("  " + currentStatus + "  ");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusBadge.setOpaque(true);
        statusBadge.setBackground(new Color(220, 252, 231));
        statusBadge.setForeground(new Color(22, 101, 52));
        statusBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel roleBadge = new JLabel("  TECHNICAL OFFICER  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setOpaque(true);
        roleBadge.setBackground(new Color(239, 246, 255));
        roleBadge.setForeground(new Color(29, 78, 216));
        roleBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        badges.add(roleBadge);
        badges.add(statusBadge);

        headerCard.add(profAvatarLabel, BorderLayout.WEST);
        headerCard.add(nameBlock, BorderLayout.CENTER);
        headerCard.add(badges, BorderLayout.EAST);

        scrollContent.add(headerCard);
        scrollContent.add(Box.createVerticalStrut(16));

        // 2.2 Two Column Details Section (Technical Staff Position & Contact Details)
        JPanel detailsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        detailsRow.setOpaque(false);

        // Technical Staff Position Card
        JPanel academicCard = new JPanel(new BorderLayout(10, 10));
        academicCard.setBackground(Color.WHITE);
        academicCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel acadTitle = new JLabel("🛠 Technical Staff Position & Responsibilities");
        acadTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        acadTitle.setForeground(new Color(30, 41, 59));
        academicCard.add(acadTitle, BorderLayout.NORTH);

        JPanel acadGrid = new JPanel(new GridLayout(6, 2, 8, 10));
        acadGrid.setOpaque(false);

        profStaffIdVal = new JLabel(currentStaffId);
        profDeptVal = new JLabel(currentDepartment);
        profDesignationVal = new JLabel(currentDesignation);
        profAssignedLabVal = new JLabel(currentAssignedLab);
        profRoleVal = new JLabel(currentRole);
        profStatusVal = new JLabel(currentStatus);

        addProfileField(acadGrid, "Staff / Member ID:", profStaffIdVal);
        addProfileField(acadGrid, "Designation:", profDesignationVal);
        addProfileField(acadGrid, "Faculty / Dept:", profDeptVal);
        addProfileField(acadGrid, "Assigned Lab:", profAssignedLabVal);
        addProfileField(acadGrid, "System Role:", profRoleVal);
        addProfileField(acadGrid, "Approval Authority:", new JLabel("Authorized (Attendance & Medicals)"));

        academicCard.add(acadGrid, BorderLayout.CENTER);
        detailsRow.add(academicCard);

        // Contact & Account Card
        JPanel contactCard = new JPanel(new BorderLayout(10, 10));
        contactCard.setBackground(Color.WHITE);
        contactCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel contactTitle = new JLabel("👤 Personal & Contact Details");
        contactTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contactTitle.setForeground(new Color(30, 41, 59));
        contactCard.add(contactTitle, BorderLayout.NORTH);

        JPanel contactGrid = new JPanel(new GridLayout(6, 2, 8, 10));
        contactGrid.setOpaque(false);

        profFullNameVal = new JLabel(currentFullName);
        profEmailVal = new JLabel(currentEmail);
        profPhoneVal = new JLabel(currentPhone);
        profAddressVal = new JLabel(currentAddress);

        addProfileField(contactGrid, "Full Name:", profFullNameVal);
        addProfileField(contactGrid, "Official Email:", profEmailVal);
        addProfileField(contactGrid, "Contact Phone:", profPhoneVal);
        addProfileField(contactGrid, "Office / Lab:", new JLabel("FOT-ICT-Lab01 (Ground Floor)"));
        addProfileField(contactGrid, "Permanent Address:", profAddressVal);
        addProfileField(contactGrid, "System Username:", new JLabel(currentUsername));

        contactCard.add(contactGrid, BorderLayout.CENTER);
        detailsRow.add(contactCard);

        scrollContent.add(detailsRow);
        scrollContent.add(Box.createVerticalStrut(16));

        // 2.3 Profile Actions & Tools
        JPanel actionsCard = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionsCard.setBackground(Color.WHITE);
        actionsCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 16, 10, 16)));
        actionsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JButton editContactBtn = new JButton("Edit Contact Info");
        editContactBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        editContactBtn.setFocusPainted(false);
        editContactBtn.addActionListener(e -> showEditContactDialog());

        JButton viewHomeBtn = new JButton("Go to Home Dashboard →");
        viewHomeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewHomeBtn.setBackground(new Color(37, 99, 235));
        viewHomeBtn.setForeground(Color.WHITE);
        viewHomeBtn.setFocusPainted(false);
        viewHomeBtn.addActionListener(e -> switchSection("HOME"));

        actionsCard.add(editContactBtn);
        actionsCard.add(viewHomeBtn);

        scrollContent.add(actionsCard);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        return root;
    }

    private void addProfileField(JPanel grid, String labelText, JLabel valueComponent) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 116, 139));

        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueComponent.setForeground(new Color(30, 41, 59));

        grid.add(lbl);
        grid.add(valueComponent);
    }

    private void updateProfileDisplay() {
        if (profFullNameVal != null)
            profFullNameVal.setText(currentFullName);
        if (profStaffIdVal != null)
            profStaffIdVal.setText(currentStaffId);
        if (profEmailVal != null)
            profEmailVal.setText(currentEmail);
        if (profPhoneVal != null)
            profPhoneVal.setText(currentPhone);
        if (profAddressVal != null)
            profAddressVal.setText(currentAddress);
        if (profDeptVal != null)
            profDeptVal.setText(currentDepartment);
        if (profDesignationVal != null)
            profDesignationVal.setText(currentDesignation);
        if (profAssignedLabVal != null)
            profAssignedLabVal.setText(currentAssignedLab);
        if (profRoleVal != null)
            profRoleVal.setText(currentRole);
        if (profHeaderNameLabel != null)
            profHeaderNameLabel.setText(currentFullName);
        if (profHeaderSubLabel != null)
            profHeaderSubLabel.setText(currentRole + "  •  Staff ID: " + currentStaffId + "  •  " + currentAssignedLab);
        if (profAvatarLabel != null) {
            profAvatarLabel.setText("Technical Officer".equalsIgnoreCase(currentRole) ? "🛠️" : "👤");
        }
    }

    private void showEditContactDialog() {
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 10));
        JTextField phoneField = new JTextField(currentPhone);
        JTextField addrField = new JTextField(currentAddress);

        form.add(new JLabel("Contact Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Permanent Address:"));
        form.add(addrField);

        int result = JOptionPane.showConfirmDialog(this, form, "Update Contact Information",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newPhone = phoneField.getText().trim();
            String newAddr = addrField.getText().trim();
            if (!newPhone.isEmpty()) {
                currentPhone = newPhone;
                currentAddress = newAddr;
                updateProfileDisplay();

                // Save to database if connected
                try (Connection conn = getConnection();
                        PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE users SET phone = ?, address = ? WHERE user_id = ?")) {
                    stmt.setString(1, newPhone);
                    stmt.setString(2, newAddr);
                    stmt.setInt(3, currentUserId);
                    stmt.executeUpdate();
                } catch (SQLException ignored) {
                }

                JOptionPane.showMessageDialog(this, "Contact details updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // ==========================================
    // DATABASE CONNECTION & STATUS
    // ==========================================
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
        }
        return DriverManager.getConnection(url, dbUser, dbPassword);
    }

    private boolean testDbConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // 3. ATTENDANCE PANEL (Table: attendance JOIN users, courses)
    // ==========================================
    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Reg No", "Student Name", "Course", "Date", "Session Type", "Session #", "Status" };
        attendanceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(attendanceModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Form Panel (Technical Officer recording student attendance)
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 6, 6));
        formPanel.setBorder(BorderFactory
                .createTitledBorder("Record Student Attendance (Technical Officer: " + currentFullName + ")"));

        JTextField regNoField = new JTextField("TG/2024/2001");
        JTextField nameField = new JTextField("M.D.R.L. Nonis");
        attCourseBox = new JComboBox<>(new String[] { "ICT2132", "ICT2113", "ICT2122", "ICT2143", "ENG2112" });
        JTextField dateField = new JTextField("2026-10-18");
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL" });
        JComboBox<String> statusBox = new JComboBox<>(new String[] { "PRESENT", "ABSENT" });
        JButton addButton = new JButton("Add Record");

        formPanel.add(new JLabel("Student Reg No:"));
        formPanel.add(regNoField);
        formPanel.add(new JLabel("Student Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Course Code:"));
        formPanel.add(attCourseBox);

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Type / Status:"));

        JPanel comboGroup = new JPanel(new GridLayout(1, 2, 4, 0));
        comboGroup.add(typeBox);
        comboGroup.add(statusBox);
        formPanel.add(comboGroup);

        formPanel.add(addButton);

        addButton.addActionListener(e -> {
            String regNo = regNoField.getText().trim();
            String name = nameField.getText().trim();
            String course = (String) attCourseBox.getSelectedItem();
            String date = dateField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();

            if (regNo.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Student Reg No and Date.", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean savedToDb = saveAttendanceToDb(regNo, course, date, type, status);
            if (!savedToDb) {
                int nextId = attendanceModel.getRowCount() + 1;
                attendanceModel.addRow(new Object[] { nextId, regNo, name, course, date, type, 1, status });
            } else {
                loadAttendanceData();
            }
            updateDashboardData();
            JOptionPane.showMessageDialog(this, "Attendance recorded successfully by Technical Officer!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private boolean saveAttendanceToDb(String regNo, String courseCode, String date, String type, String status) {
        String sql = "INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by) "
                + "VALUES ((SELECT user_id FROM users WHERE student_reg_no = ? LIMIT 1), "
                + "(SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, "
                + "(SELECT COALESCE(MAX(session_number), 0) + 1 FROM attendance a2 WHERE a2.course_id = (SELECT course_id FROM courses WHERE course_code = ? LIMIT 1) AND a2.session_type = ?), "
                + "?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regNo);
            stmt.setString(2, courseCode);
            stmt.setString(3, date);
            stmt.setString(4, type);
            stmt.setString(5, courseCode);
            stmt.setString(6, type);
            stmt.setString(7, status);
            stmt.setInt(8, currentUserId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // 4. MEDICAL PANEL (Table: medical JOIN users, courses)
    // ==========================================
    private JPanel createMedicalPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Reg No", "Student Name", "Course", "Session Type", "Session Date",
                "Submission Date", "Reason", "Status" };
        medicalModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(medicalModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(2, 6, 6, 6));
        formPanel.setBorder(
                BorderFactory.createTitledBorder("Log Student Medical Submission (Technical Officer Verification)"));

        JTextField regNoField = new JTextField("TG/2024/2006");
        JTextField nameField = new JTextField("Dilshan Madushanka");
        medCourseBox = new JComboBox<>(new String[] { "ICT2132", "ICT2113", "ICT2122", "ICT2143" });
        JComboBox<String> sessionTypeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL", "BOTH" });
        JTextField sessionDateField = new JTextField("2026-10-10");
        JTextField subDateField = new JTextField("2026-10-12");
        JTextField reasonField = new JTextField("Viral fever - rest advised");
        JButton addButton = new JButton("Submit Medical");

        formPanel.add(new JLabel("Student Reg No:"));
        formPanel.add(regNoField);
        formPanel.add(new JLabel("Student Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Course:"));
        formPanel.add(medCourseBox);

        formPanel.add(new JLabel("Session Date / Type:"));
        JPanel dateTypeGroup = new JPanel(new GridLayout(1, 2, 4, 0));
        dateTypeGroup.add(sessionDateField);
        dateTypeGroup.add(sessionTypeBox);
        formPanel.add(dateTypeGroup);

        formPanel.add(new JLabel("Reason:"));
        formPanel.add(reasonField);
        formPanel.add(addButton);

        addButton.addActionListener(e -> {
            String regNo = regNoField.getText().trim();
            String name = nameField.getText().trim();
            String course = (String) medCourseBox.getSelectedItem();
            String sType = (String) sessionTypeBox.getSelectedItem();
            String sDate = sessionDateField.getText().trim();
            String subDate = subDateField.getText().trim();
            String reason = reasonField.getText().trim();

            if (regNo.isEmpty() || reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Reg No and Reason.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean savedToDb = saveMedicalToDb(regNo, course, sType, sDate, subDate, reason);
            if (!savedToDb) {
                int nextId = medicalModel.getRowCount() + 1;
                medicalModel.addRow(
                        new Object[] { nextId, regNo, name, course, sType, sDate, subDate, reason, "NOT_APPROVED" });
            } else {
                loadMedicalData();
            }
            updateDashboardData();
            JOptionPane.showMessageDialog(this, "Medical record logged by Technical Officer (Pending Review)!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private boolean saveMedicalToDb(String regNo, String courseCode, String sType, String sDate, String subDate,
            String reason) {
        String sql = "INSERT INTO medical (student_id, course_id, session_type, session_date, submission_date, reason, status) "
                + "VALUES ((SELECT user_id FROM users WHERE student_reg_no = ? LIMIT 1), "
                + "(SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, ?, ?, 'NOT_APPROVED')";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regNo);
            stmt.setString(2, courseCode);
            stmt.setString(3, sType);
            stmt.setString(4, sDate);
            stmt.setString(5, subDate);
            stmt.setString(6, reason);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // 5. NOTICES PANEL (Table: notices JOIN users)
    // ==========================================
    private JPanel createNoticesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Title", "Content / Description", "Published Date", "Target Role", "Posted By",
                "Status" };
        noticeModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(noticeModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(2, 4, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Post Notice / Lab Announcement"));

        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JComboBox<String> targetRoleBox = new JComboBox<>(
                new String[] { "ALL", "Undergraduate", "Lecturer", "Technical Officer" });
        JTextField dateField = new JTextField("2026-10-18");
        JButton addButton = new JButton("Post Notice");

        formPanel.add(new JLabel("Notice Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Content:"));
        formPanel.add(descField);

        formPanel.add(new JLabel("Target Role & Date:"));
        JPanel roleDateGroup = new JPanel(new GridLayout(1, 2, 4, 0));
        roleDateGroup.add(targetRoleBox);
        roleDateGroup.add(dateField);
        formPanel.add(roleDateGroup);

        formPanel.add(addButton);

        addButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String desc = descField.getText().trim();
            String target = (String) targetRoleBox.getSelectedItem();
            String date = dateField.getText().trim();

            if (title.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and description cannot be empty.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean savedToDb = saveNoticeToDb(title, desc, date, target);
            if (!savedToDb) {
                int nextId = noticeModel.getRowCount() + 1;
                noticeModel.addRow(new Object[] { nextId, title, desc, date, target, currentFullName, "ACTIVE" });
            } else {
                loadNoticeData();
            }
            titleField.setText("");
            descField.setText("");
            updateDashboardData();
            JOptionPane.showMessageDialog(this, "Notice published successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private boolean saveNoticeToDb(String title, String content, String date, String target) {
        String sql = "INSERT INTO notices (title, content, published_date, created_by, target_role, status) " +
                "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, content);
            stmt.setString(3, date);
            stmt.setInt(4, currentUserId);
            stmt.setString(5, target);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // 6. TIMETABLE PANEL (Table: timetables JOIN courses, users)
    // ==========================================
    private JPanel createTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Day", "Time Slot", "Course Code", "Course Name", "Type", "Room / Hall",
                "Lecturer" };
        timetableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(timetableModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(2, 6, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Manage Lab & Lecture Timetable Entries"));

        JComboBox<String> dayBox = new JComboBox<>(
                new String[] { "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" });
        JTextField startTimeField = new JTextField("08:00:00");
        JTextField endTimeField = new JTextField("10:00:00");
        ttCourseBox = new JComboBox<>(
                new String[] { "ICT2132", "ICT2113", "ICT2122", "ICT2143", "ENG2112", "ET2101", "BST2102" });
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL" });
        JTextField roomField = new JTextField("Computer Lab 01");
        JButton addButton = new JButton("Add Slot");

        formPanel.add(new JLabel("Day:"));
        formPanel.add(dayBox);
        formPanel.add(new JLabel("Course Code:"));
        formPanel.add(ttCourseBox);
        formPanel.add(new JLabel("Session Type:"));
        formPanel.add(typeBox);

        formPanel.add(new JLabel("Start - End Time:"));
        JPanel timeGroup = new JPanel(new GridLayout(1, 2, 4, 0));
        timeGroup.add(startTimeField);
        timeGroup.add(endTimeField);
        formPanel.add(timeGroup);

        formPanel.add(new JLabel("Room / Hall:"));
        formPanel.add(roomField);
        formPanel.add(addButton);

        addButton.addActionListener(e -> {
            String day = (String) dayBox.getSelectedItem();
            String course = (String) ttCourseBox.getSelectedItem();
            String type = (String) typeBox.getSelectedItem();
            String start = startTimeField.getText().trim();
            String end = endTimeField.getText().trim();
            String room = roomField.getText().trim();

            if (start.isEmpty() || end.isEmpty() || room.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter start time, end time, and room.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean savedToDb = saveTimetableToDb(day, course, type, start, end, room);
            if (!savedToDb) {
                int nextId = timetableModel.getRowCount() + 1;
                timetableModel.addRow(new Object[] { nextId, day, start + " - " + end, course, "Academic Course", type,
                        room, "Faculty Lecturer" });
            } else {
                loadTimetableData();
            }
            JOptionPane.showMessageDialog(this, "Timetable entry added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private boolean saveTimetableToDb(String day, String courseCode, String type, String start, String end,
            String room) {
        String sql = "INSERT INTO timetables (department_id, course_id, day_of_week, start_time, end_time, room, session_type, semester, academic_year) "
                + "VALUES (1, (SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, ?, ?, ?, 1, '2024/2025')";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, day);
            stmt.setString(3, start);
            stmt.setString(4, end);
            stmt.setString(5, room);
            stmt.setString(6, type);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // DATA FETCHING & SYNCHRONIZATION
    // ==========================================
    public void refreshAllData() {
        boolean connected = testDbConnection();
        if (connected) {
            loadAttendanceData();
            loadMedicalData();
            loadNoticeData();
            loadTimetableData();
            loadUserProfileData();
        } else {
            loadInitialSeedData();
        }
        updateDashboardData();
    }

    private void loadUserProfileData() {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, u.phone, u.address, u.student_reg_no, "
                + "u.designation, u.assigned_lab, u.status, r.role_name, d.department_name "
                + "FROM users u "
                + "JOIN roles r ON u.role_id = r.role_id "
                + "LEFT JOIN departments d ON u.department_id = d.department_id "
                + "WHERE u.user_id = ? LIMIT 1";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    currentFullName = rs.getString("full_name");
                    currentEmail = rs.getString("email");
                    currentPhone = rs.getString("phone");
                    currentAddress = rs.getString("address");
                    if (rs.getString("student_reg_no") != null) {
                        currentStaffId = rs.getString("student_reg_no");
                    }
                    if (rs.getString("department_name") != null) {
                        currentDepartment = rs.getString("department_name");
                    }
                    if (rs.getString("role_name") != null) {
                        currentRole = rs.getString("role_name");
                    }
                    if (rs.getString("designation") != null) {
                        currentDesignation = rs.getString("designation");
                    }
                    if (rs.getString("assigned_lab") != null) {
                        currentAssignedLab = rs.getString("assigned_lab");
                    }
                    updateProfileDisplay();
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private void loadAttendanceData() {
        attendanceModel.setRowCount(0);
        String sql = "SELECT a.attendance_id, u.student_reg_no, u.full_name, c.course_code, " +
                "a.session_date, a.session_type, a.session_number, a.status " +
                "FROM attendance a " +
                "JOIN users u ON a.student_id = u.user_id " +
                "JOIN courses c ON a.course_id = c.course_id " +
                "ORDER BY a.session_date DESC, a.attendance_id DESC LIMIT 200";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                attendanceModel.addRow(new Object[] {
                        rs.getInt("attendance_id"),
                        rs.getString("student_reg_no"),
                        rs.getString("full_name"),
                        rs.getString("course_code"),
                        rs.getDate("session_date"),
                        rs.getString("session_type"),
                        rs.getInt("session_number"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMedicalData() {
        medicalModel.setRowCount(0);
        String sql = "SELECT m.medical_id, u.student_reg_no, u.full_name, c.course_code, " +
                "m.session_type, m.session_date, m.submission_date, m.reason, m.status " +
                "FROM medical m " +
                "JOIN users u ON m.student_id = u.user_id " +
                "JOIN courses c ON m.course_id = c.course_id " +
                "ORDER BY m.submission_date DESC, m.medical_id DESC";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicalModel.addRow(new Object[] {
                        rs.getInt("medical_id"),
                        rs.getString("student_reg_no"),
                        rs.getString("full_name"),
                        rs.getString("course_code"),
                        rs.getString("session_type"),
                        rs.getDate("session_date"),
                        rs.getDate("submission_date"),
                        rs.getString("reason"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadNoticeData() {
        noticeModel.setRowCount(0);
        String sql = "SELECT n.notice_id, n.title, n.content, n.published_date, n.target_role, " +
                "COALESCE(u.full_name, 'Administration') AS posted_by, n.status " +
                "FROM notices n " +
                "LEFT JOIN users u ON n.created_by = u.user_id " +
                "ORDER BY n.published_date DESC, n.notice_id DESC";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                noticeModel.addRow(new Object[] {
                        rs.getInt("notice_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDate("published_date"),
                        rs.getString("target_role"),
                        rs.getString("posted_by"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTimetableData() {
        timetableModel.setRowCount(0);
        String sql = "SELECT t.timetable_id, t.day_of_week, " +
                "CONCAT(TIME_FORMAT(t.start_time, '%h:%i %p'), ' - ', TIME_FORMAT(t.end_time, '%h:%i %p')) AS time_slot, "
                +
                "c.course_code, c.course_name, t.session_type, t.room, " +
                "COALESCE(u.full_name, 'Unassigned') AS lecturer " +
                "FROM timetables t " +
                "JOIN courses c ON t.course_id = c.course_id " +
                "LEFT JOIN users u ON t.lecturer_id = u.user_id " +
                "ORDER BY FIELD(t.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'), t.start_time";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                timetableModel.addRow(new Object[] {
                        rs.getInt("timetable_id"),
                        rs.getString("day_of_week"),
                        rs.getString("time_slot"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("session_type"),
                        rs.getString("room"),
                        rs.getString("lecturer")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // INITIAL SEED DATA (Exact match to database.sql)
    // ==========================================
    private void loadInitialSeedData() {
        attendanceModel.setRowCount(0);
        attendanceModel.addRow(
                new Object[] { 1, "TG/2024/2001", "M.D.R.L. Nonis", "ICT2132", "2026-08-03", "THEORY", 1, "PRESENT" });
        attendanceModel.addRow(new Object[] { 2, "TG/2024/2001", "M.D.R.L. Nonis", "ICT2132", "2026-08-03", "PRACTICAL",
                1, "PRESENT" });
        attendanceModel.addRow(
                new Object[] { 3, "TG/2024/2002", "Anuda Pasandul", "ICT2132", "2026-08-31", "THEORY", 5, "ABSENT" });
        attendanceModel.addRow(new Object[] { 4, "TG/2024/2004", "H.R.H. Nethsara", "ICT2132", "2026-10-19", "THEORY",
                12, "PRESENT" });
        attendanceModel.addRow(new Object[] { 5, "TG/2024/2006", "Dilshan Madushanka", "ICT2132", "2026-09-15",
                "THEORY", 7, "ABSENT" });

        medicalModel.setRowCount(0);
        medicalModel.addRow(new Object[] { 1, "TG/2024/2006", "Dilshan Madushanka", "ICT2132", "THEORY", "2026-09-15",
                "2026-09-17", "Viral fever - treated at Base Hospital", "APPROVED" });
        medicalModel.addRow(new Object[] { 2, "TG/2024/2006", "Dilshan Madushanka", "ICT2132", "THEORY", "2026-09-22",
                "2026-09-24", "Viral fever rest recommended", "APPROVED" });
        medicalModel.addRow(new Object[] { 3, "TG/2024/2007", "Tharindu Malshan", "ICT2132", "THEORY", "2026-09-08",
                "2026-09-10", "Dengue fever admission", "APPROVED" });
        medicalModel.addRow(new Object[] { 4, "TG/2024/2005", "Kaveesha Gimhan", "ICT2132", "THEORY", "2026-09-29",
                "2026-10-05", "Headache - submitted late", "NOT_APPROVED" });

        noticeModel.setRowCount(0);
        noticeModel.addRow(new Object[] { 1, "Commencement of Semester I Activities",
                "Academic activities for Level II Semester I commence on 3rd August 2026.", "2026-08-01", "ALL",
                "System Administrator", "ACTIVE" });
        noticeModel.addRow(new Object[] { 2, "Submission of Medical Certificates",
                "Submit medical certificates to Technical Officer within 7 days of returning.", "2026-08-10", "ALL",
                "System Administrator", "ACTIVE" });
        noticeModel.addRow(new Object[] { 3, "Continuous Assessment (CA) Marks Notice",
                "CA marks for ICT2132 and ICT2113 published. Verify before 25th October 2026.", "2026-10-01", "ALL",
                "System Administrator", "ACTIVE" });
        noticeModel.addRow(new Object[] { 4, "Semester I Final Exam Timetable Release",
                "Final exam timetable released. Attendance > 80% and CA >= 40% required.", "2026-10-05", "ALL",
                "System Administrator", "ACTIVE" });
        noticeModel.addRow(new Object[] { 5, "Lab Equipment Maintenance & Lab Access Hours",
                "Computer Lab 01 & Network Systems Lab maintenance this Saturday 8 AM - 1 PM.", "2026-10-06", "ALL",
                "Mr. Saman Kumara (Technical Officer)", "ACTIVE" });

        timetableModel.setRowCount(0);
        timetableModel.addRow(new Object[] { 1, "MONDAY", "08:00 AM - 10:00 AM", "ICT2132",
                "Object Oriented Programming Practicum", "THEORY", "Lecture Theatre 01", "Dr. Kamal Perera" });
        timetableModel.addRow(new Object[] { 2, "MONDAY", "10:00 AM - 12:00 PM", "ICT2132",
                "Object Oriented Programming Practicum", "PRACTICAL", "Computer Lab 01", "Dr. Kamal Perera" });
        timetableModel.addRow(new Object[] { 3, "TUESDAY", "08:00 AM - 10:00 AM", "ICT2113", "Database Systems",
                "THEORY", "Lecture Theatre 02", "Prof. Nimal Silva" });
        timetableModel.addRow(new Object[] { 4, "TUESDAY", "10:00 AM - 12:00 PM", "ICT2113", "Database Systems",
                "PRACTICAL", "Computer Lab 02", "Prof. Nimal Silva" });
        timetableModel.addRow(new Object[] { 5, "WEDNESDAY", "01:00 PM - 03:00 PM", "ICT2122", "Software Engineering",
                "THEORY", "Lecture Theatre 01", "Dr. Kamal Perera" });
        timetableModel.addRow(new Object[] { 6, "THURSDAY", "08:00 AM - 10:00 AM", "ICT2143", "Computer Networks",
                "THEORY", "Lecture Theatre 01", "Dr. Kamal Perera" });
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(238, 242, 246));
        table.getTableHeader().setForeground(new Color(30, 41, 59));
        table.setShowGrid(true);
        table.setGridColor(new Color(226, 232, 240));

        // Center align ID column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        if (table.getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(0).setMaxWidth(60);
        }
    }

    public static void main(String[] args) {
        // Run UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LMSGuiApplication().setVisible(true);
        });
    }
}