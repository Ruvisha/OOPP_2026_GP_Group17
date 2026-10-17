import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LMSGuiApplication extends JFrame {

    // Database Configuration (from database/database.sql:
    // faculty_academic_management)
    private static String dbHost = "localhost";
    private static String dbPort = "3306";
    private static String dbName = "faculty_academic_management";
    private static String dbUser = "root";
    private static String dbPassword = ""; // Update with your MySQL password if configured

    // Tables and Models for each module
    private DefaultTableModel attendanceModel;
    private DefaultTableModel medicalModel;
    private DefaultTableModel noticeModel;
    private DefaultTableModel timetableModel;

    // UI Status Components
    private JLabel dbStatusLabel;
    private JComboBox<String> attCourseBox;
    private JComboBox<String> medCourseBox;
    private JComboBox<String> ttCourseBox;

    public LMSGuiApplication() {
        // Set window properties
        setTitle("Learning Management System (LMS) - Faculty Academic Management");
        setSize(1080, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Apply Native System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add padding and spacing to the tabs
        UIManager.put("TabbedPane.tabInsets", new Insets(12, 20, 12, 20));

        // Create Main Tabbed Pane (Menu Navigation displayed vertically on the left)
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add modules as tabs
        tabbedPane.addTab("Attendance", createAttendancePanel());
        tabbedPane.addTab("Medical Records", createMedicalPanel());
        tabbedPane.addTab("Notices", createNoticesPanel());
        tabbedPane.addTab("Timetable", createTimetablePanel());

        // Top Toolbar with DB Status and Controls
        JPanel topBar = createTopToolbar();

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Fetch data from database or fallback to seed data
        refreshAllData();
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

    private JPanel createTopToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 5));
        bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        bar.setBackground(new Color(245, 247, 250));

        dbStatusLabel = new JLabel("Database Status: Initializing...");
        dbStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> refreshAllData());

        JButton configBtn = new JButton("DB Settings");
        configBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        configBtn.addActionListener(e -> showDbConfigDialog());

        rightControls.add(refreshBtn);
        rightControls.add(configBtn);

        bar.add(dbStatusLabel, BorderLayout.WEST);
        bar.add(rightControls, BorderLayout.EAST);
        return bar;
    }

    private void showDbConfigDialog() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
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
        }
    }

    private void updateDbStatus(boolean connected, String message) {
        if (connected) {
            dbStatusLabel.setText("Database: Connected (" + dbName + ")");
            dbStatusLabel.setForeground(new Color(20, 130, 20));
        } else {
            dbStatusLabel.setText("Database: Disconnected (" + message + " - Using Seed Data)");
            dbStatusLabel.setForeground(new Color(180, 80, 0));
        }
    }

    // ==========================================
    // 1. ATTENDANCE PANEL (Table: attendance JOIN users, courses)
    // ==========================================
    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 6, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Attendance Record (Table: attendance)"));

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
                // If DB offline, add to UI table model directly
                int nextId = attendanceModel.getRowCount() + 1;
                attendanceModel.addRow(new Object[] { nextId, regNo, name, course, date, type, 1, status });
            } else {
                loadAttendanceData();
            }
            JOptionPane.showMessageDialog(this, "Attendance recorded successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private boolean saveAttendanceToDb(String regNo, String courseCode, String date, String type, String status) {
        String sql = "INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by) "
                +
                "VALUES ((SELECT user_id FROM users WHERE student_reg_no = ? LIMIT 1), " +
                "(SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, " +
                "(SELECT COALESCE(MAX(session_number), 0) + 1 FROM attendance a2 WHERE a2.course_id = (SELECT course_id FROM courses WHERE course_code = ? LIMIT 1) AND a2.session_type = ?), "
                +
                "?, 1)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regNo);
            stmt.setString(2, courseCode);
            stmt.setString(3, date);
            stmt.setString(4, type);
            stmt.setString(5, courseCode);
            stmt.setString(6, type);
            stmt.setString(7, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // 2. MEDICAL PANEL (Table: medical JOIN users, courses)
    // ==========================================
    private JPanel createMedicalPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        formPanel.setBorder(BorderFactory.createTitledBorder("Submit Medical Record (Table: medical)"));

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
            JOptionPane.showMessageDialog(this, "Medical record submitted successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private boolean saveMedicalToDb(String regNo, String courseCode, String sType, String sDate, String subDate,
            String reason) {
        String sql = "INSERT INTO medical (student_id, course_id, session_type, session_date, submission_date, reason, status) "
                +
                "VALUES ((SELECT user_id FROM users WHERE student_reg_no = ? LIMIT 1), " +
                "(SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, ?, ?, 'NOT_APPROVED')";
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
    // 3. NOTICES PANEL (Table: notices JOIN users)
    // ==========================================
    private JPanel createNoticesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        formPanel.setBorder(BorderFactory.createTitledBorder("Post New Notice (Table: notices)"));

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
                noticeModel
                        .addRow(new Object[] { nextId, title, desc, date, target, "System Administrator", "ACTIVE" });
            } else {
                loadNoticeData();
            }
            titleField.setText("");
            descField.setText("");
            JOptionPane.showMessageDialog(this, "Notice published successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private boolean saveNoticeToDb(String title, String content, String date, String target) {
        String sql = "INSERT INTO notices (title, content, published_date, created_by, target_role, status) " +
                "VALUES (?, ?, ?, 1, ?, 'ACTIVE')";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, content);
            stmt.setString(3, date);
            stmt.setString(4, target);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ==========================================
    // 4. TIMETABLE PANEL (Table: timetables JOIN courses, users)
    // ==========================================
    private JPanel createTimetablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Timetable Entry (Table: timetables)"));

        JComboBox<String> dayBox = new JComboBox<>(
                new String[] { "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" });
        JTextField startTimeField = new JTextField("08:00:00");
        JTextField endTimeField = new JTextField("10:00:00");
        ttCourseBox = new JComboBox<>(
                new String[] { "ICT2132", "ICT2113", "ICT2122", "ICT2143", "ENG2112", "ET2101", "BST2102" });
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL" });
        JTextField roomField = new JTextField("Lecture Theatre 01");
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
                +
                "VALUES (1, (SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, ?, ?, ?, 1, '2024/2025')";
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
    // DATA FETCHING (From MySQL Tables)
    // ==========================================
    public void refreshAllData() {
        boolean connected = testDbConnection();
        if (connected) {
            updateDbStatus(true, "Connected");
            loadAttendanceData();
            loadMedicalData();
            loadNoticeData();
            loadTimetableData();
        } else {
            updateDbStatus(false, "MySQL Disconnected");
            loadInitialSeedData();
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
                "System Administrator", "ACTIVE" });

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
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(235, 238, 242));
        table.setShowGrid(true);
        table.setGridColor(new Color(225, 228, 232));

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