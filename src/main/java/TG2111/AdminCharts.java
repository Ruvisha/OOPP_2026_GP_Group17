package com.fot.ams.ui.admin;

import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Modern Java2D charts for the FOT-AMS Dashboard.
 * Anti-aliased vector rendering with gradients, rounded bars, donut charts, and trend curves.
 */
public class AdminCharts {

    public static final Color[] CHART_PALETTE = {
        new Color(37, 99, 235),   // Royal Blue #2563EB
        new Color(16, 185, 129),  // Emerald Green #10B981
        new Color(245, 158, 11),  // Amber #F59E0B
        new Color(139, 92, 246),  // Purple #8B5CF6
        new Color(14, 165, 233),  // Sky Blue #0EA5E9
        new Color(236, 72, 153),  // Rose #EC4899
        new Color(100, 116, 139)  // Slate #64748B
    };

    /**
     * Donut Chart Component with Legend
     */
    public static class DonutChartPanel extends JPanel {
        private final Map<String, Integer> data = new LinkedHashMap<>();
        private final String title;

        public DonutChartPanel(String title) {
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(320, 240));
        }

        public void setData(Map<String, Integer> newData) {
            data.clear();
            if (newData != null) {
                data.putAll(newData);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Card background
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            g2.setColor(ModernTheme.BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12));

            // Title
            g2.setColor(ModernTheme.TEXT_DARK);
            g2.setFont(ModernTheme.FONT_SUBTITLE);
            g2.drawString(title, 18, 28);

            int total = 0;
            for (int val : data.values()) total += val;

            if (total == 0) {
                g2.setFont(ModernTheme.FONT_REGULAR);
                g2.setColor(ModernTheme.TEXT_MUTED);
                g2.drawString("No distribution data available", 20, h / 2);
                g2.dispose();
                return;
            }

            int diameter = Math.min(w / 2, h - 60);
            int cx = 20 + diameter / 2;
            int cy = 35 + (h - 45) / 2;
            int radius = diameter / 2;
            int innerRadius = (int) (radius * 0.58);

            double curAngle = 90;
            int idx = 0;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                double arcAngle = 360.0 * entry.getValue() / total;
                Color c = CHART_PALETTE[idx % CHART_PALETTE.length];

                Arc2D.Double outerArc = new Arc2D.Double(
                    cx - radius, cy - radius, radius * 2, radius * 2,
                    curAngle, -arcAngle, Arc2D.PIE
                );
                g2.setColor(c);
                g2.fill(outerArc);

                curAngle -= arcAngle;
                idx++;
            }

            // Cut out donut center
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2));
            g2.setColor(ModernTheme.BORDER);
            g2.draw(new Ellipse2D.Double(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2));

            // Center total
            g2.setColor(ModernTheme.TEXT_DARK);
            g2.setFont(new Font(ModernTheme.getFontFamily(), Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            String totalStr = String.valueOf(total);
            g2.drawString(totalStr, cx - fm.stringWidth(totalStr) / 2, cy + 2);

            g2.setFont(ModernTheme.FONT_CAPTION);
            g2.setColor(ModernTheme.TEXT_MUTED);
            FontMetrics fmCap = g2.getFontMetrics();
            g2.drawString("TOTAL", cx - fmCap.stringWidth("TOTAL") / 2, cy + 16);

            // Draw Legend on right side
            int legendX = cx + radius + 25;
            int legendY = 60;
            idx = 0;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                if (legendY > h - 15) break;
                Color c = CHART_PALETTE[idx % CHART_PALETTE.length];

                // Color dot
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(legendX, legendY - 8, 10, 10, 3, 3));

                // Text
                g2.setColor(ModernTheme.TEXT_DARK);
                g2.setFont(ModernTheme.FONT_SMALL);
                String label = entry.getKey();
                int pct = (int) Math.round(100.0 * entry.getValue() / total);
                String valStr = entry.getValue() + " (" + pct + "%)";

                g2.drawString(label, legendX + 16, legendY);
                g2.setColor(ModernTheme.TEXT_MUTED);
                g2.setFont(ModernTheme.FONT_CAPTION);
                g2.drawString(valStr, legendX + 16, legendY + 12);

                legendY += 28;
                idx++;
            }

            g2.dispose();
        }
    }

    /**
     * Vertical Bar Chart Component
     */
    public static class BarChartPanel extends JPanel {
        private final Map<String, Integer> data = new LinkedHashMap<>();
        private final String title;
        private final Color barColor;

        public BarChartPanel(String title, Color barColor) {
            this.title = title;
            this.barColor = barColor != null ? barColor : ModernTheme.ACCENT;
            setOpaque(false);
            setPreferredSize(new Dimension(320, 240));
        }

        public void setData(Map<String, Integer> newData) {
            data.clear();
            if (newData != null) {
                data.putAll(newData);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background card
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            g2.setColor(ModernTheme.BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12));

            // Title
            g2.setColor(ModernTheme.TEXT_DARK);
            g2.setFont(ModernTheme.FONT_SUBTITLE);
            g2.drawString(title, 18, 28);

            if (data.isEmpty()) {
                g2.setFont(ModernTheme.FONT_REGULAR);
                g2.setColor(ModernTheme.TEXT_MUTED);
                g2.drawString("No data available", 20, h / 2);
                g2.dispose();
                return;
            }

            int maxVal = 1;
            for (int val : data.values()) {
                if (val > maxVal) maxVal = val;
            }

            int topY = 55;
            int bottomY = h - 45;
            int chartH = bottomY - topY;
            int startX = 30;
            int chartW = w - startX - 25;

            // Draw horizontal guide lines
            g2.setColor(new Color(241, 245, 249));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine(startX, topY, startX + chartW, topY);
            g2.drawLine(startX, topY + chartH / 2, startX + chartW, topY + chartH / 2);
            g2.drawLine(startX, bottomY, startX + chartW, bottomY);

            int count = data.size();
            int slotWidth = chartW / Math.max(1, count);
            int barWidth = Math.min(36, Math.max(14, slotWidth - 16));

            int idx = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int x = startX + idx * slotWidth + (slotWidth - barWidth) / 2;
                int barH = (int) ((double) entry.getValue() / maxVal * chartH);
                int y = bottomY - barH;

                // Gradient bar
                Color cTop = CHART_PALETTE[idx % CHART_PALETTE.length];
                Color cBot = new Color(cTop.getRed(), cTop.getGreen(), cTop.getBlue(), 180);
                GradientPaint gp = new GradientPaint(x, y, cTop, x, bottomY, cBot);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barH, 6, 6));

                // Value above bar
                g2.setColor(ModernTheme.TEXT_DARK);
                g2.setFont(ModernTheme.FONT_CAPTION);
                FontMetrics fm = g2.getFontMetrics();
                String valStr = String.valueOf(entry.getValue());
                g2.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 4);

                // Label below bar
                g2.setColor(ModernTheme.TEXT_MUTED);
                g2.setFont(ModernTheme.FONT_SMALL);
                FontMetrics fmLbl = g2.getFontMetrics();
                String lbl = entry.getKey();
                if (lbl.length() > 8) lbl = lbl.substring(0, 7) + "…";
                g2.drawString(lbl, x + (barWidth - fmLbl.stringWidth(lbl)) / 2, bottomY + 18);

                idx++;
            }

            g2.dispose();
        }
    }

    /**
     * Line / Area Trend Chart Component
     */
    public static class TrendLineChartPanel extends JPanel {
        private final Map<String, Integer> data = new LinkedHashMap<>();
        private final String title;

        public TrendLineChartPanel(String title) {
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(320, 240));
        }

        public void setData(Map<String, Integer> newData) {
            data.clear();
            if (newData != null) {
                data.putAll(newData);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background card
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 12, 12));
            g2.setColor(ModernTheme.BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 12, 12));

            // Title
            g2.setColor(ModernTheme.TEXT_DARK);
            g2.setFont(ModernTheme.FONT_SUBTITLE);
            g2.drawString(title, 18, 28);

            if (data.isEmpty()) {
                g2.setFont(ModernTheme.FONT_REGULAR);
                g2.setColor(ModernTheme.TEXT_MUTED);
                g2.drawString("No trend data available", 20, h / 2);
                g2.dispose();
                return;
            }

            int maxVal = 1;
            for (int val : data.values()) {
                if (val > maxVal) maxVal = val;
            }

            int topY = 60;
            int bottomY = h - 45;
            int chartH = bottomY - topY;
            int startX = 35;
            int chartW = w - startX - 30;

            // Horizontal grid lines
            g2.setColor(new Color(241, 245, 249));
            g2.drawLine(startX, topY, startX + chartW, topY);
            g2.drawLine(startX, topY + chartH / 2, startX + chartW, topY + chartH / 2);
            g2.drawLine(startX, bottomY, startX + chartW, bottomY);

            List<Point2D.Float> points = new ArrayList<>();
            List<String> labels = new ArrayList<>(data.keySet());
            int n = data.size();
            float stepX = n > 1 ? (float) chartW / (n - 1) : chartW / 2.0f;

            int idx = 0;
            for (int val : data.values()) {
                float px = n > 1 ? (startX + idx * stepX) : (startX + chartW / 2.0f);
                float py = bottomY - ((float) val / maxVal * chartH);
                points.add(new Point2D.Float(px, py));
                idx++;
            }

            // Fill area underneath path
            if (points.size() > 1) {
                Path2D.Float area = new Path2D.Float();
                area.moveTo(points.get(0).x, bottomY);
                for (Point2D.Float p : points) {
                    area.lineTo(p.x, p.y);
                }
                area.lineTo(points.get(points.size() - 1).x, bottomY);
                area.closePath();

                GradientPaint gp = new GradientPaint(
                    0, topY, new Color(37, 99, 235, 70),
                    0, bottomY, new Color(37, 99, 235, 5)
                );
                g2.setPaint(gp);
                g2.fill(area);
            }

            // Stroke line
            Path2D.Float line = new Path2D.Float();
            for (int i = 0; i < points.size(); i++) {
                Point2D.Float p = points.get(i);
                if (i == 0) line.moveTo(p.x, p.y);
                else line.lineTo(p.x, p.y);
            }
            g2.setColor(ModernTheme.ACCENT);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(line);

            // Draw dots & value text
            for (int i = 0; i < points.size(); i++) {
                Point2D.Float p = points.get(i);

                // Outer halo
                g2.setColor(new Color(219, 234, 254));
                g2.fill(new Ellipse2D.Float(p.x - 6, p.y - 6, 12, 12));

                // Inner dot
                g2.setColor(ModernTheme.ACCENT);
                g2.fill(new Ellipse2D.Float(p.x - 3.5f, p.y - 3.5f, 7, 7));

                // Value above dot
                g2.setColor(ModernTheme.TEXT_DARK);
                g2.setFont(ModernTheme.FONT_CAPTION);
                String valStr = String.valueOf(data.get(labels.get(i)));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valStr, p.x - fm.stringWidth(valStr) / 2, p.y - 8);

                // Label below axis
                g2.setColor(ModernTheme.TEXT_MUTED);
                g2.setFont(ModernTheme.FONT_SMALL);
                FontMetrics fmLbl = g2.getFontMetrics();
                String lbl = labels.get(i);
                g2.drawString(lbl, p.x - fmLbl.stringWidth(lbl) / 2, bottomY + 18);
            }

            g2.dispose();
        }
    }
}
