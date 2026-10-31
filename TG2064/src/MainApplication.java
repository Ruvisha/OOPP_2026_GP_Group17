import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainApplication extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardContainer;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String activeSection = "HOME";
    private String passwordColumn = "password"; // detected automatically by ensureProfileColumns()

    public MainApplication() {
        setTitle("Technical Operations Portal - Faculty of Technology");
        setSize(1150, 720);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setLayout(new BorderLayout());

        // Top Header
        add(createTopHeader(), BorderLayout.NORTH);

        // Main Workspace (Sidebar + Content Cards)
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);

        // Register Modules
        cardContainer.add(new DashboardPanel(), "HOME");
        cardContainer.add(createProfilePanel(7), "PROFILE");
        cardContainer.add(new AttendancePanel(7), "ATTENDANCE");
        cardContainer.add(new MedicalPanel(7), "MEDICAL");
        cardContainer.add(new TechnicalTimetablePanel(), "TIMETABLE");
        cardContainer.add(new TechnicalNoticePanel(7), "NOTICES");

        workspace.add(cardContainer, BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);

        switchSection("HOME");
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                new EmptyBorder(12, 20, 12, 20)));

        JLabel welcomeLabel = new JLabel("Welcome, Mr. Ruwan Bandara");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        welcomeLabel.setForeground(new Color(30, 41, 59));

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        JLabel dateLabel = new JLabel(currentDate);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(148, 163, 184));

        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(new Color(15, 23, 42)); // dark navy

        // Brand Top Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(22, 18, 18, 18));

        JLabel logo = new JLabel("FOT - AMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("University of Ruhuna");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(148, 163, 184));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel role = new JLabel("Role: Technical Officer");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        role.setForeground(new Color(16, 185, 129));
        role.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandPanel.add(logo);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(sub);
        brandPanel.add(Box.createVerticalStrut(6));
        brandPanel.add(role);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Menu
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(10, 12, 10, 12));

        menu.add(createNavButton("Operations Overview", "HOME"));
        menu.add(Box.createVerticalStrut(8));
        menu.add(createNavButton("Officer Profile", "PROFILE"));
        menu.add(Box.createVerticalStrut(8));
        menu.add(createNavButton("Attendance (15 Sessions)", "ATTENDANCE"));
        menu.add(Box.createVerticalStrut(8));
        menu.add(createNavButton("Medical Records", "MEDICAL"));
        menu.add(Box.createVerticalStrut(8));
        menu.add(createNavButton("Lab & Dept Timetable", "TIMETABLE"));
        menu.add(Box.createVerticalStrut(8));
        menu.add(createNavButton("Faculty Notices", "NOTICES"));

        sidebar.add(menu, BorderLayout.CENTER);

        // Logout Button Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 16, 20, 16));

        StyledButton logoutBtn = new StyledButton("Logout from Portal",
                new Color(248, 90, 90), new Color(225, 55, 55), Color.WHITE);
        logoutBtn.setHorizontalAlignment(SwingConstants.CENTER);
        logoutBtn.setBorder(new EmptyBorder(10, 12, 10, 12));

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        footer.add(logoutBtn, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String label, String cardName) {
        StyledButton btn = new StyledButton(label, new Color(51, 65, 110), new Color(30, 41, 82), Color.WHITE);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 16, 8, 14));

        btn.addActionListener(e -> switchSection(cardName));
        navButtons.put(cardName, btn);
        return btn;
    }
    private JPanel createProfilePanel(int userId) {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel pageTitle = new JLabel("Technical Officer Profile & Laboratory Assignments");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(new Color(30, 58, 138));
        panel.add(pageTitle, BorderLayout.NORTH);

        JTextField usernameField = new JTextField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField designationField = new JTextField();
        JTextField labField = new JTextField();
        JTextField departmentField = new JTextField();

        JPasswordField currentPwField = new JPasswordField();
        JPasswordField newPwField = new JPasswordField();
        JPasswordField confirmPwField = new JPasswordField();

        // ---------- Left card: Officer Profile Information ----------
        JPanel profileCard = createProfileCard("Officer Profile Information");

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Color.WHITE);
        addProfileRow(rows, "Username (Read-Only):", usernameField, false);
        addProfileRow(rows, "Full Name:", fullNameField, true);
        addProfileRow(rows, "Email Address:", emailField, true);
        addProfileRow(rows, "Contact Telephone:", phoneField, true);
        addProfileRow(rows, "Address:", addressField, true);
        addProfileRow(rows, "Technical Designation:", designationField, true);
        addProfileRow(rows, "Assigned Laboratory:", labField, true);
        addProfileRow(rows, "Department:", departmentField, false);

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        box.add(rows, BorderLayout.NORTH);
        profileCard.add(box, BorderLayout.CENTER);

        StyledButton saveBtn = new StyledButton("Save Profile Details",
                new Color(59, 89, 200), new Color(37, 60, 160), Color.WHITE);
        saveBtn.setHorizontalAlignment(SwingConstants.CENTER);
        saveBtn.setPreferredSize(new Dimension(0, 38));
        profileCard.add(saveBtn, BorderLayout.SOUTH);

        // ---------- Right card: Security & Password Management ----------
        JPanel passwordCard = createProfileCard("Security & Password Management");

        JPanel pwRows = new JPanel();
        pwRows.setLayout(new BoxLayout(pwRows, BoxLayout.Y_AXIS));
        pwRows.setBackground(Color.WHITE);
        addProfileRow(pwRows, "Current Password:", currentPwField, true);
        addProfileRow(pwRows, "New Password:", newPwField, true);
        addProfileRow(pwRows, "Confirm New Password:", confirmPwField, true);

        JPanel pwWrapper = new JPanel(new BorderLayout());
        pwWrapper.setOpaque(false);
        pwWrapper.add(pwRows, BorderLayout.NORTH);
        passwordCard.add(pwWrapper, BorderLayout.CENTER);

        StyledButton updateBtn = new StyledButton("Update Password",
                new Color(52, 211, 105), new Color(22, 163, 74), Color.WHITE);
        updateBtn.setHorizontalAlignment(SwingConstants.CENTER);
        updateBtn.setPreferredSize(new Dimension(0, 38));
        passwordCard.add(updateBtn, BorderLayout.SOUTH);

        JPanel cards = new JPanel(new GridLayout(1, 2, 24, 0));
        cards.setOpaque(false);
        cards.add(profileCard);
        cards.add(passwordCard);
        panel.add(cards, BorderLayout.CENTER);

        // ---------- Load data from MySQL ----------
        Runnable loadProfileData = () -> {
            try (var conn = DatabaseConnection.getConnection()) {
                ensureProfileColumns(conn);
                try (var stmt = conn.prepareStatement("SELECT * FROM users WHERE user_id = ?")) {
                    stmt.setInt(1, userId);
                    var rs = stmt.executeQuery();
                    if (rs.next()) {
                        usernameField.setText(nz(rs.getString("username")));
                        fullNameField.setText(nz(rs.getString("name")));
                        emailField.setText(nz(rs.getString("email")));
                        phoneField.setText(nz(rs.getString("phone")));
                        addressField.setText(nz(rs.getString("address")));
                        designationField.setText(nz(rs.getString("designation")));
                        labField.setText(nz(rs.getString("assigned_lab")));
                        departmentField.setText(nz(rs.getString("department")));
                    } else {
                        JOptionPane.showMessageDialog(panel, "User profile not found in database.");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Database Error: " + ex.getMessage());
            }
        };

        // ---------- Save Profile Details ----------
        saveBtn.addActionListener(e -> {
            String name = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Full Name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid email address.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!phone.isEmpty() && !phone.matches("^[0-9+\\- ]{7,15}$")) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid telephone number.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "UPDATE users SET name = ?, email = ?, phone = ?, address = ?, designation = ?, assigned_lab = ? WHERE user_id = ?";
            try (var conn = DatabaseConnection.getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setString(4, addressField.getText().trim());
                ps.setString(5, designationField.getText().trim());
                ps.setString(6, labField.getText().trim());
                ps.setInt(7, userId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(panel, "Profile details saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ---------- Update Password ----------
        updateBtn.addActionListener(e -> {
            String current = new String(currentPwField.getPassword());
            String newPw = new String(newPwField.getPassword());
            String confirm = new String(confirmPwField.getPassword());

            if (newPw.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in the new password and confirmation.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newPw.length() < 6) {
                JOptionPane.showMessageDialog(panel, "New password must be at least 6 characters long.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPw.equals(confirm)) {
                JOptionPane.showMessageDialog(panel, "New password and confirmation do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (var conn = DatabaseConnection.getConnection()) {
                ensureProfileColumns(conn);

                String stored = null;
                try (var ps = conn.prepareStatement("SELECT " + passwordColumn + " FROM users WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    var rs = ps.executeQuery();
                    if (rs.next()) stored = rs.getString(1);
                }

                // If no password is stored yet, the first one can be set without a current password
                if (stored != null && !stored.isEmpty() && !stored.equals(current)) {
                    JOptionPane.showMessageDialog(panel, "Current password is incorrect.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try (var ps = conn.prepareStatement("UPDATE users SET " + passwordColumn + " = ? WHERE user_id = ?")) {
                    ps.setString(1, newPw);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                currentPwField.setText("");
                newPwField.setText("");
                confirmPwField.setText("");
                JOptionPane.showMessageDialog(panel, "Password updated successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadProfileData.run(); // Load data initially when panel opens
        return panel;
    }

    // ---------- Helpers used by createProfilePanel ----------
    private JPanel createProfileCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(new Color(30, 58, 138));
        titleLbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(titleLbl, BorderLayout.NORTH);
        return card;
    }

    private void addProfileRow(JPanel parent, String labelText, JTextField field, boolean editable) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(100, 116, 139));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(8, 0, 3, 0));

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(4, 8, 4, 8)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(200, 30));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        if (editable) {
            field.setBackground(Color.WHITE);
            field.setForeground(new Color(15, 23, 42));
        } else {
            field.setEditable(false);
            field.setBackground(new Color(226, 232, 240));
            field.setForeground(new Color(100, 116, 139));
        }

        parent.add(label);
        parent.add(field);
    }

    /** Adds any missing profile columns to the users table, and detects the password column name. */
    private void ensureProfileColumns(Connection conn) throws SQLException {
        Set<String> existing = new HashSet<>();
        try (ResultSet rs = conn.getMetaData().getColumns(conn.getCatalog(), null, "users", "%")) {
            while (rs.next()) existing.add(rs.getString("COLUMN_NAME").toLowerCase());
        }

        String[][] needed = {
                {"username", "VARCHAR(100)"}, {"email", "VARCHAR(150)"}, {"phone", "VARCHAR(20)"},
                {"address", "VARCHAR(255)"}, {"designation", "VARCHAR(100)"},
                {"assigned_lab", "VARCHAR(100)"}, {"department", "VARCHAR(150)"}
        };
        for (String[] col : needed) {
            if (!existing.contains(col[0])) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE users ADD COLUMN " + col[0] + " " + col[1] + " NULL");
                }
            }
        }

        if (existing.contains("password")) {
            passwordColumn = "password";
        } else if (existing.contains("password_hash")) {
            passwordColumn = "password_hash";
        } else {
            passwordColumn = "password";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN password VARCHAR(255) NULL");
            }
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private void switchSection(String cardName) {
        activeSection = cardName;
        cardLayout.show(cardContainer, cardName);

        // Active menu item gets a green strip on its left edge
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            ((StyledButton) entry.getValue()).setAccentBar(
                    entry.getKey().equals(cardName) ? new Color(16, 185, 129) : null);
        }
    }

    private JPanel createPlaceholderPanel(String titleText) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
        JLabel label = new JLabel(titleText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(100, 116, 139));
        panel.add(label);
        return panel;
    }

    /** Button that paints its own gradient, so colours show with every Look & Feel (Windows ignores setBackground). */
    private static class StyledButton extends JButton {
        private final Color top;
        private final Color bottom;
        private Color accentBar; // green strip on the left edge of the active menu item

        StyledButton(String text, Color top, Color bottom, Color textColor) {
            super(text);
            this.top = top;
            this.bottom = bottom;
            setForeground(textColor);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        void setAccentBar(Color accentBar) {
            this.accentBar = accentBar;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color t = top;
            Color b = bottom;
            if (getModel().isPressed()) {
                t = t.darker();
                b = b.darker();
            } else if (getModel().isRollover()) {
                t = t.brighter();
                b = b.brighter();
            }

            Shape shape = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setPaint(new GradientPaint(0, 0, t, 0, getHeight(), b));
            g2.fill(shape);

            if (accentBar != null) {
                g2.setClip(shape);
                g2.setColor(accentBar);
                g2.fillRect(0, 0, 4, getHeight());
            }
            g2.dispose();

            super.paintComponent(g); // draws the text
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApplication().setVisible(true));
    }
}