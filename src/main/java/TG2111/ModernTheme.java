package com.fot.ams.ui.common;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Modern enterprise styling utilities and theme constants for Swing UI.
 * Tailored for the University of Ruhuna - Faculty of Technology Academic Management System.
 */
public class ModernTheme {

    // Primary Brand Palette - Navy & Royal Blue
    public static final Color NAVY = new Color(11, 24, 54);           // #0B1836 - Deep University Navy
    public static final Color NAVY_LIGHT = new Color(19, 36, 75);     // #13244B
    public static final Color PRIMARY = new Color(26, 54, 138);       // #1A368A - Deep Royal Blue
    public static final Color PRIMARY_DARK = new Color(15, 30, 84);   // #0F1E54 - Navy
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255); // #EEF2FF - Soft Indigo Tint
    public static final Color ACCENT = new Color(37, 99, 235);        // #2563EB - Vibrant Royal Blue
    public static final Color ACCENT_HOVER = new Color(29, 78, 216);  // #1D4ED8
    public static final Color ACCENT_CYAN = new Color(14, 165, 233);   // #0EA5E9 - Sky/Cyan
    public static final Color ACCENT_PURPLE = new Color(124, 58, 237);// #7C3AED - Royal Violet

    // Neutral & Background Shades
    public static final Color BG_MAIN = new Color(248, 250, 252);     // #F8FAFC - Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(11, 24, 54);     // Navy #0B1836 (Sidebar)
    public static final Color BG_SIDEBAR_ACTIVE = new Color(26, 54, 138); // Royal Active
    public static final Color BG_SIDEBAR_HOVER = new Color(20, 42, 90);
    public static final Color BG_HEADER = Color.WHITE;

    // Typography Colors
    public static final Color TEXT_DARK = new Color(15, 23, 42);      // #0F172A - Slate 900
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105); // #475569 - Slate 600
    public static final Color TEXT_MUTED = new Color(148, 163, 184);  // #94A3B8 - Slate 400
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);  // #F1F5F9 - Slate 100

    // Semantic Status Colors
    public static final Color SUCCESS = new Color(16, 185, 129);      // #10B981 - Emerald 500
    public static final Color SUCCESS_LIGHT = new Color(236, 253, 245);
    public static final Color WARNING = new Color(245, 158, 11);       // #F59E0B - Amber 500
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color DANGER = new Color(239, 68, 68);         // #EF4444 - Red 500
    public static final Color DANGER_LIGHT = new Color(254, 242, 242);
    public static final Color BORDER = new Color(226, 232, 240);       // #E2E8F0 - Slate 200
    public static final Color BORDER_FOCUS = new Color(37, 99, 235);

    // Font Selection - Check for modern sans-serif fonts on user system
    private static final String FONT_FAMILY = resolveFontFamily();

    private static String resolveFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Set<String> fonts = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));
        if (fonts.contains("Inter")) return "Inter";
        if (fonts.contains("Poppins")) return "Poppins";
        if (fonts.contains("Segoe UI")) return "Segoe UI";
        if (fonts.contains("Helvetica Neue")) return "Helvetica Neue";
        return "Arial";
    }

    public static final Font FONT_HERO = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY, Font.BOLD, 10);

    public static String getFontFamily() {
        return FONT_FAMILY;
    }

    private ModernTheme() {
    }

    public static JButton createPrimaryButton(String text) {
        return createModernButton(text, ACCENT, ACCENT_HOVER, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createModernButton(text, SUCCESS, new Color(5, 150, 105), Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createModernButton(text, DANGER, new Color(220, 38, 38), Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 16, 8, 16)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(241, 245, 249));
                btn.setBorder(new CompoundBorder(new LineBorder(TEXT_MUTED, 1, true), new EmptyBorder(8, 16, 8, 16)));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 16, 8, 16)));
            }
        });
        return btn;
    }

    public static JButton createModernButton(String text, Color baseColor, Color hoverColor, Color textColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFont(FONT_BOLD);
        btn.setBackground(baseColor);
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
                btn.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseColor);
                btn.repaint();
            }
        });
        return btn;
    }

    public static JPanel createCardPanel() {
        return createCardPanel(12);
    }

    public static JPanel createCardPanel(int cornerRadius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Subtle soft shadow
                g2.setColor(new Color(15, 23, 42, 8));
                g2.fill(new RoundRectangle2D.Float(1, 2, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius));

                // Card surface
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));

                // Subtle card border
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBackground(BG_CARD);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        return panel;
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setFont(FONT_CAPTION);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        return badge;
    }

    public static JPanel createEmptyState(String title, String subtitle, String iconName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(40, 20, 40, 20));

        JLabel iconLabel = new JLabel(ModernIcons.get(iconName, 44, TEXT_MUTED));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_REGULAR);
        subLabel.setForeground(TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(subLabel);

        return panel;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(36);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_DARK);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBackground(new Color(248, 250, 252));
                setForeground(TEXT_SECONDARY);
                setFont(FONT_BOLD);
                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                setHorizontalAlignment(SwingConstants.LEFT);
                return c;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                    setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                    if (!isSelected) {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                        c.setForeground(TEXT_DARK);
                    } else {
                        c.setBackground(PRIMARY_LIGHT);
                        c.setForeground(PRIMARY_DARK);
                    }
                    return c;
                }
            });
        }
    }
}
