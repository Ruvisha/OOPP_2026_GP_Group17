package com.fot.ams.ui.admin;

import com.fot.ams.dao.DepartmentDAO;
import com.fot.ams.dao.CourseDAO;
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
    private JSpinner spinStudyYear;
    private JComboBox<FilterItem> cmbDepartmentFilter;
    private JComboBox<String> cmbYearFilter;
    private JComboBox<String> cmbSemesterFilter;
    private JComboBox<DeptItem> cmbDepartment;
    private JComboBox<CourseDAO.LecturerOption> cmbLecturer;
    private final CourseDAO courseDAO = new CourseDAO();
    private JCheckBox chkTheory;
    private JCheckBox chkPractical;
    private JTextArea txtDescription;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    private JLabel lblCourseCount;
    private JButton btnFilter;
    private JButton btnResetFilter;

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
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Academic Course Curriculum Management");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JLabel subtitle = new JLabel("Faculty of Technology • University of Ruhuna | Manage curriculum modules, credits, and lecturer allocations");
        subtitle.setFont(ModernTheme.FONT_SMALL);
        subtitle.setForeground(ModernTheme.TEXT_MUTED);

        titleBlock.add(title);
        titleBlock.add(subtitle);
        topBar.add(titleBlock, BorderLayout.WEST);

        // Center Split Pane (Left: Course Table, Right: Course Specification Form)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.62);
        splitPane.setDividerLocation(660);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);

        // Left Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.setMinimumSize(new Dimension(460, 400));

        String[] cols = {"ID", "Course Code", "Course Name", "Credits", "Year", "Sem", "Department", "Theory", "Practical"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        ModernTheme.styleTable(courseTable);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && courseTable.getSelectedRow() != -1) {
                populateForm();
            }
        });

        // Filter and Toolbar Section
        JPanel filterAndToolbarPanel = new JPanel();
        filterAndToolbarPanel.setLayout(new BoxLayout(filterAndToolbarPanel, BoxLayout.Y_AXIS));
        filterAndToolbarPanel.setBackground(Color.WHITE);
        filterAndToolbarPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
            new EmptyBorder(10, 12, 10, 12)
        ));

        // Row 1: Dropdown Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.setOpaque(false);

        JLabel lblDept = new JLabel("Department:");
        lblDept.setFont(ModernTheme.FONT_BOLD);
        lblDept.setForeground(ModernTheme.TEXT_DARK);

        cmbDepartmentFilter = new JComboBox<>();
        cmbDepartmentFilter.addItem(new FilterItem(0, "All Departments"));
        cmbDepartmentFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbDepartmentFilter.setPreferredSize(new Dimension(230, 30));
        cmbDepartmentFilter.setBackground(Color.WHITE);

        JLabel lblYear = new JLabel("Year:");
        lblYear.setFont(ModernTheme.FONT_BOLD);
        lblYear.setForeground(ModernTheme.TEXT_DARK);

        cmbYearFilter = new JComboBox<>(new String[]{"All Years", "Year 1", "Year 2", "Year 3", "Year 4"});
        cmbYearFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbYearFilter.setPreferredSize(new Dimension(110, 30));
        cmbYearFilter.setBackground(Color.WHITE);

        JLabel lblSem = new JLabel("Semester:");
        lblSem.setFont(ModernTheme.FONT_BOLD);
        lblSem.setForeground(ModernTheme.TEXT_DARK);

        cmbSemesterFilter = new JComboBox<>(new String[]{"All Semesters", "Semester 1", "Semester 2"});
        cmbSemesterFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbSemesterFilter.setPreferredSize(new Dimension(125, 30));
        cmbSemesterFilter.setBackground(Color.WHITE);

        filterRow.add(lblDept);
        filterRow.add(cmbDepartmentFilter);
        filterRow.add(Box.createHorizontalStrut(5));
        filterRow.add(lblYear);
        filterRow.add(cmbYearFilter);
        filterRow.add(Box.createHorizontalStrut(5));
        filterRow.add(lblSem);
        filterRow.add(cmbSemesterFilter);

        // Row 2: Action Toolbar below Filters (prevents overlapping and clipping)
        JPanel toolbarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbarRow.setOpaque(false);

        btnFilter = ModernTheme.createPrimaryButton("Apply Filter");
        btnFilter.setPreferredSize(new Dimension(125, 32));

        btnResetFilter = ModernTheme.createSecondaryButton("Reset Filters");
        btnResetFilter.setPreferredSize(new Dimension(120, 32));

        lblCourseCount = new JLabel("Loading courses...");
        lblCourseCount.setFont(ModernTheme.FONT_REGULAR);
        lblCourseCount.setForeground(ModernTheme.TEXT_MUTED);

        toolbarRow.add(btnFilter);
        toolbarRow.add(btnResetFilter);
        toolbarRow.add(Box.createHorizontalStrut(15));
        toolbarRow.add(lblCourseCount);

        btnFilter.addActionListener(e -> loadCourses());
        btnResetFilter.addActionListener(e -> {
            cmbDepartmentFilter.setSelectedIndex(0);
            cmbYearFilter.setSelectedIndex(0);
            cmbSemesterFilter.setSelectedIndex(0);
            loadCourses();
        });

        filterAndToolbarPanel.add(filterRow);
        filterAndToolbarPanel.add(Box.createVerticalStrut(4));
        filterAndToolbarPanel.add(toolbarRow);

        tablePanel.add(filterAndToolbarPanel, BorderLayout.NORTH);

        JScrollPane tableScrollPane = new JScrollPane(courseTable);
        tableScrollPane.setBorder(null);
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Right-Side Form Panel (Course Specification Card)
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 10));
        formCard.setMinimumSize(new Dimension(360, 400));
        formCard.setPreferredSize(new Dimension(420, 650));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel formHeader = new JLabel("Course Specification");
        formHeader.setFont(ModernTheme.FONT_SUBTITLE);
        formHeader.setForeground(ModernTheme.PRIMARY);

        JLabel formSubHeader = new JLabel("Create, view, or update course modules");
        formSubHeader.setFont(ModernTheme.FONT_SMALL);
        formSubHeader.setForeground(ModernTheme.TEXT_MUTED);

        headerPanel.add(formHeader, BorderLayout.NORTH);
        headerPanel.add(formSubHeader, BorderLayout.SOUTH);
        formCard.add(headerPanel, BorderLayout.NORTH);

        // Form fields inside a scrollable container for responsiveness
        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(4, 4, 4, 10));

        txtCourseId = new JTextField();
        txtCourseId.setVisible(false);

        txtCourseCode = new JTextField();
        txtCourseCode.setEditable(false); // generated from department/year/semester/credits
        txtCourseName = new JTextField();
        spinCredits = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
        spinSemester = new JSpinner(new SpinnerNumberModel(1, 1, 2, 1));
        spinStudyYear = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        cmbDepartment = new JComboBox<>();
        cmbLecturer = new JComboBox<>();
        cmbDepartment.addActionListener(e -> { loadLecturersForSelectedDepartment(); refreshGeneratedCourseCode(); });
        spinSemester.addChangeListener(e -> refreshGeneratedCourseCode());
        spinStudyYear.addChangeListener(e -> refreshGeneratedCourseCode());
        chkTheory = new JCheckBox("Includes Theory Component (15 Sessions)", true);
        chkPractical = new JCheckBox("Includes Practical Component (15 Sessions)", false);
        chkTheory.setBackground(Color.WHITE);
        chkPractical.setBackground(Color.WHITE);
        chkTheory.setFont(ModernTheme.FONT_REGULAR);
        chkPractical.setFont(ModernTheme.FONT_REGULAR);

        txtDescription = new JTextArea(4, 20);
        txtDescription.setFont(ModernTheme.FONT_REGULAR);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        descScroll.setPreferredSize(new Dimension(200, 85));

        fields.add(createRow("Course Code (Auto-Generated):", txtCourseCode));
        fields.add(createRow("Course Name / Title:", txtCourseName));
        fields.add(createRow("Credit Value:", spinCredits));
        fields.add(createRow("Study Year (1-4):", spinStudyYear));
        fields.add(createRow("Semester Number (1-2):", spinSemester));
        fields.add(createRow("Offering Department:", cmbDepartment));
        fields.add(createRow("Assigned Lecturer:", cmbLecturer));
        fields.add(Box.createRigidArea(new Dimension(0, 4)));

        JPanel checkPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        checkPanel.setOpaque(false);
        checkPanel.setBorder(new EmptyBorder(4, 2, 4, 2));
        checkPanel.add(chkTheory);
        checkPanel.add(chkPractical);
        fields.add(checkPanel);

        fields.add(Box.createRigidArea(new Dimension(0, 6)));
        fields.add(createRow("Description / Syllabus Outline:", descScroll));

        JScrollPane fieldsScrollPane = new JScrollPane(fields);
        fieldsScrollPane.setBorder(null);
        fieldsScrollPane.setOpaque(false);
        fieldsScrollPane.getViewport().setOpaque(false);
        fieldsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        fieldsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formCard.add(fieldsScrollPane, BorderLayout.CENTER);

        // Action Buttons docked at bottom (always fully visible, consistent heights)
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER),
            new EmptyBorder(10, 0, 0, 0)
        ));

        btnAdd = ModernTheme.createPrimaryButton("Add Course");
        btnUpdate = ModernTheme.createSuccessButton("Update Course");
        btnDelete = ModernTheme.createDangerButton("Delete Course");
        btnClear = ModernTheme.createSecondaryButton("Clear Form");

        btnAdd.setPreferredSize(new Dimension(0, 36));
        btnUpdate.setPreferredSize(new Dimension(0, 36));
        btnDelete.setPreferredSize(new Dimension(0, 36));
        btnClear.setPreferredSize(new Dimension(0, 36));

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
        JPanel p = new JPanel(new BorderLayout(5, 3));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 58));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ModernTheme.FONT_BOLD);
        lbl.setForeground(ModernTheme.TEXT_DARK);

        comp.setFont(ModernTheme.FONT_REGULAR);
        if (comp instanceof JTextField) {
            comp.setPreferredSize(new Dimension(200, 30));
            comp.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
            if (!((JTextField) comp).isEditable()) {
                comp.setBackground(new Color(241, 245, 249)); // light slate tint for read-only
            }
        } else if (comp instanceof JComboBox) {
            comp.setPreferredSize(new Dimension(200, 30));
            comp.setBackground(Color.WHITE);
        } else if (comp instanceof JSpinner) {
            comp.setPreferredSize(new Dimension(200, 30));
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(2, 0, 4, 0));
        return p;
    }

    private static class FilterItem {
        final int id; final String label;
        FilterItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private void loadDepartments() {
        cmbDepartment.removeAllItems();
        cmbDepartmentFilter.removeAllItems();
        cmbDepartmentFilter.addItem(new FilterItem(0, "All Departments"));
        try {
            List<Department> list = departmentDAO.getAllDepartments();
            for (Department d : list) {
                String label = d.getDepartmentCode() + " - " + d.getDepartmentName();
                cmbDepartment.addItem(new DeptItem(d.getDepartmentId(), label));
                cmbDepartmentFilter.addItem(new FilterItem(d.getDepartmentId(), label));
            }
        } catch (Exception e) {
            System.err.println("[CourseManagementPanel] Load departments note: " + e.getMessage());
        }
    }

    private void loadLecturersForSelectedDepartment() {
        cmbLecturer.removeAllItems();
        DeptItem dept = (DeptItem) cmbDepartment.getSelectedItem();
        if (dept == null) return;
        List<CourseDAO.LecturerOption> lecturers = courseDAO.getLecturersByDepartment(dept.id);
        for (CourseDAO.LecturerOption lecturer : lecturers) cmbLecturer.addItem(lecturer);
        if (lecturers.isEmpty()) {
            cmbLecturer.addItem(new CourseDAO.LecturerOption(-1, "No active lecturers in this department"));
            cmbLecturer.setSelectedIndex(0);
        }
    }

    public void loadCourses() {
        tableModel.setRowCount(0);
        try {
            FilterItem dept = (FilterItem) cmbDepartmentFilter.getSelectedItem();
            int departmentId = dept == null ? 0 : dept.id;
            int year = cmbYearFilter.getSelectedIndex();
            int semester = cmbSemesterFilter.getSelectedIndex();
            List<Course> list = courseDAO.getCoursesFiltered(departmentId, year, semester);
            for (Course c : list) {
                tableModel.addRow(new Object[]{
                    c.getCourseId(),
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCredits(),
                    c.getStudyYear(),
                    c.getSemester(),
                    c.getDepartmentCode() != null ? c.getDepartmentCode() : c.getDepartmentName(),
                    c.isHasTheory() ? "Yes" : "No",
                    c.isHasPractical() ? "Yes" : "No"
                });
            }
            if (lblCourseCount != null) {
                lblCourseCount.setText("Showing " + list.size() + " curriculum module" + (list.size() == 1 ? "" : "s"));
            }
        } catch (Exception e) {
            System.err.println("[CourseManagementPanel] Load courses note: " + e.getMessage());
        }
        styleCourseTableColumns();
    }

    private void styleCourseTableColumns() {
        int[] colWidths = {45, 95, 210, 60, 55, 55, 120, 65, 65};
        for (int i = 0; i < colWidths.length && i < courseTable.getColumnCount(); i++) {
            courseTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            if (i == 0) {
                courseTable.getColumnModel().getColumn(i).setMinWidth(35);
                courseTable.getColumnModel().getColumn(i).setMaxWidth(60);
            } else if (i == 3 || i == 4 || i == 5) {
                courseTable.getColumnModel().getColumn(i).setMinWidth(45);
                courseTable.getColumnModel().getColumn(i).setMaxWidth(75);
            } else if (i == 7 || i == 8) {
                courseTable.getColumnModel().getColumn(i).setMinWidth(55);
                courseTable.getColumnModel().getColumn(i).setMaxWidth(80);
            }
        }

        for (int i = 0; i < courseTable.getColumnCount(); i++) {
            final int colIndex = i;
            courseTable.getColumnModel().getColumn(i).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                    setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                    if (colIndex == 0 || colIndex == 3 || colIndex == 4 || colIndex == 5 || colIndex == 7 || colIndex == 8) {
                        setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        setHorizontalAlignment(SwingConstants.LEFT);
                    }
                    if (!isSelected) {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }
                    return c;
                }
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
        spinStudyYear.setValue(c.getStudyYear());
        chkTheory.setSelected(c.isHasTheory());
        chkPractical.setSelected(c.isHasPractical());
        txtDescription.setText(c.getDescription() != null ? c.getDescription() : "");

        for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
            if (cmbDepartment.getItemAt(i).id == c.getDepartmentId()) {
                cmbDepartment.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < cmbLecturer.getItemCount(); i++) {
            if (cmbLecturer.getItemAt(i).getId() == c.getLecturerId()) {
                cmbLecturer.setSelectedIndex(i);
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
        spinStudyYear.setValue(1);
        chkTheory.setSelected(true);
        chkPractical.setSelected(false);
        txtDescription.setText("");
        if (cmbDepartment.getItemCount() > 0) cmbDepartment.setSelectedIndex(0);
        loadLecturersForSelectedDepartment();
        refreshGeneratedCourseCode();
    }


    private void refreshGeneratedCourseCode() {
        if (txtCourseCode == null || !txtCourseId.getText().trim().isEmpty()) return;
        DeptItem dept = (DeptItem) cmbDepartment.getSelectedItem();
        if (dept == null) return;
        try {
            String code = courseDAO.generateCourseCode(dept.id, (int) spinStudyYear.getValue(),
                    (int) spinSemester.getValue(), (int) spinCredits.getValue());
            txtCourseCode.setText(code);
        } catch (Exception ignored) { /* Form remains usable while database is unavailable. */ }
    }

    private void handleAdd() {
        try {
            DeptItem dept = (DeptItem) cmbDepartment.getSelectedItem();
            if (dept == null) throw new ValidationException("Please select a department.");
            if (txtCourseName.getText().trim().isEmpty()) throw new ValidationException("Please enter a course name.");
            if ((int) spinStudyYear.getValue() < 1 || (int) spinStudyYear.getValue() > 4) throw new ValidationException("Study year must be between 1 and 4.");
            if ((int) spinSemester.getValue() < 1 || (int) spinSemester.getValue() > 2) throw new ValidationException("Semester must be 1 or 2.");
            CourseDAO.LecturerOption lecturer = (CourseDAO.LecturerOption) cmbLecturer.getSelectedItem();
            if (lecturer == null || lecturer.getId() <= 0) throw new ValidationException("No lecturer is available for the selected department.");

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
            c.setStudyYear((int) spinStudyYear.getValue());
            c.setLecturerId(lecturer.getId());

            boolean saved = courseService.addCourse(c);
            if (!saved) throw new ValidationException("Course was not saved. Please try again.");
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
            if (txtCourseName.getText().trim().isEmpty()) throw new ValidationException("Please enter a course name.");
            if ((int) spinStudyYear.getValue() < 1 || (int) spinStudyYear.getValue() > 4) throw new ValidationException("Study year must be between 1 and 4.");
            if ((int) spinSemester.getValue() < 1 || (int) spinSemester.getValue() > 2) throw new ValidationException("Semester must be 1 or 2.");
            CourseDAO.LecturerOption lecturer = (CourseDAO.LecturerOption) cmbLecturer.getSelectedItem();
            if (lecturer == null || lecturer.getId() <= 0) throw new ValidationException("No lecturer is available for the selected department.");

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
            c.setStudyYear((int) spinStudyYear.getValue());
            c.setLecturerId(lecturer.getId());

            boolean updated = courseService.updateCourse(c);
            if (!updated) throw new ValidationException("Course was not updated. Please try again.");
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
                boolean deleted = courseService.deleteCourse(courseId);
                if (!deleted) throw new IllegalStateException("Course was not deleted. Please try again.");
                JOptionPane.showMessageDialog(this, "Course deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
