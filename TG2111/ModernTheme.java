package com.fot.ams.ui.common;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Modern styling utilities and theme constants for Swing UI.
 */
public class ModernTheme {
    // Color Palette
    public static final Color PRIMARY = new Color(26, 54, 138);       // Deep Royal Blue
    public static final Color PRIMARY_DARK = new Color(15, 30, 84);   // Navy
    public static final Color PRIMARY_LIGHT = new Color(238, 242, 255); // Soft Indigo Tint
    public static final Color ACCENT = new Color(37, 99, 235);        // Vibrant Blue
    public static final Color ACCENT_HOVER = new Color(29, 78, 216);

    public static final Color BG_MAIN = new Color(248, 250, 252);     // Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(15, 23, 42);     // Slate 900
    public static final Color BG_SIDEBAR_ACTIVE = new Color(30, 41, 59);

    public static final Color TEXT_DARK = new Color(15, 23, 42);      // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);  // Slate 100

    public static final Color SUCCESS = new Color(22, 163, 74);       // Emerald 600
    public static final Color WARNING = new Color(217, 119, 6);       // Amber 600
    public static final Color DANGER = new Color(220, 38, 38);        // Red 600
    public static final Color BORDER = new Color(226, 232, 240);      // Slate 200

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private ModernTheme() {
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(7, 15, 7, 15)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(BORDER);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                    setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                    if (!isSelected) {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }
                    return c;
                }
            });
        }
    }
}
