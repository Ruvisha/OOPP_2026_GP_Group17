package com.fot.ams.ui.lecturer;

import com.fot.ams.ui.common.ModernTheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * Reusable user-friendly Date Picker and Time Picker component for Java Swing.
 * Features:
 * - Interactive Calendar popup with Month and Year navigation
 * - Day selection by clicking
 * - No manual date typing required
 * - Dropdown-based Time Picker (Hours 01-12, Minutes 00-59, AM/PM)
 * - Safe Timestamp and LocalDateTime conversion using Asia/Colombo timezone
 */
public class DateTimePickerPanel extends JPanel {

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JTextField txtDate;
    private final JButton btnCalendar;
    private final JComboBox<String> cmbHour;
    private final JComboBox<String> cmbMinute;
    private final JComboBox<String> cmbAmPm;

    private LocalDate selectedDate = null;

    public DateTimePickerPanel() {
        this(null);
    }

    public DateTimePickerPanel(Timestamp initialTimestamp) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setOpaque(false);

        // Date Display Field (Read-only to prevent manual typing errors)
        txtDate = new JTextField(10);
        txtDate.setEditable(false);
        txtDate.setBackground(Color.WHITE);
        txtDate.setFont(ModernTheme.FONT_REGULAR);
        txtDate.setBorder(new CompoundBorder(
                new LineBorder(ModernTheme.BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)));
        txtDate.setToolTipText("Selected Date (Click Calendar button to pick)");

        // Calendar Button
        btnCalendar = new JButton("\uD83D\uDCC5 Pick"); // 📅
        btnCalendar.setFont(ModernTheme.FONT_BOLD);
        btnCalendar.setBackground(ModernTheme.PRIMARY);
        btnCalendar.setForeground(Color.WHITE);
        btnCalendar.setFocusPainted(false);
        btnCalendar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCalendar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnCalendar.setToolTipText("Open Calendar to select date");
        btnCalendar.addActionListener(e -> openCalendarDialog());

        // Time Label
        JLabel lblTime = new JLabel(" Time:");
        lblTime.setFont(ModernTheme.FONT_BOLD);
        lblTime.setForeground(ModernTheme.TEXT_DARK);

        // Hours Dropdown (01 - 12)
        String[] hours = new String[12];
        for (int i = 1; i <= 12; i++) {
            hours[i - 1] = String.format("%02d", i);
        }
        cmbHour = new JComboBox<>(hours);
        cmbHour.setFont(ModernTheme.FONT_REGULAR);
        cmbHour.setSelectedItem("09");
        cmbHour.setToolTipText("Select Hour (1 - 12)");

        JLabel lblColon = new JLabel(":");
        lblColon.setFont(ModernTheme.FONT_BOLD);

        // Minutes Dropdown (00 - 59)
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format("%02d", i);
        }
        cmbMinute = new JComboBox<>(minutes);
        cmbMinute.setFont(ModernTheme.FONT_REGULAR);
        cmbMinute.setSelectedItem("00");
        cmbMinute.setToolTipText("Select Minute (00 - 59)");

        // AM / PM Dropdown
        cmbAmPm = new JComboBox<>(new String[] { "AM", "PM" });
        cmbAmPm.setFont(ModernTheme.FONT_REGULAR);
        cmbAmPm.setSelectedItem("AM");
        cmbAmPm.setToolTipText("Select AM or PM");

        // Add components to panel
        add(txtDate);
        add(btnCalendar);
        add(lblTime);
        add(cmbHour);
        add(lblColon);
        add(cmbMinute);
        add(cmbAmPm);

        if (initialTimestamp != null) {
            setTimestamp(initialTimestamp);
        }
    }

    /**
     * Opens an interactive modal Calendar dialog for selecting date.
     */
    private void openCalendarDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "Select Date", true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, "Select Date", true);
        } else {
            dialog = new JDialog((Frame) null, "Select Date", true);
        }

        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setSize(380, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        LocalDate initial = selectedDate != null ? selectedDate : LocalDate.now(COLOMBO_ZONE);
        final int[] viewYear = { initial.getYear() };
        final int[] viewMonth = { initial.getMonthValue() }; // 1 - 12

        // --- Navigation Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBackground(ModernTheme.BG_CARD);
        headerPanel.setBorder(new EmptyBorder(8, 8, 4, 8));

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        navLeft.setOpaque(false);
        JButton btnPrevYear = createNavButton("<<", "Previous Year");
        JButton btnPrevMonth = createNavButton("<", "Previous Month");
        navLeft.add(btnPrevYear);
        navLeft.add(btnPrevMonth);

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        navRight.setOpaque(false);
        JButton btnNextMonth = createNavButton(">", "Next Month");
        JButton btnNextYear = createNavButton(">>", "Next Year");
        navRight.add(btnNextMonth);
        navRight.add(btnNextYear);

        JPanel centerSelectors = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        centerSelectors.setOpaque(false);

        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        JComboBox<String> cmbMonthSelect = new JComboBox<>(monthNames);
        cmbMonthSelect.setFont(ModernTheme.FONT_BOLD);
        cmbMonthSelect.setSelectedIndex(viewMonth[0] - 1);

        Integer[] yearOptions = new Integer[25];
        int startYear = 2022;
        for (int i = 0; i < yearOptions.length; i++) {
            yearOptions[i] = startYear + i;
        }
        JComboBox<Integer> cmbYearSelect = new JComboBox<>(yearOptions);
        cmbYearSelect.setFont(ModernTheme.FONT_BOLD);
        cmbYearSelect.setSelectedItem(viewYear[0]);

        centerSelectors.add(cmbMonthSelect);
        centerSelectors.add(cmbYearSelect);

        headerPanel.add(navLeft, BorderLayout.WEST);
        headerPanel.add(centerSelectors, BorderLayout.CENTER);
        headerPanel.add(navRight, BorderLayout.EAST);

        // --- Calendar Days Grid ---
        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.setBackground(Color.WHITE);
        calendarContainer.setBorder(new LineBorder(ModernTheme.BORDER, 1));

        // Days of Week Header
        JPanel daysOfWeekHeader = new JPanel(new GridLayout(1, 7, 2, 2));
        daysOfWeekHeader.setBackground(ModernTheme.PRIMARY_LIGHT);
        String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (String dayName : dayNames) {
            JLabel lblDay = new JLabel(dayName, SwingConstants.CENTER);
            lblDay.setFont(ModernTheme.FONT_BOLD);
            lblDay.setForeground(ModernTheme.PRIMARY_DARK);
            lblDay.setBorder(new EmptyBorder(4, 2, 4, 2));
            daysOfWeekHeader.add(lblDay);
        }
        calendarContainer.add(daysOfWeekHeader, BorderLayout.NORTH);

        // Days Grid (6 weeks x 7 days)
        JPanel gridPanel = new JPanel(new GridLayout(6, 7, 2, 2));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        calendarContainer.add(gridPanel, BorderLayout.CENTER);

        // Refresh Grid Runnable
        final boolean[] isUpdatingSelectors = { false };
        Runnable refreshDays = () -> {
            gridPanel.removeAll();
            LocalDate firstDayOfMonth = LocalDate.of(viewYear[0], viewMonth[0], 1);
            int daysInMonth = firstDayOfMonth.lengthOfMonth();

            // Java DayOfWeek: 1 (Mon) to 7 (Sun)
            // Swing grid starts on Sunday: 0 (Sun) to 6 (Sat)
            int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // Sunday becomes 0

            LocalDate today = LocalDate.now(COLOMBO_ZONE);

            // Previous month trailing days
            LocalDate prevMonth = firstDayOfMonth.minusMonths(1);
            int prevMonthDays = prevMonth.lengthOfMonth();
            for (int i = 0; i < startDayOfWeek; i++) {
                int dayNum = prevMonthDays - startDayOfWeek + i + 1;
                JLabel lblEmpty = new JLabel(String.valueOf(dayNum), SwingConstants.CENTER);
                lblEmpty.setFont(ModernTheme.FONT_SMALL);
                lblEmpty.setForeground(new Color(203, 213, 225)); // Slate 300
                gridPanel.add(lblEmpty);
            }

            // Current month days
            for (int d = 1; d <= daysInMonth; d++) {
                final int currentDay = d;
                LocalDate thisDate = LocalDate.of(viewYear[0], viewMonth[0], currentDay);
                JButton btnDay = new JButton(String.valueOf(currentDay));
                btnDay.setFont(ModernTheme.FONT_REGULAR);
                btnDay.setMargin(new Insets(2, 2, 2, 2));
                btnDay.setFocusPainted(false);
                btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));

                boolean isSelected = (selectedDate != null && selectedDate.equals(thisDate));
                boolean isToday = thisDate.equals(today);

                if (isSelected) {
                    btnDay.setBackground(ModernTheme.PRIMARY);
                    btnDay.setForeground(Color.WHITE);
                    btnDay.setFont(ModernTheme.FONT_BOLD);
                } else if (isToday) {
                    btnDay.setBackground(ModernTheme.PRIMARY_LIGHT);
                    btnDay.setForeground(ModernTheme.PRIMARY);
                    btnDay.setBorder(new LineBorder(ModernTheme.ACCENT, 1));
                    btnDay.setFont(ModernTheme.FONT_BOLD);
                } else {
                    btnDay.setBackground(Color.WHITE);
                    btnDay.setForeground(ModernTheme.TEXT_DARK);
                    btnDay.setBorder(new LineBorder(new Color(241, 245, 249), 1));
                }

                btnDay.addActionListener(ev -> {
                    selectedDate = thisDate;
                    txtDate.setText(selectedDate.format(DATE_FORMATTER));
                    dialog.dispose();
                });

                gridPanel.add(btnDay);
            }

            // Next month leading days to fill 42 cells
            int totalFilled = startDayOfWeek + daysInMonth;
            int remaining = 42 - totalFilled;
            for (int i = 1; i <= remaining; i++) {
                JLabel lblNext = new JLabel(String.valueOf(i), SwingConstants.CENTER);
                lblNext.setFont(ModernTheme.FONT_SMALL);
                lblNext.setForeground(new Color(203, 213, 225));
                gridPanel.add(lblNext);
            }

            if (!isUpdatingSelectors[0]) {
                isUpdatingSelectors[0] = true;
                cmbMonthSelect.setSelectedIndex(viewMonth[0] - 1);
                cmbYearSelect.setSelectedItem(viewYear[0]);
                isUpdatingSelectors[0] = false;
            }

            gridPanel.revalidate();
            gridPanel.repaint();
        };

        // Navigation Actions
        btnPrevMonth.addActionListener(ev -> {
            if (viewMonth[0] == 1) {
                viewMonth[0] = 12;
                viewYear[0]--;
            } else {
                viewMonth[0]--;
            }
            refreshDays.run();
        });

        btnNextMonth.addActionListener(ev -> {
            if (viewMonth[0] == 12) {
                viewMonth[0] = 1;
                viewYear[0]++;
            } else {
                viewMonth[0]++;
            }
            refreshDays.run();
        });

        btnPrevYear.addActionListener(ev -> {
            viewYear[0]--;
            refreshDays.run();
        });

        btnNextYear.addActionListener(ev -> {
            viewYear[0]++;
            refreshDays.run();
        });

        cmbMonthSelect.addActionListener(ev -> {
            if (isUpdatingSelectors[0])
                return;
            viewMonth[0] = cmbMonthSelect.getSelectedIndex() + 1;
            refreshDays.run();
        });

        cmbYearSelect.addActionListener(ev -> {
            if (isUpdatingSelectors[0])
                return;
            Integer y = (Integer) cmbYearSelect.getSelectedItem();
            if (y != null) {
                viewYear[0] = y;
                refreshDays.run();
            }
        });

        // --- Footer Panel ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footer.setBackground(ModernTheme.BG_CARD);

        JButton btnToday = ModernTheme.createSecondaryButton("Today");
        btnToday.addActionListener(ev -> {
            selectedDate = LocalDate.now(COLOMBO_ZONE);
            txtDate.setText(selectedDate.format(DATE_FORMATTER));
            dialog.dispose();
        });

        JButton btnClear = ModernTheme.createSecondaryButton("Clear");
        btnClear.addActionListener(ev -> {
            clear();
            dialog.dispose();
        });

        JButton btnCancel = ModernTheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(ev -> dialog.dispose());

        footer.add(btnToday);
        footer.add(btnClear);
        footer.add(btnCancel);

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(calendarContainer, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);

        refreshDays.run();
        dialog.setVisible(true);
    }

    private JButton createNavButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(ModernTheme.FONT_BOLD);
        btn.setBackground(ModernTheme.BG_CARD);
        btn.setForeground(ModernTheme.PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(ModernTheme.BORDER, 1), new EmptyBorder(3, 8, 3, 8)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        return btn;
    }

    /**
     * Converts selected Date & Time into a JDBC java.sql.Timestamp (Asia/Colombo).
     * Returns null if no date is selected.
     */
    public Timestamp getTimestamp() {
        if (selectedDate == null || txtDate.getText().trim().isEmpty()) {
            return null;
        }

        int h12 = Integer.parseInt((String) cmbHour.getSelectedItem());
        int minute = Integer.parseInt((String) cmbMinute.getSelectedItem());
        String amPm = (String) cmbAmPm.getSelectedItem();

        int hour24 = (h12 % 12);
        if ("PM".equalsIgnoreCase(amPm)) {
            hour24 += 12;
        }

        LocalTime time = LocalTime.of(hour24, minute);
        LocalDateTime ldt = LocalDateTime.of(selectedDate, time);
        ZonedDateTime zdt = ldt.atZone(COLOMBO_ZONE);

        return Timestamp.from(zdt.toInstant());
    }

    /**
     * Returns the selected Date & Time as LocalDateTime, or null if no date
     * selected.
     */
    public LocalDateTime getLocalDateTime() {
        Timestamp ts = getTimestamp();
        if (ts == null)
            return null;
        return ts.toInstant().atZone(COLOMBO_ZONE).toLocalDateTime();
    }

    /**
     * Sets the picker values from a java.sql.Timestamp using Asia/Colombo timezone.
     */
    public void setTimestamp(Timestamp ts) {
        if (ts == null) {
            clear();
            return;
        }

        ZonedDateTime zdt = ts.toInstant().atZone(COLOMBO_ZONE);
        selectedDate = zdt.toLocalDate();
        txtDate.setText(selectedDate.format(DATE_FORMATTER));

        int hour24 = zdt.getHour();
        int minute = zdt.getMinute();

        String amPm = hour24 >= 12 ? "PM" : "AM";
        int hour12 = hour24 % 12;
        if (hour12 == 0)
            hour12 = 12;

        cmbHour.setSelectedItem(String.format("%02d", hour12));
        cmbMinute.setSelectedItem(String.format("%02d", minute));
        cmbAmPm.setSelectedItem(amPm);
    }

    /**
     * Sets the picker values from a LocalDateTime.
     */
    public void setLocalDateTime(LocalDateTime ldt) {
        if (ldt == null) {
            clear();
            return;
        }
        ZonedDateTime zdt = ldt.atZone(COLOMBO_ZONE);
        setTimestamp(Timestamp.from(zdt.toInstant()));
    }

    /**
     * Returns true if a date has been selected.
     */
    public boolean hasValue() {
        return selectedDate != null && !txtDate.getText().trim().isEmpty();
    }

    /**
     * Clears selected date and resets time to default (09:00 AM).
     */
    public void clear() {
        selectedDate = null;
        txtDate.setText("");
        cmbHour.setSelectedItem("09");
        cmbMinute.setSelectedItem("00");
        cmbAmPm.setSelectedItem("AM");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        btnCalendar.setEnabled(enabled);
        cmbHour.setEnabled(enabled);
        cmbMinute.setEnabled(enabled);
        cmbAmPm.setEnabled(enabled);
    }
}
