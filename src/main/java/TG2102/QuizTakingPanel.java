package com.fot.ams.ui.undergraduate;

import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.*;
import com.fot.ams.service.AssessmentService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel enabling Students to view quizzes in their enrolled courses,
 * take quizzes with a countdown timer, auto-submit, and receive submission confirmation.
 */
public class QuizTakingPanel extends JPanel {

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

    public QuizTakingPanel() {
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
        JLabel lblCourse = new JLabel("Enrolled Course:");
        lblCourse.setFont(ModernTheme.FONT_BOLD);
        cmbCourseFilter = new JComboBox<>();
        cmbCourseFilter.setFont(ModernTheme.FONT_REGULAR);
        cmbCourseFilter.setPreferredSize(new Dimension(300, 32));
        cmbCourseFilter.addActionListener(e -> loadStudentQuizzes());
        leftFilter.add(lblCourse);
        leftFilter.add(cmbCourseFilter);

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Quizzes");
        btnRefresh.addActionListener(e -> loadStudentQuizzes());
        leftFilter.add(btnRefresh);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton btnViewPaper = ModernTheme.createSecondaryButton("📄 View Question Paper");
        btnViewPaper.addActionListener(e -> {
            int row = quizTable.getSelectedRow();
            if (row < 0 || row >= displayedQuizzes.size()) {
                JOptionPane.showMessageDialog(this, "Please select a quiz from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Quiz sel = displayedQuizzes.get(row);
            if (!sel.hasAttachment()) {
                JOptionPane.showMessageDialog(this, "No PDF question paper is attached for this quiz.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            openPdfFile(sel.getFilePath());
        });

        JButton btnStart = ModernTheme.createSuccessButton("▶ Start Selected Quiz");
        btnStart.addActionListener(e -> startSelectedQuiz());

        rightActions.add(btnViewPaper);
        rightActions.add(btnStart);

        topBar.add(leftFilter, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"ID", "Course Code", "Quiz Title", "Duration", "Questions", "Question Paper", "Start Time", "End Time", "My Status", "Marks / Result"};
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
    }

    public void loadEnrolledCourses() {
        cmbCourseFilter.removeAllItems();
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        List<Course> list = java.util.Collections.emptyList();
        try {
            list = assessmentService.getEnrolledCoursesForStudent(cur.getUserId());
        } catch (Exception ignored) {}
        cmbCourseFilter.addItem(new CourseOption(0, "All Registered Courses (" + list.size() + ")"));
        for (Course c : list) {
            cmbCourseFilter.addItem(new CourseOption(c.getCourseId(), c.getCourseCode() + " - " + c.getCourseName()));
        }
        if (!list.isEmpty()) {
            cmbCourseFilter.setSelectedIndex(0);
        }
        loadStudentQuizzes();
    }

    public void loadStudentQuizzes() {
        quizTableModel.setRowCount(0);
        displayedQuizzes.clear();

        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        CourseOption sel = (CourseOption) cmbCourseFilter.getSelectedItem();
        List<Course> courses = java.util.Collections.emptyList();
        try {
        if (sel == null || sel.id == 0) {
            courses = assessmentService.getEnrolledCoursesForStudent(cur.getUserId());
        } else {
            Course c = new Course();
            c.setCourseId(sel.id);
            courses = List.of(c);
        }
        } catch (Exception ignored) {}

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        long now = System.currentTimeMillis();

        for (Course c : courses) {
            List<Quiz> qList = assessmentService.getQuizzesByCourse(c.getCourseId());
            for (Quiz q : qList) {
                if (!q.isPublished() && !q.isClosed()) {
                    // Hide unshared draft quizzes from students
                    continue;
                }

                displayedQuizzes.add(q);
                QuizAttempt attempt = assessmentService.getStudentAttempt(q.getId(), cur.getUserId());

                String myStatus;
                String marksInfo;

                if (attempt != null && attempt.isCompleted()) {
                    myStatus = "COMPLETED";
                    if (q.isMarksReleased()) {
                        marksInfo = String.format("%.1f / %.1f", attempt.getScore(), attempt.getTotalMarks());
                    } else {
                        marksInfo = "PENDING RELEASE";
                    }
                } else if (q.isClosed()) {
                    myStatus = "CLOSED / EXPIRED";
                    marksInfo = "NOT ATTEMPTED";
                } else if (q.getStartTime() != null && now < q.getStartTime().getTime()) {
                    myStatus = "UPCOMING (Opens " + sdf.format(q.getStartTime()) + ")";
                    marksInfo = "-";
                } else if (q.getEndTime() != null && now > q.getEndTime().getTime()) {
                    myStatus = "EXPIRED";
                    marksInfo = "NOT ATTEMPTED";
                } else {
                    myStatus = "AVAILABLE TO START";
                    marksInfo = "-";
                }

                quizTableModel.addRow(new Object[]{
                        q.getId(),
                        q.getCourseCode(),
                        q.getTitle(),
                        q.getDurationMinutes() + " mins",
                        q.getQuestionCount(),
                        q.hasAttachment() ? "📄 " + q.getAttachmentFileName() : "None",
                        q.getStartTime() != null ? sdf.format(q.getStartTime()) : "Immediate",
                        q.getEndTime() != null ? sdf.format(q.getEndTime()) : "No close date",
                        myStatus,
                        marksInfo
                });
            }
        }
    }

    private void startSelectedQuiz() {
        int row = quizTable.getSelectedRow();
        if (row < 0 || row >= displayedQuizzes.size()) {
            JOptionPane.showMessageDialog(this, "Please select an available quiz from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Quiz sel = displayedQuizzes.get(row);
        User cur = SessionManager.getCurrentUser();
        if (cur == null) return;

        try {
            QuizAttempt attempt = assessmentService.startQuizAttempt(sel.getId(), cur.getUserId());
            if (attempt == null) {
                JOptionPane.showMessageDialog(this, "Unable to start quiz.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<QuizQuestion> questions = assessmentService.getQuestionsByQuiz(sel.getId());
            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "This quiz has no questions available.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Launch Quiz Taking Dialog
            openQuizRunner(sel, attempt, questions);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Start Quiz", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error starting quiz: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openQuizRunner(Quiz quiz, QuizAttempt attempt, List<QuizQuestion> questions) {
        JDialog runner = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Online Quiz - " + quiz.getTitle(), true);
        runner.setSize(850, 600);
        runner.setLocationRelativeTo(this);
        runner.setLayout(new BorderLayout(10, 10));
        runner.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Answers state
        Map<Integer, String> studentAnswers = new HashMap<>();
        List<QuizAnswer> existingAnswers = assessmentService.getAnswersByAttempt(attempt.getId());
        for (QuizAnswer ea : existingAnswers) {
            if (ea.getAnswerText() != null) {
                studentAnswers.put(ea.getQuestionId(), ea.getAnswerText());
            }
        }

        final int[] currentIndex = {0};
        final int[] secondsRemaining = {quiz.getDurationMinutes() * 60};

        // Header with title and Countdown Timer
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ModernTheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel lblHeaderTitle = new JLabel(quiz.getTitle() + " (" + quiz.getCourseCode() + ")");
        lblHeaderTitle.setFont(ModernTheme.FONT_TITLE);
        lblHeaderTitle.setForeground(Color.WHITE);

        JLabel lblTimer = new JLabel("Time Remaining: " + formatTime(secondsRemaining[0]));
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTimer.setForeground(new Color(254, 240, 138));

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);
        if (quiz.hasAttachment()) {
            JButton btnOpenPaper = ModernTheme.createSecondaryButton("📄 View Question Paper (PDF)");
            btnOpenPaper.addActionListener(ev -> openPdfFile(quiz.getFilePath()));
            headerRight.add(btnOpenPaper);
        }
        headerRight.add(lblTimer);

        header.add(lblHeaderTitle, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        runner.add(header, BorderLayout.NORTH);

        // Center Question Card
        JPanel centerCard = new JPanel(new BorderLayout(15, 15));
        centerCard.setBackground(Color.WHITE);
        centerCard.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblQNum = new JLabel();
        lblQNum.setFont(ModernTheme.FONT_SUBTITLE);
        lblQNum.setForeground(ModernTheme.PRIMARY);

        JTextArea txtQText = new JTextArea();
        txtQText.setEditable(false);
        txtQText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtQText.setLineWrap(true);
        txtQText.setWrapStyleWord(true);
        txtQText.setBackground(Color.WHITE);
        txtQText.setBorder(new EmptyBorder(5, 5, 15, 5));

        JPanel qTop = new JPanel(new BorderLayout(5, 5));
        qTop.setOpaque(false);
        qTop.add(lblQNum, BorderLayout.NORTH);
        qTop.add(txtQText, BorderLayout.CENTER);
        centerCard.add(qTop, BorderLayout.NORTH);

        // Dynamic Options Container
        JPanel optionsContainer = new JPanel();
        optionsContainer.setLayout(new BoxLayout(optionsContainer, BoxLayout.Y_AXIS));
        optionsContainer.setOpaque(false);
        centerCard.add(optionsContainer, BorderLayout.CENTER);

        runner.add(centerCard, BorderLayout.CENTER);

        // Bottom Navigation Bar
        JPanel navBar = new JPanel(new BorderLayout(10, 10));
        navBar.setBackground(ModernTheme.BG_MAIN);
        navBar.setBorder(new EmptyBorder(10, 20, 15, 20));

        JButton btnPrev = ModernTheme.createSecondaryButton("◀ Previous Question");
        JButton btnNext = ModernTheme.createSecondaryButton("Next Question ▶");
        JButton btnSubmit = ModernTheme.createSuccessButton("✔ Submit Quiz");

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftNav.setOpaque(false);
        leftNav.add(btnPrev);
        leftNav.add(btnNext);

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightNav.setOpaque(false);
        rightNav.add(btnSubmit);

        navBar.add(leftNav, BorderLayout.WEST);
        navBar.add(rightNav, BorderLayout.EAST);
        runner.add(navBar, BorderLayout.SOUTH);

        // Method to render current question
        Runnable renderQuestion = () -> {
            QuizQuestion currentQ = questions.get(currentIndex[0]);
            lblQNum.setText("Question " + (currentIndex[0] + 1) + " of " + questions.size() + "  [" + currentQ.getQuestionType() + " - " + currentQ.getMarks() + " Marks]");
            txtQText.setText(currentQ.getQuestionText());

            optionsContainer.removeAll();
            String recordedAns = studentAnswers.get(currentQ.getId());

            if (currentQ.isMultipleChoice()) {
                ButtonGroup bg = new ButtonGroup();
                String[] opts = {currentQ.getOptionA(), currentQ.getOptionB(), currentQ.getOptionC(), currentQ.getOptionD()};
                String[] letters = {"A", "B", "C", "D"};

                for (int i = 0; i < opts.length; i++) {
                    if (opts[i] != null && !opts[i].trim().isEmpty()) {
                        String letter = letters[i];
                        JRadioButton rb = new JRadioButton(letter + ") " + opts[i]);
                        rb.setFont(ModernTheme.FONT_REGULAR);
                        rb.setOpaque(false);
                        rb.setBorder(new EmptyBorder(6, 4, 6, 4));

                        if (letter.equalsIgnoreCase(recordedAns) || opts[i].equalsIgnoreCase(recordedAns)) {
                            rb.setSelected(true);
                        }

                        rb.addActionListener(ev -> {
                            studentAnswers.put(currentQ.getId(), letter);
                            assessmentService.saveStudentAnswer(attempt.getId(), currentQ.getId(), letter);
                        });

                        bg.add(rb);
                        optionsContainer.add(rb);
                    }
                }
            } else if (currentQ.isTrueFalse()) {
                ButtonGroup bg = new ButtonGroup();
                JRadioButton rbTrue = new JRadioButton("True");
                JRadioButton rbFalse = new JRadioButton("False");
                rbTrue.setFont(ModernTheme.FONT_REGULAR);
                rbFalse.setFont(ModernTheme.FONT_REGULAR);
                rbTrue.setOpaque(false);
                rbFalse.setOpaque(false);

                if ("True".equalsIgnoreCase(recordedAns)) rbTrue.setSelected(true);
                else if ("False".equalsIgnoreCase(recordedAns)) rbFalse.setSelected(true);

                rbTrue.addActionListener(ev -> {
                    studentAnswers.put(currentQ.getId(), "True");
                    assessmentService.saveStudentAnswer(attempt.getId(), currentQ.getId(), "True");
                });
                rbFalse.addActionListener(ev -> {
                    studentAnswers.put(currentQ.getId(), "False");
                    assessmentService.saveStudentAnswer(attempt.getId(), currentQ.getId(), "False");
                });

                bg.add(rbTrue);
                bg.add(rbFalse);
                optionsContainer.add(rbTrue);
                optionsContainer.add(Box.createRigidArea(new Dimension(0, 8)));
                optionsContainer.add(rbFalse);
            } else {
                // Short Answer
                JLabel lblHint = new JLabel("Type your answer below:");
                lblHint.setFont(ModernTheme.FONT_SMALL);
                lblHint.setForeground(ModernTheme.TEXT_MUTED);
                optionsContainer.add(lblHint);
                optionsContainer.add(Box.createRigidArea(new Dimension(0, 6)));

                JTextArea area = new JTextArea(recordedAns != null ? recordedAns : "", 5, 30);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setFont(ModernTheme.FONT_REGULAR);
                JScrollPane sp = new JScrollPane(area);
                sp.setMaximumSize(new Dimension(800, 140));

                area.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyReleased(java.awt.event.KeyEvent e) {
                        studentAnswers.put(currentQ.getId(), area.getText());
                        assessmentService.saveStudentAnswer(attempt.getId(), currentQ.getId(), area.getText());
                    }
                });

                optionsContainer.add(sp);
            }

            btnPrev.setEnabled(currentIndex[0] > 0);
            btnNext.setEnabled(currentIndex[0] < questions.size() - 1);
            optionsContainer.revalidate();
            optionsContainer.repaint();
        };

        btnPrev.addActionListener(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                renderQuestion.run();
            }
        });

        btnNext.addActionListener(e -> {
            if (currentIndex[0] < questions.size() - 1) {
                currentIndex[0]++;
                renderQuestion.run();
            }
        });

        // Submit action
        ActionListener doSubmit = ev -> {
            int opt = JOptionPane.showConfirmDialog(runner, "Are you sure you want to finish and submit your quiz?", "Confirm Submission", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;

            User curUser = SessionManager.getCurrentUser();
            assessmentService.submitQuizAttempt(attempt.getId(), curUser.getUserId());
            runner.dispose();
            showCompletionDialog(quiz.getTitle());
            loadStudentQuizzes();
        };
        btnSubmit.addActionListener(doSubmit);

        // Timer implementation
        Timer timer = new Timer(1000, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondsRemaining[0]--;
                lblTimer.setText("Time Remaining: " + formatTime(secondsRemaining[0]));

                if (secondsRemaining[0] <= 120) {
                    lblTimer.setForeground(new Color(239, 68, 68)); // Red warning
                }

                if (secondsRemaining[0] <= 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(runner, "Quiz time has expired! Your answers are being automatically submitted.", "Time Up", JOptionPane.WARNING_MESSAGE);
                    User curUser = SessionManager.getCurrentUser();
                    assessmentService.submitQuizAttempt(attempt.getId(), curUser.getUserId());
                    runner.dispose();
                    showCompletionDialog(quiz.getTitle());
                    loadStudentQuizzes();
                }
            }
        });

        runner.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int opt = JOptionPane.showConfirmDialog(runner, "Do you want to submit your quiz and exit?", "Submit Quiz?", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    timer.stop();
                    User curUser = SessionManager.getCurrentUser();
                    assessmentService.submitQuizAttempt(attempt.getId(), curUser.getUserId());
                    runner.dispose();
                    showCompletionDialog(quiz.getTitle());
                    loadStudentQuizzes();
                }
            }
        });

        renderQuestion.run();
        timer.start();
        runner.setVisible(true);
    }

    private String formatTime(int totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    /**
     * Required Success Screen strictly conforming to Prompt Requirement 5:
     * QUIZ FINISHED SUCCESSFULLY!
     * Your answers have been submitted successfully.
     * Status: COMPLETED
     * Marks and scores remain strictly hidden from students!
     */
    private void showCompletionDialog(String quizTitle) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quiz Completed", true);
        dlg.setSize(480, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("QUIZ FINISHED SUCCESSFULLY!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ModernTheme.SUCCESS);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel(quizTitle);
        sub.setFont(ModernTheme.FONT_SUBTITLE);
        sub.setForeground(ModernTheme.TEXT_DARK);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("Your answers have been submitted successfully.");
        msg.setFont(ModernTheme.FONT_REGULAR);
        msg.setForeground(ModernTheme.TEXT_MUTED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel status = new JLabel("Status: COMPLETED");
        status.setFont(new Font("Segoe UI", Font.BOLD, 15));
        status.setForeground(ModernTheme.PRIMARY);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel notice = new JLabel("Official marks will be published after lecturer verification.");
        notice.setFont(ModernTheme.FONT_SMALL);
        notice.setForeground(ModernTheme.TEXT_MUTED);
        notice.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(title);
        p.add(Box.createRigidArea(new Dimension(0, 10)));
        p.add(sub);
        p.add(Box.createRigidArea(new Dimension(0, 15)));
        p.add(msg);
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(status);
        p.add(Box.createRigidArea(new Dimension(0, 15)));
        p.add(notice);

        JButton btnClose = ModernTheme.createPrimaryButton("Return to Portal");
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dlg.dispose());
        p.add(Box.createRigidArea(new Dimension(0, 20)));
        p.add(btnClose);

        dlg.add(p, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void openPdfFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No question paper PDF is attached.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        File f = new File(path);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "The question paper PDF file was not found on the server at:\n" + path, "File Not Found", JOptionPane.ERROR_MESSAGE);
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

