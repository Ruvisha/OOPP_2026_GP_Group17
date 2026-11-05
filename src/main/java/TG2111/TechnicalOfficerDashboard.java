package com.fot.ams.ui.technical;

import com.fot.ams.model.User;
import com.fot.ams.ui.LoginFrame;
import com.fot.ams.ui.common.DashboardHeaderPanel;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Technical Operations Dashboard for Faculty of Technology.
 * University of Ruhuna - Member 04 Module.
 */
public class TechnicalOfficerDashboard extends JFrame implements TechnicalOverviewPanel.TechnicalNavDelegate {
    private JPanel contentContainer;
    private CardLayout cardLayout;

    private TechnicalProfilePanel profilePanel;
    private AttendancePanel attendancePanel;
    private MedicalPanel medicalPanel;
    private TechnicalTimetablePanel timetablePanel;
    private TechnicalNoticePanel noticePanel;
    private TechnicalOverviewPanel overviewPanel;

    private JButton btnOverview;
    private JButton btnProfile;
    private JButton btnAttendance;
    private JButton btnMedical;
    private JButton btnTimetable;
    private JButton btnNotices;
    private JButton btnLogout;

    private DashboardHeaderPanel headerPanel;

    public TechnicalOfficerDashboard() {
        initComponents();
    }

    private void initComponents() {
        User cur = SessionManager.getCurrentUser();
        setTitle(cur != null ? cur.getDashboardTitle() : "Faculty of Technology - Technical Operations Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 800);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ModernTheme.BG_MAIN);

        // Sidebar (Stationary, zero scrolling)
        JPanel sidebar = createSidebar();

        // Common Header
        headerPanel = new DashboardHeaderPanel(cur, () -> switchCard("PROFILE", btnProfile));

        // Center Content Container (CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(ModernTheme.BG_MAIN);

        overviewPanel = new TechnicalOverviewPanel(this);
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
        root.add(headerPanel, BorderLayout.NORTH);
        root.add(contentContainer, BorderLayout.CENTER);

        setContentPane(root);

        // Nav listeners
        btnOverview.addActionListener(e -> {
            overviewPanel.loadData();
            switchCard("OVERVIEW", btnOverview);
        });
        btnProfile.addActionListener(e -> {
            profilePanel.loadLatestProfileFromDatabase();
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
        btnLogout.addActionListener(e -> handleLogout());

        setActiveNav(btnOverview);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(245, 800));
        sidebar.setBackground(ModernTheme.NAVY);
        sidebar.setLayout(new BorderLayout());

        // Menu items container (Stationary - no scrollbars)
        JPanel menuContent = new JPanel();
        menuContent.setLayout(new BoxLayout(menuContent, BoxLayout.Y_AXIS));
        menuContent.setBackground(ModernTheme.NAVY);
        menuContent.setBorder(new EmptyBorder(22, 14, 16, 14));

        // Branding
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandTitle = new JLabel("FOT - AMS", ModernIcons.get("academic", 24, ModernTheme.ACCENT), SwingConstants.LEFT);
        brandTitle.setFont(new Font(ModernTheme.getFontFamily(), Font.BOLD, 22));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSubtitle = new JLabel("University of Ruhuna");
        brandSubtitle.setFont(ModernTheme.FONT_SMALL);
        brandSubtitle.setForeground(new Color(148, 163, 184));

        JLabel userRoleLabel = ModernTheme.createBadge("TECHNICAL OPERATIONS", new Color(6, 78, 59), new Color(110, 231, 183));

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        brandPanel.add(brandSubtitle);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        brandPanel.add(userRoleLabel);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        menuContent.add(brandPanel);

        // Section header
        JLabel secLbl = new JLabel("OPERATIONS MENU");
        secLbl.setFont(ModernTheme.FONT_CAPTION);
        secLbl.setForeground(new Color(100, 116, 139));
        secLbl.setBorder(new EmptyBorder(6, 8, 8, 0));
        menuContent.add(secLbl);

        btnOverview = createNavButton("Operations Overview", "dashboard");
        btnProfile = createNavButton("Officer Profile", "profile");
        btnAttendance = createNavButton("Attendance (15 Sessions)", "check");
        btnMedical = createNavButton("Medical Records", "audit");
        btnTimetable = createNavButton("Lab & Dept Timetable", "calendar");
        btnNotices = createNavButton("Faculty Notices", "notices");

        menuContent.add(btnOverview);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnProfile);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnAttendance);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnMedical);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnTimetable);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnNotices);

        sidebar.add(menuContent, BorderLayout.CENTER);

        // Fixed bottom logout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ModernTheme.NAVY);
        bottomPanel.setBorder(new EmptyBorder(12, 14, 16, 14));

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
        btnLogout.setBackground(new Color(153, 27, 27));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btnLogout.setMaximumSize(new Dimension(218, 42));

        bottomPanel.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text, String iconName) {
        JButton btn = new JButton(text, ModernIcons.get(iconName, 17, new Color(148, 163, 184))) {
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
        btn.setBorder(new EmptyBorder(10, 12, 10, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(218, 38));
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
        JButton[] buttons = {btnOverview, btnProfile, btnAttendance, btnMedical, btnTimetable, btnNotices};
        for (JButton b : buttons) {
            if (b == null) continue;
            if (b == active) {
                b.setBackground(ModernTheme.BG_SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
                b.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(52, 211, 153)),
                    new EmptyBorder(10, 8, 10, 12)
                ));
            } else {
                b.setBackground(ModernTheme.NAVY);
                b.setForeground(new Color(203, 213, 225));
                b.setBorder(new EmptyBorder(10, 12, 10, 12));
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
            case "ATTENDANCE":
                switchCard("ATTENDANCE", btnAttendance);
                break;
            case "MEDICAL":
                medicalPanel.loadMedicals();
                switchCard("MEDICAL", btnMedical);
                break;
            case "TIMETABLE":
                timetablePanel.loadTimetable();
                switchCard("TIMETABLE", btnTimetable);
                break;
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out from the Technical Operations portal?",
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
