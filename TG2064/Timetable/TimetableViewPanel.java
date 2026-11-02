package com.fot.ams.ui.undergraduate;

import com.fot.ams.model.Timetable;
import com.fot.ams.model.User;
import com.fot.ams.service.TimetableService;
import com.fot.ams.ui.common.ModernTheme;
import com.fot.ams.util.DateUtil;
import com.fot.ams.util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Panel enabling Undergraduates to inspect their weekly academic lecture & practical schedule
 * using a visual Weekly Timetable Grid layout with color-coded day columns and time slots.
 */
public class TimetableViewPanel extends JPanel {
    private final TimetableService timetableService;
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    // Visual Weekly Grid UI components
    private CardLayout cardLayout;
    private JPanel viewContainer;
    private JPanel gridContainer;
    private JButton btnToggleGrid;
    private JButton btnToggleTable;
    private JLabel lblSessionCount;

    // Day column color themes (Color-coded headers and accents)
    private static final String[] WEEK_DAYS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
    private static final Color[] DAY_HEADER_COLORS = {
        new Color(30, 58, 138),  // Monday: Deep Royal Blue
        new Color(13, 148, 136), // Tuesday: Emerald Teal
        new Color(217, 119, 6),  // Wednesday: Warm Amber
        new Color(109, 40, 217), // Thursday: Violet / Purple
        new Color(225, 29, 72)   // Friday: Rose / Crimson
    };
    private static final Color[] DAY_ACCENT_COLORS = {
        new Color(59, 130, 246), // Monday accent
        new Color(20, 184, 166), // Tuesday accent
        new Color(245, 158, 11), // Wednesday accent
        new Color(139, 92, 246), // Thursday accent
        new Color(244, 63, 94)   // Friday accent
    };

    public TimetableViewPanel() {
        this.timetableService = new TimetableService();
        initComponents();
        loadTimetable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernTheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Navigation Bar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new CompoundBorder(
            new LineBorder(ModernTheme.BORDER, 1),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Department Academic Timetable (Semester I)");
        title.setFont(ModernTheme.FONT_TITLE);
        title.setForeground(ModernTheme.PRIMARY);

        lblSessionCount = new JLabel("Weekly Schedule: Loading timetable sessions...");
        lblSessionCount.setFont(ModernTheme.FONT_SMALL);
        lblSessionCount.setForeground(ModernTheme.TEXT_MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 2)));
        titlePanel.add(lblSessionCount);

        // Control buttons panel (View switchers and Refresh)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlPanel.setOpaque(false);

        btnToggleGrid = ModernTheme.createPrimaryButton("Weekly Grid View");
        btnToggleTable = ModernTheme.createSecondaryButton("Table List View");

        btnToggleGrid.addActionListener(e -> {
            cardLayout.show(viewContainer, "GRID");
            btnToggleGrid.setBackground(ModernTheme.PRIMARY);
            btnToggleGrid.setForeground(Color.WHITE);
            btnToggleTable.setBackground(Color.WHITE);
            btnToggleTable.setForeground(ModernTheme.TEXT_DARK);
        });

        btnToggleTable.addActionListener(e -> {
            cardLayout.show(viewContainer, "TABLE");
            btnToggleTable.setBackground(ModernTheme.PRIMARY);
            btnToggleTable.setForeground(Color.WHITE);
            btnToggleGrid.setBackground(Color.WHITE);
            btnToggleGrid.setForeground(ModernTheme.TEXT_DARK);
        });

        JButton btnRefresh = ModernTheme.createSecondaryButton("Refresh Timetable");
        btnRefresh.addActionListener(e -> loadTimetable());

        controlPanel.add(btnToggleGrid);
        controlPanel.add(btnToggleTable);
        controlPanel.add(btnRefresh);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(controlPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // CardLayout container switching between Grid View and Table View
        cardLayout = new CardLayout();
        viewContainer = new JPanel(cardLayout);
        viewContainer.setOpaque(false);

        // 1. Weekly Grid View
        gridContainer = new JPanel();
        gridContainer.setBackground(ModernTheme.BG_MAIN);

        JScrollPane gridScrollPane = new JScrollPane(gridContainer);
        gridScrollPane.setBorder(null);
        gridScrollPane.setBackground(ModernTheme.BG_MAIN);
        gridScrollPane.getViewport().setBackground(ModernTheme.BG_MAIN);
        gridScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        gridScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        // 2. Table List View (preserved for complete backwards compatibility)
        String[] cols = {
            "Day of Week", "Time Slot", "Course Code", "Course Title",
            "Session Type", "Assigned Lecture Room / Lab", "Lecturer in Charge"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        ModernTheme.styleTable(timetableTable);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(ModernTheme.BORDER, 1));
        tablePanel.add(new JScrollPane(timetableTable), BorderLayout.CENTER);

        viewContainer.add(gridScrollPane, "GRID");
        viewContainer.add(tablePanel, "TABLE");

        add(viewContainer, BorderLayout.CENTER);
    }

    public void loadTimetable() {
        tableModel.setRowCount(0);
        User cur = SessionManager.getCurrentUser();
        int deptId = (cur != null && cur.getDepartmentId() != null) ? cur.getDepartmentId() : 1;

        List<Timetable> list = timetableService.getTimetablesByDepartment(deptId);

        // Populate backward-compatible TableModel
        for (Timetable t : list) {
            String timeSlot = DateUtil.formatTime(t.getStartTime()) + " - " + DateUtil.formatTime(t.getEndTime());
            tableModel.addRow(new Object[]{
                t.getDayOfWeek(),
                timeSlot,
                t.getCourseCode(),
                t.getCourseName(),
                t.getSessionType(),
                t.getRoom(),
                t.getLecturerName() != null ? t.getLecturerName() : "TBA"
            });
        }

        // Populate visual Weekly Timetable Grid
        buildWeeklyTimetableGrid(list);

        if (lblSessionCount != null) {
            lblSessionCount.setText("Weekly Schedule: " + list.size() + " total session(s) scheduled for this semester");
        }
    }

    /**
     * Constructs the visual Weekly Timetable Grid with color-coded day columns and lecture cards.
     */
    private void buildWeeklyTimetableGrid(List<Timetable> list) {
        gridContainer.removeAll();

        // Check if there are Saturday sessions
        boolean hasSaturday = list.stream().anyMatch(t -> "SATURDAY".equalsIgnoreCase(t.getDayOfWeek()));
        int numDays = hasSaturday ? 6 : 5;

        gridContainer.setLayout(new GridLayout(1, numDays, 12, 0));

        // Group timetable items by day of the week
        Map<String, List<Timetable>> dayMap = new LinkedHashMap<>();
        for (String day : WEEK_DAYS) {
            dayMap.put(day, new ArrayList<>());
        }
        if (hasSaturday) {
            dayMap.put("SATURDAY", new ArrayList<>());
        }

        for (Timetable t : list) {
            String day = t.getDayOfWeek() != null ? t.getDayOfWeek().toUpperCase().trim() : "MONDAY";
            if (dayMap.containsKey(day)) {
                dayMap.get(day).add(t);
            }
        }

        // Sort items in each day chronologically by start time
        for (List<Timetable> daySlots : dayMap.values()) {
            daySlots.sort((a, b) -> {
                if (a.getStartTime() == null || b.getStartTime() == null) return 0;
                return a.getStartTime().compareTo(b.getStartTime());
            });
        }

        // Build each day column
        int index = 0;
        for (Map.Entry<String, List<Timetable>> entry : dayMap.entrySet()) {
            String dayName = entry.getKey();
            List<Timetable> slots = entry.getValue();

            Color headerColor = (index < DAY_HEADER_COLORS.length) ? DAY_HEADER_COLORS[index] : new Color(71, 85, 105);
            Color accentColor = (index < DAY_ACCENT_COLORS.length) ? DAY_ACCENT_COLORS[index] : new Color(148, 163, 184);

            JPanel dayColumn = createDayColumn(dayName, slots, headerColor, accentColor);
            gridContainer.add(dayColumn);
            index++;
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private JPanel createDayColumn(String dayName, List<Timetable> slots, Color headerColor, Color accentColor) {
        JPanel column = new JPanel(new BorderLayout());
        column.setBackground(Color.WHITE);
        column.setBorder(new LineBorder(ModernTheme.BORDER, 1, true));

        // 1. Color-coded Day Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(headerColor);
        header.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lblDay = new JLabel(dayName);
        lblDay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDay.setForeground(Color.WHITE);

        String badgeText = slots.isEmpty() ? "Free" : (slots.size() == 1 ? "1 Slot" : slots.size() + " Slots");
        JLabel badge = new JLabel(badgeText);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(headerColor);
        badge.setOpaque(true);
        badge.setBackground(Color.WHITE);
        badge.setBorder(new CompoundBorder(
            new LineBorder(headerColor, 1, true),
            new EmptyBorder(2, 6, 2, 6)
        ));

        header.add(lblDay, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        column.add(header, BorderLayout.NORTH);

        // 2. Scheduled Lecture / Practical Cards Container
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBackground(new Color(248, 250, 252));
        cardsPanel.setBorder(new EmptyBorder(10, 8, 10, 8));

        if (slots.isEmpty()) {
            JPanel emptyCard = new JPanel(new BorderLayout());
            emptyCard.setBackground(Color.WHITE);
            emptyCard.setBorder(new CompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1, true),
                new EmptyBorder(30, 10, 30, 10)
            ));
            emptyCard.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));

            JLabel lblEmpty = new JLabel("No scheduled classes", SwingConstants.CENTER);
            lblEmpty.setFont(ModernTheme.FONT_SMALL);
            lblEmpty.setForeground(ModernTheme.TEXT_MUTED);
            emptyCard.add(lblEmpty, BorderLayout.CENTER);

            cardsPanel.add(emptyCard);
        } else {
            for (int i = 0; i < slots.size(); i++) {
                if (i > 0) {
                    cardsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
                cardsPanel.add(createSlotCard(slots.get(i), accentColor));
            }
        }

        cardsPanel.add(Box.createVerticalGlue());

        JScrollPane colScroll = new JScrollPane(cardsPanel);
        colScroll.setBorder(null);
        colScroll.setOpaque(false);
        colScroll.getViewport().setOpaque(false);
        colScroll.getVerticalScrollBar().setUnitIncrement(16);

        column.add(colScroll, BorderLayout.CENTER);
        return column;
    }

    private JPanel createSlotCard(Timetable t, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            new CompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)
            )
        ));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 165));

        // Time slot & Session Type Badge Row
        JPanel topRow = new JPanel(new BorderLayout(5, 0));
        topRow.setOpaque(false);

        String timeStr = DateUtil.formatTime(t.getStartTime()) + " - " + DateUtil.formatTime(t.getEndTime());
        JLabel timeLbl = new JLabel(timeStr);
        timeLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        timeLbl.setForeground(ModernTheme.PRIMARY_DARK);

        String sessionType = t.getSessionType() != null ? t.getSessionType() : "THEORY";
        boolean isPractical = "PRACTICAL".equalsIgnoreCase(sessionType);

        JLabel typeBadge = new JLabel(sessionType);
        typeBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        typeBadge.setOpaque(true);
        if (isPractical) {
            typeBadge.setBackground(new Color(236, 253, 245));
            typeBadge.setForeground(new Color(6, 95, 70));
            typeBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(167, 243, 208), 1, true),
                new EmptyBorder(2, 5, 2, 5)
            ));
        } else {
            typeBadge.setBackground(new Color(238, 242, 255));
            typeBadge.setForeground(new Color(49, 46, 129));
            typeBadge.setBorder(new CompoundBorder(
                new LineBorder(new Color(199, 210, 254), 1, true),
                new EmptyBorder(2, 5, 2, 5)
            ));
        }

        topRow.add(timeLbl, BorderLayout.WEST);
        topRow.add(typeBadge, BorderLayout.EAST);
        card.add(topRow);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        // Course Code
        JLabel codeLbl = new JLabel(t.getCourseCode() != null ? t.getCourseCode() : "N/A");
        codeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        codeLbl.setForeground(ModernTheme.PRIMARY);
        card.add(codeLbl);
        card.add(Box.createRigidArea(new Dimension(0, 2)));

        // Course Title
        JLabel nameLbl = new JLabel(t.getCourseName() != null ? t.getCourseName() : "");
        nameLbl.setFont(ModernTheme.FONT_REGULAR);
        nameLbl.setForeground(ModernTheme.TEXT_DARK);
        card.add(nameLbl);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // Divider line
        JSeparator sep = new JSeparator();
        sep.setForeground(ModernTheme.BORDER);
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        card.add(sep);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        // Hall / Lab Location
        String locationPrefix = isPractical ? "🧪 Lab: " : "🏛 Hall: ";
        JLabel roomLbl = new JLabel(locationPrefix + (t.getRoom() != null ? t.getRoom() : "TBA"));
        roomLbl.setFont(ModernTheme.FONT_SMALL);
        roomLbl.setForeground(ModernTheme.TEXT_DARK);
        card.add(roomLbl);
        card.add(Box.createRigidArea(new Dimension(0, 3)));

        // Lecturer in Charge
        JLabel lecLbl = new JLabel("👤 " + (t.getLecturerName() != null ? t.getLecturerName() : "TBA"));
        lecLbl.setFont(ModernTheme.FONT_SMALL);
        lecLbl.setForeground(ModernTheme.TEXT_MUTED);
        card.add(lecLbl);

        return card;
    }
}
