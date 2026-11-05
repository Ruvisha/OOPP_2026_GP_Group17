package com.fot.ams.ui.lecturer;

import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.*;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Panel enabling Lecturers to create, configure, publish, and grade Online Quizzes.
 */
public class QuizManagementPanel extends JPanel {

    private final AssessmentService assessmentService;
    private JComboBox<CourseOption> cmbCourseFilter;
    private JTable quizTable;
    private DefaultTableModel quizTableModel;
    private final List<Quiz> displayedQuizzes = new ArrayList<>();

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

    public QuizManagementPanel() {
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
        cmbCourseFilter.addActionListener(e -> loadQuizzes());
        leftFilter.add(lblCourse);
        leftFilter.add(cmbCourseFilter);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadQuizzes());
        leftFilter.add(btnRefresh);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnCreate = ModernTheme.createPrimaryButton("+ Create Quiz");
        btnCreate.addActionListener(e -> showCreateOrEditDialog(null));

        JButton btnEdit = ModernTheme.createSecondaryButton("Edit Quiz");
        btnEdit.addActionListener(e -> {
            Quiz sel = getSelectedQuiz();
            if (sel != null) showCreateOrEditDialog(sel);
        });

        JButton btnQuestions = ModernTheme.createPrimaryButton("Add / Manage Questions");
        btnQuestions.addActionListener(e -> {
            Quiz sel = getSelectedQuiz();
            if (sel != null) showQuestionsDialog(sel);
        });

        rightActions.add(btnCreate);
        rightActions.add(btnEdit);
        rightActions.add(btnQuestions);

        topBar.add(leftFilter, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"ID", "Course", "Quiz Title", "Duration", "Start Time", "End Time", "Questions", "Total Marks", "Question Paper", "Status", "Marks Visible"};
        quizTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        quizTable = new JTable(quizTableModel);
        ModernTheme.styleTable(quizTable);
        quizTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(quizTable);
        scrollPane.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JButton btnPublish = ModernTheme.createSuccessButton("Publish Quiz");
        btnPublish.addActionListener(e -> toggleQuizStatus("PUBLISHED"));

        JButton btnUnpublish = ModernTheme.createSecondaryButton("Unpublish (Draft)");
        btnUnpublish.addActionListener(e -> toggleQuizStatus("DRAFT"));

        JButton btnClose = ModernTheme.createDangerButton("Close Quiz");
        btnClose.addActionListener(e -> toggleQuizStatus("CLOSED"));

        JButton btnReleaseMarks = ModernTheme.createPrimaryButton("Release / Hide Marks");
        btnReleaseMarks.addActionListener(e -> toggleMarksRelease());

        JButton btnViewPdf = ModernTheme.createSecondaryButton("View PDF Paper");
        btnViewPdf.addActionListener(e -> {
            Quiz sel = getSelectedQuiz();
            if (sel != null) {
                if (!sel.hasAttachment()) {
                    JOptionPane.showMessageDialog(this, "No PDF question paper is attached to this quiz.", "No Attachment", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                openPdfFile(sel.getFilePath());
            }
        });

        JButton btnViewAttempts = ModernTheme.createSecondaryButton("View Student Attempts & Submissions");
        btnViewAttempts.addActionListener(e -> {
            Quiz sel = getSelectedQuiz();
            if (sel != null) showAttemptsDialog(sel);
        });

        JButton btnDelete = ModernTheme.createDangerButton("Delete Quiz");
        btnDelete.addActionListener(e -> deleteSelectedQuiz());

        bottomBar.add(btnPublish);
        bottomBar.add(btnUnpublish);
        bottomBar.add(btnClose);
        bottomBar.add(new JSeparator(JSeparator.VERTICAL));
        bottomBar.add(btnReleaseMarks);
        bottomBar.add(btnViewPdf);
        bottomBar.add(btnViewAttempts);
        bottomBar.add(new JSeparator(JSeparator.VERTICAL));
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void loadCourses() {
        cmbCourseFilter.removeAllItems();
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;
        try {
            List<Course> list = assessmentService.getAuthorizedCoursesForLecturer(cur.getUserId());
            cmbCourseFilter.addItem(new CourseOption(0, "All Assigned Courses (" + list.size() + ")"));
            for (Course c : list) {
                cmbCourseFilter.addItem(new CourseOption(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName()));
            }
            if (!list.isEmpty()) {
                cmbCourseFilter.setSelectedIndex(0);
            }
        } catch (Exception e) {
            System.err.println("[QuizManagementPanel] Load courses note: " + e.getMessage());
        }
        loadQuizzes();
    }

    public void loadQuizzes() {
        quizTableModel.setRowCount(0);
        displayedQuizzes.clear();

        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        CourseOption sel = (CourseOption) cmbCourseFilter.getSelectedItem();
        List<Quiz> list = java.util.Collections.emptyList();
        try {
            if (sel == null || sel.id == 0) {
                list = assessmentService.getQuizzesByLecturer(cur.getUserId());
            } else {
                list = assessmentService.getQuizzesByCourse(sel.id);
            }
        } catch (Exception e) {
            System.err.println("[QuizManagementPanel] Load quizzes note: " + e.getMessage());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Quiz q : list) {
            displayedQuizzes.add(q);
            quizTableModel.addRow(new Object[]{
                    q.getId(),
                    q.getCourseCode(),
                    q.getTitle(),
                    q.getDurationMinutes() + " mins",
                    q.getStartTime() != null ? sdf.format(q.getStartTime()) : "Immediate",
                    q.getEndTime() != null ? sdf.format(q.getEndTime()) : "No limit",
                    q.getQuestionCount(),
                    String.format("%.1f", q.getTotalMarks()),
                    q.hasAttachment() ? "📄 " + q.getAttachmentFileName() : "None",
                    q.getStatus(),
                    q.isMarksReleased() ? "YES (Released)" : "NO (Hidden)"
            });
        }
    }

    private Quiz getSelectedQuiz() {
        int row = quizTable.getSelectedRow();
        if (row < 0 || row >= displayedQuizzes.size()) {
            JOptionPane.showMessageDialog(this, "Please select a quiz from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return displayedQuizzes.get(row);
    }

    private void toggleQuizStatus(String newStatus) {
        Quiz sel = getSelectedQuiz();
        if (sel == null) return;

        if ("PUBLISHED".equals(newStatus) && sel.getQuestionCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one question before publishing this quiz.", "No Questions", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean ok = false;
            if ("PUBLISHED".equals(newStatus)) ok = assessmentService.publishQuiz(sel.getId());
            else if ("DRAFT".equals(newStatus)) ok = assessmentService.unpublishQuiz(sel.getId());
            else if ("CLOSED".equals(newStatus)) ok = assessmentService.closeQuiz(sel.getId());

            if (ok) {
                JOptionPane.showMessageDialog(this, "Quiz status updated to: " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadQuizzes();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update quiz status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleMarksRelease() {
        Quiz sel = getSelectedQuiz();
        if (sel == null) return;

        boolean newRelease = !sel.isMarksReleased();
        int opt = JOptionPane.showConfirmDialog(this,
                newRelease ? "Are you sure you want to release quiz marks to students? Marks will be synced to the official grade records."
                           : "Are you sure you want to hide quiz marks from students?",
                "Confirm Marks Visibility", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;

        boolean ok = assessmentService.setQuizMarksReleased(sel.getId(), newRelease);
        if (ok) {
            JOptionPane.showMessageDialog(this, newRelease ? "Marks are now visible to students and recorded in the database." : "Marks are now hidden from students.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadQuizzes();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update marks visibility.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedQuiz() {
        Quiz sel = getSelectedQuiz();
        if (sel == null) return;

        int opt = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete the quiz '" + sel.getTitle() + "'?\n(Quizzes with existing student attempts cannot be deleted).",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = assessmentService.deleteQuiz(sel.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Quiz deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadQuizzes();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // Create / Edit Quiz Dialog
    // ==========================================

    private void showCreateOrEditDialog(Quiz existing) {
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existing == null ? "Create New Online Quiz" : "Edit Quiz Details", true);
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
        form.add(new JLabel("Assigned Course:"), g);
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
        form.add(new JLabel("Quiz Title:"), g);
        g.gridx = 1;
        JTextField txtTitle = new JTextField(existing != null ? existing.getTitle() : "", 20);
        form.add(txtTitle, g);

        // Duration
        g.gridx = 0; g.gridy = 2;
        form.add(new JLabel("Duration (Minutes):"), g);
        g.gridx = 1;
        JSpinner spnDuration = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getDurationMinutes() : 30, 5, 360, 5));
        form.add(spnDuration, g);

        // Start Date/Time Picker
        g.gridx = 0; g.gridy = 3;
        form.add(new JLabel("Open Date & Time:"), g);
        g.gridx = 1;
        DateTimePickerPanel dtpStart = new DateTimePickerPanel(existing != null ? existing.getStartTime() : null);
        form.add(dtpStart, g);

        // End Date/Time Picker
        g.gridx = 0; g.gridy = 4;
        form.add(new JLabel("Close Date & Time:"), g);
        g.gridx = 1;
        DateTimePickerPanel dtpEnd = new DateTimePickerPanel(existing != null ? existing.getEndTime() : null);
        form.add(dtpEnd, g);

        // Status
        g.gridx = 0; g.gridy = 5;
        form.add(new JLabel("Status:"), g);
        g.gridx = 1;
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"DRAFT", "PUBLISHED", "CLOSED"});
        if (existing != null) cmbStatus.setSelectedItem(existing.getStatus());
        form.add(cmbStatus, g);

        // Question Paper (PDF) Field with Browse and Remove
        g.gridx = 0; g.gridy = 6;
        form.add(new JLabel("Question Paper (PDF):"), g);
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
            chooser.setDialogTitle("Select Question Paper (PDF)");
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
        JButton btnSave = ModernTheme.createPrimaryButton("Save Quiz");
        btnSave.addActionListener(ev -> {
            try {
                CourseOption selC = (CourseOption) cmbCourse.getSelectedItem();
                if (selC == null) throw new ValidationException("Please select a course.");

                String title = txtTitle.getText().trim();
                int dur = (Integer) spnDuration.getValue();
                String status = (String) cmbStatus.getSelectedItem();

                Timestamp stTime = dtpStart.getTimestamp();
                Timestamp enTime = dtpEnd.getTimestamp();

                // Validation rules
                if (dur <= 0) {
                    throw new ValidationException("Quiz duration must be greater than zero.");
                }
                if (stTime != null && enTime != null) {
                    if (!enTime.after(stTime)) {
                        throw new ValidationException("Quiz closing date & time must be later than opening date & time.");
                    }
                    long windowMinutes = (enTime.getTime() - stTime.getTime()) / (60 * 1000);
                    if (windowMinutes < dur) {
                        throw new ValidationException("Quiz availability window (" + windowMinutes + " mins) must be at least as long as quiz duration (" + dur + " mins).");
                    }
                }
                if ("PUBLISHED".equals(status)) {
                    if (stTime == null) {
                        throw new ValidationException("Open Date & Time must be set before publishing the quiz.");
                    }
                    if (enTime == null) {
                        throw new ValidationException("Close Date & Time must be set before publishing the quiz.");
                    }
                    if (enTime.getTime() <= System.currentTimeMillis()) {
                        throw new ValidationException("Cannot publish quiz: Closing date and time has already passed.");
                    }
                }

                // Handle PDF attachment path
                String finalFilePath = existing != null ? existing.getFilePath() : null;
                if (isPdfRemoved[0]) {
                    finalFilePath = null;
                }
                if (selectedPdfFile[0] != null) {
                    finalFilePath = assessmentService.saveQuizQuestionPaperFile(cur.getUserId(), selectedPdfFile[0]);
                }

                if (existing == null) {
                    Quiz q = new Quiz();
                    q.setCourseId(selC.id);
                    q.setLecturerId(cur.getUserId());
                    q.setTitle(title);
                    q.setDurationMinutes(dur);
                    q.setStartTime(stTime);
                    q.setEndTime(enTime);
                    q.setMaxAttempts(1);
                    q.setStatus(status);
                    q.setFilePath(finalFilePath);
                    assessmentService.createQuiz(q);
                    JOptionPane.showMessageDialog(dlg, "Quiz created successfully! Now add questions.", "Created", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    existing.setTitle(title);
                    existing.setDurationMinutes(dur);
                    existing.setStartTime(stTime);
                    existing.setEndTime(enTime);
                    existing.setStatus(status);
                    existing.setFilePath(finalFilePath);
                    assessmentService.updateQuiz(existing);
                    JOptionPane.showMessageDialog(dlg, "Quiz details updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                }

                dlg.dispose();
                loadQuizzes();
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
    // Question Management Dialog
    // ==========================================

    private void showQuestionsDialog(Quiz quiz) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Manage Questions - " + quiz.getTitle(), true);
        dlg.setSize(880, 640);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        // Questions List Table
        String[] qCols = {"ID", "Type", "Question", "Option A", "Option B", "Option C", "Option D", "Correct Answer", "Marks"};
        DefaultTableModel qModel = new DefaultTableModel(qCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(qModel);
        ModernTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane qScroll = new JScrollPane(table);
        qScroll.setPreferredSize(new Dimension(860, 220));

        Runnable refreshQuestions = () -> {
            qModel.setRowCount(0);
            List<QuizQuestion> qList = assessmentService.getQuestionsByQuiz(quiz.getId());
            for (QuizQuestion q : qList) {
                qModel.addRow(new Object[]{
                        q.getId(), q.getQuestionType(), q.getQuestionText(),
                        q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                        q.getCorrectAnswer(), q.getMarks()
                });
            }
        };
        refreshQuestions.run();

        // Question Input Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(new LineBorder(ModernTheme.BORDER, 1), "Question Builder"));
        form.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 8, 4, 8);

        JLabel lblType = new JLabel("Question Type:");
        JComboBox<String> cmbType = new JComboBox<>(new String[]{
                QuizQuestion.TYPE_MCQ,
                QuizQuestion.TYPE_TRUE_FALSE,
                QuizQuestion.TYPE_SHORT_ANSWER
        });

        JLabel lblText = new JLabel("Question Text:");
        JTextArea txtQText = new JTextArea(3, 25);
        txtQText.setLineWrap(true);
        txtQText.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(txtQText);

        JLabel lblA = new JLabel("Option A:");
        JTextField txtA = new JTextField(20);
        JLabel lblB = new JLabel("Option B:");
        JTextField txtB = new JTextField(20);
        JLabel lblC = new JLabel("Option C:");
        JTextField txtC = new JTextField(20);
        JLabel lblD = new JLabel("Option D:");
        JTextField txtD = new JTextField(20);

        JLabel lblCorrect = new JLabel("Correct Answer (A, B, C, D / True, False / Text):");
        JTextField txtCorrect = new JTextField(20);

        JLabel lblMarks = new JLabel("Marks:");
        JSpinner spnMarks = new JSpinner(new SpinnerNumberModel(2.0, 0.5, 100.0, 0.5));

        // Dynamic form visibility based on type
        cmbType.addActionListener(e -> {
            String selType = (String) cmbType.getSelectedItem();
            boolean isMCQ = QuizQuestion.TYPE_MCQ.equals(selType);
            boolean isTF = QuizQuestion.TYPE_TRUE_FALSE.equals(selType);

            txtA.setEnabled(isMCQ);
            txtB.setEnabled(isMCQ);
            txtC.setEnabled(isMCQ);
            txtD.setEnabled(isMCQ);

            if (isTF) {
                lblCorrect.setText("Correct Answer ('True' or 'False'):");
            } else if (isMCQ) {
                lblCorrect.setText("Correct Answer ('A', 'B', 'C', or 'D'):");
            } else {
                lblCorrect.setText("Correct Answer / Key phrase (Optional):");
            }
        });

        // Layout Question Form
        g.gridx = 0; g.gridy = 0; form.add(lblType, g);
        g.gridx = 1; form.add(cmbType, g);

        g.gridx = 0; g.gridy = 1; form.add(lblText, g);
        g.gridx = 1; form.add(textScroll, g);

        g.gridx = 0; g.gridy = 2; form.add(lblA, g);
        g.gridx = 1; form.add(txtA, g);

        g.gridx = 0; g.gridy = 3; form.add(lblB, g);
        g.gridx = 1; form.add(txtB, g);

        g.gridx = 0; g.gridy = 4; form.add(lblC, g);
        g.gridx = 1; form.add(txtC, g);

        g.gridx = 0; g.gridy = 5; form.add(lblD, g);
        g.gridx = 1; form.add(txtD, g);

        g.gridx = 0; g.gridy = 6; form.add(lblCorrect, g);
        g.gridx = 1; form.add(txtCorrect, g);

        g.gridx = 0; g.gridy = 7; form.add(lblMarks, g);
        g.gridx = 1; form.add(spnMarks, g);

        // Form action buttons
        JPanel formActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        formActions.setOpaque(false);

        JButton btnAddQ = ModernTheme.createPrimaryButton("Save / Add Question");
        btnAddQ.addActionListener(ev -> {
            try {
                QuizQuestion q = new QuizQuestion();
                q.setQuizId(quiz.getId());
                q.setQuestionType((String) cmbType.getSelectedItem());
                q.setQuestionText(txtQText.getText().trim());
                q.setOptionA(txtA.getText().trim());
                q.setOptionB(txtB.getText().trim());
                q.setOptionC(txtC.getText().trim());
                q.setOptionD(txtD.getText().trim());
                q.setCorrectAnswer(txtCorrect.getText().trim());
                q.setMarks(((Number) spnMarks.getValue()).doubleValue());

                assessmentService.addQuestion(q);
                refreshQuestions.run();
                loadQuizzes();

                txtQText.setText("");
                txtA.setText("");
                txtB.setText("");
                txtC.setText("");
                txtD.setText("");
                txtCorrect.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnDeleteQ = ModernTheme.createDangerButton("Delete Selected Question");
        btnDeleteQ.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dlg, "Please select a question from the table.", "Select Question", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int qId = (Integer) qModel.getValueAt(row, 0);
            int opt = JOptionPane.showConfirmDialog(dlg, "Delete this question permanently?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                assessmentService.deleteQuestion(qId);
                refreshQuestions.run();
                loadQuizzes();
            }
        });

        formActions.add(btnDeleteQ);
        formActions.add(btnAddQ);
        g.gridx = 0; g.gridy = 8; g.gridwidth = 2;
        form.add(formActions, g);

        JPanel mainCenter = new JPanel(new BorderLayout(5, 5));
        mainCenter.add(qScroll, BorderLayout.NORTH);
        mainCenter.add(form, BorderLayout.CENTER);
        dlg.add(mainCenter, BorderLayout.CENTER);

        JButton btnDone = ModernTheme.createSecondaryButton("Close Window");
        btnDone.addActionListener(ev -> dlg.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.add(btnDone);
        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ==========================================
    // Attempts & Manual Grading Dialog
    // ==========================================

    private void showAttemptsDialog(Quiz quiz) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Student Attempts - " + quiz.getTitle(), true);
        dlg.setSize(920, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        String[] attCols = {"Attempt ID", "Reg No", "Student Name", "Started At", "Submitted At", "Score", "Total Marks", "Status"};
        DefaultTableModel attModel = new DefaultTableModel(attCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(attModel);
        ModernTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);

        List<QuizAttempt> attempts = new ArrayList<>();
        Runnable refreshAttempts = () -> {
            attModel.setRowCount(0);
            attempts.clear();
            List<QuizAttempt> list = assessmentService.getAttemptsByQuiz(quiz.getId());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (QuizAttempt a : list) {
                attempts.add(a);
                attModel.addRow(new Object[]{
                        a.getId(), a.getStudentRegNo(), a.getStudentName(),
                        a.getStartedAt() != null ? sdf.format(a.getStartedAt()) : "-",
                        a.getSubmittedAt() != null ? sdf.format(a.getSubmittedAt()) : "-",
                        String.format("%.1f", a.getScore()),
                        String.format("%.1f", a.getTotalMarks()),
                        a.getStatus()
                });
            }
        };
        refreshAttempts.run();

        dlg.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnGrade = ModernTheme.createPrimaryButton("Inspect Answers & Grade Short Answers");
        btnGrade.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= attempts.size()) {
                JOptionPane.showMessageDialog(dlg, "Please select a student attempt first.", "Select Attempt", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showGradingDialog(attempts.get(row), quiz);
            refreshAttempts.run();
        });

        JButton btnClose = ModernTheme.createSecondaryButton("Close");
        btnClose.addActionListener(ev -> dlg.dispose());

        bottom.add(btnGrade);
        bottom.add(btnClose);
        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void showGradingDialog(QuizAttempt attempt, Quiz quiz) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Grade Attempt #" + attempt.getId() + " - " + attempt.getStudentName() + " (" + attempt.getStudentRegNo() + ")", true);
        dlg.setSize(840, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Ans ID", "Question", "Type", "Student Answer", "Correct Answer", "Max Marks", "Awarded Marks", "Feedback"};
        DefaultTableModel ansModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(ansModel);
        ModernTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);

        List<QuizAnswer> answers = new ArrayList<>();
        Runnable refreshAnswers = () -> {
            ansModel.setRowCount(0);
            answers.clear();
            List<QuizAnswer> list = assessmentService.getAnswersByAttempt(attempt.getId());
            for (QuizAnswer a : list) {
                answers.add(a);
                ansModel.addRow(new Object[]{
                        a.getId(), a.getQuestionText(), a.getQuestionType(),
                        a.getAnswerText() != null ? a.getAnswerText() : "(No Answer)",
                        a.getCorrectAnswer(), a.getMaxMarks(), a.getAwardedMarks(),
                        a.getFeedback() != null ? a.getFeedback() : ""
                });
            }
        };
        refreshAnswers.run();

        dlg.add(scroll, BorderLayout.CENTER);

        // Grade Input Form
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lblMarks = new JLabel("Awarded Marks:");
        JSpinner spnAwarded = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 100.0, 0.5));
        JLabel lblFeedback = new JLabel("Lecturer Feedback:");
        JTextField txtFeedback = new JTextField(20);

        JButton btnSaveGrade = ModernTheme.createPrimaryButton("Save Question Grade");
        btnSaveGrade.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= answers.size()) {
                JOptionPane.showMessageDialog(dlg, "Please select an answer from the table to grade.", "Select Answer", JOptionPane.WARNING_MESSAGE);
                return;
            }
            QuizAnswer selAns = answers.get(row);
            User cur = SessionManager.getCurrentUser();
            try {
                double marks = ((Number) spnAwarded.getValue()).doubleValue();
                String fb = txtFeedback.getText().trim();
                assessmentService.gradeAnswer(selAns.getId(), marks, fb, cur != null ? cur.getUserId() : 1);
                JOptionPane.showMessageDialog(dlg, "Grade updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                refreshAnswers.run();
                loadQuizzes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < answers.size()) {
                QuizAnswer a = answers.get(row);
                spnAwarded.setValue(a.getAwardedMarks());
                txtFeedback.setText(a.getFeedback() != null ? a.getFeedback() : "");
            }
        });

        form.add(lblMarks);
        form.add(spnAwarded);
        form.add(lblFeedback);
        form.add(txtFeedback);
        form.add(btnSaveGrade);

        dlg.add(form, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
