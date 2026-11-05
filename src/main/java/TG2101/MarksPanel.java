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

    // Tab 4: Grade Summary & Analytics
    private JComboBox<CourseItem> cmbSummaryCourse;
    private JLabel lblTotalStudentsVal;
    private JLabel lblPassCountVal;
    private JLabel lblFailCountVal;
    private JLabel lblAvgMarkVal;
    private JLabel lblHighestMarkVal;
    private JLabel lblLowestMarkVal;
    private JTable summaryTable;
    private DefaultTableModel summaryTableModel;
    private JPanel distributionChartPanel;

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
        tabbedPane.addTab("Grade Summary & Analytics", createGradeSummaryTab());

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

        String[] cols = {"Reg No", "Student Name", "CA Mark (40%)", "Final Exam (60%)", "Total Mark", "Grade Letter", "Grade Point", "Repeat Required?"};
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

        JLabel note = new JLabel("Grading Standard: UGC Commission Circular No. 12-2024. Repeat only if CA or ESA is below 40%; E = both fail, EC = CA only, EE = ESA only. Repeat pass grades are capped at C (2.00 GP).");
        note.setFont(ModernTheme.FONT_SMALL);
        note.setForeground(ModernTheme.TEXT_MUTED);
        note.setBorder(new EmptyBorder(8, 10, 8, 10));
        tablePanel.add(note, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------------
    // TAB 4: GRADE SUMMARY & ANALYTICS
    // ------------------------------------------------------------------------
    private JPanel createGradeSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ModernTheme.BG_MAIN);

        // Top Filter / Control Bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        top.setBackground(Color.WHITE);
        top.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lbl = new JLabel("Select Course:");
        lbl.setFont(ModernTheme.FONT_BOLD);
        cmbSummaryCourse = new JComboBox<>();
        cmbSummaryCourse.setPreferredSize(new Dimension(340, 30));
        cmbSummaryCourse.addActionListener(e -> loadGradeSummary());

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Summary");
        btnRefresh.addActionListener(e -> loadGradeSummary());

        top.add(lbl);
        top.add(cmbSummaryCourse);
        top.add(btnRefresh);

        // Center Content Area: Stat Cards on Top + Split/Grid for Distribution Table & Visual Representation
        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.setOpaque(false);

        // 6 Key Stat Cards
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        statsGrid.setOpaque(false);

        lblTotalStudentsVal = new JLabel("0", SwingConstants.CENTER);
        lblPassCountVal = new JLabel("0 (0.0%)", SwingConstants.CENTER);
        lblFailCountVal = new JLabel("0 (0.0%)", SwingConstants.CENTER);
        lblAvgMarkVal = new JLabel("0.0", SwingConstants.CENTER);
        lblHighestMarkVal = new JLabel("0.0", SwingConstants.CENTER);
        lblLowestMarkVal = new JLabel("0.0", SwingConstants.CENTER);

        statsGrid.add(createSummaryMetricCard("Total Enrolled Students", lblTotalStudentsVal, ModernTheme.PRIMARY));
        statsGrid.add(createSummaryMetricCard("Passed Students", lblPassCountVal, ModernTheme.SUCCESS));
        statsGrid.add(createSummaryMetricCard("Repeat / Failed Students", lblFailCountVal, ModernTheme.DANGER));
        statsGrid.add(createSummaryMetricCard("Class Average Total Mark", lblAvgMarkVal, ModernTheme.ACCENT));
        statsGrid.add(createSummaryMetricCard("Highest Total Mark", lblHighestMarkVal, new Color(16, 185, 129)));
        statsGrid.add(createSummaryMetricCard("Lowest Total Mark", lblLowestMarkVal, new Color(245, 158, 11)));

        centerPanel.add(statsGrid, BorderLayout.NORTH);

        // Bottom section: Left (Grade Distribution Table) & Right (Visual Distribution Bars)
        JSplitPane splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitBottom.setResizeWeight(0.5);
        splitBottom.setDividerLocation(520);
        splitBottom.setBorder(null);

        // Left Table: Grade Letter, Range, Student Count, Percentage
        String[] cols = {"Grade Letter", "Mark Range", "Grade Point", "Student Count", "Percentage"};
        summaryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        summaryTable = new JTable(summaryTableModel);
        ModernTheme.styleTable(summaryTable);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel tblHeader = new JLabel("  Grade Distribution Breakdown (UGC Standard)");
        tblHeader.setFont(ModernTheme.FONT_SUBTITLE);
        tblHeader.setForeground(ModernTheme.PRIMARY);
        tblHeader.setBorder(new EmptyBorder(10, 8, 10, 8));
        tableWrapper.add(tblHeader, BorderLayout.NORTH);
        tableWrapper.add(new JScrollPane(summaryTable), BorderLayout.CENTER);

        // Right Visual Chart Panel
        JPanel chartCard = ModernTheme.createCardPanel();
        chartCard.setLayout(new BorderLayout());

        JLabel chartHeader = new JLabel("Visual Distribution Representation");
        chartHeader.setFont(ModernTheme.FONT_SUBTITLE);
        chartHeader.setForeground(ModernTheme.PRIMARY);
        chartHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        chartCard.add(chartHeader, BorderLayout.NORTH);

        distributionChartPanel = new JPanel();
        distributionChartPanel.setLayout(new BoxLayout(distributionChartPanel, BoxLayout.Y_AXIS));
        distributionChartPanel.setBackground(Color.WHITE);

        JScrollPane chartScroll = new JScrollPane(distributionChartPanel);
        chartScroll.setBorder(null);
        chartScroll.getViewport().setBackground(Color.WHITE);
        chartCard.add(chartScroll, BorderLayout.CENTER);

        splitBottom.setLeftComponent(tableWrapper);
        splitBottom.setRightComponent(chartCard);

        centerPanel.add(splitBottom, BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = ModernTheme.createCardPanel();
        card.setLayout(new BorderLayout(5, 5));

        JPanel bar = new JPanel();
        bar.setBackground(accentColor);
        bar.setPreferredSize(new Dimension(5, 45));
        card.add(bar, BorderLayout.WEST);

        JPanel content = new JPanel(new GridLayout(2, 1, 2, 2));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(ModernTheme.FONT_SMALL);
        lblTitle.setForeground(ModernTheme.TEXT_MUTED);

        valueLabel.setFont(ModernTheme.FONT_TITLE);
        valueLabel.setForeground(ModernTheme.TEXT_DARK);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        content.add(lblTitle);
        content.add(valueLabel);
        content.setBorder(new EmptyBorder(5, 8, 5, 8));

        card.add(content, BorderLayout.CENTER);
        return card;
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
        if (cmbSummaryCourse != null) {
            cmbSummaryCourse.removeAllItems();
        }

        List<Course> list = java.util.Collections.emptyList();
        try {
            list = courseDAO.getAllCourses();
        } catch (Exception ignored) {}
        for (Course c : list) {
            CourseItem item = new CourseItem(c.getCourseId(), c.getCourseCode(), c.getCourseName());
            cmbMarksCourse.addItem(item);
            cmbEligCourse.addItem(item);
            cmbGradeCourse.addItem(item);
            if (cmbSummaryCourse != null) {
                cmbSummaryCourse.addItem(item);
            }
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
                g.isRepeat() ? "REPEAT REQUIRED" : "PASS"
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
                if (cmbSummaryCourse != null && cmbSummaryCourse.getSelectedItem() != null) {
                    loadGradeSummary();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error calculating grades: " + ex.getMessage(), "Calculation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadGradeSummary() {
        if (cmbSummaryCourse == null || summaryTableModel == null) return;
        CourseItem c = (CourseItem) cmbSummaryCourse.getSelectedItem();
        if (c == null) return;

        summaryTableModel.setRowCount(0);
        distributionChartPanel.removeAll();

        List<Grade> grades = gradeService.getGradesByCourse(c.id);
        int total = grades.size();

        if (total == 0) {
            lblTotalStudentsVal.setText("0");
            lblPassCountVal.setText("0 (0.0%)");
            lblFailCountVal.setText("0 (0.0%)");
            lblAvgMarkVal.setText("0.0");
            lblHighestMarkVal.setText("0.0");
            lblLowestMarkVal.setText("0.0");

            JLabel emptyLbl = new JLabel("No grades recorded or calculated for this course yet.");
            emptyLbl.setFont(ModernTheme.FONT_REGULAR);
            emptyLbl.setForeground(ModernTheme.TEXT_MUTED);
            emptyLbl.setBorder(new EmptyBorder(20, 10, 10, 10));
            distributionChartPanel.add(emptyLbl);
            distributionChartPanel.revalidate();
            distributionChartPanel.repaint();
            return;
        }

        int passCount = 0;
        int failCount = 0;
        double sumTotal = 0.0;
        double highest = Double.NEGATIVE_INFINITY;
        double lowest = Double.POSITIVE_INFINITY;

        // Map to count standard UGC grades
        String[] gradeLetters = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "E"};
        String[] markRanges = {
            "85 - 100", "80 - 84", "75 - 79", "70 - 74", "65 - 69",
            "60 - 64", "55 - 59", "50 - 54", "45 - 49", "40 - 44", "0 - 39"
        };
        double[] gradePoints = {4.00, 4.00, 3.70, 3.30, 3.00, 2.70, 2.30, 2.00, 1.70, 1.30, 0.00};
        int[] counts = new int[gradeLetters.length];

        for (Grade g : grades) {
            double mark = g.getTotalMark();
            sumTotal += mark;
            if (mark > highest) highest = mark;
            if (mark < lowest) lowest = mark;

            if (g.isRepeat() || "E".equalsIgnoreCase(g.getGradeLetter())) {
                failCount++;
            } else {
                passCount++;
            }

            String gl = g.getGradeLetter() != null ? g.getGradeLetter().trim() : "E";
            boolean matched = false;
            for (int i = 0; i < gradeLetters.length; i++) {
                if (gradeLetters[i].equalsIgnoreCase(gl)) {
                    counts[i]++;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                // If special repeat notation like EC, EE or unrecognized, map to E
                counts[gradeLetters.length - 1]++;
            }
        }

        double avgMark = sumTotal / total;
        double passPct = (passCount * 100.0) / total;
        double failPct = (failCount * 100.0) / total;

        lblTotalStudentsVal.setText(String.valueOf(total));
        lblPassCountVal.setText(String.format("%d (%.1f%%)", passCount, passPct));
        lblFailCountVal.setText(String.format("%d (%.1f%%)", failCount, failPct));
        lblAvgMarkVal.setText(String.format("%.1f", avgMark));
        lblHighestMarkVal.setText(String.format("%.1f", highest));
        lblLowestMarkVal.setText(String.format("%.1f", lowest));

        // Populate Table and Visual Representation Bars
        for (int i = 0; i < gradeLetters.length; i++) {
            int cnt = counts[i];
            double pct = (cnt * 100.0) / total;

            summaryTableModel.addRow(new Object[]{
                gradeLetters[i],
                markRanges[i],
                String.format("%.2f", gradePoints[i]),
                cnt,
                String.format("%.1f%%", pct)
            });

            // Add visual row bar
            JPanel barRow = createVisualBarRow(gradeLetters[i], cnt, pct);
            distributionChartPanel.add(barRow);
        }

        distributionChartPanel.revalidate();
        distributionChartPanel.repaint();
    }

    private JPanel createVisualBarRow(String grade, int count, double percentage) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        row.setBorder(new EmptyBorder(3, 8, 3, 8));

        JLabel lblName = new JLabel(String.format("%-3s", grade));
        lblName.setFont(ModernTheme.FONT_BOLD);
        lblName.setPreferredSize(new Dimension(36, 20));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.round(percentage));
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(160, 14));
        bar.setBackground(new Color(241, 245, 249));

        if (grade.startsWith("A")) {
            bar.setForeground(ModernTheme.SUCCESS);
        } else if (grade.startsWith("B")) {
            bar.setForeground(ModernTheme.ACCENT);
        } else if (grade.startsWith("C")) {
            bar.setForeground(ModernTheme.PRIMARY);
        } else if (grade.startsWith("D")) {
            bar.setForeground(ModernTheme.WARNING);
        } else {
            bar.setForeground(ModernTheme.DANGER);
        }

        JLabel lblStat = new JLabel(String.format("%d (%.1f%%)", count, percentage));
        lblStat.setFont(ModernTheme.FONT_SMALL);
        lblStat.setForeground(ModernTheme.TEXT_SECONDARY);
        lblStat.setPreferredSize(new Dimension(80, 20));
        lblStat.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lblName, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(lblStat, BorderLayout.EAST);

        return row;
    }
}
