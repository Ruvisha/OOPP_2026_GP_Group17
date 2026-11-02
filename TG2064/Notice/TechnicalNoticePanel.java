package com.fot.ams.ui.technical;

import com.fot.ams.model.Notice;
import com.fot.ams.service.NoticeService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Notice viewer panel for Technical Officers.
 */
public class TechnicalNoticePanel extends JPanel {
    private final NoticeService noticeService;
    private JTable noticeTable;
    private DefaultTableModel tableModel;

    public TechnicalNoticePanel() {
        this.noticeService = new NoticeService();
        initComponents();
        loadNotices();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Official Faculty Notices & Department Bulletins");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Notices");
        btnRefresh.addActionListener(e -> loadNotices());

        header.add(title, BorderLayout.WEST);
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Notice Title", "Published Date", "Audience", "Announced By", "Notice Content"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        noticeTable = new JTable(tableModel);
        ModernTheme.styleTable(noticeTable);

        add(new JScrollPane(noticeTable), BorderLayout.CENTER);
    }

    public void loadNotices() {
        tableModel.setRowCount(0);
        List<Notice> list = noticeService.getActiveNotices();
        for (Notice n : list) {
            tableModel.addRow(new Object[]{
                n.getNoticeId(),
                n.getTitle(),
                DateUtil.formatDate(n.getPublishedDate()),
                n.getTargetRole(),
                n.getCreatorName() != null ? n.getCreatorName() : "Dean's Office",
                n.getContent()
            });
        }
    }
}
