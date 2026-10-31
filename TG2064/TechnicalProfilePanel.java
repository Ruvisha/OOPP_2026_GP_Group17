package com.fot.ams.ui.technical;

import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.TechnicalOfficer;
import com.fot.ams.model.User;
import com.fot.ams.service.AuthenticationService;
import com.fot.ams.service.UserService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Panel enabling Technical Officers to view and maintain their profile (except username).
 */
public class TechnicalProfilePanel extends JPanel {
    private final UserService userService;
    private final AuthenticationService authService;

    private JTextField txtUsername;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtDesignation;
    private JTextField txtAssignedLab;
    private JTextField txtDepartment;

    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public TechnicalProfilePanel() {
        this.userService = new UserService();
        this.authService = new AuthenticationService();
        initComponents();
        loadProfile();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Technical Officer Profile & Laboratory Assignments");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);

        // Left: Profile details
        JPanel profileCard = ModernTheme.createCardPanel();
        profileCard.setLayout(new BorderLayout());

        JLabel pHeader = new JLabel("Officer Profile Information");
        pHeader.setFont(ModernTheme.FONT_SUBTITLE);
        pHeader.setForeground(ModernTheme.PRIMARY);
        pHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        profileCard.add(pHeader, BorderLayout.NORTH);

        JPanel f = new JPanel();
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setBackground(Color.WHITE);

        txtUsername = new JTextField();
        txtUsername.setEnabled(false); // Immutable username

        txtFullName = new JTextField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        txtDesignation = new JTextField();
        txtAssignedLab = new JTextField();
        txtDepartment = new JTextField();
        txtDepartment.setEnabled(false);

        f.add(createRow("Username (Read-Only):", txtUsername));
        f.add(createRow("Full Name:", txtFullName));
        f.add(createRow("Email Address:", txtEmail));
        f.add(createRow("Contact Telephone:", txtPhone));
        f.add(createRow("Address:", txtAddress));
        f.add(createRow("Technical Designation:", txtDesignation));
        f.add(createRow("Assigned Laboratory:", txtAssignedLab));
        f.add(createRow("Department:", txtDepartment));

        profileCard.add(new JScrollPane(f), BorderLayout.CENTER);

        JButton btnSave = ModernTheme.createPrimaryButton("Save Profile Details");
        btnSave.addActionListener(e -> handleSave());
        profileCard.add(btnSave, BorderLayout.SOUTH);

        // Right: Password Management
        JPanel passCard = ModernTheme.createCardPanel();
        passCard.setLayout(new BorderLayout());

        JLabel passHeader = new JLabel("Security & Password Management");
        passHeader.setFont(ModernTheme.FONT_SUBTITLE);
        passHeader.setForeground(ModernTheme.PRIMARY);
        passHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        passCard.add(passHeader, BorderLayout.NORTH);

        JPanel pf = new JPanel();
        pf.setLayout(new BoxLayout(pf, BoxLayout.Y_AXIS));
        pf.setBackground(Color.WHITE);

        txtCurrentPass = new JPasswordField();
        txtNewPass = new JPasswordField();
        txtConfirmPass = new JPasswordField();

        pf.add(createRow("Current Password:", txtCurrentPass));
        pf.add(createRow("New Password:", txtNewPass));
        pf.add(createRow("Confirm New Password:", txtConfirmPass));

        passCard.add(pf, BorderLayout.CENTER);

        JButton btnPass = ModernTheme.createSuccessButton("Update Password");
        btnPass.addActionListener(e -> handleChangePassword());
        passCard.add(btnPass, BorderLayout.SOUTH);

        grid.add(profileCard);
        grid.add(passCard);

        add(grid, BorderLayout.CENTER);
    }

    private JPanel createRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 48));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_MUTED);

        comp.setFont(ModernTheme.FONT_REGULAR);
        if (comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(200, 28));
            comp.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(2, 6, 2, 6)));
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(2, 0, 6, 0));
        return p;
    }

    public void loadProfile() {
        User cur = SessionManager.getCurrentUser();
        if (cur instanceof TechnicalOfficer to) {
            txtUsername.setText(to.getUsername());
            txtFullName.setText(to.getFullName());
            txtEmail.setText(to.getEmail());
            txtPhone.setText(to.getPhone() != null ? to.getPhone() : "");
            txtAddress.setText(to.getAddress() != null ? to.getAddress() : "");
            txtDesignation.setText(to.getDesignation() != null ? to.getDesignation() : "");
            txtAssignedLab.setText(to.getAssignedLab() != null ? to.getAssignedLab() : "");
            txtDepartment.setText(to.getDepartmentName() != null ? to.getDepartmentName() : "ICT");
        }
    }

    private void handleSave() {
        User cur = SessionManager.getCurrentUser();
        if (cur instanceof TechnicalOfficer to) {
            try {
                to.setFullName(txtFullName.getText().trim());
                to.setEmail(txtEmail.getText().trim());
                to.setPhone(txtPhone.getText().trim());
                to.setAddress(txtAddress.getText().trim());
                to.setDesignation(txtDesignation.getText().trim());
                to.setAssignedLab(txtAssignedLab.getText().trim());

                userService.updateUser(to);
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleChangePassword() {
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        String curPass = new String(txtCurrentPass.getPassword());
        String newPass = new String(txtNewPass.getPassword());
        String confPass = new String(txtConfirmPass.getPassword());

        if (!newPass.equals(confPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirm password do not match.", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            authService.changePassword(cur.getUserId(), curPass, newPass);
            JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            txtCurrentPass.setText("");
            txtNewPass.setText("");
            txtConfirmPass.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Password Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
