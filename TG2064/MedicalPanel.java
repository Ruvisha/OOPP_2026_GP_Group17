package com.fot.ams.ui.technical;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Course;
import com.fot.ams.model.Medical;
import com.fot.ams.model.User;
import com.fot.ams.service.MedicalService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

/**
 * Panel enabling Technical Officers to maintain undergraduate medical submissions and review status.
 */
public class MedicalPanel extends JPanel {
    private final MedicalService medicalService;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;

    private JTable medicalTable;
    private DefaultTableModel tableModel;

    // Form inputs
    private JTextField txtMedicalId;
    private JComboBox<StudentItem> cmbStudent;
    private JComboBox<CourseItem> cmbCourse;
    private JComboBox<String> cmbSessionType;
    private JTextField txtSessionDate;
    private JTextField txtSubmissionDate;
    private JTextArea txtReason;
    private JTextField txtDocPath;
    private JComboBox<String> cmbStatus;
    private JTextField txtRemarks;

    private JButton btnAdd;
    private JButton btnApprove;
    private JButton btnReject;
    private JButton btnDelete;
    private JButton btnClear;

    private static class StudentItem {
        final int id;
        final String regNo;
        final String name;

        StudentItem(int id, String regNo, String name) {
            this.id = id;
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public String toString() {
            return regNo + " - " + name;
        }
    }

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

    public MedicalPanel() {
        this.medicalService = new MedicalService();
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();

        initComponents();
        loadDropdowns();
        loadMedicals();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel title = new JLabel("Undergraduate Medical Certificate Management");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Records");
        btnRefresh.addActionListener(e -> loadMedicals());

        topBar.add(title);
        topBar.add(btnRefresh);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(650);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"ID", "Reg No", "Student Name", "Course", "Type", "Session Date", "Status", "Reviewer Remarks"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        medicalTable = new JTable(tableModel);
        ModernTheme.styleTable(medicalTable);
        medicalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medicalTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && medicalTable.getSelectedRow() != -1) {
                populateForm();
            }
        });

        tablePanel.add(new JScrollPane(medicalTable), BorderLayout.CENTER);

        // Form Panel
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BorderLayout());
        formCard.setPreferredSize(new Dimension(360, 520));

        JLabel formHeader = new JLabel("Medical Record Details");
        formHeader.setFont(ModernTheme.FONT_SUBTITLE);
        formHeader.setForeground(ModernTheme.PRIMARY);
        formHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Color.WHITE);

        txtMedicalId = new JTextField();
        txtMedicalId.setVisible(false);

        cmbStudent = new JComboBox<>();
        cmbCourse = new JComboBox<>();
        cmbSessionType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL", "BOTH"});
        txtSessionDate = new JTextField(DateUtil.formatDate(DateUtil.nowSqlDate()));
        txtSubmissionDate = new JTextField(DateUtil.formatDate(DateUtil.nowSqlDate()));
        txtDocPath = new JTextField("medicals/cert.pdf");
        cmbStatus = new JComboBox<>(new String[]{"NOT_APPROVED", "APPROVED"});
        txtRemarks = new JTextField();

        txtReason = new JTextArea(3, 20);
        txtReason.setFont(ModernTheme.FONT_REGULAR);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        fields.add(createRow("Undergraduate:", cmbStudent));
        fields.add(createRow("Course Module:", cmbCourse));
        fields.add(createRow("Session Component:", cmbSessionType));
        fields.add(createRow("Missed Session Date (YYYY-MM-DD):", txtSessionDate));
        fields.add(createRow("Date of Submission (YYYY-MM-DD):", txtSubmissionDate));
        fields.add(createRow("Medical Certificate Reason:", new JScrollPane(txtReason)));
        fields.add(createRow("Document File Reference:", txtDocPath));
        fields.add(createRow("Medical Status:", cmbStatus));
        fields.add(createRow("Reviewer Remarks / UMO Endorsement:", txtRemarks));

        formCard.add(new JScrollPane(fields), BorderLayout.CENTER);

        // Action Buttons Row
        JPanel btnPanel = new JPanel(new GridLayout(3, 2, 6, 6));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnAdd = ModernTheme.createPrimaryButton("Add Record");
        btnApprove = ModernTheme.createSuccessButton("Approve Medical");
        btnReject = ModernTheme.createDangerButton("Reject Medical");
        btnDelete = ModernTheme.createDangerButton("Delete Record");
        btnClear = ModernTheme.createSecondaryButton("Clear Form");

        btnPanel.add(btnAdd);
        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(formCard);

        add(topBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> handleAdd());
        btnApprove.addActionListener(e -> handleSetStatus("APPROVED"));
        btnReject.addActionListener(e -> handleSetStatus("NOT_APPROVED"));
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
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

    private void loadDropdowns() {
        cmbStudent.removeAllItems();
        List<User> students = userDAO.getUsersByRole(4);
        for (User u : students) {
            cmbStudent.addItem(new StudentItem(u.getUserId(), u.getUsername(), u.getFullName()));
        }

        cmbCourse.removeAllItems();
        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            cmbCourse.addItem(new CourseItem(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName()));
        }
    }

    public void loadMedicals() {
        tableModel.setRowCount(0);
        List<Medical> list = medicalService.getAllMedicals();
        for (Medical m : list) {
            tableModel.addRow(new Object[]{
                m.getMedicalId(),
                m.getStudentRegNo(),
                m.getStudentName(),
                m.getCourseCode(),
                m.getSessionType(),
                DateUtil.formatDate(m.getSessionDate()),
                m.getStatus(),
                m.getRemarks() != null ? m.getRemarks() : "-"
            });
        }
    }

    private void populateForm() {
        int row = medicalTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableModel.getValueAt(row, 0);
        List<Medical> list = medicalService.getAllMedicals();
        for (Medical m : list) {
            if (m.getMedicalId() == id) {
                txtMedicalId.setText(String.valueOf(m.getMedicalId()));
                cmbSessionType.setSelectedItem(m.getSessionType());
                txtSessionDate.setText(DateUtil.formatDate(m.getSessionDate()));
                txtSubmissionDate.setText(DateUtil.formatDate(m.getSubmissionDate()));
                txtReason.setText(m.getReason());
                txtDocPath.setText(m.getMedicalDocPath() != null ? m.getMedicalDocPath() : "");
                cmbStatus.setSelectedItem(m.getStatus());
                txtRemarks.setText(m.getRemarks() != null ? m.getRemarks() : "");

                for (int i = 0; i < cmbStudent.getItemCount(); i++) {
                    if (cmbStudent.getItemAt(i).id == m.getStudentId()) {
                        cmbStudent.setSelectedIndex(i);
                        break;
                    }
                }

                for (int i = 0; i < cmbCourse.getItemCount(); i++) {
                    if (cmbCourse.getItemAt(i).id == m.getCourseId()) {
                        cmbCourse.setSelectedIndex(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void clearForm() {
        medicalTable.clearSelection();
        txtMedicalId.setText("");
        txtSessionDate.setText(DateUtil.formatDate(DateUtil.nowSqlDate()));
        txtSubmissionDate.setText(DateUtil.formatDate(DateUtil.nowSqlDate()));
        txtReason.setText("");
        txtDocPath.setText("");
        txtRemarks.setText("");
        cmbStatus.setSelectedItem("NOT_APPROVED");
    }

    private void handleAdd() {
        try {
            StudentItem stu = (StudentItem) cmbStudent.getSelectedItem();
            CourseItem crs = (CourseItem) cmbCourse.getSelectedItem();
            if (stu == null || crs == null) throw new ValidationException("Student and course selection required.");

            Date sDate = DateUtil.parseDate(txtSessionDate.getText().trim());
            Date subDate = DateUtil.parseDate(txtSubmissionDate.getText().trim());
            if (sDate == null || subDate == null) throw new ValidationException("Please enter valid dates in YYYY-MM-DD format.");

            Medical m = new Medical(
                0,
                stu.id,
                crs.id,
                (String) cmbSessionType.getSelectedItem(),
                sDate,
                subDate,
                txtReason.getText().trim(),
                txtDocPath.getText().trim(),
                (String) cmbStatus.getSelectedItem()
            );
            m.setRemarks(txtRemarks.getText().trim());

            medicalService.addMedical(m);
            JOptionPane.showMessageDialog(this, "Medical record submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadMedicals();
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSetStatus(String status) {
        if (txtMedicalId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtMedicalId.getText());
            User cur = SessionManager.getCurrentUser();
            int revId = cur != null ? cur.getUserId() : 7;
            String remarks = txtRemarks.getText().trim();
            if (remarks.isEmpty()) {
                remarks = "APPROVED".equals(status) ? "Endorsed by University Medical Officer" : "Rejected by Department";
            }

            medicalService.updateMedicalStatus(id, status, revId, remarks);
            JOptionPane.showMessageDialog(this, "Medical status updated to: " + status, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            loadMedicals();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (txtMedicalId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtMedicalId.getText());
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this medical record?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                medicalService.deleteMedical(id);
                JOptionPane.showMessageDialog(this, "Medical record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMedicals();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
