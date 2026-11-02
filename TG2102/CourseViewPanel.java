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
import java.io.File;

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
    private final java.util.List<Course> displayedCourses = new java.util.ArrayList<>();

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

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        JButton btnEnroll = ModernTheme.createPrimaryButton("Enroll in Course");
        btnEnroll.addActionListener(e -> showEnrollmentDialog());
        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Courses");
        btnRefresh.addActionListener(e -> loadEnrolledCourses());
        headerActions.add(btnEnroll);
        headerActions.add(btnRefresh);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(headerActions, BorderLayout.EAST);

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
        materialTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedMaterial();
            }
        });

        rightPanel.add(new JScrollPane(materialTable), BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        JPanel coursesTab = new JPanel(new BorderLayout(15, 15));
        coursesTab.setBackground(ModernTheme.BG_MAIN);
        coursesTab.add(topBar, BorderLayout.NORTH);
        coursesTab.add(splitPane, BorderLayout.CENTER);

        quizTakingPanel = new QuizTakingPanel();
        assignmentSubmissionPanel = new AssignmentSubmissionPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernTheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("Enrolled Courses & Materials", coursesTab);
        tabbedPane.addTab("My Quizzes", quizTakingPanel);
        tabbedPane.addTab("My Assignments", assignmentSubmissionPanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 1 && quizTakingPanel != null) {
                quizTakingPanel.loadEnrolledCourses();
            } else if (idx == 2 && assignmentSubmissionPanel != null) {
                assignmentSubmissionPanel.loadEnrolledCourses();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    private QuizTakingPanel quizTakingPanel;
    private AssignmentSubmissionPanel assignmentSubmissionPanel;


    private void showEnrollmentDialog() {
        User current = SessionManager.getCurrentUser();
        if (current == null || current.getUserId() <= 0) {
            JOptionPane.showMessageDialog(this, "Please log in as a student before enrolling.", "Login Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Course> allCourses = courseDAO.getAllCourses();
        if (allCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No courses are currently available.", "No Courses", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JComboBox<Course> cmbCourse = new JComboBox<>();
        for (Course course : allCourses) cmbCourse.addItem(course);

        JComboBox<String> cmbAcademicYear = new JComboBox<>();
        List<String> years = enrollmentDAO.getAvailableAcademicYears();
        for (String yr : years) cmbAcademicYear.addItem(yr);
        String activeYear = enrollmentDAO.getActiveAcademicYear();
        if (activeYear != null) cmbAcademicYear.setSelectedItem(activeYear);

        JCheckBox chkRepeat = new JCheckBox("Repeat course");
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Course:")); form.add(cmbCourse);
        form.add(new JLabel("Academic Year:")); form.add(cmbAcademicYear);
        form.add(new JLabel("Registration type:")); form.add(chkRepeat);

        int result = JOptionPane.showConfirmDialog(this, form, "Student Course Enrollment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        Course selected = (Course) cmbCourse.getSelectedItem();
        String academicYear = (String) cmbAcademicYear.getSelectedItem();
        if (selected == null || academicYear == null || academicYear.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a course and academic year.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!academicYear.matches("^\\d{4}$")) {
            JOptionPane.showMessageDialog(this, "Academic year must be in simple 4-digit YYYY format (e.g. 2026).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (enrollmentDAO.isStudentEnrolled(current.getUserId(), selected.getCourseId(), academicYear, selected.getSemester())) {
                JOptionPane.showMessageDialog(this, "You are already enrolled in this course for that academic year and semester.", "Already Enrolled", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean enrolled = enrollmentDAO.enrollStudent(current.getUserId(), selected.getCourseId(), academicYear, selected.getSemester(), chkRepeat.isSelected());
            if (!enrolled) throw new IllegalStateException("The enrollment was not saved.");
            JOptionPane.showMessageDialog(this, "Course enrollment saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadEnrolledCourses();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enrollment failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadEnrolledCourses() {
        courseTableModel.setRowCount(0);
        displayedCourses.clear();
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<CourseEnrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(cur.getUserId());
        for (CourseEnrollment ce : enrollments) {
            Course c = courseDAO.getCourseById(ce.getCourseId());
            if (c != null) {
                displayedCourses.add(c);
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

        if (quizTakingPanel != null) quizTakingPanel.loadStudentQuizzes();
        if (assignmentSubmissionPanel != null) assignmentSubmissionPanel.loadStudentAssignments();
    }


    private void openSelectedMaterial() {
        int row = materialTable.getSelectedRow();
        if (row < 0) return;
        String path = String.valueOf(materialTableModel.getValueAt(row, 2));
        try {
            File file = new File(path);
            if (!file.exists()) throw new IllegalStateException("The material file cannot be found: " + path);
            if (!Desktop.isDesktopSupported()) throw new IllegalStateException("File opening is not supported on this computer.");
            Desktop.getDesktop().open(file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open material: " + ex.getMessage(), "Open Material", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMaterialsForSelectedCourse() {
        materialTableModel.setRowCount(0);
        int row = courseTable.getSelectedRow();
        if (row == -1) return;

        String code = (String) courseTableModel.getValueAt(row, 0);
        String name = (String) courseTableModel.getValueAt(row, 1);
        lblSelectedCourseMaterials.setText("Course Materials: " + code + " - " + name);

        if (row >= 0 && row < displayedCourses.size()) {
            int courseId = displayedCourses.get(row).getCourseId();
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
