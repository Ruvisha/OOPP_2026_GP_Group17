package com.fot.ams.ui.lecturer;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.*;
import com.fot.ams.service.EligibilityService;
import com.fot.ams.service.GradeService;
import com.fot.ams.service.MarkService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.SessionManager;
import com.fot.ams.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main marks evaluation, eligibility calculation, and grade management panel for lecturers.
 */
public class MarksPanel extends JPanel {
    private final MarkService markService;
    private final EligibilityService eligibilityService;
    private final GradeService gradeService;
    private final CourseDAO courseDAO;
    private final CourseEnrollmentDAO enrollmentDAO;

    private JTabbedPane tabbedPane;

    // Tab 1: Marks Entry
    private JComboBox<CourseItem> cmbMarksCourse;
    private JComboBox<EvalItem> cmbMarksEvaluation;
    private JTable marksTable;
    private DefaultTableModel marksTableModel;
    private JTextField txtStudentName;
    private JTextField txtMarkValue;
    private int selectedStudentId = 0;

    // Tab 2: Eligibility
    private JComboBox<CourseItem> cmbEligCourse;
    private JTable eligTable;
    private DefaultTableModel eligTableModel;
    private JComboBox<String> cmbEligFilter;

    // Tab 3: Grades & GPA
    private JComboBox<CourseItem> cmbGradeCourse;
    private JTable gradeTable;
    private DefaultTableModel gradeTableModel;

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
            return code + " - " + name;
        }
    }

    private static class EvalItem {
        final int id;
        final String name;
        final double weight;

        EvalItem(int id, String name, double weight) {
            this.id = id;
            this.name = name;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return name + " (" + weight + "%)";
        }
    }

    public MarksPanel() {
        this.markService = new MarkService();
        this.eligibilityService = new EligibilityService();
        this.gradeService = new GradeService();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();

        initComponents();
        loadCourses();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_BOLD);

        tabbedPane.addTab("Marks Upload & Evaluation", createMarksEntryTab());
        tabbedPane.addTab("CA & Exam Eligibility (CA \u2265 40%)", createEligibilityTab());
        tabbedPane.addTab("Final Grades & GPA Calculation", createGradesTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------------
    // TAB 1: MARKS ENTRY
    // ------------------------------------------------------------------------
    private JPanel createMarksEntryTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernTheme.BG_MAIN);

        // Top Filter
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        top.setBackground(Color.WHITE);
        top.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lCourse = new JLabel("Course:");
        lCourse.setFont(ModernTheme.FONT_BOLD);
        cmbMarksCourse = new JComboBox<>();
        cmbMarksCourse.addActionListener(e -> onCourseChanged());

        JLabel lEval = new JLabel("Evaluation Component:");
        lEval.setFont(ModernTheme.FONT_BOLD);
        cmbMarksEvaluation = new JComboBox<>();
        cmbMarksEvaluation.addActionListener(e -> loadMarksForSelectedEvaluation());

        JButton btnReload = ModernTheme.createSecondaryButton("Refresh Marks");
        btnReload.addActionListener(e -> loadMarksForSelectedEvaluation());

        top.add(lCourse);
        top.add(cmbMarksCourse);
        top.add(lEval);
        top.add(cmbMarksEvaluation);
        top.add(btnReload);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.70);
        split.setDividerLocation(680);

        // Marks Table (Editable marks column)
        String[] cols = {"Student ID", "Reg No", "Student Name", "Marks (out of 100)", "Status"};
        marksTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3; // Allow inline mark editing!
            }
        };

        marksTable = new JTable(marksTableModel);
        ModernTheme.styleTable(marksTable);
        marksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && marksTable.getSelectedRow() != -1) {
                int r = marksTable.getSelectedRow();
                selectedStudentId = (int) marksTableModel.getValueAt(r, 0);
                txtStudentName.setText(marksTableModel.getValueAt(r, 1) + " - " + marksTableModel.getValueAt(r, 2));
                txtMarkValue.setText(String.valueOf(marksTableModel.getValueAt(r, 3)));
            }
        });

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(marksTable), BorderLayout.CENTER);

        // Bottom batch save action
        JPanel tableFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        tableFooter.setBackground(Color.WHITE);
        JButton btnBatchSave = ModernTheme.createSuccessButton("Save All Table Marks in Batch");
        btnBatchSave.addActionListener(e -> handleBatchSave());
        tableFooter.add(btnBatchSave);
        tablePanel.add(tableFooter, BorderLayout.SOUTH);

        // Right Single Edit Form
        JPanel editCard = ModernTheme.createCardPanel();
        editCard.setLayout(new BorderLayout());
        editCard.setPreferredSize(new Dimension(320, 400));

        JLabel h = new JLabel("Individual Mark Input");
        h.setFont(ModernTheme.FONT_SUBTITLE);
        h.setForeground(ModernTheme.PRIMARY);
        h.setBorder(new EmptyBorder(0, 0, 10, 0));
        editCard.add(h, BorderLayout.NORTH);

        JPanel f = new JPanel();
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setBackground(Color.WHITE);

        txtStudentName = new JTextField();
        txtStudentName.setEditable(false);
        txtMarkValue = new JTextField();

        f.add(createRow("Selected Student:", txtStudentName));
        f.add(createRow("Marks Obtained (0 - 100):", txtMarkValue));

        JLabel rule = new JLabel("<html><b>Evaluation Rules:</b><br/>" +
            "• All marks must be out of 100.<br/>" +
            "• CA requires \u2265 40% for exam eligibility.<br/>" +
            "• You can edit marks in table directly or via this box.</html>");
        rule.setFont(ModernTheme.FONT_SMALL);
        rule.setForeground(ModernTheme.TEXT_MUTED);
        f.add(rule);

        editCard.add(f, BorderLayout.CENTER);

        JButton btnSaveSingle = ModernTheme.createPrimaryButton("Save Student Mark");
        btnSaveSingle.addActionListener(e -> handleSingleSave());
        editCard.add(btnSaveSingle, BorderLayout.SOUTH);

        split.setLeftComponent(tablePanel);
        split.setRightComponent(editCard);

        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------------
    // TAB 2: ELIGIBILITY
    // ------------------------------------------------------------------------
    private JPanel createEligibilityTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernTheme.BG_MAIN);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        top.setBackground(Color.WHITE);
        top.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lbl = new JLabel("Select Course:");
        lbl.setFont(ModernTheme.FONT_BOLD);
        cmbEligCourse = new JComboBox<>();
        cmbEligCourse.addActionListener(e -> loadEligibility());

        JLabel lblF = new JLabel("Filter:");
        lblF.setFont(ModernTheme.FONT_BOLD);
        cmbEligFilter = new JComboBox<>(new String[]{"All Undergraduates", "Eligible to Sit Exam", "Barred / Ineligible"});
        cmbEligFilter.addActionListener(e -> loadEligibility());

        JButton btnRefresh = ModernTheme.createSecondaryButton("Evaluate Eligibility");
        btnRefresh.addActionListener(e -> loadEligibility());

        top.add(lbl);
        top.add(cmbEligCourse);
        top.add(lblF);
        top.add(cmbEligFilter);
        top.add(btnRefresh);

        String[] cols = {"Student Reg No", "Full Name", "CA Mark (%)", "CA Status", "Attendance (%)", "Attendance Status", "Final Exam Status"};
        eligTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        eligTable = new JTable(eligTableModel);
        ModernTheme.styleTable(eligTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(eligTable), BorderLayout.CENTER);

        // Footer note
        JLabel footer = new JLabel("Criteria: CA Part \u2265 40% (Marks) AND Attendance \u2265 80% (including approved medicals) are required to sit for the final examination.");
        footer.setFont(ModernTheme.FONT_SMALL);
        footer.setForeground(ModernTheme.PRIMARY);
        footer.setBorder(new EmptyBorder(8, 10, 8, 10));
        tablePanel.add(footer, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------------
    // TAB 3: GRADES & GPA
    // ------------------------------------------------------------------------
    private JPanel createGradesTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernTheme.BG_MAIN);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        top.setBackground(Color.WHITE);
        top.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lbl = new JLabel("Select Course:");
        lbl.setFont(ModernTheme.FONT_BOLD);
        cmbGradeCourse = new JComboBox<>();
        cmbGradeCourse.addActionListener(e -> loadGrades());

        JButton btnCalc = ModernTheme.createPrimaryButton("Calculate & Generate Final Grades");
        btnCalc.addActionListener(e -> handleCalculateGrades());

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Grades");
        btnRefresh.addActionListener(e -> loadGrades());

        top.add(lbl);
        top.add(cmbGradeCourse);
        top.add(btnCalc);
        top.add(btnRefresh);

        String[] cols = {"Reg No", "Student Name", "CA Mark (40%)", "Final Exam (60%)", "Total Mark", "Grade Letter", "Grade Point", "Repeat Flag"};
        gradeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        gradeTable = new JTable(gradeTableModel);
        ModernTheme.styleTable(gradeTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        JLabel note = new JLabel("Grading Standard: UGC Commission Circular No. 12-2024. Repeat student pass grades capped at C (2.00 GP). CA < 40% results in Grade E.");
        note.setFont(ModernTheme.FONT_SMALL);
        note.setForeground(ModernTheme.TEXT_MUTED);
        note.setBorder(new EmptyBorder(8, 10, 8, 10));
        tablePanel.add(note, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_SMALL);
        lbl.setForeground(ModernTheme.TEXT_MUTED);

        comp.setFont(ModernTheme.FONT_REGULAR);
        if (comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(200, 26));
            comp.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(2, 6, 2, 6)));
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(2, 0, 4, 0));
        return p;
    }

    public void loadCourses() {
        cmbMarksCourse.removeAllItems();
        cmbEligCourse.removeAllItems();
        cmbGradeCourse.removeAllItems();

        List<Course> list = courseDAO.getAllCourses();
        for (Course c : list) {
            CourseItem item = new CourseItem(c.getCourseId(), c.getCourseCode(), c.getCourseName());
            cmbMarksCourse.addItem(item);
            cmbEligCourse.addItem(item);
            cmbGradeCourse.addItem(item);
        }

        if (cmbMarksCourse.getItemCount() > 0) {
            cmbMarksCourse.setSelectedIndex(0);
        }
    }

    private void onCourseChanged() {
        cmbMarksEvaluation.removeAllItems();
        CourseItem c = (CourseItem) cmbMarksCourse.getSelectedItem();
        if (c == null) return;

        List<Evaluation> evals = markService.getEvaluationsByCourse(c.id);
        for (Evaluation ev : evals) {
            cmbMarksEvaluation.addItem(new EvalItem(ev.getEvaluationId(), ev.getEvaluationName(), ev.getWeightagePercent()));
        }

        if (cmbMarksEvaluation.getItemCount() > 0) {
            cmbMarksEvaluation.setSelectedIndex(0);
        } else {
            marksTableModel.setRowCount(0);
        }
    }

    private void loadMarksForSelectedEvaluation() {
        marksTableModel.setRowCount(0);
        CourseItem course = (CourseItem) cmbMarksCourse.getSelectedItem();
        EvalItem eval = (EvalItem) cmbMarksEvaluation.getSelectedItem();
        if (course == null || eval == null) return;

        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByCourse(course.id);
        List<Mark> existingMarks = markService.getMarksByEvaluation(eval.id);

        for (CourseEnrollment ce : enrollments) {
            double currentMark = 0.0;
            boolean recorded = false;
            for (Mark m : existingMarks) {
                if (m.getStudentId() == ce.getStudentId()) {
                    currentMark = m.getMarksObtained();
                    recorded = true;
                    break;
                }
            }

            marksTableModel.addRow(new Object[]{
                ce.getStudentId(),
                ce.getStudentRegNo(),
                ce.getStudentName(),
                currentMark,
                recorded ? "Recorded" : "Pending"
            });
        }
    }

    private void handleSingleSave() {
        if (selectedStudentId == 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EvalItem eval = (EvalItem) cmbMarksEvaluation.getSelectedItem();
        if (eval == null) return;

        try {
            double val = Double.parseDouble(txtMarkValue.getText().trim());
            ValidationUtil.validateMark(val);

            User cur = SessionManager.getCurrentUser();
            Integer entererId = cur != null ? cur.getUserId() : 2;

            Mark m = new Mark(0, eval.id, selectedStudentId, val, entererId);
            markService.saveMark(m);

            JOptionPane.showMessageDialog(this, "Mark saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMarksForSelectedEvaluation();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric mark between 0 and 100.", "Invalid Mark", JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleBatchSave() {
        EvalItem eval = (EvalItem) cmbMarksEvaluation.getSelectedItem();
        if (eval == null) return;

        // Build and validate the full batch before persisting any student marks.
        List<Mark> batchList = new ArrayList<>();
        User cur = SessionManager.getCurrentUser();
        Integer entererId = cur != null ? cur.getUserId() : 2;

        try {
            for (int i = 0; i < marksTableModel.getRowCount(); i++) {
                int studentId = (int) marksTableModel.getValueAt(i, 0);
                Object valObj = marksTableModel.getValueAt(i, 3);
                double val = Double.parseDouble(valObj.toString().trim());
                ValidationUtil.validateMark(val);

                batchList.add(new Mark(0, eval.id, studentId, val, entererId));
            }

            markService.batchSaveMarks(batchList);
            JOptionPane.showMessageDialog(this, "Successfully saved marks for all " + batchList.size() + " students in batch!", "Batch Save Successful", JOptionPane.INFORMATION_MESSAGE);
            loadMarksForSelectedEvaluation();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "One or more marks contain invalid numbers. Marks must be between 0 and 100.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error in batch save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEligibility() {
        eligTableModel.setRowCount(0);
        CourseItem c = (CourseItem) cmbEligCourse.getSelectedItem();
        if (c == null) return;

        // Apply the selected filter to the eligibility results before showing rows.
        List<EligibilityRecord> list = eligibilityService.getBatchEligibility(c.id);
        String filter = (String) cmbEligFilter.getSelectedItem();

        for (EligibilityRecord er : list) {
            if ("Eligible to Sit Exam".equals(filter) && !er.isOverallEligible()) continue;
            if ("Barred / Ineligible".equals(filter) && er.isOverallEligible()) continue;

            eligTableModel.addRow(new Object[]{
                er.getStudentRegNo(),
                er.getStudentName(),
                String.format("%.1f%%", er.getCaMark()),
                er.isCaEligible() ? "Eligible (\u2265 40%)" : "NOT Eligible (< 40%)",
                String.format("%.1f%%", er.getAttendancePercentage()),
                er.isAttendanceEligible() ? "Eligible (\u2265 80%)" : "NOT Eligible (< 80%)",
                er.isOverallEligible() ? "ELIGIBLE" : "BARRED"
            });
        }
    }

    private void loadGrades() {
        gradeTableModel.setRowCount(0);
        CourseItem c = (CourseItem) cmbGradeCourse.getSelectedItem();
        if (c == null) return;

        List<Grade> list = gradeService.getGradesByCourse(c.id);
        for (Grade g : list) {
            gradeTableModel.addRow(new Object[]{
                g.getStudentRegNo(),
                g.getStudentName(),
                String.format("%.1f", g.getCaMark()),
                String.format("%.1f", g.getFinalExamMark()),
                String.format("%.1f", g.getTotalMark()),
                g.getGradeLetter(),
                String.format("%.2f", g.getGradePoint()),
                g.isRepeat() ? "REPEAT" : "REGULAR"
            });
        }
    }

    private void handleCalculateGrades() {
        CourseItem c = (CourseItem) cmbGradeCourse.getSelectedItem();
        if (c == null) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Calculate final grades for all students in " + c.code + " based on CA & Final Exam marks?\n" +
            "Grading follows UGC Commission Circular No. 12-2024.",
            "Confirm Grade Calculation",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                gradeService.calculateGradesForCourse(c.id, "2024/2025");
                JOptionPane.showMessageDialog(this, "Final grades calculated and updated successfully!", "Grades Finalized", JOptionPane.INFORMATION_MESSAGE);
                loadGrades();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error calculating grades: " + ex.getMessage(), "Calculation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
