package com.fot.ams.ui.admin;

import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Notice;
import com.fot.ams.model.User;
import com.fot.ams.service.NoticeService;
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
import java.util.List;

/**
 * Administrative panel for managing academic announcements and notices.
 */
public class NoticeManagementPanel extends JPanel {
    private final NoticeService noticeService;

    private JTable noticeTable;
    private DefaultTableModel tableModel;

    private JTextField txtNoticeId;
    private JTextField txtTitle;
    private JTextArea txtContent;
    private JTextField txtPublishedDate;
    private JComboBox<String> cmbTargetRole;
    private JComboBox<String> cmbStatus;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    public NoticeManagementPanel() {
        this.noticeService = new NoticeService();
        initComponents();
        loadNotices();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        JLabel title = new JLabel("Official Faculty Notice & Announcement Management");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.PRIMARY);
        topBar.add(title);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(650);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"ID", "Title", "Date", "Target Role", "Created By", "Status"};
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

        // Form Panel
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BorderLayout());
        formCard.setPreferredSize(new Dimension(360, 500));

        JLabel formHeader = new JLabel("Notice Details");
        formHeader.setFont(ModernTheme.FONT_SUBTITLE);
        formHeader.setForeground(ModernTheme.PRIMARY);
        formHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Color.WHITE);

        txtNoticeId = new JTextField();
        txtNoticeId.setVisible(false);

        txtTitle = new JTextField();
        txtPublishedDate = new JTextField(DateUtil.formatDate(DateUtil.nowSqlDate()));
        cmbTargetRole = new JComboBox<>(new String[]{"ALL", "Undergraduate", "Lecturer", "Technical Officer"});
        cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "ARCHIVED"});

        txtContent = new JTextArea(8, 20);
        txtContent.setFont(ModernTheme.FONT_REGULAR);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        fields.add(createRow("Title:", txtTitle));
        fields.add(createRow("Published Date (YYYY-MM-DD):", txtPublishedDate));
        fields.add(createRow("Target Audience:", cmbTargetRole));
        fields.add(createRow("Status:", cmbStatus));
        fields.add(createRow("Notice Content / Announcement Body:", new JScrollPane(txtContent)));

        formCard.add(fields, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnAdd = ModernTheme.createPrimaryButton("Create Notice");
        btnUpdate = ModernTheme.createSuccessButton("Update Notice");
        btnDelete = ModernTheme.createDangerButton("Delete Notice");
        btnClear = ModernTheme.createSecondaryButton("Clear Form");

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

    private JPanel createRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
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

    public void loadNotices() {
        tableModel.setRowCount(0);
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
    }

    private void populateForm() {
        int row = noticeTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableModel.getValueAt(row, 0);
        List<Notice> list = noticeService.getAllNotices();
        for (Notice n : list) {
            if (n.getNoticeId() == id) {
                txtNoticeId.setText(String.valueOf(n.getNoticeId()));
                txtTitle.setText(n.getTitle());
                txtContent.setText(n.getContent());
                txtPublishedDate.setText(DateUtil.formatDate(n.getPublishedDate()));
                cmbTargetRole.setSelectedItem(n.getTargetRole());
                cmbStatus.setSelectedItem(n.getStatus());
                break;
            }
        }
    }

    private void clearForm() {
        noticeTable.clearSelection();
        txtNoticeId.setText("");
        txtTitle.setText("");
        txtContent.setText("");
        txtPublishedDate.setText(DateUtil.formatDate(DateUtil.nowSqlDate()));
        cmbTargetRole.setSelectedItem("ALL");
        cmbStatus.setSelectedItem("ACTIVE");
    }

    private void handleAdd() {
        try {
            Date pubDate = DateUtil.parseDate(txtPublishedDate.getText().trim());
            if (pubDate == null) throw new ValidationException("Invalid published date format (Use YYYY-MM-DD).");

            User current = SessionManager.getCurrentUser();
            Integer creatorId = current != null ? current.getUserId() : 1;

            Notice n = new Notice(
                0,
                txtTitle.getText().trim(),
                txtContent.getText().trim(),
                pubDate,
                creatorId,
                (String) cmbTargetRole.getSelectedItem(),
                (String) cmbStatus.getSelectedItem()
            );

            noticeService.addNotice(n);
            JOptionPane.showMessageDialog(this, "Notice published successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
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

            Notice n = new Notice(
                id,
                txtTitle.getText().trim(),
                txtContent.getText().trim(),
                pubDate,
                null,
                (String) cmbTargetRole.getSelectedItem(),
                (String) cmbStatus.getSelectedItem()
            );

            noticeService.updateNotice(n);
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
