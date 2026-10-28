// ==========================================
// 14. SERVICE: TimetableService.java
// ==========================================
import java.sql.SQLException;
import java.util.List;

public class TimetableService {
    private final TimetableDAO timetableDAO = new TimetableDAO();

    public boolean addSlot(Timetable timetable) throws Exception {
        boolean conflict = timetableDAO.checkConflict(
                timetable.getDayOfWeek(),
                timetable.getRoom(),
                timetable.getStartTime(),
                timetable.getEndTime()
        );

        if (conflict) {
            throw new Exception("Timetable Conflict Error: Room " + timetable.getRoom() +
                    " is already booked on " + timetable.getDayOfWeek() + " for the selected time.");
        }
        return timetableDAO.addTimetableSlot(timetable);
    }

    public List<Timetable> getTimetable() throws SQLException {
        return timetableDAO.getAllTimetables();
    }
}
