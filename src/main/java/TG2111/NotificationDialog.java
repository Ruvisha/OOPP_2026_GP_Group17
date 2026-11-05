package com.fot.ams.ui.common;

import com.fot.ams.dao.NotificationDAO;
import com.fot.ams.model.User;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Modern University Notification Center Dialog.
 * Shows role-specific announcements with persistent per-user read/unread tracking.
 */
public class NotificationDialog extends JDialog {

    private final NotificationDAO notificationDAO;
    private final Runnable onBadgeUpdate;
    private JPanel listContainer;
    private JLabel lblHeaderCount;

    public static void showDialog(Component parent, Runnable onBadgeUpdate) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        NotificationDialog dialog = new NotificationDialog(window, onBadgeUpdate);
        dialog.setVisible(true);
    }

    public NotificationDialog(Window owner, Runnable onBadgeUpdate) {
        super(owner, "Official Bulletins & Notifications", ModalityType.APPLICATION_MODAL);
        this.notificationDAO = new NotificationDAO();
        this.onBadgeUpdate = onBadgeUpdate;

        setSize(560, 520);
        setLocationRelativeTo(owner);
        initComponents();
        loadNotifications();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(ModernTheme.BG_MAIN);
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTitle.setOpaque(false);
        leftTitle.add(new JLabel(ModernIcons.get("notices", 22, ModernTheme.PRIMARY)));

        JLabel title = new JLabel("Faculty Announcements");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.TEXT_DARK);
        leftTitle.add(title);

        lblHeaderCount = ModernTheme.createBadge("0 Unread", ModernTheme.PRIMARY_LIGHT, ModernTheme.PRIMARY);
        leftTitle.add(lblHeaderCount);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightActions.setOpaque(false);

        JButton btnMarkAll = ModernTheme.createSecondaryButton("Mark All as Read");
        btnMarkAll.setFont(ModernTheme.FONT_SMALL);
        btnMarkAll.addActionListener(e -> markAllRead());
        rightActions.add(btnMarkAll);

        header.add(leftTitle, BorderLayout.WEST);
        header.add(rightActions, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // List scroll area
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(ModernTheme.BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        root.add(scrollPane, BorderLayout.CENTER);

        // Bottom close bar
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btm.setOpaque(false);
        JButton btnClose = ModernTheme.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        btm.add(btnClose);
        root.add(btm, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadNotifications() {
        listContainer.removeAll();
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<NotificationDAO.NotificationItem> items = notificationDAO.getUserNotifications(cur.getUserId(), cur.getRoleName());
        int unread = 0;
        for (NotificationDAO.NotificationItem it : items) {
            if (!it.isRead) unread++;
        }
        lblHeaderCount.setText(unread + " Unread");
        if (unread > 0) {
            lblHeaderCount.setBackground(ModernTheme.DANGER_LIGHT);
            lblHeaderCount.setForeground(ModernTheme.DANGER);
        } else {
            lblHeaderCount.setBackground(ModernTheme.PRIMARY_LIGHT);
            lblHeaderCount.setForeground(ModernTheme.PRIMARY);
        }

        if (items.isEmpty()) {
            listContainer.add(ModernTheme.createEmptyState("No Announcements", "There are no faculty bulletins for your role at this time.", "notices"));
        } else {
            for (NotificationDAO.NotificationItem item : items) {
                listContainer.add(createNoticeRow(item));
                listContainer.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createNoticeRow(NotificationDAO.NotificationItem item) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.setColor(!item.isRead ? new Color(199, 210, 254) : ModernTheme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 6));
        card.setBackground(!item.isRead ? new Color(248, 250, 255) : Color.WHITE);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Left unread dot indicator
        JPanel dotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!item.isRead) {
                    g2.setColor(ModernTheme.ACCENT);
                    g2.fillOval(4, getHeight() / 2 - 4, 8, 8);
                }
                g2.dispose();
            }
        };
        dotPanel.setOpaque(false);
        dotPanel.setPreferredSize(new Dimension(16, 20));

        // Center content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(item.title);
        titleLbl.setFont(item.isRead ? ModernTheme.FONT_BOLD : new Font(ModernTheme.getFontFamily(), Font.BOLD, 14));
        titleLbl.setForeground(ModernTheme.TEXT_DARK);

        String excerpt = item.content != null ? item.content : "";
        if (excerpt.length() > 90) excerpt = excerpt.substring(0, 87) + "...";
        JLabel excerptLbl = new JLabel(excerpt);
        excerptLbl.setFont(ModernTheme.FONT_SMALL);
        excerptLbl.setForeground(ModernTheme.TEXT_SECONDARY);

        JLabel metaLbl = new JLabel("Date: " + DateUtil.formatDisplayDate(item.publishedDate) + " • Audience: " + item.targetRole);
        metaLbl.setFont(ModernTheme.FONT_CAPTION);
        metaLbl.setForeground(ModernTheme.TEXT_MUTED);

        contentPanel.add(titleLbl);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        contentPanel.add(excerptLbl);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        contentPanel.add(metaLbl);

        card.add(dotPanel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);

        // Click to view full notice & mark read
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openNoticeDetail(item);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(238, 242, 255));
                card.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(!item.isRead ? new Color(248, 250, 255) : Color.WHITE);
                card.repaint();
            }
        });

        return card;
    }

    private void openNoticeDetail(NotificationDAO.NotificationItem item) {
        User cur = SessionManager.getCurrentUser();
        if (cur != null && !item.isRead) {
            notificationDAO.markAsRead(cur.getUserId(), item.noticeId);
            if (onBadgeUpdate != null) onBadgeUpdate.run();
        }

        JTextArea ta = new JTextArea(item.content);
        ta.setFont(ModernTheme.FONT_REGULAR);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setBackground(Color.WHITE);
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(480, 260));

        JPanel detailP = new JPanel(new BorderLayout(8, 8));
        detailP.add(new JLabel("Audience: " + item.targetRole + " | Published: " + DateUtil.formatDisplayDate(item.publishedDate)), BorderLayout.NORTH);
        detailP.add(sp, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, detailP, item.title, JOptionPane.PLAIN_MESSAGE);
        loadNotifications();
    }

    private void markAllRead() {
        User cur = SessionManager.getCurrentUser();
        if (cur != null) {
            notificationDAO.markAllAsRead(cur.getUserId(), cur.getRoleName());
            if (onBadgeUpdate != null) onBadgeUpdate.run();
            loadNotifications();
        }
    }
}
