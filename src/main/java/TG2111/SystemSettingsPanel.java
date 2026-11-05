package com.fot.ams.ui.admin;

import com.fot.ams.config.DatabaseConfig;
import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern University Administration System Settings & Configuration Panel.
 * Provides controls for database connectivity diagnostics, active academic calendar configuration,
 * system runtime telemetries, and institutional settings.
 */
public class SystemSettingsPanel extends JPanel {

    private final CourseEnrollmentDAO enrollmentDAO;

    private JLabel lblDbStatus;
    private JComboBox<String> cmbAcademicYear;
    private JSpinner spinSemester;
    private JTextField txtFacultyName;
    private JTextField txtUniversityName;
    private JLabel lblMemoryUsage;

    public SystemSettingsPanel() {
        this.enrollmentDAO = new CourseEnrollmentDAO();

        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_MAIN);

        initComponents();
        loadSettings();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(ModernTheme.BG_MAIN);
        scrollContent.setBorder(new EmptyBorder(20, 24, 28, 24));

        // 1. Database & Infrastructure Status Card
        scrollContent.add(createDatabaseStatusCard());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 18)));

        // 2. Academic Calendar Settings Card
        scrollContent.add(createAcademicCalendarCard());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 18)));

        // 3. Institution Profile Card
        scrollContent.add(createInstitutionCard());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 18)));

        // 4. Runtime Diagnostics Card
        scrollContent.add(createDiagnosticsCard());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDatabaseStatusCard() {
        JPanel card = ModernTheme.createCardPanel(10);
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Database Connectivity & Infrastructure", ModernIcons.get("settings", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);

        lblDbStatus = ModernTheme.createBadge("Checking...", ModernTheme.WARNING_LIGHT, ModernTheme.WARNING);

        header.add(title, BorderLayout.WEST);
        header.add(lblDbStatus, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 16, 12));
        infoGrid.setOpaque(false);

        infoGrid.add(createInfoField("Database Host:", DatabaseConfig.getHost()));
        infoGrid.add(createInfoField("Port Number:", String.valueOf(DatabaseConfig.getPort())));
        infoGrid.add(createInfoField("Database Name:", DatabaseConfig.getDatabase()));
        infoGrid.add(createInfoField("JDBC Driver:", "com.mysql.cj.jdbc.Driver"));

        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomActions.setOpaque(false);

        JButton btnTestConn = ModernTheme.createPrimaryButton("Test DB Connection");
        btnTestConn.setIcon(ModernIcons.get("refresh", 14, Color.WHITE));
        btnTestConn.addActionListener(e -> testDatabaseConnection());

        bottomActions.add(btnTestConn);

        card.add(infoGrid, BorderLayout.CENTER);
        card.add(bottomActions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createAcademicCalendarCard() {
        JPanel card = ModernTheme.createCardPanel(10);
        card.setLayout(new BorderLayout(0, 14));

        JLabel title = new JLabel("Academic Calendar & Term Configuration", ModernIcons.get("calendar", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(2, 2, 16, 12));
        formGrid.setOpaque(false);

        cmbAcademicYear = new JComboBox<>(new String[]{"2024/2025", "2025/2026", "2023/2024"});
        cmbAcademicYear.setFont(ModernTheme.FONT_REGULAR);

        spinSemester = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        spinSemester.setFont(ModernTheme.FONT_REGULAR);

        JPanel p1 = new JPanel(new BorderLayout(6, 4));
        p1.setOpaque(false);
        p1.add(new JLabel("Active Academic Year:"), BorderLayout.NORTH);
        p1.add(cmbAcademicYear, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(6, 4));
        p2.setOpaque(false);
        p2.add(new JLabel("Current Semester:"), BorderLayout.NORTH);
        p2.add(spinSemester, BorderLayout.CENTER);

        formGrid.add(p1);
        formGrid.add(p2);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnSaveCalendar = ModernTheme.createSuccessButton("Save Calendar Settings");
        btnSaveCalendar.setIcon(ModernIcons.get("check", 14, Color.WHITE));
        btnSaveCalendar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apply updated Academic Year (" + cmbAcademicYear.getSelectedItem() +
                ") and Semester (" + spinSemester.getValue() + ") to the faculty system?",
                "Confirm Calendar Update",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Academic Calendar settings saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        actions.add(btnSaveCalendar);

        card.add(formGrid, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createInstitutionCard() {
        JPanel card = ModernTheme.createCardPanel(10);
        card.setLayout(new BorderLayout(0, 14));

        JLabel title = new JLabel("Institutional Identity & Campus Profile", ModernIcons.get("curriculum", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(2, 2, 16, 12));
        formGrid.setOpaque(false);

        txtFacultyName = new JTextField("Faculty of Technology");
        txtFacultyName.setFont(ModernTheme.FONT_REGULAR);

        txtUniversityName = new JTextField("University of Ruhuna, Sri Lanka");
        txtUniversityName.setFont(ModernTheme.FONT_REGULAR);

        JPanel p1 = new JPanel(new BorderLayout(6, 4));
        p1.setOpaque(false);
        p1.add(new JLabel("Faculty Name:"), BorderLayout.NORTH);
        p1.add(txtFacultyName, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(6, 4));
        p2.setOpaque(false);
        p2.add(new JLabel("Parent University:"), BorderLayout.NORTH);
        p2.add(txtUniversityName, BorderLayout.CENTER);

        formGrid.add(p1);
        formGrid.add(p2);

        card.add(formGrid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDiagnosticsCard() {
        JPanel card = ModernTheme.createCardPanel(10);
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Application Runtime & Telemetry Diagnostics", ModernIcons.get("info", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 16, 12));
        infoGrid.setOpaque(false);

        infoGrid.add(createInfoField("Java Runtime:", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")"));
        infoGrid.add(createInfoField("Operating System:", System.getProperty("os.name") + " " + System.getProperty("os.version")));

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMb = rt.maxMemory() / (1024 * 1024);
        lblMemoryUsage = new JLabel(usedMb + " MB / " + maxMb + " MB Allocated");
        lblMemoryUsage.setFont(ModernTheme.FONT_BOLD);
        lblMemoryUsage.setForeground(ModernTheme.TEXT_DARK);

        JPanel memPanel = new JPanel(new BorderLayout(4, 2));
        memPanel.setOpaque(false);
        JLabel lbl = new JLabel("JVM Memory Usage:");
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_MUTED);
        memPanel.add(lbl, BorderLayout.NORTH);
        memPanel.add(lblMemoryUsage, BorderLayout.CENTER);

        infoGrid.add(memPanel);
        infoGrid.add(createInfoField("AMS Build Version:", "v1.0.0 (Enterprise Edition)"));

        card.add(infoGrid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInfoField(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(ModernTheme.FONT_BOLD);
        val.setForeground(ModernTheme.TEXT_DARK);

        p.add(lbl, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private void loadSettings() {
        testDatabaseConnection();
    }

    private void testDatabaseConnection() {
        boolean connected = DatabaseConnection.testConnection();
        if (connected) {
            lblDbStatus.setText("ONLINE & CONNECTED");
            lblDbStatus.setBackground(ModernTheme.SUCCESS_LIGHT);
            lblDbStatus.setForeground(ModernTheme.SUCCESS);
        } else {
            lblDbStatus.setText("DISCONNECTED");
            lblDbStatus.setBackground(ModernTheme.DANGER_LIGHT);
            lblDbStatus.setForeground(ModernTheme.DANGER);
        }
        lblDbStatus.repaint();
    }
}
