package com.fot.ams.ui.undergraduate;

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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel enabling Undergraduates to view assignments for registered courses,
 * read instructions, upload solutions, submit files, and inspect submission confirmations.
 */
public class AssignmentSubmissionPanel extends JPanel {

    private final AssessmentService assessmentService;
    private JComboBox<CourseOption> cmbCourseFilter;
    private JTable assignmentTable;
    private DefaultTableModel assignmentTableModel;
    private final List<Assignment> displayedAssignments = new ArrayList<>();

    // Details & Submission view
    private JTextArea txtInstructions;
    private JLabel lblSelectedTitle;
    private JLabel lblDeadline;
    private JLabel lblMyStatus;
    private JLabel lblAttachedFile;
    private JLabel lblAttachedBrief;
    private JButton btnViewBrief;
    private JTextField txtFilePath;
    private File selectedFile;

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

    public AssignmentSubmissionPanel() {
        this.assessmentService = new AssessmentService();
        initComponents();
        loadEnrolledCourses();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top Header and Filter Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilter.setOpaque(false);
        JLabel lblCourse = new JLabel("Registered Course:");
        lblCourse.setFont(ModernTheme.FONT_BOLD);
        cmbCourseFilter = new JComboBox<>();
        cmbCourseFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbCourseFilter.setPreferredSize(new Dimension(300, 32));
        cmbCourseFilter.addActionListener(e -> loadStudentAssignments());
        leftFilter.add(lblCourse);
        leftFilter.add(cmbCourseFilter);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Assignments");
        btnRefresh.addActionListener(e -> loadStudentAssignments());
        leftFilter.add(btnRefresh);

        JButton btnViewBriefTop = ModernTheme.createSecondaryButton("📄 View Assignment Brief");
        btnViewBriefTop.addActionListener(e -> {
            int row = assignmentTable.getSelectedRow();
            if (row < 0 || row >= displayedAssignments.size()) {
                JOptionPane.showMessageDialog(this, "Please select an assignment from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Assignment sel = displayedAssignments.get(row);
            if (!sel.hasAttachment()) {
                JOptionPane.showMessageDialog(this, "No PDF assignment brief is attached for this assignment.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            openPdfFile(sel.getFilePath());
        });
        leftFilter.add(btnViewBriefTop);

        topBar.add(leftFilter, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // Split Pane: Left (Table of Assignments) | Right (Instructions & Upload Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerLocation(520);

        // Left Table
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lHeader = new JLabel("Curriculum Assignments");
        lHeader.setFont(ModernTheme.FONT_SUBTITLE);
        lHeader.setForeground(ModernTheme.PRIMARY);
        lHeader.setBorder(new EmptyBorder(10, 12, 5, 12));
        leftPanel.add(lHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "Course Code", "Assignment Title", "Brief (PDF)", "Deadline", "Max Marks", "Submission Status"};
        assignmentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        assignmentTable = new JTable(assignmentTableModel);
        ModernTheme.styleTable(assignmentTable);
        assignmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assignmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedAssignmentDetails();
            }
        });

        JScrollPane scrollTable = new JScrollPane(assignmentTable);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.add(scrollTable, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // Right Detail & Submission Panel
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel detailHeader = new JPanel(new GridLayout(5, 1, 4, 4));
        detailHeader.setOpaque(false);

        lblSelectedTitle = new JLabel("Select an assignment to view details");
        lblSelectedTitle.setFont(ModernTheme.FONT_SUBTITLE);
        lblSelectedTitle.setForeground(ModernTheme.PRIMARY);

        lblDeadline = new JLabel("Deadline: -");
        lblDeadline.setFont(ModernTheme.FONT_REGULAR);
        lblDeadline.setForeground(ModernTheme.TEXT_MUTED);

        lblMyStatus = new JLabel("My Status: -");
        lblMyStatus.setFont(ModernTheme.FONT_BOLD);
        lblMyStatus.setForeground(ModernTheme.TEXT_DARK);

        lblAttachedFile = new JLabel("Current Submitted File: None");
        lblAttachedFile.setFont(ModernTheme.FONT_SMALL);
        lblAttachedFile.setForeground(ModernTheme.TEXT_MUTED);

        JPanel briefRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        briefRow.setOpaque(false);
        lblAttachedBrief = new JLabel("Assignment Brief: None");
        lblAttachedBrief.setFont(ModernTheme.FONT_SMALL);
        lblAttachedBrief.setForeground(ModernTheme.TEXT_DARK);

        btnViewBrief = ModernTheme.createSecondaryButton("📄 View Assignment Brief (PDF)");
        btnViewBrief.setVisible(false);
        btnViewBrief.addActionListener(e -> {
            int row = assignmentTable.getSelectedRow();
            if (row >= 0 && row < displayedAssignments.size()) {
                Assignment a = displayedAssignments.get(row);
                if (a.hasAttachment()) {
                    openPdfFile(a.getFilePath());
                }
            }
        });

        briefRow.add(lblAttachedBrief);
        briefRow.add(btnViewBrief);

        detailHeader.add(lblSelectedTitle);
        detailHeader.add(lblDeadline);
        detailHeader.add(lblMyStatus);
        detailHeader.add(lblAttachedFile);
        detailHeader.add(briefRow);
        rightPanel.add(detailHeader, BorderLayout.NORTH);

        // Center: Instructions Text Area
        JPanel instrPanel = new JPanel(new BorderLayout(4, 4));
        instrPanel.setOpaque(false);
        JLabel lblInst = new JLabel("Assignment Instructions:");
        lblInst.setFont(ModernTheme.FONT_BOLD);
        txtInstructions = new JTextArea();
        txtInstructions.setEditable(false);
        txtInstructions.setLineWrap(true);
        txtInstructions.setWrapStyleWord(true);
        txtInstructions.setFont(ModernTheme.FONT_REGULAR);
        txtInstructions.setBackground(new Color(248, 250, 252));
        txtInstructions.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scrollInst = new JScrollPane(txtInstructions);
        scrollInst.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        instrPanel.add(lblInst, BorderLayout.NORTH);
        instrPanel.add(scrollInst, BorderLayout.CENTER);
        rightPanel.add(instrPanel, BorderLayout.CENTER);

        // Bottom: File Upload and Submit Controls
        JPanel uploadPanel = new JPanel(new BorderLayout(8, 8));
        uploadPanel.setOpaque(false);
        uploadPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel fileChooserRow = new JPanel(new BorderLayout(6, 6));
        fileChooserRow.setOpaque(false);
        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        txtFilePath.setFont(ModernTheme.FONT_REGULAR);

        JButton btnBrowse = ModernTheme.createSecondaryButton("Browse File...");
        btnBrowse.addActionListener(e -> chooseFile());

        fileChooserRow.add(txtFilePath, BorderLayout.CENTER);
        fileChooserRow.add(btnBrowse, BorderLayout.EAST);

        JButton btnSubmit = ModernTheme.createSuccessButton("Upload & Submit Assignment");
        btnSubmit.addActionListener(e -> submitAssignmentFile());

        uploadPanel.add(new JLabel("Upload Submission File (PDF / DOC / ZIP / Code):"), BorderLayout.NORTH);
        uploadPanel.add(fileChooserRow, BorderLayout.CENTER);
        uploadPanel.add(btnSubmit, BorderLayout.SOUTH);

        rightPanel.add(uploadPanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    public void loadEnrolledCourses() {
        cmbCourseFilter.removeAllItems();
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<Course> list = assessmentService.getEnrolledCoursesForStudent(cur.getUserId());
        cmbCourseFilter.addItem(new CourseOption(0, "All Registered Courses (" + list.size() + ")"));
        for (Course c : list) {
            cmbCourseFilter.addItem(new CourseOption(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName()));
        }
        if (!list.isEmpty()) {
            cmbCourseFilter.setSelectedIndex(0);
        }
        loadStudentAssignments();
    }

    public void loadStudentAssignments() {
        assignmentTableModel.setRowCount(0);
        displayedAssignments.clear();

        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        CourseOption sel = (CourseOption) cmbCourseFilter.getSelectedItem();
        List<Course> courses;
        if (sel == null || sel.id == 0) {
            courses = assessmentService.getEnrolledCoursesForStudent(cur.getUserId());
        } else {
            Course c = new Course();
            c.setCourseId(sel.id);
            courses = List.of(c);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Course c : courses) {
            List<Assignment> aList = assessmentService.getAssignmentsByCourse(c.getCourseId());
            for (Assignment a : aList) {
                if (!a.isPublished() && !a.isClosed()) {
                    continue; // Skip draft assignments
                }

                displayedAssignments.add(a);
                AssignmentSubmission sub = assessmentService.getStudentSubmission(a.getId(), cur.getUserId());

                String status;
                if (sub != null) {
                    status = "SUBMITTED";
                } else if (a.isClosed() || a.isPastDue()) {
                    status = "CLOSED / MISSED";
                } else {
                    status = "PENDING SUBMISSION";
                }

                assignmentTableModel.addRow(new Object[]{
                        a.getId(),
                        a.getCourseCode(),
                        a.getTitle(),
                        a.hasAttachment() ? "📄 " + a.getAttachmentFileName() : "None",
                        a.getDueDate() != null ? sdf.format(a.getDueDate()) : "No deadline",
                        String.format("%.1f", a.getMaxMarks()),
                        status
                });
            }
        }

        if (!displayedAssignments.isEmpty()) {
            assignmentTable.setRowSelectionInterval(0, 0);
        } else {
            clearDetails();
        }
    }

    private void clearDetails() {
        lblSelectedTitle.setText("No assignments available.");
        lblDeadline.setText("Deadline: -");
        lblMyStatus.setText("My Status: -");
        lblAttachedFile.setText("Current Submitted File: None");
        if (lblAttachedBrief != null) {
            lblAttachedBrief.setText("Assignment Brief: None");
        }
        if (btnViewBrief != null) {
            btnViewBrief.setVisible(false);
        }
        txtInstructions.setText("");
        txtFilePath.setText("");
        selectedFile = null;
    }

    private void displaySelectedAssignmentDetails() {
        int row = assignmentTable.getSelectedRow();
        if (row < 0 || row >= displayedAssignments.size()) {
            clearDetails();
            return;
        }

        Assignment a = displayedAssignments.get(row);
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        lblSelectedTitle.setText(a.getTitle() + " (" + a.getCourseCode() + ")");
        lblDeadline.setText("Deadline: " + (a.getDueDate() != null ? sdf.format(a.getDueDate()) : "No deadline set"));
        txtInstructions.setText(a.getInstructions() != null && !a.getInstructions().trim().isEmpty() ? a.getInstructions() : "No special instructions provided.");

        if (a.hasAttachment()) {
            lblAttachedBrief.setText("Assignment Brief: 📄 " + a.getAttachmentFileName());
            btnViewBrief.setVisible(true);
        } else {
            lblAttachedBrief.setText("Assignment Brief: None");
            btnViewBrief.setVisible(false);
        }

        AssignmentSubmission sub = assessmentService.getStudentSubmission(a.getId(), cur.getUserId());
        if (sub != null) {
            lblMyStatus.setText("My Status: SUBMITTED (Recorded on " + sdf.format(sub.getSubmittedAt()) + ")");
            lblMyStatus.setForeground(ModernTheme.SUCCESS);
            lblAttachedFile.setText("Current Submitted File: " + sub.getFileName());
        } else if (a.isClosed()) {
            lblMyStatus.setText("My Status: CLOSED (Submissions not accepted)");
            lblMyStatus.setForeground(ModernTheme.DANGER);
            lblAttachedFile.setText("Current Submitted File: None");
        } else if (a.isPastDue()) {
            lblMyStatus.setText("My Status: DEADLINE PASSED");
            lblMyStatus.setForeground(ModernTheme.WARNING);
            lblAttachedFile.setText("Current Submitted File: None");
        } else {
            lblMyStatus.setText("My Status: PENDING SUBMISSION");
            lblMyStatus.setForeground(ModernTheme.PRIMARY);
            lblAttachedFile.setText("Current Submitted File: None");
        }
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Assignment Submission File");
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            txtFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    private void submitAssignmentFile() {
        int row = assignmentTable.getSelectedRow();
        if (row < 0 || row >= displayedAssignments.size()) {
            JOptionPane.showMessageDialog(this, "Please select an assignment from the table.", "Select Assignment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "Please browse and select a file to submit.", "File Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Assignment a = displayedAssignments.get(row);
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        if (a.isClosed()) {
            JOptionPane.showMessageDialog(this, "This assignment is closed. Submissions cannot be accepted.", "Assignment Closed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Submit file '" + selectedFile.getName() + "' for '" + a.getTitle() + "'?",
                "Confirm Submission", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = assessmentService.submitAssignmentFile(a.getId(), cur.getUserId(), selectedFile);
            if (ok) {
                // Confirmation screen conforming strictly to Prompt 7
                showSubmissionSuccessDialog(a.getTitle());
                selectedFile = null;
                txtFilePath.setText("");
                loadStudentAssignments();
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Submission Notice", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error submitting assignment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Required Success Screen strictly conforming to Prompt Requirement 7:
     * ASSIGNMENT SUBMITTED SUCCESSFULLY!
     * Your assignment has been submitted successfully.
     * Status: SUBMITTED
     * Strictly does not display marks or grading information.
     */
    private void showSubmissionSuccessDialog(String assignmentTitle) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Assignment Submitted", true);
        dlg.setSize(480, 290);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("ASSIGNMENT SUBMITTED SUCCESSFULLY!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(ModernTheme.SUCCESS);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel(assignmentTitle);
        sub.setFont(ModernTheme.FONT_SUBTITLE);
        sub.setForeground(ModernTheme.TEXT_DARK);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("Your assignment has been submitted successfully.");
        msg.setFont(ModernTheme.FONT_REGULAR);
        msg.setForeground(ModernTheme.TEXT_MUTED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel status = new JLabel("Status: SUBMITTED");
        status.setFont(new Font("Segoe UI", Font.BOLD, 15));
        status.setForeground(ModernTheme.PRIMARY);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dbNotice = new JLabel("Confirmation: File and attempt safely stored in MySQL database.");
        dbNotice.setFont(ModernTheme.FONT_SMALL);
        dbNotice.setForeground(ModernTheme.TEXT_MUTED);
        dbNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(title);
        p.add(Box.createRigidArea(new Dimension(0, 10)));
        p.add(sub);
        p.add(Box.createRigidArea(new Dimension(0, 15)));
        p.add(msg);
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(status);
        p.add(Box.createRigidArea(new Dimension(0, 15)));
        p.add(dbNotice);

        JButton btnClose = ModernTheme.createPrimaryButton("OK");
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dlg.dispose());
        p.add(Box.createRigidArea(new Dimension(0, 20)));
        p.add(btnClose);

        dlg.add(p, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void openPdfFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assignment brief PDF is attached.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        File f = new File(path);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "The assignment brief PDF file was not found on the server at:\n" + path, "File Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(f);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open PDF file automatically: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "File located at:\n" + f.getAbsolutePath(), "File Location", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

