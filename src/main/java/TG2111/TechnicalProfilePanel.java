package com.fot.ams.ui.technical;

import com.fot.ams.database.DatabaseConnection;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.TechnicalOfficer;
import com.fot.ams.model.User;
import com.fot.ams.service.AuthenticationService;
import com.fot.ams.service.UserService;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.PasswordUtil;
import com.fot.ams.util.SessionManager;
import com.fot.ams.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Panel enabling Technical Officers to view and maintain their profile.
 * Redesigned into a single unified profile information card with view-only
 * and edit modes, backed permanently by MySQL database persistence.
 */
public class TechnicalProfilePanel extends JPanel {
    private final UserService userService;
    private final AuthenticationService authService;

    // Profile input fields
    private JTextField txtUsername;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtDesignation;
    private JTextField txtAssignedLab;
    private JTextField txtDepartment;

    // Mode status indicator badge
    private JLabel lblModeBadge;

    // Action buttons
    private JButton btnEditProfile;
    private JButton btnSaveChanges;
    private JButton btnCancel;

    // Password fields
    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public TechnicalProfilePanel() {
        this.userService = new UserService();
        this.authService = new AuthenticationService();
        initComponents();
        loadLatestProfileFromDatabase();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Top Header Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 4));
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Technical Officer Profile & Laboratory Assignments");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JLabel subtitle = new JLabel("View and maintain your personal and professional profile details");
        subtitle.setFont(ModernTheme.FONT_REGULAR);
        subtitle.setForeground(ModernTheme.TEXT_MUTED);

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(subtitle, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Center: Single Unified Profile Card
        JPanel profileCard = ModernTheme.createCardPanel();
        profileCard.setLayout(new BorderLayout(20, 20));

        // Card Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel pHeader = new JLabel("Officer Profile Information");
        pHeader.setFont(ModernTheme.FONT_SUBTITLE);
        pHeader.setForeground(ModernTheme.PRIMARY);

        lblModeBadge = new JLabel("Mode: View Only");
        lblModeBadge.setFont(ModernTheme.FONT_BOLD);
        lblModeBadge.setForeground(ModernTheme.TEXT_MUTED);
        lblModeBadge.setOpaque(true);
        lblModeBadge.setBackground(new Color(241, 245, 249));
        lblModeBadge.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));

        cardHeader.add(pHeader, BorderLayout.WEST);
        cardHeader.add(lblModeBadge, BorderLayout.EAST);
        profileCard.add(cardHeader, BorderLayout.NORTH);

        // Initialize text fields
        txtUsername = new JTextField();
        txtFullName = new JTextField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        txtDesignation = new JTextField();
        txtAssignedLab = new JTextField();
        txtDepartment = new JTextField();

        // 2-Column Balanced Form Grid
        JPanel formGrid = new JPanel(new GridLayout(1, 2, 35, 0));
        formGrid.setOpaque(false);

        // Left Column (Fields 1-4)
        JPanel colLeft = new JPanel();
        colLeft.setLayout(new BoxLayout(colLeft, BoxLayout.Y_AXIS));
        colLeft.setOpaque(false);

        colLeft.add(createFieldRow("Username (Read-Only):", txtUsername));
        colLeft.add(Box.createRigidArea(new Dimension(0, 14)));
        colLeft.add(createFieldRow("Full Name:", txtFullName));
        colLeft.add(Box.createRigidArea(new Dimension(0, 14)));
        colLeft.add(createFieldRow("Email Address:", txtEmail));
        colLeft.add(Box.createRigidArea(new Dimension(0, 14)));
        colLeft.add(createFieldRow("Contact Telephone:", txtPhone));

        // Right Column (Fields 5-8)
        JPanel colRight = new JPanel();
        colRight.setLayout(new BoxLayout(colRight, BoxLayout.Y_AXIS));
        colRight.setOpaque(false);

        colRight.add(createFieldRow("Address:", txtAddress));
        colRight.add(Box.createRigidArea(new Dimension(0, 14)));
        colRight.add(createFieldRow("Technical Designation:", txtDesignation));
        colRight.add(Box.createRigidArea(new Dimension(0, 14)));
        colRight.add(createFieldRow("Assigned Laboratory:", txtAssignedLab));
        colRight.add(Box.createRigidArea(new Dimension(0, 14)));
        colRight.add(createFieldRow("Department (Read-Only):", txtDepartment));

        formGrid.add(colLeft);
        formGrid.add(colRight);

        JScrollPane scrollPane = new JScrollPane(formGrid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        profileCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnEditProfile = ModernTheme.createPrimaryButton("Edit Profile");
        btnEditProfile.setPreferredSize(new Dimension(140, 38));
        btnEditProfile.addActionListener(e -> handleEditProfile());

        btnSaveChanges = ModernTheme.createSuccessButton("Save Changes");
        btnSaveChanges.setPreferredSize(new Dimension(140, 38));
        btnSaveChanges.addActionListener(e -> handleSaveProfile());

        btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(120, 38));
        btnCancel.addActionListener(e -> handleCancelEditing());

        buttonPanel.add(btnEditProfile);
        buttonPanel.add(btnSaveChanges);
        buttonPanel.add(btnCancel);

        profileCard.add(buttonPanel, BorderLayout.SOUTH);

        // Content panel containing Profile Card & Password Card
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(profileCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        contentPanel.add(createSecurityCard());

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);

        // Initially in VIEW-ONLY mode
        disableProfileEditing();
    }

    private JPanel createFieldRow(String labelText, JTextField comp) {
        JPanel p = new JPanel(new BorderLayout(5, 4));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 62));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_BOLD);
        lbl.setForeground(ModernTheme.TEXT_MUTED);

        comp.setFont(ModernTheme.FONT_REGULAR);
        comp.setPreferredSize(new Dimension(250, 34));

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    /**
     * Loads the latest Technical Officer profile directly from the MySQL database
     * using the current logged-in user's user_id. Updates the session and UI.
     */
    public void loadLatestProfileFromDatabase() {
        User cur = SessionManager.getCurrentUser();
        if (cur != null) {
            try {
                User freshUser = userService.getUserById(cur.getUserId());
                if (freshUser instanceof TechnicalOfficer to) {
                    SessionManager.setCurrentUser(to);
                }
            } catch (Exception ex) {
                System.err.println("[TechnicalProfilePanel] Error fetching latest profile from database: " + ex.getMessage());
            }
        }
        loadProfile();
        disableProfileEditing();
    }

    /**
     * Populates UI text fields with current session information.
     */
    public void loadProfile() {
        User cur = SessionManager.getCurrentUser();
        if (cur instanceof TechnicalOfficer to) {
            txtUsername.setText(to.getUsername() != null ? to.getUsername() : "");
            txtFullName.setText(to.getFullName() != null ? to.getFullName() : "");
            txtEmail.setText(to.getEmail() != null ? to.getEmail() : "");
            txtPhone.setText(to.getPhone() != null ? to.getPhone() : "");
            txtAddress.setText(to.getAddress() != null ? to.getAddress() : "");
            txtDesignation.setText(to.getDesignation() != null ? to.getDesignation() : "");
            txtAssignedLab.setText(to.getAssignedLab() != null ? to.getAssignedLab() : "");
            txtDepartment.setText(to.getDepartmentName() != null ? to.getDepartmentName() : "ICT");
        }
    }

    /**
     * Switches the card interface to Edit mode.
     * Enables editing for editable fields and reveals Save / Cancel buttons.
     */
    public void enableProfileEditing() {
        setFieldEditable(txtFullName, true);
        setFieldEditable(txtEmail, true);
        setFieldEditable(txtPhone, true);
        setFieldEditable(txtAddress, true);
        setFieldEditable(txtDesignation, true);
        setFieldEditable(txtAssignedLab, true);

        // Keep Username and Department strictly disabled
        setFieldReadOnly(txtUsername);
        setFieldReadOnly(txtDepartment);

        // Toggle action buttons
        btnEditProfile.setVisible(false);
        btnSaveChanges.setVisible(true);
        btnCancel.setVisible(true);

        if (lblModeBadge != null) {
            lblModeBadge.setText("Mode: Editing Profile");
            lblModeBadge.setForeground(ModernTheme.ACCENT);
            lblModeBadge.setBackground(ModernTheme.PRIMARY_LIGHT);
        }

        revalidate();
        repaint();
    }

    /**
     * Switches the card interface to View-Only mode.
     * Disables editing for all fields and reveals the Edit Profile button.
     */
    public void disableProfileEditing() {
        setFieldViewOnly(txtFullName);
        setFieldViewOnly(txtEmail);
        setFieldViewOnly(txtPhone);
        setFieldViewOnly(txtAddress);
        setFieldViewOnly(txtDesignation);
        setFieldViewOnly(txtAssignedLab);

        setFieldReadOnly(txtUsername);
        setFieldReadOnly(txtDepartment);

        // Toggle action buttons
        btnEditProfile.setVisible(true);
        btnSaveChanges.setVisible(false);
        btnCancel.setVisible(false);

        if (lblModeBadge != null) {
            lblModeBadge.setText("Mode: View Only");
            lblModeBadge.setForeground(ModernTheme.TEXT_MUTED);
            lblModeBadge.setBackground(new Color(241, 245, 249));
        }

        revalidate();
        repaint();
    }

    private void setFieldEditable(JTextField tf, boolean editable) {
        tf.setEditable(editable);
        tf.setEnabled(editable);
        if (editable) {
            tf.setBackground(Color.WHITE);
            tf.setForeground(ModernTheme.TEXT_DARK);
            tf.setBorder(new CompoundBorder(new LineBorder(ModernTheme.ACCENT, 1), new EmptyBorder(4, 10, 4, 10)));
        } else {
            tf.setBackground(new Color(248, 250, 252));
            tf.setForeground(ModernTheme.TEXT_DARK);
            tf.setDisabledTextColor(ModernTheme.TEXT_DARK);
            tf.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(4, 10, 4, 10)));
        }
    }

    private void setFieldViewOnly(JTextField tf) {
        tf.setEditable(false);
        tf.setEnabled(false);
        tf.setBackground(new Color(248, 250, 252));
        tf.setForeground(ModernTheme.TEXT_DARK);
        tf.setDisabledTextColor(ModernTheme.TEXT_DARK);
        tf.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(4, 10, 4, 10)));
    }

    private void setFieldReadOnly(JTextField tf) {
        tf.setEditable(false);
        tf.setEnabled(false);
        tf.setBackground(new Color(241, 245, 249));
        tf.setForeground(new Color(100, 116, 139));
        tf.setDisabledTextColor(new Color(100, 116, 139));
        tf.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(4, 10, 4, 10)));
    }

    /**
     * Handler invoked when the user clicks 'Edit Profile'.
     */
    public void handleEditProfile() {
        enableProfileEditing();
    }

    /**
     * Handler invoked when the user clicks 'Cancel'.
     * Discards unsaved changes, restores previously saved info, and returns to VIEW-ONLY mode.
     */
    public void handleCancelEditing() {
        loadLatestProfileFromDatabase();
        disableProfileEditing();
    }

    /**
     * Validates all profile inputs before submitting changes to the database.
     *
     * @throws ValidationException if any validation check fails
     */
    public void validateProfileDetails() throws ValidationException {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String designation = txtDesignation.getText().trim();
        String assignedLab = txtAssignedLab.getText().trim();

        ValidationUtil.validateNotEmpty(fullName, "Full Name");
        ValidationUtil.validateNotEmpty(email, "Email Address");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validateNotEmpty(phone, "Contact Telephone");
        ValidationUtil.validatePhone(phone);
        ValidationUtil.validateNotEmpty(address, "Address");
        ValidationUtil.validateNotEmpty(designation, "Technical Designation");
        ValidationUtil.validateNotEmpty(assignedLab, "Assigned Laboratory");
    }

    /**
     * Directly updates the Technical Officer profile fields in XAMPP MySQL
     * using PreparedStatement and user_id.
     */
    private boolean updateTechnicalOfficerInMySQL(TechnicalOfficer to) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, address = ?, " +
                     "designation = ?, assigned_lab = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, to.getFullName());
            stmt.setString(2, to.getEmail());
            stmt.setString(3, to.getPhone());
            stmt.setString(4, to.getAddress());
            stmt.setString(5, to.getDesignation());
            stmt.setString(6, to.getAssignedLab());
            stmt.setInt(7, to.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TechnicalProfilePanel] MySQL update error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handler invoked when the user clicks 'Save Changes'.
     * Validates inputs, updates MySQL database, refreshes session, and toggles view mode.
     */
    public void handleSaveProfile() {
        User cur = SessionManager.getCurrentUser();
        if (!(cur instanceof TechnicalOfficer to)) {
            JOptionPane.showMessageDialog(this, "No active Technical Officer session found.", "Session Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            validateProfileDetails();

            // Set new values on the officer model
            to.setFullName(txtFullName.getText().trim());
            to.setEmail(txtEmail.getText().trim());
            to.setPhone(txtPhone.getText().trim());
            to.setAddress(txtAddress.getText().trim());
            to.setDesignation(txtDesignation.getText().trim());
            to.setAssignedLab(txtAssignedLab.getText().trim());

            // Run validation and database update through UserService and MySQL PreparedStatement
            userService.updateUser(to);
            boolean updated = updateTechnicalOfficerInMySQL(to);

            if (!updated) {
                JOptionPane.showMessageDialog(
                    this,
                    "Database update was not completed. Please verify that MySQL service is running in XAMPP.",
                    "Update Failed",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Retrieve the fresh record from the database to confirm persistence and update session
            User freshUser = userService.getUserById(to.getUserId());
            if (freshUser instanceof TechnicalOfficer freshTo) {
                SessionManager.setCurrentUser(freshTo);
            } else {
                SessionManager.setCurrentUser(to);
            }

            // Return to view-only mode and display the newly saved information
            loadProfile();
            disableProfileEditing();

            JOptionPane.showMessageDialog(
                this,
                "Profile information has been successfully updated and saved in MySQL!",
                "Update Successful",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "An error occurred while updating the profile in MySQL: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Backward-compatible handleSave delegating to handleSaveProfile().
     */
    public void handleSave() {
        handleSaveProfile();
    }

    /**
     * Backend password change operation preserved to avoid breaking authentication features.
     */
    public boolean changePassword(String currentPass, String newPass) throws Exception {
        User cur = SessionManager.getCurrentUser();
        if (cur == null) {
            throw new ValidationException("No active user session.");
        }
        return authService.changePassword(cur.getUserId(), currentPass, newPass);
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

        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 12));
        grid.setOpaque(false);

        txtCurrentPass = new JPasswordField();
        txtNewPass = new JPasswordField();
        txtConfirmPass = new JPasswordField();

        grid.add(createField("Current Password:", txtCurrentPass));
        grid.add(new JPanel()); // Empty spacer
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

        JButton btnCancelPass = ModernTheme.createSecondaryButton("Cancel");
        btnCancelPass.addActionListener(e -> {
            txtCurrentPass.setText("");
            txtNewPass.setText("");
            txtConfirmPass.setText("");
        });

        JButton btnUpdatePass = ModernTheme.createSuccessButton("Change Password");
        btnUpdatePass.addActionListener(e -> handleChangePassword());

        actions.add(btnCancelPass);
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
        lbl.setForeground(ModernTheme.TEXT_MUTED);
        field.setPreferredSize(new Dimension(220, 32));
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void handleChangePassword() {
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        String curPass = new String(txtCurrentPass.getPassword()).trim();
        String newPass = new String(txtNewPass.getPassword()).trim();
        String confPass = new String(txtConfirmPass.getPassword()).trim();

        if (curPass.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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

        if (!newPass.equals(confPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirm password do not match.", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authService.changePassword(cur.getUserId(), curPass, newPass);
            JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            txtCurrentPass.setText("");
            txtNewPass.setText("");
            txtConfirmPass.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Password Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
