package com.fot.ams.ui.undergraduate;

import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.model.Undergraduate;
import com.fot.ams.model.User;
import com.fot.ams.service.GPAService;
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
 * Main Undergraduate Student Academic Portal Frame.
 */
public class UndergraduateDashboard extends JFrame {
    private JPanel contentContainer;
    private CardLayout cardLayout;

    private UndergraduateProfilePanel profilePanel;
    private CourseViewPanel coursePanel;
    private AttendanceViewPanel attendancePanel;
    private MedicalViewPanel medicalPanel;
    private GradeViewPanel gradePanel;
    private TimetableViewPanel timetablePanel;
    private NoticeViewPanel noticePanel;
    private JPanel overviewPanel;

    private JButton btnOverview;
    private JButton btnProfile;
    private JButton btnCourses;
    private JButton btnAttendance;
    private JButton btnMedical;
    private JButton btnGrades;
    private JButton btnTimetable;
    private JButton btnNotices;

    public UndergraduateDashboard() {
        initComponents();
    }

    private void initComponents() {
        User cur = SessionManager.getCurrentUser();
        setTitle(cur != null ? cur.getDashboardTitle() : "Student Academic Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ModernTheme.BG_MAIN);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 780));
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

        String regNo = (cur instanceof Undergraduate ug) ? ug.getStudentRegNo() : "";
        JLabel rLabel = new JLabel("Student: " + regNo);
        rLabel.setFont(ModernTheme.FONT_SMALL);
        rLabel.setForeground(new Color(147, 197, 253));
        rLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(bTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(bSub);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(rLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        btnOverview = createNavButton("Portal Overview");
        btnProfile = createNavButton("Profile & Contact");
        btnCourses = createNavButton("Enrolled Courses");
        btnAttendance = createNavButton("Attendance Tracking");
        btnMedical = createNavButton("Medical Records");
        btnGrades = createNavButton("Grades & GPA");
        btnTimetable = createNavButton("My Timetable");
        btnNotices = createNavButton("Faculty Bulletins");

        sidebar.add(btnOverview);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnProfile);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnCourses);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnAttendance);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnMedical);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnGrades);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnTimetable);
        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnNotices);

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = ModernTheme.createDangerButton("Sign Out");
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(220, 38));
        btnLogout.addActionListener(e -> handleLogout());
        sidebar.add(btnLogout);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(990, 60));
        header.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(10, 25, 10, 25)));

        String studentInfo = (cur != null ? cur.getFullName() : "Undergraduate");
        if (cur instanceof Undergraduate ug && (ug.isRepeat() || ug.isBatchMissed())) {
            studentInfo += " [" + ug.getStudentType() + " STUDENT]";
        }

        JLabel welcome = new JLabel("Welcome, " + studentInfo);
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
        profilePanel = new UndergraduateProfilePanel();
        coursePanel = new CourseViewPanel();
        attendancePanel = new AttendanceViewPanel();
        medicalPanel = new MedicalViewPanel();
        gradePanel = new GradeViewPanel();
        timetablePanel = new TimetableViewPanel();
        noticePanel = new NoticeViewPanel();

        contentContainer.add(overviewPanel, "OVERVIEW");
        contentContainer.add(profilePanel, "PROFILE");
        contentContainer.add(coursePanel, "COURSES");
        contentContainer.add(attendancePanel, "ATTENDANCE");
        contentContainer.add(medicalPanel, "MEDICAL");
        contentContainer.add(gradePanel, "GRADES");
        contentContainer.add(timetablePanel, "TIMETABLE");
        contentContainer.add(noticePanel, "NOTICES");

        root.add(sidebar, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);
        root.add(contentContainer, BorderLayout.CENTER);

        setContentPane(root);

        btnOverview.addActionListener(e -> switchCard("OVERVIEW", btnOverview));
        btnProfile.addActionListener(e -> {
            profilePanel.loadProfileData();
            switchCard("PROFILE", btnProfile);
        });
        btnCourses.addActionListener(e -> {
            coursePanel.loadEnrolledCourses();
            switchCard("COURSES", btnCourses);
        });
        btnAttendance.addActionListener(e -> {
            attendancePanel.loadAttendanceDetails();
            switchCard("ATTENDANCE", btnAttendance);
        });
        btnMedical.addActionListener(e -> {
            medicalPanel.loadStudentMedicals();
            switchCard("MEDICAL", btnMedical);
        });
        btnGrades.addActionListener(e -> {
            gradePanel.loadStudentGrades();
            switchCard("GRADES", btnGrades);
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
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(220, 38));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void setActiveNav(JButton active) {
        JButton[] buttons = {btnOverview, btnProfile, btnCourses, btnAttendance, btnMedical, btnGrades, btnTimetable, btnNotices};
        for (JButton b : buttons) {
            if (b == active) {
                b.setBackground(ModernTheme.BG_SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
                b.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, ModernTheme.ACCENT),
                    new EmptyBorder(8, 10, 8, 14)
                ));
            } else {
                b.setBackground(ModernTheme.BG_SIDEBAR);
                b.setForeground(new Color(203, 213, 225));
                b.setBorder(new EmptyBorder(8, 14, 8, 14));
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

        User cur = SessionManager.getCurrentUser();
        int enrolledCount = 0;
        double cgpa = 0.0;
        String standing = "Good Standing";

        if (cur != null) {
            enrolledCount = new CourseEnrollmentDAO().getEnrollmentsByStudent(cur.getUserId()).size();
            GPAService gpaService = new GPAService();
            cgpa = gpaService.calculateCgpaForStudent(cur.getUserId());
            standing = gpaService.getClassification(cgpa);
        }

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setOpaque(false);

        statsRow.add(createStatCard("Enrolled Modules", String.valueOf(enrolledCount), ModernTheme.PRIMARY));
        statsRow.add(createStatCard("Current CGPA", cgpa > 0 ? String.format("%.2f", cgpa) : "N/A", ModernTheme.SUCCESS));
        statsRow.add(createStatCard("Attendance Target", "\u2265 80% Required", ModernTheme.WARNING));
        statsRow.add(createStatCard("Academic Standing", standing, new Color(79, 70, 229)));

        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Academic Regulations & Student Rights (University of Ruhuna)");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JTextArea text = new JTextArea(
            "\n• Course Materials: Access study guides, presentations, and lab sheets uploaded by course lecturers.\n\n" +
            "• Attendance Compliance (15 Sessions):\n" +
            "  You must maintain at least 80% attendance in theory and practical sessions to be eligible to sit for semester examinations.\n" +
            "  Approved medical certificates can excuse absences and raise your effective attendance rate.\n\n" +
            "• Continuous Assessment (CA) Criterion:\n" +
            "  You must score \u2265 40% in CA marks (Quizzes, Assignments, Mid Exams) to qualify for final exams.\n\n" +
            "• Examination Grading & Repeat Policies:\n" +
            "  Grades are awarded based on UGC Commission Circular No. 12-2024. Repeat courses are capped at grade C (2.00 GP)."
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
            new EmptyBorder(12, 15, 12, 15)
        ));
        JLabel t = new JLabel(title);
        t.setFont(ModernTheme.FONT_BOLD);
        t.setForeground(ModernTheme.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 19));
        v.setForeground(ModernTheme.TEXT_DARK);

        c.add(t, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        return c;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out from the Student portal?",
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
