package com.fot.ams.ui.technical;
import com.fot.ams.model.Notice;
import com.fot.ams.service.NoticeService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
/**
 * Notice viewer panel for Technical Officers configured with card viewing for full notice contents.
 */
public class TechnicalNoticePanel extends JPanel {
    private final NoticeService noticeService;
    private JPanel cardsContainer;

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

        // Cards Container Panel (Vertical Stack with spacing)
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(ModernTheme.BG_MAIN);

        // Wrap inside JScrollPane for smooth scrolling through notice cards
        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadNotices() {
        cardsContainer.removeAll();
        List<Notice> list = noticeService.getActiveNotices();

        if (list == null || list.isEmpty()) {
            JLabel lblEmpty = new JLabel("No active notices available at the moment.", JLabel.CENTER);
            lblEmpty.setFont(ModernTheme.FONT_REGULAR);
            lblEmpty.setForeground(ModernTheme.TEXT_MUTED);
            cardsContainer.add(lblEmpty);
        } else {
            for (Notice n : list) {
                cardsContainer.add(createNoticeCard(n));
                cardsContainer.add(Box.createRigidArea(new Dimension(0, 12))); // Spacing between cards
            }
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    // Factory method to build a modern card showing full content
    private JPanel createNoticeCard(Notice notice) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250)); // Prevent cards from stretching too tall

        // Card Header (Title & Published Date)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(notice.getTitle());
        lblTitle.setFont(ModernTheme.FONT_SUBTITLE);
        lblTitle.setForeground(ModernTheme.PRIMARY);

        JLabel lblDate = new JLabel(notice.getPublishedDate() != null ? DateUtil.formatDate(notice.getPublishedDate()) : "");
        lblDate.setFont(ModernTheme.FONT_SMALL);
        lblDate.setForeground(ModernTheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblDate, BorderLayout.EAST);

        // Meta Info Panel (Audience & Author/Announced By)
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
        metaPanel.setOpaque(false);

        String audience = notice.getTargetRole() != null ? notice.getTargetRole() : "ALL";
        JLabel lblAudience = new JLabel("Audience: " + audience);
        lblAudience.setFont(ModernTheme.FONT_SMALL);
        lblAudience.setForeground(ModernTheme.TEXT_MUTED);

        String author = notice.getCreatorName() != null ? notice.getCreatorName() : "Dean's Office";
        JLabel lblAuthor = new JLabel("Announced By: " + author);
        lblAuthor.setFont(ModernTheme.FONT_SMALL);
        lblAuthor.setForeground(ModernTheme.TEXT_MUTED);

        metaPanel.add(lblAudience);
        metaPanel.add(lblAuthor);

        // Full Content Area (Read-only multi-line text area)
        JTextArea txtContent = new JTextArea(notice.getContent() != null ? notice.getContent() : "");
        txtContent.setFont(ModernTheme.FONT_REGULAR);
        txtContent.setForeground(Color.DARK_GRAY); // Fixed: replaced ModernTheme.TEXT_MAIN with Color.DARK_GRAY
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setOpaque(false);
        txtContent.setBorder(null);

        // Assembly of top header elements
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);
        topWrapper.add(headerPanel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 4)));
        topWrapper.add(metaPanel);

        card.add(topWrapper, BorderLayout.NORTH);
        card.add(new JScrollPane(txtContent), BorderLayout.CENTER);

        return card;
    }
}