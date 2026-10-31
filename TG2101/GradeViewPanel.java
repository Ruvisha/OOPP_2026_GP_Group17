package com.fot.ams.ui.undergraduate;

import com.fot.ams.model.Grade;
import com.fot.ams.model.User;
import com.fot.ams.service.GPAService;
import com.fot.ams.service.GradeService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel enabling Undergraduates to view their academic grades, SGPA, and CGPA standing.
 */
public class GradeViewPanel extends JPanel {
    private final GradeService gradeService;
    private final GPAService gpaService;

    private JTable gradeTable;
    private DefaultTableModel tableModel;

    private JLabel lblSgpa;
    private JLabel lblCgpa;
    private JLabel lblClassification;
    private JLabel lblTotalCredits;

    public GradeViewPanel() {
        this.gradeService = new GradeService();
        this.gpaService = new GPAService();

        initComponents();
        loadStudentGrades();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top GPA Cards
        JPanel gpaRow = new JPanel(new GridLayout(1, 4, 15, 0));
        gpaRow.setOpaque(false);

        lblSgpa = new JLabel("0.00", SwingConstants.CENTER);
        lblCgpa = new JLabel("0.00", SwingConstants.CENTER);
        lblTotalCredits = new JLabel("0", SwingConstants.CENTER);
        lblClassification = new JLabel("-", SwingConstants.CENTER);

        gpaRow.add(createCard("Semester GPA (SGPA)", lblSgpa, ModernTheme.PRIMARY));
        gpaRow.add(createCard("Cumulative GPA (CGPA)", lblCgpa, ModernTheme.SUCCESS));
        gpaRow.add(createCard("Total Credits Completed", lblTotalCredits, new Color(79, 70, 229)));
        gpaRow.add(createCard("Degree Academic Standing", lblClassification, ModernTheme.WARNING));

        add(gpaRow, BorderLayout.NORTH);

        // Center Table: Course Grades
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {
            "Course Code", "Course Name", "Credits", "CA Mark (40%)",
            "Final Exam (60%)", "Total Mark", "Grade", "Grade Point (GP)", "Status"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        gradeTable = new JTable(tableModel);
        ModernTheme.styleTable(gradeTable);

        tablePanel.add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        JLabel footer = new JLabel(
            "<html><b>Grading Scale (UGC Commission Circular No. 12-2024):</b> " +
            "A+/A (4.00, \u226580) | A- (3.70, 75-79) | B+ (3.30, 70-74) | B (3.00, 65-69) | B- (2.70, 60-64) | " +
            "C+ (2.30, 55-59) | C (2.00, 50-54) | C- (1.70, 45-49) | D+ (1.30, 40-44) | D (1.00, 35-39) | E (0.00, &lt;35). " +
            "Repeat pass grades are capped at C (2.00 GP).</html>"
        );
        footer.setFont(ModernTheme.FONT_SMALL);
        footer.setForeground(ModernTheme.TEXT_MUTED);
        footer.setBorder(new EmptyBorder(8, 12, 8, 12));
        tablePanel.add(footer, BorderLayout.SOUTH);

        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel valLabel, Color accent) {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(ModernTheme.FONT_SMALL);
        t.setForeground(ModernTheme.TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLabel.setForeground(ModernTheme.TEXT_DARK);

        card.add(t, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadStudentGrades() {
        tableModel.setRowCount(0);
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<Grade> grades = gradeService.getGradesByStudent(cur.getUserId());
        int totalCredits = 0;

        for (Grade g : grades) {
            totalCredits += g.getCredits();
            tableModel.addRow(new Object[]{
                g.getCourseCode(),
                g.getCourseName(),
                g.getCredits(),
                String.format("%.1f", g.getCaMark()),
                String.format("%.1f", g.getFinalExamMark()),
                String.format("%.1f", g.getTotalMark()),
                g.getGradeLetter(),
                String.format("%.2f", g.getGradePoint()),
                g.isRepeat() ? "REPEAT PASS" : (g.getGradePoint() >= 2.0 ? "PASS" : "FAIL / REPEAT")
            });
        }

        double sgpa = gpaService.calculateSgpa(grades);
        double cgpa = gpaService.calculateCgpaForStudent(cur.getUserId());
        String standing = gpaService.getClassification(cgpa);

        // Update the summary cards from the same grade list shown in the table.
        lblSgpa.setText(String.format("%.2f", sgpa));
        lblCgpa.setText(String.format("%.2f", cgpa));
        lblTotalCredits.setText(String.valueOf(totalCredits));
        lblClassification.setText(standing);
    }
}
