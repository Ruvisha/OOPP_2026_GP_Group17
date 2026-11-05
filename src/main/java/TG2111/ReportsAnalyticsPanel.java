package com.fot.ams.ui.admin;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.DepartmentDAO;
import com.fot.ams.dao.NoticeDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.Notice;
import com.fot.ams.model.Undergraduate;
import com.fot.ams.model.User;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Modern University Reports & Analytics Portal.
 * Enables Administrators to view, audit, and export academic reports across departments,
 * student cohorts, curriculum units, and official notices.
 */
public class ReportsAnalyticsPanel extends JPanel {

    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final NoticeDAO noticeDAO;
    private final DepartmentDAO departmentDAO;

    private JComboBox<String> cmbReportType;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel lblReportSummary;
    private JLabel lblTotalRecords;

    public ReportsAnalyticsPanel() {
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
        this.noticeDAO = new NoticeDAO();
        this.departmentDAO = new DepartmentDAO();

        setLayout(new BorderLayout(16, 16));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(18, 20, 20, 20));

        initComponents();
        loadSelectedReport();
    }

    private void initComponents() {
        // Top Controls Bar
        JPanel topBar = ModernTheme.createCardPanel(10);
        topBar.setLayout(new BorderLayout(15, 0));
        topBar.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JLabel lblType = new JLabel("Academic Report Category:", ModernIcons.get("reports", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        lblType.setFont(ModernTheme.FONT_BOLD);
        lblType.setForeground(ModernTheme.TEXT_DARK);

        cmbReportType = new JComboBox<>(new String[]{
            "Student Cohort & Enrollment Report",
            "Curriculum & Course Units Summary",
            "Faculty Notices & Announcements Audit",
            "System User Accounts Distribution"
        });
        cmbReportType.setFont(ModernTheme.FONT_REGULAR);
        cmbReportType.setPreferredSize(new Dimension(300, 32));
        cmbReportType.addActionListener(e -> loadSelectedReport());

        leftPanel.add(lblType);
        leftPanel.add(cmbReportType);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton btnExport = ModernTheme.createPrimaryButton("Export Report (CSV)");
        btnExport.setIcon(ModernIcons.get("download", 14, Color.WHITE));
        btnExport.addActionListener(e -> exportToCsv());

        JButton btnPrint = ModernTheme.createSecondaryButton("Print / Text Preview");
        btnPrint.setIcon(ModernIcons.get("reports", 14, ModernTheme.TEXT_DARK));
        btnPrint.addActionListener(e -> showPrintPreview());

        rightActions.add(btnExport);
        rightActions.add(btnPrint);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Content Card
        JPanel centerCard = ModernTheme.createCardPanel(10);
        centerCard.setLayout(new BorderLayout(0, 12));

        // Subheader summary
        JPanel subheader = new JPanel(new BorderLayout());
        subheader.setOpaque(false);

        lblReportSummary = new JLabel("Report Overview");
        lblReportSummary.setFont(ModernTheme.FONT_SUBTITLE);
        lblReportSummary.setForeground(ModernTheme.TEXT_DARK);

        lblTotalRecords = ModernTheme.createBadge("Total: 0 Records", ModernTheme.PRIMARY_LIGHT, ModernTheme.PRIMARY);

        subheader.add(lblReportSummary, BorderLayout.WEST);
        subheader.add(lblTotalRecords, BorderLayout.EAST);
        centerCard.add(subheader, BorderLayout.NORTH);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        ModernTheme.styleTable(reportTable);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        add(centerCard, BorderLayout.CENTER);
    }

    private void loadSelectedReport() {
        int idx = cmbReportType.getSelectedIndex();
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        try {
            switch (idx) {
                case 0:
                    loadStudentCohortReport();
                    break;
                case 1:
                    loadCourseSummaryReport();
                    break;
                case 2:
                    loadNoticesAuditReport();
                    break;
                case 3:
                    loadUserDistributionReport();
                    break;
            }
        } catch (Exception e) {
            lblReportSummary.setText("Database Offline - Connect to XAMPP MySQL to view live report data");
            System.err.println("[ReportsAnalyticsPanel] Load report error: " + e.getMessage());
        }
    }

    private void loadStudentCohortReport() {
        lblReportSummary.setText("Undergraduate Student Cohorts & Degree Progression");
        tableModel.addColumn("Student ID");
        tableModel.addColumn("Reg No");
        tableModel.addColumn("Full Name");
        tableModel.addColumn("Department");
        tableModel.addColumn("Academic Year");
        tableModel.addColumn("Current Semester");
        tableModel.addColumn("Cohort Standing");
        tableModel.addColumn("Account Status");

        List<User> students = userDAO.getUsersByRole(4);
        for (User u : students) {
            if (u instanceof Undergraduate ug) {
                tableModel.addRow(new Object[]{
                    ug.getUserId(),
                    ug.getStudentRegNo() != null ? ug.getStudentRegNo() : "N/A",
                    ug.getFullName(),
                    ug.getDepartmentName() != null ? ug.getDepartmentName() : "General",
                    ug.getAcademicYear() != null ? ug.getAcademicYear() : "2024/2025",
                    "Semester " + ug.getCurrentSemester(),
                    ug.getStudentType() != null ? ug.getStudentType() : "REGULAR",
                    ug.getStatus()
                });
            }
        }
        lblTotalRecords.setText("Total Undergraduates: " + students.size());
    }

    private void loadCourseSummaryReport() {
        lblReportSummary.setText("Curriculum Course Units & Department Offerings");
        tableModel.addColumn("Course ID");
        tableModel.addColumn("Course Code");
        tableModel.addColumn("Course Name");
        tableModel.addColumn("Credits");
        tableModel.addColumn("Study Year");
        tableModel.addColumn("Semester");
        tableModel.addColumn("Theory");
        tableModel.addColumn("Practical");

        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            tableModel.addRow(new Object[]{
                c.getCourseId(),
                c.getCourseCode(),
                c.getCourseName(),
                c.getCredits() + " Credits",
                "Year " + c.getStudyYear(),
                "Semester " + c.getSemester(),
                c.isHasTheory() ? "YES" : "NO",
                c.isHasPractical() ? "YES" : "NO"
            });
        }
        lblTotalRecords.setText("Total Curriculum Units: " + courses.size());
    }

    private void loadNoticesAuditReport() {
        lblReportSummary.setText("Faculty Announcements & Official Bulletins Audit");
        tableModel.addColumn("Notice ID");
        tableModel.addColumn("Notice Title");
        tableModel.addColumn("Published Date");
        tableModel.addColumn("Target Audience");
        tableModel.addColumn("Status");

        List<Notice> notices = noticeDAO.getAllNotices();
        for (Notice n : notices) {
            tableModel.addRow(new Object[]{
                n.getNoticeId(),
                n.getTitle(),
                n.getPublishedDate(),
                n.getTargetRole(),
                n.getStatus()
            });
        }
        lblTotalRecords.setText("Total Notices: " + notices.size());
    }

    private void loadUserDistributionReport() {
        lblReportSummary.setText("System Staff & Student User Accounts Directory");
        tableModel.addColumn("User ID");
        tableModel.addColumn("Username");
        tableModel.addColumn("Role");
        tableModel.addColumn("Full Name");
        tableModel.addColumn("Email");
        tableModel.addColumn("Department");
        tableModel.addColumn("Status");

        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getRoleName(),
                u.getFullName(),
                u.getEmail(),
                u.getDepartmentName() != null ? u.getDepartmentName() : "Faculty Level",
                u.getStatus()
            });
        }
        lblTotalRecords.setText("Total Accounts: " + users.size());
    }

    private void exportToCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available to export.", "Export Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String defaultName = "FOT_AMS_Report_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv";
        fileChooser.setSelectedFile(new File(defaultName));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                // Header
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    fw.write("\"" + tableModel.getColumnName(c) + "\"");
                    if (c < tableModel.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");

                // Rows
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        Object val = tableModel.getValueAt(r, c);
                        fw.write("\"" + (val != null ? val.toString() : "") + "\"");
                        if (c < tableModel.getColumnCount() - 1) fw.write(",");
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + file.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPrintPreview() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("FACULTY OF TECHNOLOGY - UNIVERSITY OF RUHUNA\n");
        sb.append("ACADEMIC MANAGEMENT SYSTEM (FOT-AMS) - OFFICIAL REPORT\n");
        sb.append("Category: ").append(cmbReportType.getSelectedItem()).append("\n");
        sb.append("Generated on: ").append(new Date()).append("\n");
        sb.append("========================================================================================\n\n");

        for (int r = 0; r < tableModel.getRowCount(); r++) {
            sb.append(String.format("[%03d] ", r + 1));
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                sb.append(tableModel.getColumnName(c)).append(": ").append(tableModel.getValueAt(r, c)).append(" | ");
            }
            sb.append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(800, 500));

        JOptionPane.showMessageDialog(this, scroll, "Official Report Preview", JOptionPane.PLAIN_MESSAGE);
    }
}
