package com.fot.ams.ui.admin;

import com.fot.ams.dao.AdminAnalyticsDAO;
import com.fot.ams.ui.common.ModernIcons;
import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Modern University Administration Audit Trail & Security Logs Panel.
 * Tracks user account changes, administrative interventions, notice publications,
 * and system security events with timestamped records.
 */
public class AuditLogsPanel extends JPanel {

    private final AdminAnalyticsDAO analyticsDAO;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbCategory;
    private JTextField txtSearch;
    private JLabel lblLogCount;

    public AuditLogsPanel() {
        this.analyticsDAO = new AdminAnalyticsDAO();

        setLayout(new BorderLayout(16, 16));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(18, 20, 20, 20));

        initComponents();
        loadAuditLogs();
    }

    private void initComponents() {
        // Top Filter Card
        JPanel topBar = ModernTheme.createCardPanel(10);
        topBar.setLayout(new BorderLayout(15, 0));
        topBar.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        JLabel lblIcon = new JLabel("Audit Filters:", ModernIcons.get("audit", 18, ModernTheme.PRIMARY), SwingConstants.LEFT);
        lblIcon.setFont(ModernTheme.FONT_BOLD);
        lblIcon.setForeground(ModernTheme.TEXT_DARK);

        cmbCategory = new JComboBox<>(new String[]{"ALL", "USER", "COURSE", "NOTICE", "GENERAL"});
        cmbCategory.setFont(ModernTheme.FONT_REGULAR);
        cmbCategory.addActionListener(e -> loadAuditLogs());

        txtSearch = new JTextField(18);
        txtSearch.setFont(ModernTheme.FONT_REGULAR);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search action, user, details...");
        txtSearch.addActionListener(e -> loadAuditLogs());

        JButton btnSearch = ModernTheme.createSecondaryButton("Filter Logs");
        btnSearch.setIcon(ModernIcons.get("search", 14, ModernTheme.TEXT_DARK));
        btnSearch.addActionListener(e -> loadAuditLogs());

        JButton btnReset = ModernTheme.createSecondaryButton("Reset");
        btnReset.setIcon(ModernIcons.get("refresh", 14, ModernTheme.TEXT_DARK));
        btnReset.addActionListener(e -> {
            cmbCategory.setSelectedIndex(0);
            txtSearch.setText("");
            loadAuditLogs();
        });

        leftFilters.add(lblIcon);
        leftFilters.add(new JLabel("Category:"));
        leftFilters.add(cmbCategory);
        leftFilters.add(txtSearch);
        leftFilters.add(btnSearch);
        leftFilters.add(btnReset);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton btnExport = ModernTheme.createSecondaryButton("Export Log (CSV)");
        btnExport.setIcon(ModernIcons.get("download", 14, ModernTheme.TEXT_DARK));
        btnExport.addActionListener(e -> exportLogsToCsv());

        rightActions.add(btnExport);

        topBar.add(leftFilters, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = ModernTheme.createCardPanel(10);
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Security & System Activity Trail");
        tableTitle.setFont(ModernTheme.FONT_SUBTITLE);
        tableTitle.setForeground(ModernTheme.TEXT_DARK);

        lblLogCount = ModernTheme.createBadge("0 Entries", ModernTheme.PRIMARY_LIGHT, ModernTheme.PRIMARY);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(lblLogCount, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Log ID", "Timestamp", "Operator", "Action Event", "Category", "Event Details", "Client IP"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        logTable = new JTable(tableModel);
        ModernTheme.styleTable(logTable);

        // Adjust column widths
        logTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        logTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        logTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        logTable.getColumnModel().getColumn(5).setPreferredWidth(320);
        logTable.getColumnModel().getColumn(6).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadAuditLogs() {
        tableModel.setRowCount(0);
        String category = (String) cmbCategory.getSelectedItem();
        String query = txtSearch.getText().trim();

        List<AdminAnalyticsDAO.AuditLog> logs = analyticsDAO.getAuditLogs(category, query, 100);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (AdminAnalyticsDAO.AuditLog log : logs) {
            String tsStr = log.createdAt != null ? sdf.format(log.createdAt) : "N/A";
            tableModel.addRow(new Object[]{
                log.id,
                tsStr,
                log.username,
                log.action,
                log.category,
                log.details,
                log.ipAddress
            });
        }
        lblLogCount.setText(logs.size() + " Audit Entries");
    }

    private void exportLogsToCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No audit records available to export.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("FOT_AMS_AuditTrail_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(f)) {
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    fw.write("\"" + tableModel.getColumnName(c) + "\"");
                    if (c < tableModel.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        Object val = tableModel.getValueAt(r, c);
                        fw.write("\"" + (val != null ? val.toString() : "") + "\"");
                        if (c < tableModel.getColumnCount() - 1) fw.write(",");
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Audit logs exported successfully.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export audit logs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
