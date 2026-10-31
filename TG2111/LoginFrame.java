package com.fot.ams.ui;

import com.fot.ams.exception.AuthenticationException;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.User;
import com.fot.ams.service.AuthenticationService;
import com.fot.ams.ui.admin.AdminDashboard;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.ui.lecturer.LecturerDashboard;
import com.fot.ams.ui.technical.TechnicalOfficerDashboard;
import com.fot.ams.ui.undergraduate.UndergraduateDashboard;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Authentication and Entry Point Window for Faculty Academic Management System.
 */
public class LoginFrame extends JFrame {
    private final AuthenticationService authService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton togglePasswordButton;
    private char passwordEchoChar;
    private JButton loginButton;
    private JButton clearButton;
    private JButton exitButton;

    public LoginFrame() {
        this.authService = new AuthenticationService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Faculty of Technology Academic Management System - University of Ruhuna");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BG_MAIN);

        // Left Banner Panel (Branding)
        JPanel bannerPanel = new JPanel();
        bannerPanel.setPreferredSize(new Dimension(360, 600));
        bannerPanel.setBackground(ModernTheme.PRIMARY);
        bannerPanel.setLayout(new BoxLayout(bannerPanel, BoxLayout.Y_AXIS));
        bannerPanel.setBorder(new EmptyBorder(60, 30, 40, 30));

        JLabel facultyLabel = new JLabel("FACULTY OF");
        facultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        facultyLabel.setForeground(new Color(191, 219, 254));
        facultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel techLabel = new JLabel("TECHNOLOGY");
        techLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        techLabel.setForeground(Color.WHITE);
        techLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel uniLabel = new JLabel("University of Ruhuna");
        uniLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        uniLabel.setForeground(new Color(224, 231, 255));
        uniLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel divider = new JLabel("____________________________");
        divider.setForeground(new Color(96, 165, 250));
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel systemTitle = new JLabel("Academic Management");
        systemTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        systemTitle.setForeground(Color.WHITE);
        systemTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel systemSubtitle = new JLabel("System (AMS)");
        systemSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        systemSubtitle.setForeground(new Color(254, 240, 138));
        systemSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        //JLabel courseUnit = new JLabel("ICT2132 - OOP Practicum");
        //courseUnit.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        //courseUnit.setForeground(new Color(203, 213, 225));
        //courseUnit.setAlignmentX(Component.CENTER_ALIGNMENT);

        //JLabel groupLabel = new JLabel("Group 17 Project");
        //groupLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        //roupLabel.setForeground(new Color(241, 245, 249));
        //groupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bannerPanel.add(facultyLabel);
        bannerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        bannerPanel.add(techLabel);
        bannerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        bannerPanel.add(uniLabel);
        bannerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        bannerPanel.add(divider);
        bannerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        bannerPanel.add(systemTitle);
        bannerPanel.add(systemSubtitle);
        bannerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        //bannerPanel.add(courseUnit);
        // bannerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        //bannerPanel.add(groupLabel);

        // Right Form Panel
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(ModernTheme.BG_MAIN);

        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(460, 480));
        formCard.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(30, 35, 30, 35)));

        JLabel loginHeader = new JLabel("Portal Login");
        loginHeader.setFont(ModernTheme.FONT_TITLE);
        loginHeader.setForeground(ModernTheme.TEXT_DARK);

        JLabel loginSub = new JLabel("Enter your academic credentials to sign in");
        loginSub.setFont(ModernTheme.FONT_REGULAR);
        loginSub.setForeground(ModernTheme.TEXT_MUTED);

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ModernTheme.FONT_BOLD);
        userLabel.setForeground(ModernTheme.TEXT_DARK);

        usernameField = new JTextField(20);
        usernameField.setFont(ModernTheme.FONT_REGULAR);
        usernameField.setPreferredSize(new Dimension(380, 38));
        usernameField.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(5, 10, 5, 10)));

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ModernTheme.FONT_BOLD);
        passLabel.setForeground(ModernTheme.TEXT_DARK);

        passwordField = new JPasswordField(20);
        passwordField.setFont(ModernTheme.FONT_REGULAR);
        passwordField.setPreferredSize(new Dimension(380, 38));
        passwordField.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(5, 10, 5, 10)));
        passwordEchoChar = passwordField.getEchoChar();

        // Button to show or hide the password while typing.
        togglePasswordButton = new JButton("\uD83D\uDC41");
        togglePasswordButton.setToolTipText("Show password");
        togglePasswordButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        togglePasswordButton.setFocusable(false);
        togglePasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePasswordButton.setPreferredSize(new Dimension(45, 38));
        togglePasswordButton.addActionListener(e -> {
            boolean hidden = passwordField.getEchoChar() != 0;
            if (hidden) {
                passwordField.setEchoChar((char) 0);
                togglePasswordButton.setToolTipText("Hide password");
            } else {
                passwordField.setEchoChar(passwordEchoChar);
                togglePasswordButton.setToolTipText("Show password");
            }
        });

        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setOpaque(false);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(togglePasswordButton, BorderLayout.EAST);

        loginButton = ModernTheme.createPrimaryButton("Login to Portal");
        loginButton.setPreferredSize(new Dimension(380, 42));
        loginButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));

        clearButton = ModernTheme.createSecondaryButton("Clear");
        exitButton = ModernTheme.createDangerButton("Exit");

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(clearButton);
        buttonRow.add(exitButton);

        // Quick Demo Accounts Fill Panel (helpful for examiners/reviewers)
        JLabel demoLabel = new JLabel("Quick Demo Credentials:");
        demoLabel.setFont(ModernTheme.FONT_SMALL);
        demoLabel.setForeground(ModernTheme.TEXT_MUTED);

        JPanel demoButtons = new JPanel(new GridLayout(1, 4, 6, 0));
        demoButtons.setOpaque(false);
        JButton btnAdmin = createQuickBtn("Admin", "admin", "admin123");
        JButton btnLec = createQuickBtn("Lecturer", "lec_kamal", "lec123");
        JButton btnTO = createQuickBtn("Tech Officer", "to_saman", "to123");
        JButton btnStudent = createQuickBtn("Student", "tg20242001", "student123");
        demoButtons.add(btnAdmin);
        demoButtons.add(btnLec);
        demoButtons.add(btnTO);
        demoButtons.add(btnStudent);

        formCard.add(loginHeader);
        formCard.add(Box.createRigidArea(new Dimension(0, 4)));
        formCard.add(loginSub);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));
        formCard.add(userLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));
        formCard.add(usernameField);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));
        formCard.add(passLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));
        formCard.add(passwordPanel);
        formCard.add(Box.createRigidArea(new Dimension(0, 22)));
        formCard.add(loginButton);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));
        formCard.add(buttonRow);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));
        formCard.add(demoLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));
        formCard.add(demoButtons);

        formContainer.add(formCard);

        mainPanel.add(bannerPanel, BorderLayout.WEST);
        mainPanel.add(formContainer, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Event Handlers
        loginButton.addActionListener(e -> performLogin());
        clearButton.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            usernameField.requestFocus();
        });
        exitButton.addActionListener(e -> System.exit(0));

        // Enter key to login
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);
    }

    private JButton createQuickBtn(String title, String user, String pass) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(ModernTheme.TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            usernameField.setText(user);
            passwordField.setText(pass);
        });
        return btn;
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.login(username, password);
            JOptionPane.showMessageDialog(
                this,
                "Welcome back, " + user.getFullName() + "!\nLogged in as: " + user.getRoleName(),
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Role-based Dashboard routing
            dispose();
            switch (user.getRoleId()) {
                case 1:
                    new AdminDashboard().setVisible(true);
                    break;
                case 2:
                    new LecturerDashboard().setVisible(true);
                    break;
                case 3:
                    new TechnicalOfficerDashboard().setVisible(true);
                    break;
                case 4:
                    new UndergraduateDashboard().setVisible(true);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Unknown role. Cannot open dashboard.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (ValidationException | AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Database error: " + ex.getMessage() + "\nMake sure XAMPP MySQL is running.",
                "System Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
