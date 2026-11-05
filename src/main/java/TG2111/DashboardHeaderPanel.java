package com.fot.ams.ui.common;

import com.fot.ams.dao.NotificationDAO;
import com.fot.ams.model.Undergraduate;
import com.fot.ams.model.User;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Date;

/**
 * Common, compact, modern dashboard header used across all four roles:
 * Admin, Lecturer, Technical Officer, and Undergraduate Student.
 */
public class DashboardHeaderPanel extends JPanel {

    private final NotificationDAO notificationDAO;
    private JLabel lblNotificationBadge;
    private final Runnable onProfileClicked;

    public DashboardHeaderPanel(User user, Runnable onProfileClicked) {
        this.notificationDAO = new NotificationDAO();
        this.onProfileClicked = onProfileClicked;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, 58));
        setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
            new EmptyBorder(8, 20, 8, 20)
        ));

        initComponents(user);
        refreshNotificationBadge();
    }

    private void initComponents(User user) {
        // Left: Welcome greeting & subtitle
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        String name = user != null ? user.getFullName() : "User";
        JLabel lblWelcome = new JLabel("Welcome, " + name);
        lblWelcome.setFont(ModernTheme.FONT_SUBTITLE);
        lblWelcome.setForeground(ModernTheme.TEXT_DARK);

        String subtitle;
        if (user instanceof Undergraduate ug) {
            String reg = ug.getStudentRegNo() != null ? ug.getStudentRegNo() : "Student";
            subtitle = reg + " • " + (ug.getDepartmentName() != null ? ug.getDepartmentName() : "Faculty of Technology");
            if (ug.isRepeat() || ug.isBatchMissed()) {
                subtitle += " [" + ug.getStudentType() + "]";
            }
        } else if (user != null) {
            subtitle = user.getRoleTitle() + " • " + (user.getDepartmentName() != null ? user.getDepartmentName() : "Faculty of Technology");
        } else {
            subtitle = "Academic Management Portal";
        }

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(ModernTheme.FONT_SMALL);
        lblSub.setForeground(ModernTheme.TEXT_MUTED);

        leftPanel.add(lblWelcome);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        leftPanel.add(lblSub);

        // Right: Date badge, Notification bell with unread badge, and Profile button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        // Current Date
        JLabel dateBadge = new JLabel(DateUtil.formatDisplayDate(new Date()), ModernIcons.get("calendar", 15, ModernTheme.PRIMARY), SwingConstants.LEFT);
        dateBadge.setFont(ModernTheme.FONT_SMALL);
        dateBadge.setForeground(ModernTheme.TEXT_SECONDARY);
        dateBadge.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));

        // Notification Bell Container
        JPanel notifContainer = new JPanel(null);
        notifContainer.setOpaque(false);
        notifContainer.setPreferredSize(new Dimension(38, 34));

        JButton btnNotif = new JButton(ModernIcons.get("notices", 18, ModernTheme.TEXT_DARK));
        btnNotif.setBounds(0, 0, 34, 34);
        btnNotif.setContentAreaFilled(false);
        btnNotif.setFocusPainted(false);
        btnNotif.setBorder(new LineBorder(ModernTheme.BORDER, 1, true));
        btnNotif.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotif.setToolTipText("Official Faculty Notifications");
        btnNotif.addActionListener(e -> NotificationDialog.showDialog(this, this::refreshNotificationBadge));

        lblNotificationBadge = new JLabel("0") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ModernTheme.DANGER);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblNotificationBadge.setFont(new Font(ModernTheme.getFontFamily(), Font.BOLD, 9));
        lblNotificationBadge.setForeground(Color.WHITE);
        lblNotificationBadge.setHorizontalAlignment(SwingConstants.CENTER);
        lblNotificationBadge.setBounds(18, -2, 16, 16);
        lblNotificationBadge.setVisible(false);

        notifContainer.add(lblNotificationBadge);
        notifContainer.add(btnNotif);

        // Profile Button
        JButton btnProfile = new JButton("Profile", ModernIcons.get("profile", 16, ModernTheme.PRIMARY));
        btnProfile.setFont(ModernTheme.FONT_SMALL);
        btnProfile.setForeground(ModernTheme.TEXT_DARK);
        btnProfile.setBackground(Color.WHITE);
        btnProfile.setFocusPainted(false);
        btnProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProfile.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        btnProfile.addActionListener(e -> {
            if (onProfileClicked != null) onProfileClicked.run();
        });

        rightPanel.add(dateBadge);
        rightPanel.add(notifContainer);
        rightPanel.add(btnProfile);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    public void refreshNotificationBadge() {
        User cur = SessionManager.getCurrentUser();
        if (cur != null) {
            int unread = notificationDAO.getUnreadCount(cur.getUserId(), cur.getRoleName());
            lblNotificationBadge.setText(unread > 9 ? "9+" : String.valueOf(unread));
            lblNotificationBadge.setVisible(unread > 0);
            lblNotificationBadge.repaint();
        }
    }
}
