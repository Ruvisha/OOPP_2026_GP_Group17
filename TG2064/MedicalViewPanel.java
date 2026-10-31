package com.fot.ams.ui.undergraduate;

import com.fot.ams.model.Medical;
import com.fot.ams.model.User;
import com.fot.ams.service.MedicalService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel enabling Undergraduates to view their submitted medical records and review approval status.
 */
public class MedicalViewPanel extends JPanel {
    private final MedicalService medicalService;
    private JTable medicalTable;
    private DefaultTableModel tableModel;

    public MedicalViewPanel() {
        this.medicalService = new MedicalService();
        initComponents();
        loadStudentMedicals();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("My Submitted Medical Certificates & Absence Excuses");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Records");
        btnRefresh.addActionListener(e -> loadStudentMedicals());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {
            "Medical ID", "Course", "Session Type", "Missed Date", "Date Submitted",
            "Medical Reason", "Approval Status", "University Medical Officer Endorsement"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        medicalTable = new JTable(tableModel);
        ModernTheme.styleTable(medicalTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(medicalTable), BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "<html><b>Medical Policy:</b> Medical certificates must be submitted within 7 days of returning to academic activities. " +
            "Approved medicals excuse the absence and count towards effective attendance for examination eligibility.</html>"
        );
        footer.setFont(ModernTheme.FONT_SMALL);
        footer.setForeground(ModernTheme.TEXT_MUTED);
        footer.setBorder(new EmptyBorder(8, 12, 8, 12));
        tablePanel.add(footer, BorderLayout.SOUTH);

        add(tablePanel, BorderLayout.CENTER);
    }

    public void loadStudentMedicals() {
        tableModel.setRowCount(0);
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<Medical> list = medicalService.getMedicalsByStudent(cur.getUserId());
        for (Medical m : list) {
            tableModel.addRow(new Object[]{
                m.getMedicalId(),
                m.getCourseCode(),
                m.getSessionType(),
                DateUtil.formatDate(m.getSessionDate()),
                DateUtil.formatDate(m.getSubmissionDate()),
                m.getReason(),
                m.getStatus(),
                m.getRemarks() != null ? m.getRemarks() : "Pending UMO Review"
            });
        }
    }
}
