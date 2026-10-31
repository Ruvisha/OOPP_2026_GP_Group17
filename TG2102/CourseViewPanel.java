package com.fot.ams.ui.undergraduate;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.dao.CourseMaterialDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.CourseEnrollment;
import com.fot.ams.model.CourseMaterial;
import com.fot.ams.model.User;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel enabling Undergraduates to view their enrolled courses and download course materials.
 */
public class CourseViewPanel extends JPanel {
    private final CourseDAO courseDAO;
    private final CourseEnrollmentDAO enrollmentDAO;
    private final CourseMaterialDAO materialDAO;

    private JTable courseTable;
    private DefaultTableModel courseTableModel;

    private JTable materialTable;
    private DefaultTableModel materialTableModel;
    private JLabel lblSelectedCourseMaterials;

    public CourseViewPanel() {
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();
        this.materialDAO = new CourseMaterialDAO();

        initComponents();
        loadEnrolledCourses();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        topBar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("My Enrolled Courses & Study Materials");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Courses");
        btnRefresh.addActionListener(e -> loadEnrolledCourses());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.50);
        splitPane.setDividerLocation(520);

        // Left: Enrolled Courses Table
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        JLabel lHeader = new JLabel("Enrolled Curriculum Modules");
        lHeader.setFont(ModernTheme.FONT_SUBTITLE);
        lHeader.setForeground(ModernTheme.PRIMARY);
        lHeader.setBorder(new EmptyBorder(10, 12, 5, 12));
        leftPanel.add(lHeader, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Name", "Credits", "Theory", "Practical", "Status"};
        courseTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        courseTable = new JTable(courseTableModel);
        ModernTheme.styleTable(courseTable);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && courseTable.getSelectedRow() != -1) {
                loadMaterialsForSelectedCourse();
            }
        });

        leftPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        // Right: Course Materials Table
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        lblSelectedCourseMaterials = new JLabel("Course Materials (Select a course on left)");
        lblSelectedCourseMaterials.setFont(ModernTheme.FONT_SUBTITLE);
        lblSelectedCourseMaterials.setForeground(ModernTheme.PRIMARY);
        lblSelectedCourseMaterials.setBorder(new EmptyBorder(10, 12, 5, 12));
        rightPanel.add(lblSelectedCourseMaterials, BorderLayout.NORTH);

        String[] mCols = {"Title / Topic", "Lecturer", "File Resource", "Upload Date"};
        materialTableModel = new DefaultTableModel(mCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        materialTable = new JTable(materialTableModel);
        ModernTheme.styleTable(materialTable);

        rightPanel.add(new JScrollPane(materialTable), BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    public void loadEnrolledCourses() {
        courseTableModel.setRowCount(0);
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(cur.getUserId());
        for (CourseEnrollment ce : enrollments) {
            Course c = courseDAO.getCourseById(ce.getCourseId());
            if (c != null) {
                courseTableModel.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCredits() + " Credits",
                    c.isHasTheory() ? "Yes" : "No",
                    c.isHasPractical() ? "Yes" : "No",
                    ce.isRepeat() ? "REPEAT" : "REGULAR"
                });
            }
        }

        if (courseTableModel.getRowCount() > 0) {
            courseTable.setRowSelectionInterval(0, 0);
            loadMaterialsForSelectedCourse();
        }
    }

    private void loadMaterialsForSelectedCourse() {
        materialTableModel.setRowCount(0);
        int row = courseTable.getSelectedRow();
        if (row == -1) return;

        String code = (String) courseTableModel.getValueAt(row, 0);
        String name = (String) courseTableModel.getValueAt(row, 1);
        lblSelectedCourseMaterials.setText("Course Materials: " + code + " - " + name);

        // Find course ID
        List<Course> courses = courseDAO.getAllCourses();
        int courseId = 0;
        for (Course c : courses) {
            if (c.getCourseCode().equalsIgnoreCase(code)) {
                courseId = c.getCourseId();
                break;
            }
        }

        if (courseId > 0) {
            List<CourseMaterial> materials = materialDAO.getMaterialsByCourse(courseId);
            for (CourseMaterial m : materials) {
                materialTableModel.addRow(new Object[]{
                    m.getTitle(),
                    m.getLecturerName() != null ? m.getLecturerName() : "Lecturer",
                    m.getFilePath(),
                    DateUtil.formatDisplayDate(m.getUploadedAt())
                });
            }
        }
    }
}
