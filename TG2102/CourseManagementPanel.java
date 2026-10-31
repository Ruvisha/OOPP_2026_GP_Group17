package com.fot.ams.ui.admin;

import com.fot.ams.dao.DepartmentDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Course;
import com.fot.ams.model.Department;
import com.fot.ams.service.CourseService;
import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Administrative panel for managing academic courses.
 */
public class CourseManagementPanel extends JPanel {
    private final CourseService courseService;
    private final DepartmentDAO departmentDAO;

    private JTable courseTable;
    private DefaultTableModel tableModel;

    // Form inputs
    private JTextField txtCourseId;
    private JTextField txtCourseCode;
    private JTextField txtCourseName;
    private JSpinner spinCredits;
    private JSpinner spinSemester;
    private JComboBox<DeptItem> cmbDepartment;
    private JCheckBox chkTheory;
    private JCheckBox chkPractical;
    private JTextArea txtDescription;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    public CourseManagementPanel() {
        this.courseService = new CourseService();
        this.departmentDAO = new DepartmentDAO();
        initComponents();
        loadDepartments();
        loadCourses();
    }

    private static class DeptItem {
        final int id;
        final String label;

        DeptItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Header
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        JLabel title = new JLabel("Academic Course Curriculum Management");
        title.setFont(ModernTheme.FONT_SUBTITLE);
        title.setForeground(ModernTheme.PRIMARY);
        topBar.add(title);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(650);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"ID", "Course Code", "Course Name", "Credits", "Sem", "Department", "Theory", "Practical"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        ModernTheme.styleTable(courseTable);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && courseTable.getSelectedRow() != -1) {
                populateForm();
            }
        });

        tablePanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        // Form Panel
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BorderLayout());
        formCard.setPreferredSize(new Dimension(360, 500));

        JLabel formHeader = new JLabel("Course Specification");
        formHeader.setFont(ModernTheme.FONT_SUBTITLE);
        formHeader.setForeground(ModernTheme.PRIMARY);
        formHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Color.WHITE);

        txtCourseId = new JTextField();
        txtCourseId.setVisible(false);

        txtCourseCode = new JTextField();
        txtCourseName = new JTextField();
        spinCredits = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
        spinSemester = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        cmbDepartment = new JComboBox<>();
        chkTheory = new JCheckBox("Includes Theory Component (15 Sessions)", true);
        chkPractical = new JCheckBox("Includes Practical Component (15 Sessions)", false);
        chkTheory.setBackground(Color.WHITE);
        chkPractical.setBackground(Color.WHITE);

        txtDescription = new JTextArea(4, 20);
        txtDescription.setFont(ModernTheme.FONT_REGULAR);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        fields.add(createRow("Course Code (e.g. ICT2132):", txtCourseCode));
        fields.add(createRow("Course Name / Title:", txtCourseName));
        fields.add(createRow("Credit Value:", spinCredits));
        fields.add(createRow("Semester Number:", spinSemester));
        fields.add(createRow("Offering Department:", cmbDepartment));
        fields.add(Box.createRigidArea(new Dimension(0, 5)));
        fields.add(chkTheory);
        fields.add(chkPractical);
        fields.add(Box.createRigidArea(new Dimension(0, 8)));
        fields.add(createRow("Description / Syllabus Outline:", new JScrollPane(txtDescription)));

        formCard.add(fields, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnAdd = ModernTheme.createPrimaryButton("Add Course");
        btnUpdate = ModernTheme.createSuccessButton("Update Course");
        btnDelete = ModernTheme.createDangerButton("Delete Course");
        btnClear = ModernTheme.createSecondaryButton("Clear Form");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(formCard);

        add(topBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Event listeners
        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());
    }

    private JPanel createRow(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 2));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
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

    private void loadDepartments() {
        cmbDepartment.removeAllItems();
        List<Department> list = departmentDAO.getAllDepartments();
        for (Department d : list) {
            cmbDepartment.addItem(new DeptItem(d.getDepartmentId(), d.getDepartmentCode() + " - " + d.getDepartmentName()));
        }
    }

    public void loadCourses() {
        tableModel.setRowCount(0);
        List<Course> list = courseService.getAllCourses();
        for (Course c : list) {
            tableModel.addRow(new Object[]{
                c.getCourseId(),
                c.getCourseCode(),
                c.getCourseName(),
                c.getCredits(),
                c.getSemester(),
                c.getDepartmentCode() != null ? c.getDepartmentCode() : c.getDepartmentName(),
                c.isHasTheory() ? "Yes" : "No",
                c.isHasPractical() ? "Yes" : "No"
            });
        }
    }

    private void populateForm() {
        int row = courseTable.getSelectedRow();
        if (row == -1) return;

        int courseId = (int) tableModel.getValueAt(row, 0);
        Course c = courseService.getCourseById(courseId);
        if (c == null) return;

        txtCourseId.setText(String.valueOf(c.getCourseId()));
        txtCourseCode.setText(c.getCourseCode());
        txtCourseName.setText(c.getCourseName());
        spinCredits.setValue(c.getCredits());
        spinSemester.setValue(c.getSemester());
        chkTheory.setSelected(c.isHasTheory());
        chkPractical.setSelected(c.isHasPractical());
        txtDescription.setText(c.getDescription() != null ? c.getDescription() : "");

        for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
            if (cmbDepartment.getItemAt(i).id == c.getDepartmentId()) {
                cmbDepartment.setSelectedIndex(i);
                break;
            }
        }
    }

    private void clearForm() {
        courseTable.clearSelection();
        txtCourseId.setText("");
        txtCourseCode.setText("");
        txtCourseName.setText("");
        spinCredits.setValue(2);
        spinSemester.setValue(1);
        chkTheory.setSelected(true);
        chkPractical.setSelected(false);
        txtDescription.setText("");
        if (cmbDepartment.getItemCount() > 0) cmbDepartment.setSelectedIndex(0);
    }

    private void handleAdd() {
        try {
            DeptItem dept = (DeptItem) cmbDepartment.getSelectedItem();
            if (dept == null) throw new ValidationException("Please select a department.");

            Course c = new Course(
                0,
                txtCourseCode.getText().trim().toUpperCase(),
                txtCourseName.getText().trim(),
                (int) spinCredits.getValue(),
                (int) spinSemester.getValue(),
                dept.id,
                chkTheory.isSelected(),
                chkPractical.isSelected(),
                txtDescription.getText().trim()
            );

            courseService.addCourse(c);
            JOptionPane.showMessageDialog(this, "Course created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (txtCourseId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a course to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int courseId = Integer.parseInt(txtCourseId.getText());
            DeptItem dept = (DeptItem) cmbDepartment.getSelectedItem();
            if (dept == null) throw new ValidationException("Please select a department.");

            Course c = new Course(
                courseId,
                txtCourseCode.getText().trim().toUpperCase(),
                txtCourseName.getText().trim(),
                (int) spinCredits.getValue(),
                (int) spinSemester.getValue(),
                dept.id,
                chkTheory.isSelected(),
                chkPractical.isSelected(),
                txtDescription.getText().trim()
            );

            courseService.updateCourse(c);
            JOptionPane.showMessageDialog(this, "Course updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (txtCourseId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = Integer.parseInt(txtCourseId.getText());
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete course " + txtCourseCode.getText() + "?\nAll associated enrollments, marks, and attendance records will be removed.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                courseService.deleteCourse(courseId);
                JOptionPane.showMessageDialog(this, "Course deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
