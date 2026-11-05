package com.fot.ams.ui.admin;

import com.fot.ams.dao.NotificationDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Notice;
import com.fot.ams.model.User;
import com.fot.ams.service.NoticeService;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern University Notice Management Panel.
 * Features multi-role target selection checkboxes, priority level, optional expiry,
 * and user-specific notification distribution.
 */
public class NoticeManagementPanel extends JPanel {
    private final NoticeService noticeService;
    private final NotificationDAO notificationDAO;

    private JTable noticeTable;
    private DefaultTableModel tableModel;

    private JTextField txtNoticeId;
    private JTextField txtTitle;
    private JTextArea txtContent;
    private JTextField txtPublishedDate;
    private JTextField txtExpiryDate;
    private JComboBox<String> cmbPriority;
    private JComboBox<String> cmbStatus;

    // Multi-role checkboxes
    private JCheckBox chkAdmin;
    private JCheckBox chkLecturer;
    private JCheckBox chkTech;
    private JCheckBox chkStudent;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    public NoticeManagementPanel() {
        this.noticeService = new NoticeService();
        this.notificationDAO = new NotificationDAO();
        initComponents();
        loadNotices();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        JLabel title = new JLabel("Official Faculty Notice & Announcement Management", ModernIcons.get("notices", 20, ModernTheme.PRIMARY), SwingConstants.LEFT);
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.PRIMARY);
        topBar.add(title);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerLocation(620);

        // Table Panel (Left)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"ID", "Title", "Date", "Target Roles", "Created By", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        noticeTable = new JTable(tableModel);
        ModernTheme.styleTable(noticeTable);
        noticeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noticeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && noticeTable.getSelectedRow() != -1) {
                populateForm();
            }
        });

        tablePanel.add(new JScrollPane(noticeTable), BorderLayout.CENTER);

        // Form Panel (Right)
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 10));
        formCard.setPreferredSize(new Dimension(420, 560));

        JLabel formHeader = new JLabel("Notice & Announcement Details");
        formHeader.setFont(ModernTheme.FONT_SUBTITLE);
        formHeader.setForeground(ModernTheme.PRIMARY);
        formHeader.setBorder(new EmptyBorder(0, 0, 8, 0));
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Color.WHITE);

        txtNoticeId = new JTextField();
        txtNoticeId.setVisible(false);

        txtTitle = new JTextField();
        txtPublishedDate = new JTextField(DateUtil.formatDate(DateUtil.nowSqlDate()));
        txtExpiryDate = new JTextField();
        cmbPriority = new JComboBox<>(new String[]{"Normal", "Important", "Urgent"});
        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "ARCHIVED"});

        txtContent = new JTextArea(6, 20);
        txtContent.setFont(ModernTheme.FONT_REGULAR);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        fields.add(createRow("Notice Title:", txtTitle));
        fields.add(createRow("Published Date (YYYY-MM-DD):", txtPublishedDate));
        fields.add(createRow("Expiry Date (Optional, YYYY-MM-DD):", txtExpiryDate));
        fields.add(createRow("Notice Priority Level:", cmbPriority));
        fields.add(createTargetRolesPanel());
        fields.add(createRow("Status:", cmbStatus));
        fields.add(createRow("Notice Description / Body:", new JScrollPane(txtContent)));

        JScrollPane formScroll = new JScrollPane(fields);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        formCard.add(formScroll, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnAdd = ModernTheme.createPrimaryButton("Publish Notice");
        btnUpdate = ModernTheme.createSuccessButton("Update Notice");
        btnDelete = ModernTheme.createDangerButton("Delete Notice");
        btnClear = ModernTheme.createSecondaryButton("Cancel");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(formCard);

        add(topBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
    }

    private JPanel createTargetRolesPanel() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 0, 8, 0));

        JLabel lbl = new JLabel("Target Roles (Multi-Role Distribution):");
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_MUTED);
        p.add(lbl, BorderLayout.NORTH);

        JPanel roleBox = new JPanel();
        roleBox.setLayout(new BoxLayout(roleBox, BoxLayout.Y_AXIS));
        roleBox.setBackground(new Color(248, 250, 252));
        roleBox.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(6, 8, 6, 8)));

        chkAdmin = new JCheckBox("Administrator");
        chkLecturer = new JCheckBox("Lecturer");
        chkTech = new JCheckBox("Technical Officer");
        chkStudent = new JCheckBox("Undergraduate Student");

        chkAdmin.setOpaque(false);
        chkLecturer.setOpaque(false);
        chkTech.setOpaque(false);
        chkStudent.setOpaque(false);

        // Default all selected
        chkAdmin.setSelected(true);
        chkLecturer.setSelected(true);
        chkTech.setSelected(true);
        chkStudent.setSelected(true);

        JPanel checkGrid = new JPanel(new GridLayout(2, 2, 6, 4));
        checkGrid.setOpaque(false);
        checkGrid.add(chkAdmin);
        checkGrid.add(chkLecturer);
        checkGrid.add(chkTech);
        checkGrid.add(chkStudent);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        actionRow.setOpaque(false);

        JButton btnSelectAll = new JButton("Select All Roles");
        btnSelectAll.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSelectAll.setFocusPainted(false);
        btnSelectAll.addActionListener(e -> {
            chkAdmin.setSelected(true);
            chkLecturer.setSelected(true);
            chkTech.setSelected(true);
            chkStudent.setSelected(true);
        });

        JButton btnClearRoles = new JButton("Clear Selection");
        btnClearRoles.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnClearRoles.setFocusPainted(false);
        btnClearRoles.addActionListener(e -> {
            chkAdmin.setSelected(false);
            chkLecturer.setSelected(false);
            chkTech.setSelected(false);
            chkStudent.setSelected(false);
        });

        actionRow.add(btnSelectAll);
        actionRow.add(btnClearRoles);

        roleBox.add(checkGrid);
        roleBox.add(Box.createRigidArea(new Dimension(0, 4)));
        roleBox.add(actionRow);

        p.add(roleBox, BorderLayout.CENTER);
        return p;
    }

    private JPanel createRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 52));
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
        p.setBorder(new EmptyBorder(2, 0, 4, 0));
        return p;
    }

    public void loadNotices() {
        tableModel.setRowCount(0);
        try {
            List<Notice> list = noticeService.getAllNotices();
            for (Notice n : list) {
                tableModel.addRow(new Object[]{
                    n.getNoticeId(),
                    n.getTitle(),
                    DateUtil.formatDate(n.getPublishedDate()),
                    n.getTargetRole(),
                    n.getCreatorName() != null ? n.getCreatorName() : "Admin",
                    n.getStatus()
                });
            }
        } catch (Exception e) {
            System.err.println("[NoticeManagementPanel] Notice load note: " + e.getMessage());
        }
    }

    private void populateForm() {
        int row = noticeTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableModel.getValueAt(row, 0);
        try {
            List<Notice> list = noticeService.getAllNotices();
            for (Notice n : list) {
                if (n.getNoticeId() == id) {
                    txtNoticeId.setText(String.valueOf(n.getNoticeId()));
                    txtTitle.setText(n.getTitle());
                    txtContent.setText(n.getContent());
                    txtPublishedDate.setText(DateUtil.formatDate(n.getPublishedDate()));
                    setTargetRolesSelection(n.getTargetRole());
                    cmbStatus.setSelectedItem(n.getStatus());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[NoticeManagementPanel] Populate form note: " + e.getMessage());
        }
    }

    private String getSelectedRoles() throws ValidationException {
        List<String> roles = new ArrayList<>();
        if (chkAdmin.isSelected()) roles.add("ADMIN");
        if (chkLecturer.isSelected()) roles.add("LECTURER");
        if (chkTech.isSelected()) roles.add("TECH");
        if (chkStudent.isSelected()) roles.add("UNDERGRADUATE");

        if (roles.isEmpty()) {
            throw new ValidationException("Please select at least one recipient role.");
        }
        if (roles.size() == 4) {
            return "ALL";
        }
        return String.join(",", roles);
    }

    private void setTargetRolesSelection(String role) {
        if (role == null || role.trim().isEmpty() || "ALL".equalsIgnoreCase(role) || "All Users".equalsIgnoreCase(role)) {
            chkAdmin.setSelected(true);
            chkLecturer.setSelected(true);
            chkTech.setSelected(true);
            chkStudent.setSelected(true);
        } else {
            String upper = role.toUpperCase();
            chkAdmin.setSelected(upper.contains("ADMIN"));
            chkLecturer.setSelected(upper.contains("LECTURER"));
            chkTech.setSelected(upper.contains("TECH"));
            chkStudent.setSelected(upper.contains("UNDERGRADUATE") || upper.contains("STUDENT"));
        }
    }

    private void clearForm() {
        noticeTable.clearSelection();
        txtNoticeId.setText("");
        txtTitle.setText("");
        txtContent.setText("");
        txtPublishedDate.setText(DateUtil.formatDate(DateUtil.nowSqlDate()));
        txtExpiryDate.setText("");
        cmbPriority.setSelectedItem("Normal");
        chkAdmin.setSelected(true);
        chkLecturer.setSelected(true);
        chkTech.setSelected(true);
        chkStudent.setSelected(true);
        cmbStatus.setSelectedItem("ACTIVE");
    }

    private void handleAdd() {
        try {
            Date pubDate = DateUtil.parseDate(txtPublishedDate.getText().trim());
            if (pubDate == null) throw new ValidationException("Invalid published date format (Use YYYY-MM-DD).");

            String title = txtTitle.getText().trim();
            if (title.isEmpty()) throw new ValidationException("Notice title cannot be empty.");

            String content = txtContent.getText().trim();
            if (content.isEmpty()) throw new ValidationException("Notice description cannot be empty.");

            String targetRoles = getSelectedRoles();

            User current = SessionManager.getCurrentUser();
            Integer creatorId = current != null ? current.getUserId() : 1;

            Notice n = new Notice(
                0,
                title,
                content,
                pubDate,
                creatorId,
                targetRoles,
                (String) cmbStatus.getSelectedItem()
            );

            boolean added = noticeService.addNotice(n);
            if (added && "ACTIVE".equalsIgnoreCase(n.getStatus())) {
                notificationDAO.distributeNoticeNotifications(n.getNoticeId(), n.getTargetRole());
            }

            JOptionPane.showMessageDialog(this, "Notice published successfully and distributed to target roles!", "Notice Published", JOptionPane.INFORMATION_MESSAGE);
            loadNotices();
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error publishing notice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (txtNoticeId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a notice to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtNoticeId.getText());
            Date pubDate = DateUtil.parseDate(txtPublishedDate.getText().trim());
            if (pubDate == null) throw new ValidationException("Invalid published date format (Use YYYY-MM-DD).");

            String title = txtTitle.getText().trim();
            if (title.isEmpty()) throw new ValidationException("Notice title cannot be empty.");

            String content = txtContent.getText().trim();
            if (content.isEmpty()) throw new ValidationException("Notice description cannot be empty.");

            String targetRoles = getSelectedRoles();

            Notice n = new Notice(
                id,
                title,
                content,
                pubDate,
                null,
                targetRoles,
                (String) cmbStatus.getSelectedItem()
            );

            boolean updated = noticeService.updateNotice(n);
            if (updated && "ACTIVE".equalsIgnoreCase(n.getStatus())) {
                notificationDAO.distributeNoticeNotifications(n.getNoticeId(), n.getTargetRole());
            }

            JOptionPane.showMessageDialog(this, "Notice updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadNotices();
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating notice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (txtNoticeId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a notice to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtNoticeId.getText());
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this notice?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                noticeService.deleteNotice(id);
                JOptionPane.showMessageDialog(this, "Notice deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadNotices();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting notice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}