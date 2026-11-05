package com.fot.ams.ui.undergraduate;

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
import java.util.List;

/**
 * Panel enabling Undergraduates to view their submitted medical records and review approval status.
 */
public class MedicalViewPanel extends JPanel {
    private final MedicalService medicalService;
    private JTable medicalTable;
    private DefaultTableModel tableModel;

    public MedicalViewPanel() {
        this.medicalService = new MedicalService();
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
                "Medical ID", "Course", "Session Type", "Missed Date", "Date Submitted",
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
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents", "pdf"));
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            User cur = SessionManager.getCurrentUser();
            if (cur == null) {
                JOptionPane.showMessageDialog(this, "No active user session found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Fetch courses dynamically from database using CourseDAO or CourseService
                com.fot.ams.dao.CourseDAO courseDAO = new com.fot.ams.dao.CourseDAO();
                java.util.List<com.fot.ams.model.Course> courses = courseDAO.getAllCourses();

                JComboBox<CourseComboItem> courseComboBox = new JComboBox<>();
                for (com.fot.ams.model.Course c : courses) {
                    courseComboBox.addItem(new CourseComboItem(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName()));
                }

                JTextField sessionTypeField = new JTextField("THEORY");
                JTextField reasonField = new JTextField();
                JTextField missedDateField = new JTextField(java.time.LocalDate.now().toString());

                Object[] message = {
                        "Select Course:", courseComboBox,
                        "Session Type (THEORY / PRACTICAL / BOTH):", sessionTypeField,
                        "Medical Reason:", reasonField,
                        "Missed Date (YYYY-MM-DD):", missedDateField
                };

                int option = JOptionPane.showConfirmDialog(this, message, "Submit Medical Report", JOptionPane.OK_CANCEL_OPTION);
                if (option != JOptionPane.OK_OPTION) {
                    return;
                }

                CourseComboItem selectedCourse = (CourseComboItem) courseComboBox.getSelectedItem();
                if (selectedCourse == null) {
                    JOptionPane.showMessageDialog(this, "Please select a valid course.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int courseId = selectedCourse.getCourseId();
                String sessionType = sessionTypeField.getText().trim();
                String reason = reasonField.getText().trim();
                String missedDateStr = missedDateField.getText().trim();

                Medical medical = new Medical();
                medical.setStudentId(cur.getUserId());
                medical.setCourseId(courseId);
                medical.setSessionType(sessionType);
                medical.setReason(reason);
                medical.setSessionDate(java.sql.Date.valueOf(java.time.LocalDate.parse(missedDateStr)));
                medical.setSubmissionDate(new java.sql.Date(System.currentTimeMillis()));
                medical.setStatus("NOT_APPROVED");

                MedicalService service = new MedicalService();
                boolean success = service.submitMedicalReport(medical, selectedFile);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Medical report submitted successfully as PDF!");
                    loadStudentMedicals();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to submit medical report.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input data format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class CourseComboItem {
        private final int courseId;
        private final String displayText;

        public CourseComboItem(int courseId, String displayText) {
            this.courseId = courseId;
            this.displayText = displayText;
        }

        public int getCourseId() {
            return courseId;
        }

        @Override
        public String toString() {
            return displayText;
        }
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
                    DateUtil.formatDate(m.getSessionDate()),
                    DateUtil.formatDate(m.getSubmissionDate()),
                    m.getReason(),
                    m.getStatus(),
                    m.getRemarks() != null ? m.getRemarks() : "Pending UMO Review"
            });
        }
    }
}