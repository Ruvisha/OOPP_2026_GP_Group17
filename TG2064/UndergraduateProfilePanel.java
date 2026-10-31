package com.fot.ams.ui.undergraduate;

import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Undergraduate;
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
import java.io.File;

/**
 * Panel enabling Undergraduates to view their profile, update contact details, and update profile photo.
 */
public class UndergraduateProfilePanel extends JPanel {
    private final UserService userService;
    private final AuthenticationService authService;

    // Read-only profile fields
    private JTextField txtRegNo;
    private JTextField txtFullName;
    private JTextField txtUsername;
    private JTextField txtDepartment;
    private JTextField txtStudentType;
    private JTextField txtAcademicYear;
    private JTextField txtSemester;

    // Editable contact details
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtProfilePicPath;

    // Password change
    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public UndergraduateProfilePanel() {
        this.userService = new UserService();
        this.authService = new AuthenticationService();
        initComponents();
        loadProfileData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Undergraduate Student Profile & Contact Settings");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);

        // Left Card: Academic & Contact Details
        JPanel profileCard = ModernTheme.createCardPanel();
        profileCard.setLayout(new BorderLayout());

        JLabel pHeader = new JLabel("Personal & Contact Details");
        pHeader.setFont(ModernTheme.FONT_SUBTITLE);
        pHeader.setForeground(ModernTheme.PRIMARY);
        pHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        profileCard.add(pHeader, BorderLayout.NORTH);

        JPanel f = new JPanel();
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setBackground(Color.WHITE);

        txtRegNo = new JTextField();
        txtRegNo.setEnabled(false);
        txtFullName = new JTextField();
        txtFullName.setEnabled(false);
        txtUsername = new JTextField();
        txtUsername.setEnabled(false);
        txtDepartment = new JTextField();
        txtDepartment.setEnabled(false);
        txtStudentType = new JTextField();
        txtStudentType.setEnabled(false);
        txtAcademicYear = new JTextField();
        txtAcademicYear.setEnabled(false);
        txtSemester = new JTextField();
        txtSemester.setEnabled(false);

        // Editable
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        txtProfilePicPath = new JTextField();

        JButton btnBrowsePic = ModernTheme.createSecondaryButton("Browse Pic");
        btnBrowsePic.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = ch.getSelectedFile();
                txtProfilePicPath.setText(file.getAbsolutePath());
            }
        });

        JPanel picRow = new JPanel(new BorderLayout(5, 0));
        picRow.setOpaque(false);
        picRow.add(txtProfilePicPath, BorderLayout.CENTER);
        picRow.add(btnBrowsePic, BorderLayout.EAST);

        f.add(createRow("Registration No (Read-Only):", txtRegNo));
        f.add(createRow("Full Name (Read-Only):", txtFullName));
        f.add(createRow("Student Status (Regular / Repeat / Missed):", txtStudentType));
        f.add(createRow("Department & Faculty:", txtDepartment));
        f.add(createRow("Academic Year & Semester:", txtAcademicYear));
        f.add(Box.createRigidArea(new Dimension(0, 8)));
        f.add(new JSeparator());
        f.add(Box.createRigidArea(new Dimension(0, 8)));
        f.add(createRow("Contact Email Address (Editable):", txtEmail));
        f.add(createRow("Contact Phone Number (Editable):", txtPhone));
        f.add(createRow("Residential / Postal Address (Editable):", txtAddress));
        f.add(createRow("Profile Picture File Path (Editable):", picRow));

        profileCard.add(new JScrollPane(f), BorderLayout.CENTER);

        JButton btnSaveContact = ModernTheme.createPrimaryButton("Update Contact Details & Picture");
        btnSaveContact.addActionListener(e -> handleSaveContact());
        profileCard.add(btnSaveContact, BorderLayout.SOUTH);

        // Right Card: Password Management
        JPanel passCard = ModernTheme.createCardPanel();
        passCard.setLayout(new BorderLayout());

        JLabel passHeader = new JLabel("Account Security & Password");
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

        JLabel info = new JLabel("<html><i>Students can update their contact details, address, profile picture and login password. Course registration and official registration numbers are managed by the Dean's Office.</i></html>");
        info.setFont(ModernTheme.FONT_SMALL);
        info.setForeground(ModernTheme.TEXT_MUTED);
        pf.add(Box.createRigidArea(new Dimension(0, 15)));
        pf.add(info);

        passCard.add(pf, BorderLayout.CENTER);

        JButton btnPass = ModernTheme.createSuccessButton("Change Password");
        btnPass.addActionListener(e -> handleChangePassword());
        passCard.add(btnPass, BorderLayout.SOUTH);

        grid.add(profileCard);
        grid.add(passCard);

        add(grid, BorderLayout.CENTER);
    }

    private JPanel createRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_MUTED);

        comp.setFont(ModernTheme.FONT_REGULAR);
        if (comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(200, 26));
            comp.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(2, 6, 2, 6)));
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(2, 0, 4, 0));
        return p;
    }

    public void loadProfileData() {
        User cur = SessionManager.getCurrentUser();
        if (cur instanceof Undergraduate ug) {
            txtRegNo.setText(ug.getStudentRegNo());
            txtFullName.setText(ug.getFullName());
            txtUsername.setText(ug.getUsername());
            txtDepartment.setText(ug.getDepartmentName() != null ? ug.getDepartmentName() : "ICT");
            txtStudentType.setText(ug.getStudentType() + (ug.isRepeat() ? " (Repeat Student)" : ug.isBatchMissed() ? " (Batch-Missed Student)" : ""));
            txtAcademicYear.setText(ug.getAcademicYear() + " - Semester " + ug.getCurrentSemester());

            txtEmail.setText(ug.getEmail());
            txtPhone.setText(ug.getPhone() != null ? ug.getPhone() : "");
            txtAddress.setText(ug.getAddress() != null ? ug.getAddress() : "");
            txtProfilePicPath.setText(ug.getProfilePic() != null ? ug.getProfilePic() : "default_avatar.png");
        }
    }

    private void handleSaveContact() {
        User cur = SessionManager.getCurrentUser();
        if (cur instanceof Undergraduate ug) {
            try {
                String email = txtEmail.getText().trim();
                String phone = txtPhone.getText().trim();
                String address = txtAddress.getText().trim();
                String pic = txtProfilePicPath.getText().trim();

                userService.updateContactDetails(ug.getUserId(), email, phone, address);
                if (!pic.isEmpty()) {
                    userService.updateProfilePicture(ug.getUserId(), pic);
                    ug.setProfilePic(pic);
                }

                ug.setEmail(email);
                ug.setPhone(phone);
                ug.setAddress(address);

                JOptionPane.showMessageDialog(this, "Contact details and profile picture updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating contact info: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
