import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class LMSGuiApplication extends JFrame {

    // Database Configuration (XAMPP MySQL: faculty_academic_management)
    private static String dbHost = "localhost";
    private static String dbPort = "3307"; // XAMPP MySQL is active on port 3307
    private static String dbName = "faculty_academic_management";
    private static String dbUser = "root";
    private static String dbPassword = ""; // Default XAMPP root password is empty

    // Tables and Models for each module
    private DefaultTableModel attendanceModel;
    private DefaultTableModel medicalModel;
    private DefaultTableModel noticeModel;
    private DefaultTableModel timetableModel;

    // UI Status and Dynamic Lookup Components
    private JLabel dbStatusLabel;
    private JComboBox<String> attCourseBox;
    private JComboBox<String> attStudentBox;
    private JComboBox<String> medCourseBox;
    private JComboBox<String> medStudentBox;
    private JComboBox<String> ttCourseBox;
    private JComboBox<String> ttLecturerBox;

    public LMSGuiApplication() {
        setTitle("Learning Management System (LMS) - Faculty Academic Management");
        setSize(1120, 700);
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

        // Fetch live data directly from the XAMPP database
        refreshAllData();
    }

    // ==========================================
    // DATABASE CONNECTION & STATUS
    // ==========================================
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
        }

        String primaryUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            return DriverManager.getConnection(primaryUrl, dbUser, dbPassword);
        } catch (SQLException primaryEx) {
            // Automatic port fallback between 3307 and 3306 for XAMPP environments
            String fallbackPort = "3307".equals(dbPort) ? "3306" : ("3306".equals(dbPort) ? "3307" : null);
            if (fallbackPort != null) {
                try {
                    String fallbackUrl = "jdbc:mysql://" + dbHost + ":" + fallbackPort + "/" + dbName +
                            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                    Connection fallbackConn = DriverManager.getConnection(fallbackUrl, dbUser, dbPassword);
                    if (fallbackConn != null && !fallbackConn.isClosed()) {
                        dbPort = fallbackPort; // Switch to the responsive port
                        return fallbackConn;
                    }
                } catch (SQLException ignored) {
                }
            }
            throw primaryEx;
        }
    }

    private JPanel createTopToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 5));
        bar.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        bar.setBackground(new Color(245, 247, 250));

        dbStatusLabel = new JLabel("Database Status: Connecting...");
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
            dbStatusLabel.setText("Database: Connected (" + dbHost + ":" + dbPort + " / " + dbName + ")");
            dbStatusLabel.setForeground(new Color(20, 130, 20));
        } else {
            dbStatusLabel.setText("Database: Disconnected (" + message + ")");
            dbStatusLabel.setForeground(new Color(180, 40, 20));
        }
    }

    private String extractCode(Object item) {
        if (item == null) return "";
        String str = item.toString().trim();
        if (str.contains(" - ")) {
            return str.split(" - ")[0].trim();
        }
        return str;
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

        attStudentBox = new JComboBox<>();
        attStudentBox.setEditable(true);
        attCourseBox = new JComboBox<>();
        JTextField dateField = new JTextField(LocalDate.now().toString());
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL" });
        JComboBox<String> statusBox = new JComboBox<>(new String[] { "PRESENT", "ABSENT" });
        JButton addButton = new JButton("Add Record");

        formPanel.add(new JLabel("Student (Reg No):"));
        formPanel.add(attStudentBox);
        formPanel.add(new JLabel("Course:"));
        formPanel.add(attCourseBox);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);

        formPanel.add(new JLabel("Session Type:"));
        formPanel.add(typeBox);
        formPanel.add(new JLabel("Attendance Status:"));
        formPanel.add(statusBox);
        formPanel.add(addButton);

        addButton.addActionListener(e -> {
            String regNo = extractCode(attStudentBox.getSelectedItem());
            String course = extractCode(attCourseBox.getSelectedItem());
            String date = dateField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();

            if (regNo.isEmpty() || course.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select/enter Student Reg No, Course, and Date.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String error = saveAttendanceToDb(regNo, course, date, type, status);
            if (error != null) {
                JOptionPane.showMessageDialog(this, "Failed to record attendance in database:\n" + error,
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                loadAttendanceData();
                JOptionPane.showMessageDialog(this, "Attendance recorded successfully in database!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private String saveAttendanceToDb(String regNo, String courseCode, String date, String type, String status) {
        String sql = "INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by) "
                + "VALUES ((SELECT user_id FROM users WHERE student_reg_no = ? LIMIT 1), "
                + "(SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, "
                + "(SELECT COALESCE(MAX(session_number), 0) + 1 FROM (SELECT * FROM attendance) a2 WHERE a2.course_id = (SELECT course_id FROM courses WHERE course_code = ? LIMIT 1) AND a2.session_type = ?), "
                + "?, 1)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regNo);
            stmt.setString(2, courseCode);
            stmt.setString(3, date);
            stmt.setString(4, type);
            stmt.setString(5, courseCode);
            stmt.setString(6, type);
            stmt.setString(7, status);
            stmt.executeUpdate();
            return null;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("student_id")) {
                return "Student registration number '" + regNo + "' was not found in the users table.";
            }
            return e.getMessage();
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

        medStudentBox = new JComboBox<>();
        medStudentBox.setEditable(true);
        medCourseBox = new JComboBox<>();
        JComboBox<String> sessionTypeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL", "BOTH" });
        JTextField sessionDateField = new JTextField(LocalDate.now().toString());
        JTextField subDateField = new JTextField(LocalDate.now().toString());
        JTextField reasonField = new JTextField();
        JButton addButton = new JButton("Submit Medical");

        formPanel.add(new JLabel("Student (Reg No):"));
        formPanel.add(medStudentBox);
        formPanel.add(new JLabel("Course:"));
        formPanel.add(medCourseBox);
        formPanel.add(new JLabel("Session Type:"));
        formPanel.add(sessionTypeBox);

        formPanel.add(new JLabel("Session Date (YYYY-MM-DD):"));
        formPanel.add(sessionDateField);
        formPanel.add(new JLabel("Submission Date:"));
        formPanel.add(subDateField);
        formPanel.add(new JLabel("Reason:"));
        formPanel.add(reasonField);
        formPanel.add(addButton);

        addButton.addActionListener(e -> {
            String regNo = extractCode(medStudentBox.getSelectedItem());
            String course = extractCode(medCourseBox.getSelectedItem());
            String sType = (String) sessionTypeBox.getSelectedItem();
            String sDate = sessionDateField.getText().trim();
            String subDate = subDateField.getText().trim();
            String reason = reasonField.getText().trim();

            if (regNo.isEmpty() || course.isEmpty() || reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Student, Course, and Reason.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String error = saveMedicalToDb(regNo, course, sType, sDate, subDate, reason);
            if (error != null) {
                JOptionPane.showMessageDialog(this, "Failed to submit medical record to database:\n" + error,
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                loadMedicalData();
                reasonField.setText("");
                JOptionPane.showMessageDialog(this, "Medical record submitted successfully to database!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private String saveMedicalToDb(String regNo, String courseCode, String sType, String sDate, String subDate,
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
            stmt.executeUpdate();
            return null;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("student_id")) {
                return "Student registration number '" + regNo + "' was not found in the users table.";
            }
            return e.getMessage();
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
        JTextField dateField = new JTextField(LocalDate.now().toString());
        JButton addButton = new JButton("Post Notice");

        formPanel.add(new JLabel("Notice Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Content / Description:"));
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

            if (title.isEmpty() || desc.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title, content, and date cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String error = saveNoticeToDb(title, desc, date, target);
            if (error != null) {
                JOptionPane.showMessageDialog(this, "Failed to publish notice to database:\n" + error,
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                loadNoticeData();
                titleField.setText("");
                descField.setText("");
                JOptionPane.showMessageDialog(this, "Notice published successfully in database!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private String saveNoticeToDb(String title, String content, String date, String target) {
        String sql = "INSERT INTO notices (title, content, published_date, created_by, target_role, status) " +
                "VALUES (?, ?, ?, 1, ?, 'ACTIVE')";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, content);
            stmt.setString(3, date);
            stmt.setString(4, target);
            stmt.executeUpdate();
            return null;
        } catch (SQLException e) {
            return e.getMessage();
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
        ttCourseBox = new JComboBox<>();
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "THEORY", "PRACTICAL" });
        ttLecturerBox = new JComboBox<>();

        JTextField startTimeField = new JTextField("08:00:00");
        JTextField endTimeField = new JTextField("10:00:00");
        JTextField roomField = new JTextField("Lecture Theatre 01");
        JButton addButton = new JButton("Add Slot");

        formPanel.add(new JLabel("Day:"));
        formPanel.add(dayBox);
        formPanel.add(new JLabel("Course:"));
        formPanel.add(ttCourseBox);
        formPanel.add(new JLabel("Session Type:"));
        formPanel.add(typeBox);

        formPanel.add(new JLabel("Lecturer:"));
        formPanel.add(ttLecturerBox);
        formPanel.add(new JLabel("Time (Start - End):"));
        JPanel timeGroup = new JPanel(new GridLayout(1, 2, 4, 0));
        timeGroup.add(startTimeField);
        timeGroup.add(endTimeField);
        formPanel.add(timeGroup);

        formPanel.add(new JLabel("Room / Hall:"));
        formPanel.add(roomField);
        formPanel.add(addButton);

        addButton.addActionListener(e -> {
            String day = (String) dayBox.getSelectedItem();
            String course = extractCode(ttCourseBox.getSelectedItem());
            String type = (String) typeBox.getSelectedItem();
            String lecturer = (String) ttLecturerBox.getSelectedItem();
            String start = startTimeField.getText().trim();
            String end = endTimeField.getText().trim();
            String room = roomField.getText().trim();

            if (course.isEmpty() || start.isEmpty() || end.isEmpty() || room.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all timetable slot details.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String error = saveTimetableToDb(day, course, type, start, end, room, lecturer);
            if (error != null) {
                JOptionPane.showMessageDialog(this, "Failed to add timetable entry in database:\n" + error,
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                loadTimetableData();
                JOptionPane.showMessageDialog(this, "Timetable entry added successfully to database!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private String saveTimetableToDb(String day, String courseCode, String type, String start, String end,
            String room, String lecturerName) {
        String sql = "INSERT INTO timetables (department_id, course_id, day_of_week, start_time, end_time, room, session_type, semester, academic_year, lecturer_id) "
                + "VALUES (1, (SELECT course_id FROM courses WHERE course_code = ? LIMIT 1), ?, ?, ?, ?, ?, 1, '2025/2026', "
                + "(SELECT user_id FROM users WHERE full_name = ? LIMIT 1))";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, day);
            stmt.setString(3, start);
            stmt.setString(4, end);
            stmt.setString(5, room);
            stmt.setString(6, type);
            stmt.setString(7, lecturerName);
            stmt.executeUpdate();
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    // ==========================================
    // DATA FETCHING (Exclusively From MySQL Tables)
    // ==========================================
    public void refreshAllData() {
        try (Connection conn = getConnection()) {
            updateDbStatus(true, "Connected to MySQL (" + dbHost + ":" + dbPort + "/" + dbName + ")");
            loadLookupData();
            loadAttendanceData();
            loadMedicalData();
            loadNoticeData();
            loadTimetableData();
        } catch (SQLException e) {
            updateDbStatus(false, e.getMessage());
            // Clear all models when database is offline - do NOT show fake seed data
            attendanceModel.setRowCount(0);
            medicalModel.setRowCount(0);
            noticeModel.setRowCount(0);
            timetableModel.setRowCount(0);
        }
    }

    private void loadLookupData() {
        try (Connection conn = getConnection()) {
            // 1. Dynamic Courses
            String prevAttCourse = (String) attCourseBox.getSelectedItem();
            String prevMedCourse = (String) medCourseBox.getSelectedItem();
            String prevTtCourse = (String) ttCourseBox.getSelectedItem();

            attCourseBox.removeAllItems();
            medCourseBox.removeAllItems();
            ttCourseBox.removeAllItems();

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT course_code, course_name FROM courses ORDER BY course_code")) {
                while (rs.next()) {
                    String item = rs.getString("course_code") + " - " + rs.getString("course_name");
                    attCourseBox.addItem(item);
                    medCourseBox.addItem(item);
                    ttCourseBox.addItem(item);
                }
            }

            if (prevAttCourse != null) attCourseBox.setSelectedItem(prevAttCourse);
            if (prevMedCourse != null) medCourseBox.setSelectedItem(prevMedCourse);
            if (prevTtCourse != null) ttCourseBox.setSelectedItem(prevTtCourse);

            // 2. Dynamic Students
            String prevAttStudent = (String) attStudentBox.getSelectedItem();
            String prevMedStudent = (String) medStudentBox.getSelectedItem();

            attStudentBox.removeAllItems();
            medStudentBox.removeAllItems();

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT student_reg_no, full_name FROM users WHERE student_reg_no IS NOT NULL ORDER BY student_reg_no")) {
                while (rs.next()) {
                    String item = rs.getString("student_reg_no") + " - " + rs.getString("full_name");
                    attStudentBox.addItem(item);
                    medStudentBox.addItem(item);
                }
            }

            if (prevAttStudent != null) attStudentBox.setSelectedItem(prevAttStudent);
            if (prevMedStudent != null) medStudentBox.setSelectedItem(prevMedStudent);

            // 3. Dynamic Lecturers
            String prevLecturer = (String) ttLecturerBox.getSelectedItem();
            ttLecturerBox.removeAllItems();

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT full_name FROM users WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'Lecturer') ORDER BY full_name")) {
                while (rs.next()) {
                    ttLecturerBox.addItem(rs.getString("full_name"));
                }
            }

            if (prevLecturer != null) ttLecturerBox.setSelectedItem(prevLecturer);

        } catch (SQLException e) {
            System.err.println("Could not load lookup data from MySQL: " + e.getMessage());
        }
    }

    private void loadAttendanceData() {
        attendanceModel.setRowCount(0);
        String sql = "SELECT a.attendance_id, u.student_reg_no, u.full_name, c.course_code, " +
                "a.session_date, a.session_type, a.session_number, a.status " +
                "FROM attendance a " +
                "JOIN users u ON a.student_id = u.user_id " +
                "JOIN courses c ON a.course_id = c.course_id " +
                "ORDER BY a.session_date DESC, a.attendance_id DESC";

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
                "CONCAT(TIME_FORMAT(t.start_time, '%h:%i %p'), ' - ', TIME_FORMAT(t.end_time, '%h:%i %p')) AS time_slot, " +
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