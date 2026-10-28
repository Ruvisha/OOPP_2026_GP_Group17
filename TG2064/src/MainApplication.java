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
        cardContainer.add(createPlaceholderPanel("Officer Profile View"), "PROFILE");
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
        sidebar.setBackground(new Color(24, 32, 47));

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
        logoutBtn.setForeground(Color.WHITE);
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

        // Colors & Visibility Fixes:
        btn.setForeground(Color.WHITE); // High contrast text
        btn.setBackground(new Color(36, 48, 70)); // Dark solid blue background
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 75, 100), 1, true),
                new EmptyBorder(8, 14, 8, 14)));

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!cardName.equals(activeSection)) {
                    btn.setBackground(new Color(48, 64, 92));
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!cardName.equals(activeSection)) {
                    btn.setBackground(new Color(36, 48, 70));
                    btn.setForeground(new Color(226, 232, 240));
                }
            }
        });

        btn.addActionListener(e -> switchSection(cardName));
        navButtons.put(cardName, btn);
        return btn;
    }

    private void switchSection(String cardName) {
        activeSection = cardName;
        cardLayout.show(cardContainer, cardName);

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(cardName)) {
                b.setBackground(new Color(37, 99, 235)); // Vibrant Active Blue
                b.setForeground(Color.WHITE);
                b.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(96, 165, 250), 1, true),
                        new EmptyBorder(8, 14, 8, 14)));
            } else {
                b.setBackground(new Color(36, 48, 70));
                b.setForeground(new Color(226, 232, 240));
                b.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(60, 75, 100), 1, true),
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