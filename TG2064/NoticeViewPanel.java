package com.fot.ams.ui.undergraduate;

import com.fot.ams.model.Notice;
import com.fot.ams.service.NoticeService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Notice viewer panel for Undergraduates.
 */
public class NoticeViewPanel extends JPanel {
    private final NoticeService noticeService;
    private JTable noticeTable;
    private DefaultTableModel tableModel;

    public NoticeViewPanel() {
        this.noticeService = new NoticeService();
        initComponents();
        loadNotices();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Official Student Bulletins & Dean's Office Notices");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Notices");
        btnRefresh.addActionListener(e -> loadNotices());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Notice Title", "Date", "Published By", "Announcement Content"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        noticeTable = new JTable(tableModel);
        ModernTheme.styleTable(noticeTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(noticeTable), BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);
    }

    public void loadNotices() {
        tableModel.setRowCount(0);
        List<Notice> list = noticeService.getActiveNotices();
        for (Notice n : list) {
            // Undergraduates see 'ALL' or 'Undergraduate' notices
            if ("ALL".equalsIgnoreCase(n.getTargetRole()) || "Undergraduate".equalsIgnoreCase(n.getTargetRole())) {
                tableModel.addRow(new Object[]{
                    n.getNoticeId(),
                    n.getTitle(),
                    DateUtil.formatDate(n.getPublishedDate()),
                    n.getCreatorName() != null ? n.getCreatorName() : "Dean's Office",
                    n.getContent()
                });
            }
        }
    }
}
