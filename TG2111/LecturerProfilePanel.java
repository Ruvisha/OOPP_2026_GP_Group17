package com.fot.ams.ui.lecturer;

import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Lecturer;
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
 * Panel allowing a Lecturer to view and update their profile (except username).
 */
public class LecturerProfilePanel extends JPanel {
    private final UserService userService;
    private final AuthenticationService authService;

    private JTextField txtUsername;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtDesignation;
    private JTextField txtOfficeRoom;
    private JTextField txtDepartment;

    // Password change fields
    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public LecturerProfilePanel() {
        this.userService = new UserService();
        this.authService = new AuthenticationService();
        initComponents();
        loadProfileData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Lecturer Academic Profile & Credentials");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel gridContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        gridContainer.setOpaque(false);

        // Left Card: Profile Details
        JPanel profileCard = ModernTheme.createCardPanel();
        profileCard.setLayout(new BorderLayout());

        JLabel pHeader = new JLabel("Personal & Academic Details");
        pHeader.setFont(ModernTheme.FONT_SUBTITLE);
        pHeader.setForeground(ModernTheme.PRIMARY);
        pHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        profileCard.add(pHeader, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);

        txtUsername = new JTextField();
        txtUsername.setEnabled(false); // Username is strictly read-only per SRS!

        txtFullName = new JTextField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        txtDesignation = new JTextField();
        txtOfficeRoom = new JTextField();
        txtDepartment = new JTextField();
        txtDepartment.setEnabled(false);

        fieldsPanel.add(createRow("Username (Read-Only):", txtUsername));
        fieldsPanel.add(createRow("Full Name:", txtFullName));
        fieldsPanel.add(createRow("Email Address:", txtEmail));
        fieldsPanel.add(createRow("Contact Phone:", txtPhone));
        fieldsPanel.add(createRow("Residential / Postal Address:", txtAddress));
        fieldsPanel.add(createRow("Academic Designation:", txtDesignation));
        fieldsPanel.add(createRow("Office Room Location:", txtOfficeRoom));
        fieldsPanel.add(createRow("Department:", txtDepartment));

        profileCard.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);

        JButton btnSaveProfile = ModernTheme.createPrimaryButton("Save Profile Changes");
        btnSaveProfile.addActionListener(e -> handleSaveProfile());
        profileCard.add(btnSaveProfile, BorderLayout.SOUTH);

        // Right Card: Password Management
        JPanel passCard = ModernTheme.createCardPanel();
        passCard.setLayout(new BorderLayout());

        JLabel passHeader = new JLabel("Security & Password Management");
        passHeader.setFont(ModernTheme.FONT_SUBTITLE);
        passHeader.setForeground(ModernTheme.PRIMARY);
        passHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        passCard.add(passHeader, BorderLayout.NORTH);

        JPanel passFields = new JPanel();
        passFields.setLayout(new BoxLayout(passFields, BoxLayout.Y_AXIS));
        passFields.setBackground(Color.WHITE);

        txtCurrentPass = new JPasswordField();
        txtNewPass = new JPasswordField();
        txtConfirmPass = new JPasswordField();

        passFields.add(createRow("Current Password:", txtCurrentPass));
        passFields.add(createRow("New Password:", txtNewPass));
        passFields.add(createRow("Confirm New Password:", txtConfirmPass));

        JLabel note = new JLabel("<html><i>Note: Choose a strong password containing at least 4 characters.</i></html>");
        note.setFont(ModernTheme.FONT_SMALL);
        note.setForeground(ModernTheme.TEXT_MUTED);
        passFields.add(note);

        passCard.add(passFields, BorderLayout.CENTER);

        JButton btnChangePass = ModernTheme.createSuccessButton("Update Password");
        btnChangePass.addActionListener(e -> handleChangePassword());
        passCard.add(btnChangePass, BorderLayout.SOUTH);

        gridContainer.add(profileCard);
        gridContainer.add(passCard);

        add(gridContainer, BorderLayout.CENTER);
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

    public void loadProfileData() {
        User cur = SessionManager.getCurrentUser();
        if (cur instanceof Lecturer lec) {
            txtUsername.setText(lec.getUsername());
            txtFullName.setText(lec.getFullName());
            txtEmail.setText(lec.getEmail());
            txtPhone.setText(lec.getPhone() != null ? lec.getPhone() : "");
            txtAddress.setText(lec.getAddress() != null ? lec.getAddress() : "");
            txtDesignation.setText(lec.getDesignation() != null ? lec.getDesignation() : "");
            txtOfficeRoom.setText(lec.getOfficeRoom() != null ? lec.getOfficeRoom() : "");
            txtDepartment.setText(lec.getDepartmentName() != null ? lec.getDepartmentName() : "ICT");
        }
    }

    private void handleSaveProfile() {
        User cur = SessionManager.getCurrentUser();
        if (cur instanceof Lecturer lec) {
            try {
                lec.setFullName(txtFullName.getText().trim());
                lec.setEmail(txtEmail.getText().trim());
                lec.setPhone(txtPhone.getText().trim());
                lec.setAddress(txtAddress.getText().trim());
                lec.setDesignation(txtDesignation.getText().trim());
                lec.setOfficeRoom(txtOfficeRoom.getText().trim());

                userService.updateUser(lec);
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
