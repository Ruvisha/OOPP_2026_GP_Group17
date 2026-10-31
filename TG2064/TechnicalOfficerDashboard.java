package com.fot.ams.ui.technical;

import com.fot.ams.dao.AttendanceDAO;
import com.fot.ams.dao.MedicalDAO;
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
 * Main Technical Operations Dashboard Frame.
 */
public class TechnicalOfficerDashboard extends JFrame {
    private JPanel contentContainer;
    private CardLayout cardLayout;

    private TechnicalProfilePanel profilePanel;
    private AttendancePanel attendancePanel;
    private MedicalPanel medicalPanel;
    private TechnicalTimetablePanel timetablePanel;
    private TechnicalNoticePanel noticePanel;
    private JPanel overviewPanel;

    private JButton btnOverview;
    private JButton btnProfile;
    private JButton btnAttendance;
    private JButton btnMedical;
    private JButton btnTimetable;
    private JButton btnNotices;

    public TechnicalOfficerDashboard() {
        initComponents();
    }

    private void initComponents() {
        User cur = SessionManager.getCurrentUser();
        setTitle(cur != null ? cur.getDashboardTitle() : "Technical Operations Portal");
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

        JLabel rLabel = new JLabel("Role: Technical Officer");
        rLabel.setFont(ModernTheme.FONT_SMALL);
        rLabel.setForeground(new Color(52, 211, 153));
        rLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(bTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(bSub);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(rLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));

        btnOverview = createNavButton("Operations Overview");
        btnProfile = createNavButton("Officer Profile");
        btnAttendance = createNavButton("Attendance (15 Sessions)");
        btnMedical = createNavButton("Medical Records");
        btnTimetable = createNavButton("Lab & Dept Timetable");
        btnNotices = createNavButton("Faculty Notices");

        sidebar.add(btnOverview);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnProfile);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnAttendance);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnMedical);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnTimetable);
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

        JLabel welcome = new JLabel("Welcome, " + (cur != null ? cur.getFullName() : "Technical Officer"));
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
        profilePanel = new TechnicalProfilePanel();
        attendancePanel = new AttendancePanel();
        medicalPanel = new MedicalPanel();
        timetablePanel = new TechnicalTimetablePanel();
        noticePanel = new TechnicalNoticePanel();

        contentContainer.add(overviewPanel, "OVERVIEW");
        contentContainer.add(profilePanel, "PROFILE");
        contentContainer.add(attendancePanel, "ATTENDANCE");
        contentContainer.add(medicalPanel, "MEDICAL");
        contentContainer.add(timetablePanel, "TIMETABLE");
        contentContainer.add(noticePanel, "NOTICES");

        root.add(sidebar, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);
        root.add(contentContainer, BorderLayout.CENTER);

        setContentPane(root);

        btnOverview.addActionListener(e -> switchCard("OVERVIEW", btnOverview));
        btnProfile.addActionListener(e -> {
            profilePanel.loadProfile();
            switchCard("PROFILE", btnProfile);
        });
        btnAttendance.addActionListener(e -> switchCard("ATTENDANCE", btnAttendance));
        btnMedical.addActionListener(e -> {
            medicalPanel.loadMedicals();
            switchCard("MEDICAL", btnMedical);
        });
        btnTimetable.addActionListener(e -> {
            timetablePanel.loadTimetable();
            switchCard("TIMETABLE", btnTimetable);
        });
        btnNotices.addActionListener(e -> {
            noticePanel.loadNotices();
            switchCard("NOTICES", btnNotices);
        });

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
        JButton[] buttons = {btnOverview, btnProfile, btnAttendance, btnMedical, btnTimetable, btnNotices};
        for (JButton b : buttons) {
            if (b == active) {
                b.setBackground(ModernTheme.BG_SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
                b.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(16, 185, 129)),
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

        MedicalDAO mDAO = new MedicalDAO();
        int medCount = mDAO.getAllMedicals().size();

        statsRow.add(createStatCard("Sessions Per Module", "15 Theory + 15 Practical", ModernTheme.PRIMARY));
        statsRow.add(createStatCard("Medical Certificates Logged", String.valueOf(medCount), ModernTheme.WARNING));
        statsRow.add(createStatCard("Attendance Eligibility", "\u2265 80% Threshold", ModernTheme.SUCCESS));

        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Technical Officer Operational Responsibilities (SRS Group 17)");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JTextArea text = new JTextArea(
            "\n• Attendance Recording: Record theory and practical attendance session by session (1 to 15) for student batches.\n\n" +
            "• Attendance Summary & Scenarios: Analyze attendance percentages and verify compliance across the 5 mandatory scenarios:\n" +
            "   1. More than 80%\n" +
            "   2. Exactly 80%\n" +
            "   3. Less than 80% without medicals\n" +
            "   4. More than 80% with approved medicals\n" +
            "   5. Less than 80% with approved medicals\n\n" +
            "• Medical Records: Maintain student medical certificates and verify approval status (APPROVED / NOT_APPROVED) by the University Medical Officer.\n\n" +
            "• Laboratory Timetables: Inspect department timetable allocations to prepare lab workstations and apparatus."
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

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out from the Technical portal?",
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
