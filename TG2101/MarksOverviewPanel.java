package com.fot.ams.ui.admin;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.EligibilityRecord;
import com.fot.ams.model.Grade;
import com.fot.ams.service.EligibilityService;
import com.fot.ams.service.GPAService;
import com.fot.ams.service.GradeService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Admin panel for viewing student marks, grades, and eligibility across all courses.
 * Member 01 (Admin) uses this to monitor academic data for all students.
 */
public class MarksOverviewPanel extends JPanel {

    private final GradeService gradeService;
    private final EligibilityService eligibilityService;
    private final CourseDAO courseDAO;

    private JTabbedPane tabbedPane;

    // Tab 1 – Grade Overview
    private JComboBox<CourseItem> cmbGradeCourse;
    private JTable gradesTable;
    private DefaultTableModel gradesModel;
    private JLabel lblGradeStats;

    // Tab 2 – Eligibility Overview
    private JComboBox<CourseItem> cmbEligCourse;
    private JTable eligTable;
    private DefaultTableModel eligModel;
    private JComboBox<String> cmbEligFilter;

    private static class CourseItem {
        final int id;
        final String code;
        final String name;

        CourseItem(int id, String code, String name) {
            this.id = id;
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return code + " – " + name;
        }
    }

    public MarksOverviewPanel() {
        this.gradeService = new GradeService();
        this.eligibilityService = new EligibilityService();
        this.courseDAO = new CourseDAO();
        initComponents();
        loadCourses();
    }

    // -------------------------------------------------------------------------
    // UI Construction
    // -------------------------------------------------------------------------
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JLabel header = new JLabel("Marks & Grade Overview");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(ModernTheme.TEXT_DARK);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_BOLD);
        tabbedPane.addTab("Grades by Course", buildGradesTab());
        tabbedPane.addTab("Eligibility Status", buildEligibilityTab());
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ---- Tab 1: Grades by Course ----
    private JPanel buildGradesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Controls bar
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        controls.setBackground(Color.WHITE);
        controls.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lbl = new JLabel("Course:");
        lbl.setFont(ModernTheme.FONT_BOLD);
        cmbGradeCourse = new JComboBox<>();
        cmbGradeCourse.setFont(ModernTheme.FONT_REGULAR);
        cmbGradeCourse.setPreferredSize(new Dimension(340, 30));

        JButton btnLoad = ModernTheme.createPrimaryButton("Load Grades");
        JButton btnCalc = ModernTheme.createSecondaryButton("Recalculate All Grades");
        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh");

        lblGradeStats = new JLabel("  Select a course to view grades.");
        lblGradeStats.setFont(ModernTheme.FONT_SMALL);
        lblGradeStats.setForeground(ModernTheme.TEXT_MUTED);

        controls.add(lbl);
        controls.add(cmbGradeCourse);
        controls.add(btnLoad);
        controls.add(btnCalc);
        controls.add(btnRefresh);
        controls.add(lblGradeStats);
        panel.add(controls, BorderLayout.NORTH);

        // Grades table
        String[] cols = {"Reg No", "Student Name", "CA Mark (%)", "Final Exam (%)", "Total (%)", "Grade", "Grade Point", "Repeat"};
        gradesModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        gradesTable = new JTable(gradesModel);
        gradesTable.setRowHeight(26);
        gradesTable.setFont(ModernTheme.FONT_REGULAR);
        gradesTable.getTableHeader().setFont(ModernTheme.FONT_BOLD);
        gradesTable.getTableHeader().setBackground(ModernTheme.BG_SIDEBAR);
        gradesTable.getTableHeader().setForeground(Color.WHITE);
        gradesTable.setSelectionBackground(new Color(219, 234, 254));
        gradesTable.setGridColor(ModernTheme.BORDER);
        gradesTable.setDefaultRenderer(Object.class, new GradeCellRenderer());

        JScrollPane scroll = new JScrollPane(gradesTable);
        scroll.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        // Wire actions
        btnLoad.addActionListener(e -> loadGrades());
        btnCalc.addActionListener(e -> recalculateGrades());
        btnRefresh.addActionListener(e -> loadGrades());
        cmbGradeCourse.addActionListener(e -> loadGrades());

        return panel;
    }

    // ---- Tab 2: Eligibility Status ----
    private JPanel buildEligibilityTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ModernTheme.BG_MAIN);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Controls bar
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        controls.setBackground(Color.WHITE);
        controls.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lbl = new JLabel("Course:");
        lbl.setFont(ModernTheme.FONT_BOLD);
        cmbEligCourse = new JComboBox<>();
        cmbEligCourse.setFont(ModernTheme.FONT_REGULAR);
        cmbEligCourse.setPreferredSize(new Dimension(300, 30));

        JLabel lblFilter = new JLabel("  Show:");
        lblFilter.setFont(ModernTheme.FONT_BOLD);
        cmbEligFilter = new JComboBox<>(new String[]{"All Students", "Eligible Only", "Not Eligible Only"});
        cmbEligFilter.setFont(ModernTheme.FONT_REGULAR);

        JButton btnLoad = ModernTheme.createPrimaryButton("Load Eligibility");
        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh");

        controls.add(lbl);
        controls.add(cmbEligCourse);
        controls.add(lblFilter);
        controls.add(cmbEligFilter);
        controls.add(btnLoad);
        controls.add(btnRefresh);
        panel.add(controls, BorderLayout.NORTH);

        // Eligibility table
        String[] cols = {"Reg No", "Student Name", "CA Mark (%)", "Att. % (Theory)", "CA Eligible", "Att. Eligible", "OVERALL"};
        eligModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        eligTable = new JTable(eligModel);
        eligTable.setRowHeight(26);
        eligTable.setFont(ModernTheme.FONT_REGULAR);
        eligTable.getTableHeader().setFont(ModernTheme.FONT_BOLD);
        eligTable.getTableHeader().setBackground(ModernTheme.BG_SIDEBAR);
        eligTable.getTableHeader().setForeground(Color.WHITE);
        eligTable.setSelectionBackground(new Color(219, 234, 254));
        eligTable.setGridColor(ModernTheme.BORDER);
        eligTable.setDefaultRenderer(Object.class, new EligibilityCellRenderer());

        JScrollPane scroll = new JScrollPane(eligTable);
        scroll.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        // Wire actions
        btnLoad.addActionListener(e -> loadEligibility());
        btnRefresh.addActionListener(e -> loadEligibility());
        cmbEligCourse.addActionListener(e -> loadEligibility());
        cmbEligFilter.addActionListener(e -> applyEligibilityFilter());

        return panel;
    }

    // -------------------------------------------------------------------------
    // Data Loading
    // -------------------------------------------------------------------------
    private void loadCourses() {
        // Run database work off the Swing event thread to keep the screen responsive.
        SwingWorker<List<Course>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Course> doInBackground() {
                return courseDAO.getAllCourses();
            }

            @Override
            protected void done() {
                try {
                    List<Course> courses = get();
                    cmbGradeCourse.removeAllItems();
                    cmbEligCourse.removeAllItems();
                    for (Course c : courses) {
                        CourseItem item = new CourseItem(c.getCourseId(), c.getCourseCode(), c.getCourseName());
                        cmbGradeCourse.addItem(item);
                        cmbEligCourse.addItem(item);
                    }
                    if (courses.isEmpty()) {
                        lblGradeStats.setText("  No courses found in the database.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MarksOverviewPanel.this,
                            "Error loading courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadGrades() {
        CourseItem selected = (CourseItem) cmbGradeCourse.getSelectedItem();
        if (selected == null) return;

        SwingWorker<List<Grade>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Grade> doInBackground() {
                return gradeService.getGradesByCourse(selected.id);
            }

            @Override
            protected void done() {
                try {
                    List<Grade> grades = get();
                    gradesModel.setRowCount(0);
                    int aPlus = 0, aCount = 0, bCount = 0, cCount = 0, dCount = 0, eCount = 0;
                    for (Grade g : grades) {
                        String repeat = g.isRepeat() ? "Yes" : "No";
                        gradesModel.addRow(new Object[]{
                                g.getStudentRegNo(),
                                g.getStudentName(),
                                String.format("%.1f", g.getCaMark()),
                                String.format("%.1f", g.getFinalExamMark()),
                                String.format("%.1f", g.getTotalMark()),
                                g.getGradeLetter(),
                                String.format("%.2f", g.getGradePoint()),
                                repeat
                        });
                        String gl = g.getGradeLetter();
                        if (gl.startsWith("A")) aPlus++;
                        else if (gl.startsWith("B")) bCount++;
                        else if (gl.startsWith("C")) cCount++;
                        else if (gl.startsWith("D")) dCount++;
                        else eCount++;
                    }
                    lblGradeStats.setText(String.format(
                            "  Total: %d  |  A: %d  B: %d  C: %d  D: %d  E: %d",
                            grades.size(), aPlus, bCount, cCount, dCount, eCount));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MarksOverviewPanel.this,
                            "Error loading grades: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void recalculateGrades() {
        CourseItem selected = (CourseItem) cmbGradeCourse.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a course first.", "No Course", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Recalculate and overwrite grades for all students in " + selected.code + "?\n" +
                "This will update the grades table based on current marks and attendance.",
                "Confirm Recalculation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                gradeService.calculateGradesForCourse(selected.id, "2024/2025");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(MarksOverviewPanel.this,
                            "Grades recalculated successfully for " + selected.code + ".",
                            "Done", JOptionPane.INFORMATION_MESSAGE);
                    loadGrades();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MarksOverviewPanel.this,
                            "Error recalculating grades: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private List<EligibilityRecord> cachedEligibility = null;

    private void loadEligibility() {
        CourseItem selected = (CourseItem) cmbEligCourse.getSelectedItem();
        if (selected == null) return;

        SwingWorker<List<EligibilityRecord>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<EligibilityRecord> doInBackground() {
                return eligibilityService.getBatchEligibility(selected.id);
            }

            @Override
            protected void done() {
                try {
                    cachedEligibility = get();
                    applyEligibilityFilter();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MarksOverviewPanel.this,
                            "Error loading eligibility: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyEligibilityFilter() {
        if (cachedEligibility == null) return;
        eligModel.setRowCount(0);
        String filter = (String) cmbEligFilter.getSelectedItem();
        for (EligibilityRecord r : cachedEligibility) {
            boolean caOk = r.isCaEligible();
            boolean attOk = r.isAttendanceEligible();
            boolean overall = r.isOverallEligible();
            if ("Eligible Only".equals(filter) && !overall) continue;
            if ("Not Eligible Only".equals(filter) && overall) continue;
            eligModel.addRow(new Object[]{
                    r.getStudentRegNo(),
                    r.getStudentName(),
                    String.format("%.1f", r.getCaMark()),
                    String.format("%.1f", r.getAttendancePercentage()),
                    caOk ? "✓ YES" : "✗ NO",
                    attOk ? "✓ YES" : "✗ NO",
                    overall ? "✅ ELIGIBLE" : "❌ NOT ELIGIBLE"
            });
        }
    }

    // -------------------------------------------------------------------------
    // Custom Cell Renderers for colour coding
    // -------------------------------------------------------------------------
    private static class GradeCellRenderer extends JLabel implements TableCellRenderer {
        GradeCellRenderer() { setOpaque(true); setBorder(new EmptyBorder(2, 8, 2, 8)); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
            setText(val == null ? "" : val.toString());
            setFont(ModernTheme.FONT_REGULAR);
            setForeground(Color.BLACK);

            if (sel) {
                setBackground(new Color(219, 234, 254));
            } else if (col == 5) { // Grade letter column
                String g = val == null ? "" : val.toString();
                setBackground(gradeColor(g));
                setForeground(Color.WHITE);
                setFont(ModernTheme.FONT_BOLD);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            return this;
        }

        private Color gradeColor(String g) {
            if (g.startsWith("A")) return new Color(22, 163, 74);   // green
            if (g.startsWith("B")) return new Color(37, 99, 235);   // blue
            if (g.startsWith("C")) return new Color(217, 119, 6);   // amber
            if (g.startsWith("D")) return new Color(234, 88, 12);   // orange
            return new Color(185, 28, 28);                           // red = E
        }
    }

    private static class EligibilityCellRenderer extends JLabel implements TableCellRenderer {
        EligibilityCellRenderer() { setOpaque(true); setBorder(new EmptyBorder(2, 8, 2, 8)); }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
            setText(val == null ? "" : val.toString());
            setFont(ModernTheme.FONT_REGULAR);
            setForeground(Color.BLACK);

            if (sel) {
                setBackground(new Color(219, 234, 254));
            } else if (col == 6) { // OVERALL column
                String v = val == null ? "" : val.toString();
                boolean eligible = v.contains("ELIGIBLE") && !v.contains("NOT");
                setBackground(eligible ? new Color(220, 252, 231) : new Color(254, 226, 226));
                setForeground(eligible ? new Color(22, 101, 52) : new Color(153, 27, 27));
                setFont(ModernTheme.FONT_BOLD);
            } else if (col == 4 || col == 5) { // CA/Att eligible
                String v = val == null ? "" : val.toString();
                setBackground(v.contains("YES") ? new Color(220, 252, 231) : new Color(254, 226, 226));
                setForeground(v.contains("YES") ? new Color(22, 101, 52) : new Color(153, 27, 27));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            return this;
        }
    }
}
