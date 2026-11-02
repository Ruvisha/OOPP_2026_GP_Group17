package com.fot.ams.ui.admin;

import com.fot.ams.dao.DepartmentDAO;
import com.fot.ams.dao.RoleDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.*;
import com.fot.ams.service.UserService;
import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Administrative panel for managing user accounts (CRUD operations).
 */
public class UserManagementPanel extends JPanel {
    private final UserService userService;
    private final RoleDAO roleDAO;
    private final DepartmentDAO departmentDAO;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> roleFilterCombo;

    // Form inputs
    private JTextField txtUserId;
    private JComboBox<String> cmbRole;
    private JTextField txtFullName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JComboBox<DepartmentItem> cmbDepartment;
    private JComboBox<String> cmbStatus;

    // Role-specific fields
    private JTextField txtRegNo;
    private JComboBox<String> cmbStudentType;
    private JTextField txtAcademicYear;
    private JSpinner spinSemester;
    private JTextField txtDesignation;
    private JTextField txtExtraDetail; // Office Room or Assigned Lab
    private JLabel lblExtraDetail;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    public UserManagementPanel() {
        this.userService = new UserService();
        this.roleDAO = new RoleDAO();
        this.departmentDAO = new DepartmentDAO();
        initComponents();
        loadDepartments();
        loadUsers(0);
    }

    private static class DepartmentItem {
        final int id;
        final String name;

        DepartmentItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel filterLabel = new JLabel("Filter by Role:");
        filterLabel.setFont(ModernTheme.FONT_BOLD);
        roleFilterCombo = new JComboBox<>(new String[]{"All Roles", "Admin", "Lecturer", "Technical Officer", "Undergraduate"});
        roleFilterCombo.setFont(ModernTheme.FONT_REGULAR);
        roleFilterCombo.addActionListener(e -> {
            int selectedIndex = roleFilterCombo.getSelectedIndex();
            loadUsers(selectedIndex);
        });

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh List");
        btnRefresh.addActionListener(e -> loadUsers(roleFilterCombo.getSelectedIndex()));

        topBar.add(filterLabel);
        topBar.add(roleFilterCombo);
        topBar.add(btnRefresh);

        // Center: Split Pane (Left Table, Right Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.62);
        splitPane.setDividerLocation(650);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"ID", "Role", "Username", "Full Name", "Email", "Phone", "Dept", "Reg No / Details", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        ModernTheme.styleTable(userTable);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane scrollTable = new JScrollPane(userTable);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollTable, BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = ModernTheme.createCardPanel();
        formPanel.setLayout(new BorderLayout());
        formPanel.setPreferredSize(new Dimension(380, 500));

        JLabel formTitle = new JLabel("User Details / Edit Form");
        formTitle.setFont(ModernTheme.FONT_SUBTITLE);
        formTitle.setForeground(ModernTheme.PRIMARY);
        formTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        formPanel.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsContainer = new JPanel();
        fieldsContainer.setLayout(new BoxLayout(fieldsContainer, BoxLayout.Y_AXIS));
        fieldsContainer.setBackground(Color.WHITE);

        txtUserId = new JTextField();
        txtUserId.setVisible(false);

        cmbRole = new JComboBox<>(new String[]{"Admin", "Lecturer", "Technical Officer", "Undergraduate"});
        txtFullName = new JTextField();
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        cmbDepartment = new JComboBox<>();
        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        txtRegNo = new JTextField();
        cmbStudentType = new JComboBox<>(new String[]{"REGULAR", "REPEAT", "BATCH_MISSED"});
        txtAcademicYear = new JTextField("2024/2025");
        spinSemester = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        txtDesignation = new JTextField();
        txtExtraDetail = new JTextField();
        lblExtraDetail = new JLabel("Office / Lab Room:");

        cmbRole.addActionListener(e -> updateRoleSpecificFieldsVisibility());

        fieldsContainer.add(createFieldRow("User Role:", cmbRole));
        fieldsContainer.add(createFieldRow("Full Name:", txtFullName));
        fieldsContainer.add(createFieldRow("Username:", txtUsername));
        fieldsContainer.add(createFieldRow("Password (Blank to keep):", txtPassword));
        fieldsContainer.add(createFieldRow("Email Address:", txtEmail));
        fieldsContainer.add(createFieldRow("Phone Number:", txtPhone));
        fieldsContainer.add(createFieldRow("Address:", txtAddress));
        fieldsContainer.add(createFieldRow("Department:", cmbDepartment));
        fieldsContainer.add(createFieldRow("Status:", cmbStatus));

        // Dynamic extra fields
        fieldsContainer.add(createFieldRow("Reg No (Undergraduate):", txtRegNo));
        fieldsContainer.add(createFieldRow("Student Type:", cmbStudentType));
        fieldsContainer.add(createFieldRow("Academic Year:", txtAcademicYear));
        fieldsContainer.add(createFieldRow("Designation (Staff):", txtDesignation));
        fieldsContainer.add(createFieldRow("Office / Lab Location:", txtExtraDetail));

        JScrollPane formScroll = new JScrollPane(fieldsContainer);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(formScroll, BorderLayout.CENTER);

        // Action Buttons Row
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnAdd = ModernTheme.createPrimaryButton("Add User");
        btnUpdate = ModernTheme.createSuccessButton("Update User");
        btnDelete = ModernTheme.createDangerButton("Delete User");
        btnClear = ModernTheme.createSecondaryButton("Clear Form");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formPanel.add(btnPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(formPanel);

        add(topBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Handlers
        btnAdd.addActionListener(e -> handleAddUser());
        btnUpdate.addActionListener(e -> handleUpdateUser());
        btnDelete.addActionListener(e -> handleDeleteUser());
        btnClear.addActionListener(e -> clearForm());

        updateRoleSpecificFieldsVisibility();
    }

    private JPanel createFieldRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_MUTED);

        comp.setFont(ModernTheme.FONT_REGULAR);
        if (comp instanceof JTextField || comp instanceof JPasswordField) {
            comp.setPreferredSize(new Dimension(200, 26));
            comp.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(2, 6, 2, 6)));
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(2, 0, 4, 0));
        return p;
    }

    private void updateRoleSpecificFieldsVisibility() {
        String role = (String) cmbRole.getSelectedItem();
        boolean isStudent = "Undergraduate".equalsIgnoreCase(role);
        txtRegNo.setEnabled(isStudent);
        cmbStudentType.setEnabled(isStudent);
        txtAcademicYear.setEnabled(isStudent);

        txtDesignation.setEnabled(!isStudent);
        txtExtraDetail.setEnabled(!isStudent);
    }

    private void loadDepartments() {
        cmbDepartment.removeAllItems();
        cmbDepartment.addItem(new DepartmentItem(0, "-- None --"));
        List<Department> list = departmentDAO.getAllDepartments();
        for (Department d : list) {
            cmbDepartment.addItem(new DepartmentItem(d.getDepartmentId(), d.getDepartmentCode() + " - " + d.getDepartmentName()));
        }
    }

    public void loadUsers(int roleIndex) {
        tableModel.setRowCount(0);
        List<User> list;
        if (roleIndex == 0) {
            list = userService.getAllUsers();
        } else {
            list = userService.getUsersByRole(roleIndex);
        }

        for (User u : list) {
            String roleExtra = "";
            if (u instanceof Undergraduate ug) {
                roleExtra = ug.getStudentRegNo() + " (" + ug.getStudentType() + ")";
            } else if (u instanceof Lecturer lec) {
                roleExtra = lec.getDesignation() != null ? lec.getDesignation() : "Lecturer";
            } else if (u instanceof TechnicalOfficer to) {
                roleExtra = to.getAssignedLab() != null ? to.getAssignedLab() : "Lab";
            } else if (u instanceof Admin adm) {
                roleExtra = adm.getDesignation() != null ? adm.getDesignation() : "Admin";
            }

            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getRoleName(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getDepartmentName() != null ? u.getDepartmentName() : "-",
                roleExtra,
                u.getStatus()
            });
        }
    }

    private void populateFormFromSelectedRow() {
        int row = userTable.getSelectedRow();
        if (row == -1) return;

        int userId = (int) tableModel.getValueAt(row, 0);
        User user = userService.getUserById(userId);
        if (user == null) return;

        txtUserId.setText(String.valueOf(user.getUserId()));
        cmbRole.setSelectedIndex(user.getRoleId() - 1);
        txtFullName.setText(user.getFullName());
        txtUsername.setText(user.getUsername());
        txtUsername.setEnabled(false); // Username is identity, immutable once created
        txtPassword.setText("");
        txtEmail.setText(user.getEmail());
        txtPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        txtAddress.setText(user.getAddress() != null ? user.getAddress() : "");
        cmbStatus.setSelectedItem(user.getStatus());

        if (user.getDepartmentId() != null) {
            for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
                if (cmbDepartment.getItemAt(i).id == user.getDepartmentId()) {
                    cmbDepartment.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cmbDepartment.setSelectedIndex(0);
        }

        if (user instanceof Undergraduate ug) {
            txtRegNo.setText(ug.getStudentRegNo() != null ? ug.getStudentRegNo() : "");
            cmbStudentType.setSelectedItem(ug.getStudentType());
            txtAcademicYear.setText(ug.getAcademicYear() != null ? ug.getAcademicYear() : "2024/2025");
            spinSemester.setValue(ug.getCurrentSemester());
        } else if (user instanceof Lecturer lec) {
            txtDesignation.setText(lec.getDesignation() != null ? lec.getDesignation() : "");
            txtExtraDetail.setText(lec.getOfficeRoom() != null ? lec.getOfficeRoom() : "");
        } else if (user instanceof TechnicalOfficer to) {
            txtDesignation.setText(to.getDesignation() != null ? to.getDesignation() : "");
            txtExtraDetail.setText(to.getAssignedLab() != null ? to.getAssignedLab() : "");
        } else if (user instanceof Admin adm) {
            txtDesignation.setText(adm.getDesignation() != null ? adm.getDesignation() : "");
            txtExtraDetail.setText("");
        }

        updateRoleSpecificFieldsVisibility();
    }

    private void clearForm() {
        userTable.clearSelection();
        txtUserId.setText("");
        txtUsername.setText("");
        txtUsername.setEnabled(true);
        txtPassword.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        cmbDepartment.setSelectedIndex(0);
        cmbStatus.setSelectedItem("ACTIVE");
        txtRegNo.setText("");
        cmbStudentType.setSelectedItem("REGULAR");
        txtAcademicYear.setText("2024/2025");
        spinSemester.setValue(1);
        txtDesignation.setText("");
        txtExtraDetail.setText("");
        updateRoleSpecificFieldsVisibility();
    }

    private void handleAddUser() {
        try {
            int roleId = cmbRole.getSelectedIndex() + 1;
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());
            String fullName = txtFullName.getText();
            String email = txtEmail.getText();
            String phone = txtPhone.getText();
            String address = txtAddress.getText();
            DepartmentItem deptItem = (DepartmentItem) cmbDepartment.getSelectedItem();
            Integer deptId = (deptItem != null && deptItem.id > 0) ? deptItem.id : null;

            User user;
            switch (roleId) {
                case 1:
                    Admin adm = new Admin(0, username, "", fullName, email, phone, address, deptId, txtDesignation.getText());
                    user = adm;
                    break;
                case 2:
                    Lecturer lec = new Lecturer(0, username, "", fullName, email, phone, address, deptId,
                        txtDesignation.getText(), txtExtraDetail.getText());
                    user = lec;
                    break;
                case 3:
                    TechnicalOfficer to = new TechnicalOfficer(0, username, "", fullName, email, phone, address, deptId,
                        txtDesignation.getText(), txtExtraDetail.getText());
                    user = to;
                    break;
                case 4:
                    Undergraduate ug = new Undergraduate(0, username, "", fullName, email, phone, address, deptId,
                        txtRegNo.getText(), txtAcademicYear.getText(), (int) spinSemester.getValue(),
                        (String) cmbStudentType.getSelectedItem());
                    user = ug;
                    break;
                default:
                    throw new ValidationException("Please select a valid role.");
            }
            user.setStatus((String) cmbStatus.getSelectedItem());

            boolean saved = userService.createUser(user, password);
            if (!saved) {
                JOptionPane.showMessageDialog(
                    this,
                    "The user could not be saved to the database. Please check the database connection and try again.",
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            JOptionPane.showMessageDialog(this, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Reload the list from the database after a successful insert.
            loadUsers(roleFilterCombo.getSelectedIndex());
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateUser() {
        if (txtUserId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int userId = Integer.parseInt(txtUserId.getText());
            User user = userService.getUserById(userId);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Selected user not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            user.setFullName(txtFullName.getText());
            user.setEmail(txtEmail.getText());
            user.setPhone(txtPhone.getText());
            user.setAddress(txtAddress.getText());
            user.setStatus((String) cmbStatus.getSelectedItem());

            DepartmentItem deptItem = (DepartmentItem) cmbDepartment.getSelectedItem();
            user.setDepartmentId((deptItem != null && deptItem.id > 0) ? deptItem.id : null);

            if (user instanceof Undergraduate ug) {
                ug.setStudentRegNo(txtRegNo.getText());
                ug.setStudentType((String) cmbStudentType.getSelectedItem());
                ug.setAcademicYear(txtAcademicYear.getText());
                ug.setCurrentSemester((int) spinSemester.getValue());
            } else if (user instanceof Lecturer lec) {
                lec.setDesignation(txtDesignation.getText());
                lec.setOfficeRoom(txtExtraDetail.getText());
            } else if (user instanceof TechnicalOfficer to) {
                to.setDesignation(txtDesignation.getText());
                to.setAssignedLab(txtExtraDetail.getText());
            } else if (user instanceof Admin adm) {
                adm.setDesignation(txtDesignation.getText());
            }

            // Update user record
            userService.updateUser(user);

            // Optional password update
            String newPass = new String(txtPassword.getPassword());
            if (!newPass.trim().isEmpty()) {
                user.setPasswordHash(com.fot.ams.util.PasswordUtil.hashPassword(newPass.trim()));
                new com.fot.ams.dao.UserDAO().updatePassword(userId, user.getPasswordHash());
            }

            JOptionPane.showMessageDialog(this, "User profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadUsers(roleFilterCombo.getSelectedIndex());
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteUser() {
        if (txtUserId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = Integer.parseInt(txtUserId.getText());
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete user '" + txtUsername.getText() + "'?\nThis will remove related records.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = userService.deleteUser(userId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers(roleFilterCombo.getSelectedIndex());
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "User could not be deleted.", "Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
