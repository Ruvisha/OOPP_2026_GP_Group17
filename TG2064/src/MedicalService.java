// ==========================================
// 12. SERVICE: MedicalService.java
// ==========================================
import java.sql.SQLException;
import java.util.List;

public class MedicalService {
    private final MedicalDAO medicalDAO = new MedicalDAO();

    public boolean submitMedical(Medical medical) throws SQLException {
        return medicalDAO.addMedical(medical);
    }

    public boolean approveMedical(int medicalId, int reviewerId) throws SQLException {
        return medicalDAO.updateMedicalStatus(medicalId, "APPROVED", reviewerId);
    }

    public boolean rejectMedical(int medicalId, int reviewerId) throws SQLException {
        return medicalDAO.updateMedicalStatus(medicalId, "NOT_APPROVED", reviewerId);
    }

    public List<Medical> getAllMedicals() throws SQLException {
        return medicalDAO.getAllMedicals();
    }

    public List<Medical> getStudentMedicals(String studentRegNo) throws SQLException {
        return medicalDAO.getMedicalsByStudent(studentRegNo);
    }
}
