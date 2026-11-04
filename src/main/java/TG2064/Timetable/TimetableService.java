package com.fot.ams.service;

import com.fot.ams.dao.TimetableDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Timetable;
import com.fot.ams.util.ValidationUtil;

import java.util.List;

/**
 * Service managing timetable schedules.
 */
public class TimetableService {
    private final TimetableDAO timetableDAO;

    public TimetableService() {
        this.timetableDAO = new TimetableDAO();
    }

    public TimetableService(TimetableDAO timetableDAO) {
        this.timetableDAO = timetableDAO;
    }

    public List<Timetable> getAllTimetables() {
        return timetableDAO.getAllTimetables();
    }

    public List<Timetable> getTimetablesByDepartment(int departmentId) {
        return timetableDAO.getTimetablesByDepartment(departmentId);
    }

    public boolean addTimetable(Timetable tt) throws ValidationException {
        ValidationUtil.validatePositive(tt.getDepartmentId(), "Department");
        ValidationUtil.validatePositive(tt.getCourseId(), "Course");
        ValidationUtil.validateNotEmpty(tt.getDayOfWeek(), "Day of Week");
        ValidationUtil.validateNotEmpty(tt.getRoom(), "Room / Hall");
        if (tt.getStartTime() == null || tt.getEndTime() == null) {
            throw new ValidationException("Start time and end time are required.");
        }
        if (!tt.getStartTime().before(tt.getEndTime())) {
            throw new ValidationException("Start time must be before end time.");
        }
        return timetableDAO.addTimetable(tt);
    }

    public boolean updateTimetable(Timetable tt) throws ValidationException {
        ValidationUtil.validatePositive(tt.getDepartmentId(), "Department");
        ValidationUtil.validatePositive(tt.getCourseId(), "Course");
        ValidationUtil.validateNotEmpty(tt.getDayOfWeek(), "Day of Week");
        ValidationUtil.validateNotEmpty(tt.getRoom(), "Room / Hall");
        if (tt.getStartTime() == null || tt.getEndTime() == null) {
            throw new ValidationException("Start time and end time are required.");
        }
        if (!tt.getStartTime().before(tt.getEndTime())) {
            throw new ValidationException("Start time must be before end time.");
        }
        return timetableDAO.updateTimetable(tt);
    }

    public boolean deleteTimetable(int timetableId) {
        return timetableDAO.deleteTimetable(timetableId);
    }
}
