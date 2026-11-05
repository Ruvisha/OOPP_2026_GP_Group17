package com.fot.ams.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * High-performance vector icon renderer for the FOT-AMS modern dashboard.
 * Pure Java2D implementation ensures zero external dependencies and crisp rendering at any DPI.
 */
public class ModernIcons {

    public static Icon get(String name, int size, Color color) {
        return new VectorIcon(name, size, color);
    }

    public static Icon get(String name, int size) {
        return new VectorIcon(name, size, ModernTheme.TEXT_MUTED);
    }

    private static class VectorIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;

        public VectorIcon(String name, int size, Color color) {
            this.name = name != null ? name.toLowerCase() : "";
            this.size = size;
            this.color = color != null ? color : Color.DARK_GRAY;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.setColor(color);

            float s = size / 24.0f; // Baseline 24x24 scale
            g2.scale(s, s);
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (name) {
                case "dashboard":
                case "overview":
                    // 4 grid boxes
                    g2.draw(new RoundRectangle2D.Float(3, 3, 7.5f, 7.5f, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(13.5f, 3, 7.5f, 7.5f, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(3, 13.5f, 7.5f, 7.5f, 2, 2));
                    g2.draw(new RoundRectangle2D.Float(13.5f, 13.5f, 7.5f, 7.5f, 2, 2));
                    break;

                case "users":
                case "user_group":
                    // Main user head & body
                    g2.draw(new Ellipse2D.Float(6.5f, 3, 6, 6));
                    g2.draw(new Arc2D.Float(3.5f, 11, 12, 10, 0, 180, Arc2D.OPEN));
                    // Second user in background
                    g2.draw(new Arc2D.Float(14.5f, 5, 5, 5, -30, 200, Arc2D.OPEN));
                    g2.draw(new Arc2D.Float(13.5f, 12, 8, 8, 20, 120, Arc2D.OPEN));
                    break;

                case "user":
                case "profile":
                    g2.draw(new Ellipse2D.Float(7.5f, 3.5f, 9, 9));
                    g2.draw(new Arc2D.Float(3.5f, 13.5f, 17, 13, 0, 180, Arc2D.OPEN));
                    break;

                case "courses":
                case "book":
                case "curriculum":
                    // Open book
                    Path2D bookLeft = new Path2D.Float();
                    bookLeft.moveTo(12, 6);
                    bookLeft.quadTo(7, 4, 3, 5);
                    bookLeft.lineTo(3, 18);
                    bookLeft.quadTo(7, 17, 12, 19);
                    bookLeft.closePath();
                    g2.draw(bookLeft);

                    Path2D bookRight = new Path2D.Float();
                    bookRight.moveTo(12, 6);
                    bookRight.quadTo(17, 4, 21, 5);
                    bookRight.lineTo(21, 18);
                    bookRight.quadTo(17, 17, 12, 19);
                    bookRight.closePath();
                    g2.draw(bookRight);
                    break;

                case "students":
                case "enrollments":
                case "academic":
                    // Graduation cap
                    Path2D cap = new Path2D.Float();
                    cap.moveTo(12, 4);
                    cap.lineTo(22, 9);
                    cap.lineTo(12, 14);
                    cap.lineTo(2, 9);
                    cap.closePath();
                    g2.draw(cap);
                    // Cap bottom
                    g2.draw(new Arc2D.Float(6, 11, 12, 8, 190, 160, Arc2D.OPEN));
                    // Tassel
                    g2.drawLine(20, 10, 20, 16);
                    break;

                case "marks":
                case "grades":
                case "award":
                    // Medal / Award
                    g2.draw(new Ellipse2D.Float(6, 3, 12, 12));
                    Path2D ribbon = new Path2D.Float();
                    ribbon.moveTo(8.5f, 14);
                    ribbon.lineTo(6.5f, 21);
                    ribbon.lineTo(12, 18.5f);
                    ribbon.lineTo(17.5f, 21);
                    ribbon.lineTo(15.5f, 14);
                    g2.draw(ribbon);
                    break;

                case "notices":
                case "bell":
                    // Bell
                    Path2D bell = new Path2D.Float();
                    bell.moveTo(12, 3);
                    bell.lineTo(12, 4.5f);
                    bell.curveTo(8.5f, 5, 6, 8, 6, 12);
                    bell.lineTo(4, 16);
                    bell.lineTo(20, 16);
                    bell.lineTo(18, 12);
                    bell.curveTo(18, 8, 15.5f, 5, 12, 4.5f);
                    g2.draw(bell);
                    g2.draw(new Arc2D.Float(10, 16, 4, 3.5f, 180, 180, Arc2D.OPEN));
                    break;

                case "timetables":
                case "calendar":
                    // Calendar
                    g2.draw(new RoundRectangle2D.Float(3, 4, 18, 17, 3, 3));
                    g2.drawLine(3, 9, 21, 9);
                    g2.drawLine(7, 2, 7, 5);
                    g2.drawLine(17, 2, 17, 5);
                    g2.fill(new Rectangle2D.Float(7, 12, 2, 2));
                    g2.fill(new Rectangle2D.Float(11, 12, 2, 2));
                    g2.fill(new Rectangle2D.Float(15, 12, 2, 2));
                    g2.fill(new Rectangle2D.Float(7, 15, 2, 2));
                    g2.fill(new Rectangle2D.Float(11, 15, 2, 2));
                    break;

                case "reports":
                case "analytics":
                case "chart":
                    // Bar chart
                    g2.drawLine(3, 21, 21, 21);
                    g2.draw(new Rectangle2D.Float(5, 12, 3, 9));
                    g2.draw(new Rectangle2D.Float(10.5f, 7, 3, 14));
                    g2.draw(new Rectangle2D.Float(16, 4, 3, 17));
                    break;

                case "audit":
                case "logs":
                case "shield":
                    // Shield with check
                    Path2D shield = new Path2D.Float();
                    shield.moveTo(12, 3);
                    shield.lineTo(20, 6.5f);
                    shield.curveTo(20, 13, 16.5f, 18, 12, 21);
                    shield.curveTo(7.5f, 18, 4, 13, 4, 6.5f);
                    shield.closePath();
                    g2.draw(shield);
                    g2.drawLine(9, 12, 11, 14);
                    g2.drawLine(11, 14, 15, 9);
                    break;

                case "settings":
                    // Gear
                    g2.draw(new Ellipse2D.Float(8, 8, 8, 8));
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.toRadians(i * 45);
                        float x1 = (float) (12 + 4 * Math.cos(angle));
                        float y1 = (float) (12 + 4 * Math.sin(angle));
                        float x2 = (float) (12 + 6.5 * Math.cos(angle));
                        float y2 = (float) (12 + 6.5 * Math.sin(angle));
                        g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                    }
                    break;

                case "logout":
                    // Exit box & arrow
                    Path2D door = new Path2D.Float();
                    door.moveTo(9, 3);
                    door.lineTo(4, 3);
                    door.lineTo(4, 21);
                    door.lineTo(9, 21);
                    g2.draw(door);
                    g2.drawLine(9, 12, 20, 12);
                    g2.drawLine(16, 8, 20, 12);
                    g2.drawLine(16, 16, 20, 12);
                    break;

                case "search":
                    g2.draw(new Ellipse2D.Float(4, 4, 11, 11));
                    g2.drawLine(13, 13, 20, 20);
                    break;

                case "plus":
                case "add":
                    g2.drawLine(12, 5, 12, 19);
                    g2.drawLine(5, 12, 19, 12);
                    break;

                case "refresh":
                    g2.draw(new Arc2D.Float(4, 4, 16, 16, 30, 280, Arc2D.OPEN));
                    g2.drawLine(17, 3, 20, 6);
                    g2.drawLine(15, 8, 20, 6);
                    break;

                case "check":
                    g2.drawLine(5, 12, 10, 17);
                    g2.drawLine(10, 17, 19, 6);
                    break;

                case "download":
                case "export":
                    g2.drawLine(12, 3, 12, 15);
                    g2.drawLine(8, 11, 12, 15);
                    g2.drawLine(16, 11, 12, 15);
                    g2.drawLine(4, 19, 20, 19);
                    break;

                case "info":
                    g2.draw(new Ellipse2D.Float(3, 3, 18, 18));
                    g2.drawLine(12, 11, 12, 16);
                    g2.fill(new Ellipse2D.Float(11, 7, 2, 2));
                    break;

                default:
                    // Generic bullet dot / circle
                    g2.draw(new Ellipse2D.Float(6, 6, 12, 12));
                    break;
            }

            g2.dispose();
        }
    }
}
