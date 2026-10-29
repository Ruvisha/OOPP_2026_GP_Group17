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

public class MainApplication extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardContainer;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String activeSection = "HOME";

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
        sidebar.setBackground(Color.black);

        // Brand Top Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(22, 18, 18, 18));

        JLabel logo = new JLabel("FOT - AMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("University of Ruhuna");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(148, 163, 184));

        JLabel role = new JLabel("Role: Technical Officer");
        role.setFont(new Font("Segoe UI", Font.BOLD, 11));
        role.setForeground(new Color(16, 185, 129));

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

        JButton logoutBtn = new JButton("Logout from Portal");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setBackground(new Color(239, 68, 68)); // Red color
        logoutBtn.setFocusPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.setContentAreaFilled(true);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 38, 38), 1, true),
                new EmptyBorder(8, 12, 8, 12)));

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
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Essential fixes so Swing actually renders custom background colors
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setFocusPainted(false);

        // ==========================================
        // 1. NORMAL / INACTIVE (Green Background, Black Text)
        // ==========================================
        btn.setBackground(new Color(160, 210, 180)); // Soft Green Background
        btn.setForeground(Color.BLACK);             // Black text

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 160, 130), 1, true),
                new EmptyBorder(8, 14, 8, 14)));

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!cardName.equals(activeSection)) {
                    // ==========================================
                    // 2. MOUSE HOVER (Brighter Green, Black Text)
                    // ==========================================
                    btn.setBackground(new Color(130, 195, 155));
                    btn.setForeground(Color.BLACK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!cardName.equals(activeSection)) {
                    // Revert back to normal green when mouse leaves
                    btn.setBackground(new Color(160, 210, 180));
                    btn.setForeground(Color.BLACK);
                }
            }
        });

        btn.addActionListener(e -> switchSection(cardName));
        navButtons.put(cardName, btn);
        return btn;
    }
    private JPanel createProfilePanel(int userId) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel(new GridLayout(5, 2, 10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel idVal = new JLabel();
        JLabel nameVal = new JLabel();
        JLabel roleVal = new JLabel();
        JLabel statusVal = new JLabel();

        card.add(new JLabel("User ID:")); card.add(idVal);
        card.add(new JLabel("Full Name:")); card.add(nameVal);
        card.add(new JLabel("Role:")); card.add(roleVal);
        card.add(new JLabel("System Status:")); card.add(statusVal);

        JButton refreshBtn = new JButton("Refresh Profile");
        card.add(new JLabel("")); card.add(refreshBtn);

        // Code to fetch data from your XAMPP MySQL database
        Runnable loadProfileData = () -> {
            try (var conn = DatabaseConnection.getConnection();
                 var stmt = conn.prepareStatement("SELECT * FROM users WHERE user_id = ?")) {

                stmt.setInt(1, userId);
                var rs = stmt.executeQuery();

                if (rs.next()) {
                    idVal.setText(String.valueOf(rs.getInt("user_id")));
                    nameVal.setText(rs.getString("name")); // Adjust column name if it's 'username' or 'full_name' in your table
                    roleVal.setText(rs.getString("role"));
                    statusVal.setText("Active");
                } else {
                    JOptionPane.showMessageDialog(panel, "User profile not found in database.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Database Error: " + ex.getMessage());
            }
        };

        refreshBtn.addActionListener(e -> loadProfileData.run());
        loadProfileData.run(); // Load data initially when panel opens

        panel.add(card);
        return panel;
    }

    private void switchSection(String cardName) {
        activeSection = cardName;
        cardLayout.show(cardContainer, cardName);

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(cardName)) {
                // ==========================================
                // 3. ACTIVE / CLICKED (Vibrant Green, Black Text)
                // ==========================================
                b.setBackground(new Color(52, 211, 153)); // Active Green Background
                b.setForeground(Color.BLACK);             // Black text
                b.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(16, 185, 129), 1, true),
                        new EmptyBorder(8, 14, 8, 14)));
            } else {
                // Inactive state for all other buttons in the loop
                b.setBackground(new Color(160, 210, 180));
                b.setForeground(Color.BLACK);             // Black text
                b.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(100, 160, 130), 1, true),
                        new EmptyBorder(8, 14, 8, 14)));
            }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApplication().setVisible(true));
    }
}