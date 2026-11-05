package com.fot.ams.ui.undergraduate;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.Medical;
import com.fot.ams.model.User;
import com.fot.ams.service.MedicalService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Panel enabling Undergraduates to view their submitted medical records and review approval status.
 */
public class MedicalViewPanel extends JPanel {
    private final MedicalService medicalService;
    private final CourseDAO courseDAO;
    private JTable medicalTable;
    private DefaultTableModel tableModel;

    private static class CourseItem {
        final int id;
        final String code;

        CourseItem(int id, String code) {
            this.id = id;
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    public MedicalViewPanel() {
        this.medicalService = new MedicalService();
        this.courseDAO = new CourseDAO();
        initComponents();
        loadStudentMedicals();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("My Submitted Medical Certificates & Absence Excuses");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JPanel rightActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActionPanel.setBackground(Color.WHITE);

        JButton btnSubmitPDF = new JButton("Submit Medical PDF");
        btnSubmitPDF.addActionListener(e -> openSubmitDialog());
        rightActionPanel.add(btnSubmitPDF);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Records");
        btnRefresh.addActionListener(e -> loadStudentMedicals());
        rightActionPanel.add(btnRefresh);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(rightActionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {
                "Medical ID", "Course", "Session Type", "From Date", "To Date", "Date Submitted",
                "Medical Reason", "Approval Status", "University Medical Officer Endorsement"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        medicalTable = new JTable(tableModel);
        ModernTheme.styleTable(medicalTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(medicalTable), BorderLayout.CENTER);

        JLabel footer = new JLabel(
                "<html><b>Medical Policy:</b> Medical certificates must be submitted within 7 days of returning to academic activities. " +
                        "Approved medicals excuse the absence and count towards effective attendance for examination eligibility.</html>"
        );
        footer.setFont(ModernTheme.FONT_SMALL);
        footer.setForeground(ModernTheme.TEXT_MUTED);
        footer.setBorder(new EmptyBorder(8, 12, 8, 12));
        tablePanel.add(footer, BorderLayout.SOUTH);

        add(tablePanel, BorderLayout.CENTER);
    }

    private void openSubmitDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Submit Medical Report", true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(Color.WHITE);

        JComboBox<CourseItem> cmbCourse = new JComboBox<>();
        try {
            List<Course> courses = courseDAO.getAllCourses();
            for (Course c : courses) {
                cmbCourse.addItem(new CourseItem(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName()));
            }
        } catch (Exception ignored) {}

        JComboBox<String> cmbSessionType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL", "BOTH"});
        JTextField txtFromDate = new JTextField(DateUtil.formatDate(DateUtil.nowSqlDate()));
        JTextField txtToDate = new JTextField(DateUtil.formatDate(DateUtil.nowSqlDate()));
        JTextField txtReason = new JTextField();

        JLabel lblFile = new JLabel("No file chosen");
        JButton btnChooseFile = new JButton("Browse PDF...");
        final File[] selectedFile = {null};

        btnChooseFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents", "pdf"));
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fileChooser.getSelectedFile();
                lblFile.setText(selectedFile[0].getName());
            }
        });

        formPanel.add(new JLabel("Select Course:"));
        formPanel.add(cmbCourse);
        formPanel.add(new JLabel("Session Type:"));
        formPanel.add(cmbSessionType);
        formPanel.add(new JLabel("From Date (YYYY-MM-DD):"));
        formPanel.add(txtFromDate);
        formPanel.add(new JLabel("To Date (YYYY-MM-DD):"));
        formPanel.add(txtToDate);
        formPanel.add(new JLabel("Medical Reason:"));
        formPanel.add(txtReason);
        formPanel.add(new JLabel("Medical Certificate:"));
        formPanel.add(btnChooseFile);

        JButton btnSubmit = ModernTheme.createPrimaryButton("Submit");
        btnSubmit.addActionListener(e -> {
            try {
                CourseItem selectedCourse = (CourseItem) cmbCourse.getSelectedItem();
                if (selectedCourse == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select a course.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sessionType = (String) cmbSessionType.getSelectedItem();
                String fromDateStr = txtFromDate.getText().trim();
                String toDateStr = txtToDate.getText().trim();
                String reason = txtReason.getText().trim();

                if (fromDateStr.isEmpty() || toDateStr.isEmpty() || reason.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (selectedFile[0] == null) {
                    JOptionPane.showMessageDialog(dialog, "Please attach your medical certificate PDF.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Date Range & 2-Week (14 Days) Validation
                LocalDate fromDate = LocalDate.parse(fromDateStr);
                LocalDate toDate = LocalDate.parse(toDateStr);

                if (toDate.isBefore(fromDate)) {
                    JOptionPane.showMessageDialog(dialog, "To Date cannot be earlier than From Date.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
                if (daysBetween > 14) {
                    JOptionPane.showMessageDialog(dialog, "The medical absence date range cannot exceed 2 weeks (14 days).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                User cur = SessionManager.getCurrentUser();
                Medical medical = new Medical();
                medical.setStudentId(cur.getUserId());
                medical.setCourseId(selectedCourse.id);
                medical.setSessionType(sessionType);
                medical.setReason(reason);

                // Set From Date and To Date
                medical.setFromDate(fromDateStr);
                medical.setToDate(toDateStr);

                medical.setSubmissionDate(new java.sql.Date(System.currentTimeMillis()));
                medical.setStatus("NOT_APPROVED");

                MedicalService service = new MedicalService();
                boolean success = service.submitMedicalReport(medical, selectedFile[0]);

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Medical report submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadStudentMedicals(); // Refresh student records table
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to submit medical report.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(btnSubmit);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void loadStudentMedicals() {
        tableModel.setRowCount(0);
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<Medical> list = java.util.Collections.emptyList();
        try {
            list = medicalService.getMedicalsByStudent(cur.getUserId());
        } catch (Exception ignored) {}
        for (Medical m : list) {
            tableModel.addRow(new Object[]{
                    m.getMedicalId(),
                    m.getCourseCode(),
                    m.getSessionType(),
                    m.getFromDate(),
                    m.getToDate(),
                    DateUtil.formatDate(m.getSubmissionDate()),
                    m.getReason(),
                    m.getStatus(),
                    m.getRemarks() != null ? m.getRemarks() : "Pending UMO Review"
            });
        }
    }
}