import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBorder(new EmptyBorder(16, 20, 20, 20));
        scrollContent.setBackground(new Color(245, 247, 250));

        // Top Metrics Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 14, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        statsGrid.add(createMetricCard("Sessions Per Module", "15 Theory + 15 Practical", new Color(37, 99, 235)));
        statsGrid.add(createMetricCard("Medical Certificates Logged", "6", new Color(245, 158, 11)));
        statsGrid.add(createMetricCard("Attendance Eligibility", "≥ 80% Threshold", new Color(16, 185, 129)));

        scrollContent.add(statsGrid);
        scrollContent.add(Box.createVerticalStrut(18));

        // Responsibilities Card
        JPanel infoCard = new JPanel(new BorderLayout(10, 10));
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel title = new JLabel("Technical Officer Operational Responsibilities (SRS Group 17)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(30, 41, 59));
        infoCard.add(title, BorderLayout.NORTH);

        JTextArea infoText = new JTextArea(
                "• Attendance Recording: Record theory and practical attendance session by session (1 to 15) for student batches.\n\n" +
                        "• Attendance Summary & Scenarios: Analyze attendance percentages and verify compliance across the 5 mandatory scenarios:\n" +
                        "   1. More than 80%\n   2. Exactly 80%\n   3. Less than 80% without medicals\n   4. More than 80% with approved medicals\n   5. Less than 80% with approved medicals\n\n" +
                        "• Medical Records: Maintain student medical certificates and verify approval status (APPROVED / NOT_APPROVED) by the University Medical Officer.\n\n" +
                        "• Laboratory Timetables: Inspect department timetable allocations to prepare lab workstations and apparatus."
        );
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoText.setForeground(new Color(51, 65, 85));
        infoText.setEditable(false);
        infoText.setOpaque(false);
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);

        infoCard.add(infoText, BorderLayout.CENTER);
        scrollContent.add(infoCard);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, String value, Color barColor) {
        JPanel card = new JPanel(new BorderLayout(8, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        JPanel leftIndicator = new JPanel();
        leftIndicator.setBackground(barColor);
        leftIndicator.setPreferredSize(new Dimension(5, 0));
        card.add(leftIndicator, BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(4, 4));
        content.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(100, 116, 139));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(new Color(30, 41, 59));

        content.add(titleLbl, BorderLayout.NORTH);
        content.add(valLbl, BorderLayout.CENTER);
        card.add(content, BorderLayout.CENTER);

        return card;
    }
}
