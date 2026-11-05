package com.fot.ams.ui.lecturer;

import com.fot.ams.dao.NoticeDAO;
import com.fot.ams.model.Notice;
import com.fot.ams.model.User;
import com.fot.ams.service.NoticeService;
import com.fot.ams.ui.LoginFrame;
import com.fot.ams.ui.common.DashboardHeaderPanel;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Modern Lecturer Academic Portal Frame for Faculty of Technology.
 * University of Ruhuna - Group 17.
 */
public class LecturerDashboard extends JFrame implements LecturerOverviewPanel.LecturerNavDelegate {
    private JPanel contentContainer;
    private CardLayout cardLayout;

    private LecturerProfilePanel profilePanel;
    private CourseMaterialPanel materialPanel;
    private MarksPanel marksPanel;
    private StudentDetailsPanel studentDetailsPanel;
    private JPanel noticeViewPanel;
    private LecturerOverviewPanel overviewPanel;

    private JButton btnOverview;
    private JButton btnProfile;
    private JButton btnMaterials;
    private JButton btnMarks;
    private JButton btnStudents;
    private JButton btnNotices;
    private JButton btnLogout;

    private DashboardHeaderPanel headerPanel;

    public LecturerDashboard() {
        initComponents();
    }

    private void initComponents() {
        User cur = SessionManager.getCurrentUser();
        setTitle(cur != null ? cur.getDashboardTitle() : "Faculty of Technology - Lecturer Academic Portal");
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

        // Center Cards
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(ModernTheme.BG_MAIN);

        overviewPanel = new LecturerOverviewPanel(this);
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
        root.add(headerPanel, BorderLayout.NORTH);
        root.add(contentContainer, BorderLayout.CENTER);

        setContentPane(root);

        // Nav Handlers
        btnOverview.addActionListener(e -> {
            overviewPanel.loadData();
            switchCard("OVERVIEW", btnOverview);
        });
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
        btnLogout.addActionListener(e -> handleLogout());

        setActiveNav(btnOverview);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(245, 800));
        sidebar.setBackground(ModernTheme.NAVY);
        sidebar.setLayout(new BorderLayout());

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

        JLabel userRoleLabel = ModernTheme.createBadge("LECTURER PORTAL", new Color(30, 58, 138), new Color(147, 197, 253));

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        brandPanel.add(brandSubtitle);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        brandPanel.add(userRoleLabel);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        menuContent.add(brandPanel);

        JLabel secLbl = new JLabel("ACADEMIC MENU");
        secLbl.setFont(ModernTheme.FONT_CAPTION);
        secLbl.setForeground(new Color(100, 116, 139));
        secLbl.setBorder(new EmptyBorder(6, 8, 8, 0));
        menuContent.add(secLbl);

        btnOverview = createNavButton("Academic Overview", "dashboard");
        btnProfile = createNavButton("My Profile", "profile");
        btnMaterials = createNavButton("Course Materials", "courses");
        btnMarks = createNavButton("Marks & Eligibility", "marks");
        btnStudents = createNavButton("Undergraduates & GPA", "students");
        btnNotices = createNavButton("Faculty Notices", "notices");

        menuContent.add(btnOverview);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnProfile);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnMaterials);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnMarks);
        menuContent.add(Box.createRigidArea(new Dimension(0, 4)));
        menuContent.add(btnStudents);
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
        JButton[] buttons = {btnOverview, btnProfile, btnMaterials, btnMarks, btnStudents, btnNotices};
        for (JButton b : buttons) {
            if (b == null) continue;
            if (b == active) {
                b.setBackground(ModernTheme.BG_SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
                b.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, ModernTheme.ACCENT),
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
            case "MATERIALS":
                materialPanel.loadMaterials(0);
                switchCard("MATERIALS", btnMaterials);
                break;
            case "MARKS":
                marksPanel.loadCourses();
                switchCard("MARKS", btnMarks);
                break;
            case "STUDENTS":
                switchCard("STUDENTS", btnStudents);
                break;
        }
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

        try {
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
        } catch (Exception ignored) {
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out from the Lecturer portal?",
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
