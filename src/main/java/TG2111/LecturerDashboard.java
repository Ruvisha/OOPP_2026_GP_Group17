package com.fot.ams.ui.lecturer;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.NoticeDAO;
import com.fot.ams.model.Notice;
import com.fot.ams.model.User;
import com.fot.ams.service.NoticeService;
import com.fot.ams.ui.LoginFrame;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * Main Lecturer Academic Portal Frame.
 */
public class LecturerDashboard extends JFrame {
    private JPanel contentContainer;
    private CardLayout cardLayout;

    private LecturerProfilePanel profilePanel;
    private CourseMaterialPanel materialPanel;
    private MarksPanel marksPanel;
    private StudentDetailsPanel studentDetailsPanel;
    private JPanel noticeViewPanel;
    private JPanel overviewPanel;

    private JButton btnOverview;
    private JButton btnProfile;
    private JButton btnMaterials;
    private JButton btnMarks;
    private JButton btnStudents;
    private JButton btnNotices;

    public LecturerDashboard() {
        initComponents();
    }

    private void initComponents() {
        User cur = SessionManager.getCurrentUser();
        setTitle(cur != null ? cur.getDashboardTitle() : "Lecturer Academic Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1220, 760);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ModernTheme.BG_MAIN);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 760));
        sidebar.setBackground(ModernTheme.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel bTitle = new JLabel("FOT - AMS");
        bTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        bTitle.setForeground(Color.WHITE);
        bTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bSub = new JLabel("University of Ruhuna");
        bSub.setFont(ModernTheme.FONT_SMALL);
        bSub.setForeground(new Color(148, 163, 184));
        bSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel rLabel = new JLabel("Role: Lecturer");
        rLabel.setFont(ModernTheme.FONT_SMALL);
        rLabel.setForeground(new Color(147, 197, 253));
        rLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(bTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(bSub);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(rLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));

        btnOverview = createNavButton("Academic Overview");
        btnProfile = createNavButton("My Profile");
        btnMaterials = createNavButton("Course Materials");
        btnMarks = createNavButton("Marks & Eligibility");
        btnStudents = createNavButton("Undergraduates & GPA");
        btnNotices = createNavButton("Faculty Notices");

        sidebar.add(btnOverview);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnProfile);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnMaterials);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnMarks);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnStudents);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnNotices);

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = ModernTheme.createDangerButton("Logout from Portal");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(210, 38));
        btnLogout.addActionListener(e -> handleLogout());
        sidebar.add(btnLogout);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(980, 60));
        header.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(10, 25, 10, 25)));

        JLabel welcome = new JLabel("Welcome, " + (cur != null ? cur.getFullName() : "Lecturer"));
        welcome.setFont(ModernTheme.FONT_SUBTITLE);
        welcome.setForeground(ModernTheme.TEXT_DARK);

        JLabel dateLbl = new JLabel(DateUtil.formatDisplayDate(new Date()));
        dateLbl.setFont(ModernTheme.FONT_REGULAR);
        dateLbl.setForeground(ModernTheme.TEXT_MUTED);

        header.add(welcome, BorderLayout.WEST);
        header.add(dateLbl, BorderLayout.EAST);

        // Center Cards
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(ModernTheme.BG_MAIN);

        overviewPanel = createOverviewPanel();
        profilePanel = new LecturerProfilePanel();
        materialPanel = new CourseMaterialPanel();
        marksPanel = new MarksPanel();
        studentDetailsPanel = new StudentDetailsPanel();
        noticeViewPanel = createNoticesViewPanel();

        contentContainer.add(overviewPanel, "OVERVIEW");
        contentContainer.add(profilePanel, "PROFILE");
        contentContainer.add(materialPanel, "MATERIALS");
        contentContainer.add(marksPanel, "MARKS");
        contentContainer.add(studentDetailsPanel, "STUDENTS");
        contentContainer.add(noticeViewPanel, "NOTICES");

        root.add(sidebar, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);
        root.add(contentContainer, BorderLayout.CENTER);

        setContentPane(root);

        // Nav Handlers
        btnOverview.addActionListener(e -> switchCard("OVERVIEW", btnOverview));
        btnProfile.addActionListener(e -> {
            profilePanel.loadProfileData();
            switchCard("PROFILE", btnProfile);
        });
        btnMaterials.addActionListener(e -> {
            materialPanel.loadMaterials(0);
            switchCard("MATERIALS", btnMaterials);
        });
        btnMarks.addActionListener(e -> {
            marksPanel.loadCourses();
            switchCard("MARKS", btnMarks);
        });
        btnStudents.addActionListener(e -> switchCard("STUDENTS", btnStudents));
        btnNotices.addActionListener(e -> switchCard("NOTICES", btnNotices));

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
        JButton[] buttons = {btnOverview, btnProfile, btnMaterials, btnMarks, btnStudents, btnNotices};
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

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statsRow.setOpaque(false);

        CourseDAO cDAO = new CourseDAO();
        statsRow.add(createStatCard("Offered Courses", String.valueOf(cDAO.getAllCourses().size()), ModernTheme.PRIMARY));
        statsRow.add(createStatCard("Grading Scheme", "UGC Circular 12-2024", ModernTheme.SUCCESS));
        statsRow.add(createStatCard("CA Requirement", "CA \u2265 40% Required", ModernTheme.WARNING));

        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Lecturer Academic Duties & Modules (SRS Group 17)");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JTextArea text = new JTextArea(
            "\n• Course Materials: Add, update, view, and delete lecture slides and laboratory instruction sheets.\n\n" +
            "• Marks Upload: Upload individual and batch evaluation marks out of 100 for quizzes, assignments, mid exams, and final assessments.\n\n" +
            "• Eligibility Verification: Verify CA \u2265 40% criteria and Attendance \u2265 80% criteria before authorizing exam admission.\n\n" +
            "• Grade Calculation: Automatically generate student grades (A+ to E) and compute SGPA/CGPA points.\n\n" +
            "• Student Overview: Monitor student attendance records, medical endorsements, and academic progress."
        );
        text.setFont(ModernTheme.FONT_REGULAR);
        text.setEditable(false);
        text.setOpaque(false);
        text.setWrapStyleWord(true);
        text.setLineWrap(true);

        card.add(title);
        card.add(text);

        panel.add(statsRow, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel c = ModernTheme.createCardPanel();
        c.setLayout(new BorderLayout());
        c.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, color),
            new EmptyBorder(15, 15, 15, 15)
        ));
        JLabel t = new JLabel(title);
        t.setFont(ModernTheme.FONT_BOLD);
        t.setForeground(ModernTheme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(ModernTheme.TEXT_DARK);

        c.add(t, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        return c;
    }

    private JPanel createNoticesViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Faculty Official Announcements");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Notice ID", "Title", "Date", "Audience", "Published By", "Notice Content"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        ModernTheme.styleTable(table);

        List<Notice> notices = new NoticeService().getActiveNotices();
        for (Notice n : notices) {
            model.addRow(new Object[]{
                n.getNoticeId(),
                n.getTitle(),
                DateUtil.formatDate(n.getPublishedDate()),
                n.getTargetRole(),
                n.getCreatorName() != null ? n.getCreatorName() : "Dean's Office",
                n.getContent()
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out from the Lecturer portal?",
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
