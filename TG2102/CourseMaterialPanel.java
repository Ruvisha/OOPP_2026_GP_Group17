package com.fot.ams.ui.lecturer;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Course;
import com.fot.ams.model.CourseMaterial;
import com.fot.ams.model.User;
import com.fot.ams.service.CourseService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Panel enabling Lecturers to upload and manage course study materials.
 */
public class CourseMaterialPanel extends JPanel {
    private final CourseService courseService;
    private final CourseDAO courseDAO;

    private JComboBox<CourseFilterItem> cmbCourseFilter;
    private JTable materialTable;
    private DefaultTableModel tableModel;

    // Form inputs
    private JTextField txtMaterialId;
    private JComboBox<CourseFilterItem> cmbFormCourse;
    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JTextField txtFilePath;
    private JButton btnBrowse;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    private static class CourseFilterItem {
        final int id;
        final String label;

        CourseFilterItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public CourseMaterialPanel() {
        this.courseService = new CourseService();
        this.courseDAO = new CourseDAO();
        initComponents();
        loadCourses();
        loadMaterials(0);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lblFilter = new JLabel("Filter Course:");
        lblFilter.setFont(ModernTheme.FONT_BOLD);
        cmbCourseFilter = new JComboBox<>();
        cmbCourseFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbCourseFilter.addActionListener(e -> {
            CourseFilterItem sel = (CourseFilterItem) cmbCourseFilter.getSelectedItem();
            if (sel != null) {
                loadMaterials(sel.id);
            }
        });

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Materials");
        btnRefresh.addActionListener(e -> {
            CourseFilterItem sel = (CourseFilterItem) cmbCourseFilter.getSelectedItem();
            loadMaterials(sel != null ? sel.id : 0);
        });

        topBar.add(lblFilter);
        topBar.add(cmbCourseFilter);
        topBar.add(btnRefresh);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(650);

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        String[] cols = {"ID", "Course", "Title", "File Path", "Uploaded By", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        materialTable = new JTable(tableModel);
        ModernTheme.styleTable(materialTable);
        materialTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && materialTable.getSelectedRow() != -1) {
                populateForm();
            }
        });

        tablePanel.add(new JScrollPane(materialTable), BorderLayout.CENTER);

        // Form Panel
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BorderLayout());
        formCard.setPreferredSize(new Dimension(360, 500));

        JLabel formHeader = new JLabel("Upload Course Material");
        formHeader.setFont(ModernTheme.FONT_SUBTITLE);
        formHeader.setForeground(ModernTheme.PRIMARY);
        formHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(Color.WHITE);

        txtMaterialId = new JTextField();
        txtMaterialId.setVisible(false);

        cmbFormCourse = new JComboBox<>();
        txtTitle = new JTextField();
        txtDescription = new JTextArea(4, 20);
        txtDescription.setFont(ModernTheme.FONT_REGULAR);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        txtFilePath = new JTextField();
        btnBrowse = ModernTheme.createSecondaryButton("Browse...");
        btnBrowse.addActionListener(e -> handleBrowseFile());

        JPanel fileRow = new JPanel(new BorderLayout(6, 0));
        fileRow.setOpaque(false);
        fileRow.add(txtFilePath, BorderLayout.CENTER);
        fileRow.add(btnBrowse, BorderLayout.EAST);

        fields.add(createRow("Target Course:", cmbFormCourse));
        fields.add(createRow("Material Title / Topic:", txtTitle));
        fields.add(createRow("Description / Notes:", new JScrollPane(txtDescription)));
        fields.add(createRow("Document File Path:", fileRow));

        formCard.add(new JScrollPane(fields), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnAdd = ModernTheme.createPrimaryButton("Add Material");
        btnUpdate = ModernTheme.createSuccessButton("Update Material");
        btnDelete = ModernTheme.createDangerButton("Delete Material");
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

    private void handleBrowseFile() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            txtFilePath.setText(f.getAbsolutePath());
        }
    }

    private void loadCourses() {
        cmbCourseFilter.removeAllItems();
        cmbCourseFilter.addItem(new CourseFilterItem(0, "All Courses"));

        cmbFormCourse.removeAllItems();
        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            CourseFilterItem item = new CourseFilterItem(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName());
            cmbCourseFilter.addItem(item);
            cmbFormCourse.addItem(item);
        }
    }

    public void loadMaterials(int courseId) {
        tableModel.setRowCount(0);
        List<CourseMaterial> list;
        if (courseId == 0) {
            list = courseService.getAllMaterials();
        } else {
            list = courseService.getMaterialsByCourse(courseId);
        }

        for (CourseMaterial m : list) {
            tableModel.addRow(new Object[]{
                m.getMaterialId(),
                m.getCourseCode(),
                m.getTitle(),
                m.getFilePath(),
                m.getLecturerName() != null ? m.getLecturerName() : "Lecturer",
                DateUtil.formatDisplayDateTime(m.getUploadedAt())
            });
        }
    }

    private void populateForm() {
        int row = materialTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableModel.getValueAt(row, 0);
        List<CourseMaterial> list = courseService.getAllMaterials();
        for (CourseMaterial m : list) {
            if (m.getMaterialId() == id) {
                txtMaterialId.setText(String.valueOf(m.getMaterialId()));
                txtTitle.setText(m.getTitle());
                txtDescription.setText(m.getDescription() != null ? m.getDescription() : "");
                txtFilePath.setText(m.getFilePath());

                for (int i = 0; i < cmbFormCourse.getItemCount(); i++) {
                    if (cmbFormCourse.getItemAt(i).id == m.getCourseId()) {
                        cmbFormCourse.setSelectedIndex(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void clearForm() {
        materialTable.clearSelection();
        txtMaterialId.setText("");
        txtTitle.setText("");
        txtDescription.setText("");
        txtFilePath.setText("");
        if (cmbFormCourse.getItemCount() > 0) cmbFormCourse.setSelectedIndex(0);
    }

    private void handleAdd() {
        try {
            CourseFilterItem item = (CourseFilterItem) cmbFormCourse.getSelectedItem();
            if (item == null || item.id <= 0) throw new ValidationException("Please select a target course.");

            User current = SessionManager.getCurrentUser();
            int lecturerId = current != null ? current.getUserId() : 2;

            CourseMaterial cm = new CourseMaterial(
                0,
                item.id,
                lecturerId,
                txtTitle.getText().trim(),
                txtDescription.getText().trim(),
                txtFilePath.getText().trim()
            );

            courseService.addMaterial(cm);
            JOptionPane.showMessageDialog(this, "Course material uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            CourseFilterItem sel = (CourseFilterItem) cmbCourseFilter.getSelectedItem();
            loadMaterials(sel != null ? sel.id : 0);
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding material: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (txtMaterialId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a material to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtMaterialId.getText());
            CourseFilterItem item = (CourseFilterItem) cmbFormCourse.getSelectedItem();
            if (item == null || item.id <= 0) throw new ValidationException("Please select a target course.");

            CourseMaterial cm = new CourseMaterial(
                id,
                item.id,
                0,
                txtTitle.getText().trim(),
                txtDescription.getText().trim(),
                txtFilePath.getText().trim()
            );

            courseService.updateMaterial(cm);
            JOptionPane.showMessageDialog(this, "Course material updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            CourseFilterItem sel = (CourseFilterItem) cmbCourseFilter.getSelectedItem();
            loadMaterials(sel != null ? sel.id : 0);
            clearForm();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating material: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (txtMaterialId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a material to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtMaterialId.getText());
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this study material?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                courseService.deleteMaterial(id);
                JOptionPane.showMessageDialog(this, "Course material deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                CourseFilterItem sel = (CourseFilterItem) cmbCourseFilter.getSelectedItem();
                loadMaterials(sel != null ? sel.id : 0);
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting material: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
