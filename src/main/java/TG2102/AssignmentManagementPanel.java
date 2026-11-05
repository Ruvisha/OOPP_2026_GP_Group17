package com.fot.ams.ui.lecturer;

import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Assignment;
import com.fot.ams.model.AssignmentSubmission;
import com.fot.ams.model.Course;
import com.fot.ams.model.User;
import com.fot.ams.service.AssessmentService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Panel enabling Lecturers to create, publish, and grade Course Assignments.
 */
public class AssignmentManagementPanel extends JPanel {

    private final AssessmentService assessmentService;
    private JComboBox<CourseOption> cmbCourseFilter;
    private JTable assignmentTable;
    private DefaultTableModel assignmentTableModel;
    private final List<Assignment> displayedAssignments = new ArrayList<>();

    private static class CourseOption {
        final int id;
        final String label;

        CourseOption(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public AssignmentManagementPanel() {
        this.assessmentService = new AssessmentService();
        initComponents();
        loadCourses();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Filter Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilter.setOpaque(false);
        JLabel lblCourse = new JLabel("Assigned Course:");
        lblCourse.setFont(ModernTheme.FONT_BOLD);
        cmbCourseFilter = new JComboBox<>();
        cmbCourseFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbCourseFilter.setPreferredSize(new Dimension(280, 32));
        cmbCourseFilter.addActionListener(e -> loadAssignments());
        leftFilter.add(lblCourse);
        leftFilter.add(cmbCourseFilter);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadAssignments());
        leftFilter.add(btnRefresh);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnCreate = ModernTheme.createPrimaryButton("+ Create Assignment");
        btnCreate.addActionListener(e -> showCreateOrEditDialog(null));

        JButton btnEdit = ModernTheme.createSecondaryButton("Edit Assignment");
        btnEdit.addActionListener(e -> {
            Assignment sel = getSelectedAssignment();
            if (sel != null) showCreateOrEditDialog(sel);
        });

        rightActions.add(btnCreate);
        rightActions.add(btnEdit);

        topBar.add(leftFilter, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"ID", "Course Code", "Assignment Title", "Max Marks", "Deadline", "Brief (PDF)", "Submissions", "Status", "Marks Visible"};
        assignmentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        assignmentTable = new JTable(assignmentTableModel);
        ModernTheme.styleTable(assignmentTable);
        assignmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(assignmentTable);
        scrollPane.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JButton btnPublish = ModernTheme.createSuccessButton("Publish Assignment");
        btnPublish.addActionListener(e -> toggleAssignmentStatus("PUBLISHED"));

        JButton btnUnpublish = ModernTheme.createSecondaryButton("Unpublish (Draft)");
        btnUnpublish.addActionListener(e -> toggleAssignmentStatus("DRAFT"));

        JButton btnClose = ModernTheme.createDangerButton("Close Assignment");
        btnClose.addActionListener(e -> toggleAssignmentStatus("CLOSED"));

        JButton btnReleaseMarks = ModernTheme.createPrimaryButton("Release / Hide Marks");
        btnReleaseMarks.addActionListener(e -> toggleMarksRelease());

        JButton btnViewBrief = ModernTheme.createSecondaryButton("View Assignment Brief");
        btnViewBrief.addActionListener(e -> {
            Assignment sel = getSelectedAssignment();
            if (sel != null) {
                if (!sel.hasAttachment()) {
                    JOptionPane.showMessageDialog(this, "No PDF brief is attached to this assignment.", "No Attachment", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                openPdfFile(sel.getFilePath());
            }
        });

        JButton btnSubmissions = ModernTheme.createSecondaryButton("View Submissions & Grade");
        btnSubmissions.addActionListener(e -> {
            Assignment sel = getSelectedAssignment();
            if (sel != null) showSubmissionsDialog(sel);
        });

        JButton btnDelete = ModernTheme.createDangerButton("Delete Assignment");
        btnDelete.addActionListener(e -> deleteSelectedAssignment());

        bottomBar.add(btnPublish);
        bottomBar.add(btnUnpublish);
        bottomBar.add(btnClose);
        bottomBar.add(new JSeparator(JSeparator.VERTICAL));
        bottomBar.add(btnReleaseMarks);
        bottomBar.add(btnViewBrief);
        bottomBar.add(btnSubmissions);
        bottomBar.add(new JSeparator(JSeparator.VERTICAL));
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadCourses() {
        cmbCourseFilter.removeAllItems();
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<Course> list = java.util.Collections.emptyList();
        try {
            list = assessmentService.getAuthorizedCoursesForLecturer(cur.getUserId());
        } catch (Exception ignored) {}
        cmbCourseFilter.addItem(new CourseOption(0, "All Assigned Courses (" + list.size() + ")"));
        for (Course c : list) {
            cmbCourseFilter.addItem(new CourseOption(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName()));
        }
        if (!list.isEmpty()) {
            cmbCourseFilter.setSelectedIndex(0);
        }
        loadAssignments();
    }

    public void loadAssignments() {
        assignmentTableModel.setRowCount(0);
        displayedAssignments.clear();

        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        CourseOption sel = (CourseOption) cmbCourseFilter.getSelectedItem();
        List<Assignment> list = java.util.Collections.emptyList();
        try {
        if (sel == null || sel.id == 0) {
            list = assessmentService.getAssignmentsByLecturer(cur.getUserId());
        } else {
            list = assessmentService.getAssignmentsByCourse(sel.id);
        }
        } catch (Exception ignored) {}

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Assignment a : list) {
            displayedAssignments.add(a);
            assignmentTableModel.addRow(new Object[]{
                    a.getId(),
                    a.getCourseCode(),
                    a.getTitle(),
                    String.format("%.1f", a.getMaxMarks()),
                    a.getDueDate() != null ? sdf.format(a.getDueDate()) : "No deadline",
                    a.hasAttachment() ? "📄 " + a.getAttachmentFileName() : "None",
                    a.getSubmissionCount(),
                    a.getStatus(),
                    a.isMarksReleased() ? "YES (Released)" : "NO (Hidden)"
            });
        }
    }

    private Assignment getSelectedAssignment() {
        int row = assignmentTable.getSelectedRow();
        if (row < 0 || row >= displayedAssignments.size()) {
            JOptionPane.showMessageDialog(this, "Please select an assignment from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return displayedAssignments.get(row);
    }

    private void toggleAssignmentStatus(String newStatus) {
        Assignment sel = getSelectedAssignment();
        if (sel == null) return;

        try {
            boolean ok = false;
            if ("PUBLISHED".equals(newStatus)) ok = assessmentService.publishAssignment(sel.getId());
            else if ("DRAFT".equals(newStatus)) ok = assessmentService.updateAssignmentStatus(sel.getId(), "DRAFT");
            else if ("CLOSED".equals(newStatus)) ok = assessmentService.closeAssignment(sel.getId());

            if (ok) {
                JOptionPane.showMessageDialog(this, "Assignment status updated to: " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadAssignments();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update assignment status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleMarksRelease() {
        Assignment sel = getSelectedAssignment();
        if (sel == null) return;

        boolean newRelease = !sel.isMarksReleased();
        int opt = JOptionPane.showConfirmDialog(this,
                newRelease ? "Release marks and lecturer feedback to students for this assignment?\nMarks will be synced to the official grade records."
                           : "Hide marks from students for this assignment?",
                "Confirm Marks Visibility", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;

        boolean ok = assessmentService.setAssignmentMarksReleased(sel.getId(), newRelease);
        if (ok) {
            JOptionPane.showMessageDialog(this, newRelease ? "Assignment marks are now visible to students and recorded in the database." : "Assignment marks are now hidden from students.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAssignments();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update marks visibility.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAssignment() {
        Assignment sel = getSelectedAssignment();
        if (sel == null) return;

        int opt = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete assignment '" + sel.getTitle() + "'?\n(Assignments with existing student submissions cannot be deleted).",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = assessmentService.deleteAssignment(sel.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Assignment deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadAssignments();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // Create / Edit Assignment Dialog
    // ==========================================

    private void showCreateOrEditDialog(Assignment existing) {
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existing == null ? "Create New Assignment" : "Edit Assignment Details", true);
        dlg.setSize(660, 580);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        // Course Selector
        g.gridx = 0; g.gridy = 0;
        form.add(new JLabel("Course:"), g);
        g.gridx = 1;
        JComboBox<CourseOption> cmbCourse = new JComboBox<>();
        List<Course> courses = assessmentService.getAuthorizedCoursesForLecturer(cur.getUserId());
        for (Course c : courses) {
            cmbCourse.addItem(new CourseOption(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName()));
        }
        if (existing != null) {
            for (int i = 0; i < cmbCourse.getItemCount(); i++) {
                if (cmbCourse.getItemAt(i).id == existing.getCourseId()) {
                    cmbCourse.setSelectedIndex(i);
                    break;
                }
            }
            cmbCourse.setEnabled(false);
        }
        form.add(cmbCourse, g);

        // Title
        g.gridx = 0; g.gridy = 1;
        form.add(new JLabel("Assignment Title:"), g);
        g.gridx = 1;
        JTextField txtTitle = new JTextField(existing != null ? existing.getTitle() : "", 22);
        form.add(txtTitle, g);

        // Instructions
        g.gridx = 0; g.gridy = 2;
        form.add(new JLabel("Instructions:"), g);
        g.gridx = 1;
        JTextArea txtInstructions = new JTextArea(existing != null ? existing.getInstructions() : "", 4, 22);
        txtInstructions.setLineWrap(true);
        txtInstructions.setWrapStyleWord(true);
        form.add(new JScrollPane(txtInstructions), g);

        // Max Marks
        g.gridx = 0; g.gridy = 3;
        form.add(new JLabel("Maximum Marks:"), g);
        g.gridx = 1;
        JSpinner spnMaxMarks = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getMaxMarks() : 100.0, 1.0, 1000.0, 5.0));
        form.add(spnMaxMarks, g);

        // Deadline Date & Time Picker
        g.gridx = 0; g.gridy = 4;
        form.add(new JLabel("Deadline:"), g);
        g.gridx = 1;
        DateTimePickerPanel dtpDue = new DateTimePickerPanel(existing != null ? existing.getDueDate() : null);
        form.add(dtpDue, g);

        // Status
        g.gridx = 0; g.gridy = 5;
        form.add(new JLabel("Status:"), g);
        g.gridx = 1;
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"DRAFT", "PUBLISHED", "CLOSED"});
        if (existing != null) cmbStatus.setSelectedItem(existing.getStatus());
        form.add(cmbStatus, g);

        // Assignment Brief (PDF) Field with Browse and Remove
        g.gridx = 0; g.gridy = 6;
        form.add(new JLabel("Assignment Brief (PDF):"), g);
        g.gridx = 1;
        final File[] selectedPdfFile = {null};
        final boolean[] isPdfRemoved = {false};

        JTextField txtPdfName = new JTextField(existing != null && existing.hasAttachment() ? existing.getAttachmentFileName() : "(No PDF attached)", 16);
        txtPdfName.setEditable(false);
        txtPdfName.setBackground(Color.WHITE);
        txtPdfName.setFont(ModernTheme.FONT_REGULAR);

        JButton btnBrowsePdf = ModernTheme.createSecondaryButton("Browse PDF...");
        btnBrowsePdf.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Assignment Brief (PDF)");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
            int res = chooser.showOpenDialog(dlg);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".pdf")) {
                    JOptionPane.showMessageDialog(dlg, "Please select a valid PDF file (*.pdf).", "Invalid File", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                selectedPdfFile[0] = f;
                isPdfRemoved[0] = false;
                txtPdfName.setText(f.getName());
            }
        });

        JButton btnRemovePdf = ModernTheme.createSecondaryButton("Remove");
        btnRemovePdf.addActionListener(ev -> {
            selectedPdfFile[0] = null;
            isPdfRemoved[0] = true;
            txtPdfName.setText("(No PDF attached)");
        });

        JPanel pdfRow = new JPanel(new BorderLayout(6, 0));
        pdfRow.setOpaque(false);
        pdfRow.add(txtPdfName, BorderLayout.CENTER);
        JPanel pdfBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pdfBtns.setOpaque(false);
        pdfBtns.add(btnBrowsePdf);
        pdfBtns.add(btnRemovePdf);
        pdfRow.add(pdfBtns, BorderLayout.EAST);
        form.add(pdfRow, g);

        dlg.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = ModernTheme.createPrimaryButton("Save Assignment");
        btnSave.addActionListener(ev -> {
            try {
                CourseOption selC = (CourseOption) cmbCourse.getSelectedItem();
                if (selC == null) throw new ValidationException("Please select a course.");

                String title = txtTitle.getText().trim();
                String inst = txtInstructions.getText().trim();
                double maxMarks = ((Number) spnMaxMarks.getValue()).doubleValue();
                String status = (String) cmbStatus.getSelectedItem();

                Timestamp dueTime = dtpDue.getTimestamp();

                if (maxMarks <= 0) {
                    throw new ValidationException("Maximum marks must be greater than zero.");
                }

                if ("PUBLISHED".equals(status)) {
                    if (dueTime == null) {
                        throw new ValidationException("Deadline Date & Time must be set before publishing the assignment.");
                    }
                    if (dueTime.getTime() <= System.currentTimeMillis()) {
                        throw new ValidationException("Cannot publish assignment: Deadline must be a valid future date and time.");
                    }
                }

                // Handle PDF attachment path
                String finalFilePath = existing != null ? existing.getFilePath() : null;
                if (isPdfRemoved[0]) {
                    finalFilePath = null;
                }
                if (selectedPdfFile[0] != null) {
                    finalFilePath = assessmentService.saveAssignmentBriefFile(cur.getUserId(), selectedPdfFile[0]);
                }

                if (existing == null) {
                    Assignment a = new Assignment();
                    a.setCourseId(selC.id);
                    a.setLecturerId(cur.getUserId());
                    a.setTitle(title);
                    a.setInstructions(inst);
                    a.setMaxMarks(maxMarks);
                    a.setDueDate(dueTime);
                    a.setStatus(status);
                    a.setFilePath(finalFilePath);
                    assessmentService.createAssignment(a);
                    JOptionPane.showMessageDialog(dlg, "Assignment created successfully!", "Created", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    existing.setTitle(title);
                    existing.setInstructions(inst);
                    existing.setMaxMarks(maxMarks);
                    existing.setDueDate(dueTime);
                    existing.setStatus(status);
                    existing.setFilePath(finalFilePath);
                    assessmentService.updateAssignment(existing);
                    JOptionPane.showMessageDialog(dlg, "Assignment updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                }

                dlg.dispose();
                loadAssignments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(ev -> dlg.dispose());

        btnBar.add(btnCancel);
        btnBar.add(btnSave);
        dlg.add(btnBar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void openPdfFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No file path specified.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File f = new File(path);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "The attached PDF file was not found on the server at:\n" + path, "File Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(f);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open PDF file automatically: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "File located at: " + f.getAbsolutePath(), "File Location", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==========================================
    // Submissions & Grading Dialog
    // ==========================================

    private void showSubmissionsDialog(Assignment assignment) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Student Submissions - " + assignment.getTitle(), true);
        dlg.setSize(960, 580);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        String[] subCols = {"Submission ID", "Student Reg No", "Student Name", "Submitted At", "File Attached", "Status", "Marks", "Feedback"};
        DefaultTableModel subModel = new DefaultTableModel(subCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(subModel);
        ModernTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);

        List<AssignmentSubmission> submissions = new ArrayList<>();
        Runnable refreshSubmissions = () -> {
            subModel.setRowCount(0);
            submissions.clear();
            List<AssignmentSubmission> list = assessmentService.getSubmissionsByAssignment(assignment.getId());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (AssignmentSubmission s : list) {
                submissions.add(s);
                subModel.addRow(new Object[]{
                        s.getId(), s.getStudentRegNo(), s.getStudentName(),
                        s.getSubmittedAt() != null ? sdf.format(s.getSubmittedAt()) : "-",
                        s.getFileName(),
                        s.getStatus(),
                        s.getMarks() != null ? String.format("%.1f", s.getMarks()) : "-",
                        s.getFeedback() != null ? s.getFeedback() : ""
                });
            }
        };
        refreshSubmissions.run();

        dlg.add(scroll, BorderLayout.CENTER);

        // Grade Input Form at Bottom
        JPanel gradePanel = new JPanel(new GridBagLayout());
        gradePanel.setBackground(Color.WHITE);
        gradePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);

        JLabel lblMarks = new JLabel("Enter Marks (Max " + assignment.getMaxMarks() + "):");
        JSpinner spnMarks = new JSpinner(new SpinnerNumberModel(0.0, 0.0, assignment.getMaxMarks(), 1.0));
        JLabel lblFeedback = new JLabel("Lecturer Feedback:");
        JTextField txtFeedback = new JTextField(25);

        JButton btnDownload = ModernTheme.createSecondaryButton("Open / Save File");
        btnDownload.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= submissions.size()) {
                JOptionPane.showMessageDialog(dlg, "Please select a student submission from the table.", "Select Submission", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AssignmentSubmission s = submissions.get(row);
            File f = new File(s.getFilePath());
            if (!f.exists()) {
                JOptionPane.showMessageDialog(dlg, "File not found on server at: " + s.getFilePath(), "File Missing", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(f);
                } else {
                    JFileChooser fc = new JFileChooser();
                    fc.setSelectedFile(new File(f.getName()));
                    if (fc.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                        Files.copy(f.toPath(), fc.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(dlg, "File saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnSaveGrade = ModernTheme.createPrimaryButton("Save Marks & Feedback");
        btnSaveGrade.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= submissions.size()) {
                JOptionPane.showMessageDialog(dlg, "Please select a student submission from the table first.", "Select Submission", JOptionPane.WARNING_MESSAGE);
                return;
            }
            AssignmentSubmission s = submissions.get(row);
            User cur = SessionManager.getCurrentUser();
            try {
                double marks = ((Number) spnMarks.getValue()).doubleValue();
                String fb = txtFeedback.getText().trim();
                assessmentService.gradeAssignmentSubmission(s.getId(), marks, fb, cur != null ? cur.getUserId() : 1);
                JOptionPane.showMessageDialog(dlg, "Marks and feedback saved successfully!", "Graded", JOptionPane.INFORMATION_MESSAGE);
                refreshSubmissions.run();
                loadAssignments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < submissions.size()) {
                AssignmentSubmission s = submissions.get(row);
                if (s.getMarks() != null) spnMarks.setValue(s.getMarks());
                txtFeedback.setText(s.getFeedback() != null ? s.getFeedback() : "");
            }
        });

        g.gridx = 0; g.gridy = 0; gradePanel.add(lblMarks, g);
        g.gridx = 1; gradePanel.add(spnMarks, g);
        g.gridx = 2; gradePanel.add(btnDownload, g);

        g.gridx = 0; g.gridy = 1; gradePanel.add(lblFeedback, g);
        g.gridx = 1; gradePanel.add(txtFeedback, g);
        g.gridx = 2; gradePanel.add(btnSaveGrade, g);

        dlg.add(gradePanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
