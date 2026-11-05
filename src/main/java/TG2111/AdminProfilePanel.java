package com.fot.ams.ui.admin;

import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.User;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.PasswordUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Administrator Profile & Account Security Management Panel.
 * Allows viewing administrator credentials, modifying contact details, and updating login passwords.
 */
public class AdminProfilePanel extends JPanel {

    private final UserDAO userDAO;

    // Contact info fields
    private JTextField txtFullName;
    private JTextField txtUsername;
    private JTextField txtRole;
    private JTextField txtRoleSpecificId;
    private JTextField txtDesignation;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;

    // Password fields
    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public AdminProfilePanel() {
        this.userDAO = new UserDAO();

        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_MAIN);

        initComponents();
        loadProfileData();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(ModernTheme.BG_MAIN);
        scrollContent.setBorder(new EmptyBorder(20, 24, 28, 24));

        // 1. Top Header Banner
        scrollContent.add(createHeaderBanner());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 18)));

        // 2. Profile Details & Contact Form Card
        scrollContent.add(createContactCard());
        scrollContent.add(Box.createRigidArea(new Dimension(0, 18)));

        // 3. Security & Password Change Card
        scrollContent.add(createSecurityCard());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Deep Navy gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, ModernTheme.NAVY,
                    getWidth(), 0, ModernTheme.PRIMARY
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setLayout(new BorderLayout(20, 0));
        banner.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(56, 56));
        avatar.setLayout(new GridBagLayout());
        avatar.add(new JLabel(ModernIcons.get("profile", 30, Color.WHITE)));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        User cur = SessionManager.getCurrentUser();
        String nameStr = cur != null ? cur.getFullName() : "System Administrator";
        String desigStr = cur != null ? cur.getRoleTitle() : "Senior Assistant Registrar";

        JLabel nameLbl = new JLabel(nameStr);
        nameLbl.setFont(ModernTheme.FONT_TITLE);
        nameLbl.setForeground(Color.WHITE);

        JLabel desigLbl = new JLabel(desigStr + " • Faculty of Technology");
        desigLbl.setFont(ModernTheme.FONT_REGULAR);
        desigLbl.setForeground(new Color(191, 219, 254));

        titles.add(nameLbl);
        titles.add(Box.createRigidArea(new Dimension(0, 4)));
        titles.add(desigLbl);

        left.add(avatar);
        left.add(titles);

        banner.add(left, BorderLayout.WEST);
        return banner;
    }

    private JPanel createContactCard() {
        JPanel card = ModernTheme.createCardPanel(10);
        card.setLayout(new BorderLayout(0, 16));

        JLabel title = new JLabel("Official Administrator Details & Contact Information", ModernIcons.get("profile", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4, 2, 16, 12));
        grid.setOpaque(false);

        txtFullName = new JTextField();
        txtFullName.setEditable(false);

        txtUsername = new JTextField();
        txtUsername.setEditable(false);

        txtRole = new JTextField();
        txtRole.setEditable(false);

        txtRoleSpecificId = new JTextField();
        txtRoleSpecificId.setEditable(false);

        txtDesignation = new JTextField();
        txtDesignation.setEditable(false);

        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();

        grid.add(createField("Full Name:", txtFullName));
        grid.add(createField("Username:", txtUsername));
        grid.add(createField("System Role:", txtRole));
        grid.add(createField("Role Specific ID:", txtRoleSpecificId));
        grid.add(createField("Designation:", txtDesignation));
        grid.add(createField("Official Email Address:", txtEmail));
        grid.add(createField("Contact Telephone:", txtPhone));
        grid.add(createField("Office / Postal Address:", txtAddress));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnSave = ModernTheme.createPrimaryButton("Save Contact Changes");
        btnSave.setIcon(ModernIcons.get("check", 14, Color.WHITE));
        btnSave.addActionListener(e -> saveContactDetails());
        actions.add(btnSave);

        card.add(grid, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createSecurityCard() {
        JPanel card = ModernTheme.createCardPanel(10);
        card.setLayout(new BorderLayout(0, 16));

        JLabel title = new JLabel("Security & Change Password", ModernIcons.get("shield", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 12));
        grid.setOpaque(false);

        txtCurrentPass = new JPasswordField();
        txtNewPass = new JPasswordField();
        txtConfirmPass = new JPasswordField();

        grid.add(createField("Current Password:", txtCurrentPass));
        grid.add(new JPanel()); // Empty placeholder to align nicely
        grid.add(createField("New Password (min 8 characters):", txtNewPass));
        grid.add(createField("Confirm New Password:", txtConfirmPass));

        centerPanel.add(grid);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JCheckBox chkShowPass = new JCheckBox("Show / Hide Password");
        chkShowPass.setFont(ModernTheme.FONT_REGULAR);
        chkShowPass.setForeground(ModernTheme.TEXT_DARK);
        chkShowPass.setOpaque(false);
        chkShowPass.addActionListener(e -> {
            char echo = chkShowPass.isSelected() ? (char) 0 : '•';
            txtCurrentPass.setEchoChar(echo);
            txtNewPass.setEchoChar(echo);
            txtConfirmPass.setEchoChar(echo);
        });
        centerPanel.add(chkShowPass);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.setIcon(ModernIcons.get("close", 14, ModernTheme.TEXT_MUTED));
        btnCancel.addActionListener(e -> {
            txtCurrentPass.setText("");
            txtNewPass.setText("");
            txtConfirmPass.setText("");
        });

        JButton btnUpdatePass = ModernTheme.createSuccessButton("Change Password");
        btnUpdatePass.setIcon(ModernIcons.get("shield", 14, Color.WHITE));
        btnUpdatePass.addActionListener(e -> updatePassword());

        actions.add(btnCancel);
        actions.add(btnUpdatePass);

        card.add(centerPanel, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void loadProfileData() {
        try {
            User cur = SessionManager.getCurrentUser();
            if (cur == null) {
                // Fallback load admin user from DB
                cur = userDAO.getUserByUsername("admin");
            }
            if (cur != null) {
                txtFullName.setText(cur.getFullName());
                txtUsername.setText(cur.getUsername());
                txtRole.setText(cur.getRoleName() != null ? cur.getRoleName() : "Administrator");
                txtRoleSpecificId.setText(cur.getRoleSpecificId() != null ? cur.getRoleSpecificId() : "ADM-001");
                txtDesignation.setText("Senior Assistant Registrar");
                txtEmail.setText(cur.getEmail());
                txtPhone.setText(cur.getPhone() != null ? cur.getPhone() : "");
                txtAddress.setText(cur.getAddress() != null ? cur.getAddress() : "");
            } else {
                txtFullName.setText("System Administrator");
                txtUsername.setText("admin");
                txtRole.setText("Administrator");
                txtRoleSpecificId.setText("ADM-001");
                txtDesignation.setText("Senior Assistant Registrar");
                txtEmail.setText("admin@fot.ruh.ac.lk");
            }
        } catch (Exception e) {
            txtFullName.setText("System Administrator");
            txtUsername.setText("admin");
            txtRole.setText("Administrator");
            txtRoleSpecificId.setText("ADM-001");
            txtDesignation.setText("Senior Assistant Registrar");
            txtEmail.setText("admin@fot.ruh.ac.lk");
            System.err.println("[AdminProfilePanel] Profile load notice: " + e.getMessage());
        }
    }

    private void saveContactDetails() {
        User cur = SessionManager.getCurrentUser();
        if (cur == null) cur = userDAO.getUserByUsername("admin");
        if (cur == null) return;

        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email address cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean ok = userDAO.updateContactDetails(cur.getUserId(), email, phone, address);
            if (ok) {
                cur.setEmail(email);
                cur.setPhone(phone);
                cur.setAddress(address);
                JOptionPane.showMessageDialog(this, "Contact details updated successfully.", "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update contact details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePassword() {
        User cur = SessionManager.getCurrentUser();
        if (cur == null) cur = userDAO.getUserByUsername("admin");
        if (cur == null) return;

        String currentPass = new String(txtCurrentPass.getPassword()).trim();
        String newPass = new String(txtNewPass.getPassword()).trim();
        String confirmPass = new String(txtConfirmPass.getPassword()).trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!PasswordUtil.verifyPassword(currentPass, cur.getPasswordHash())) {
            JOptionPane.showMessageDialog(this, "Current password does not match.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newPass.length() < 8) {
            JOptionPane.showMessageDialog(this, "New password must be at least 8 characters long.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!PasswordUtil.isValidPassword(newPass)) {
            JOptionPane.showMessageDialog(this, "New password must contain at least one uppercase letter (A-Z), one lowercase letter (a-z), and one number (0-9).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation password do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String newHash = PasswordUtil.hashPassword(newPass);
            boolean ok = userDAO.updatePassword(cur.getUserId(), newHash);
            if (ok) {
                cur.setPasswordHash(newHash);
                txtCurrentPass.setText("");
                txtNewPass.setText("");
                txtConfirmPass.setText("");
                JOptionPane.showMessageDialog(this, "Password changed successfully.", "Security Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
