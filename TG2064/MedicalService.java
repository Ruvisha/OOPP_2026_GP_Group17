package com.fot.ams.service;

import com.fot.ams.dao.MedicalDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Medical;
import com.fot.ams.util.ValidationUtil;

import java.util.List;

/**
 * Service managing medical certificates and records.
 */
public class MedicalService {
    private final MedicalDAO medicalDAO;

    public MedicalService() {
        this.medicalDAO = new MedicalDAO();
    }

    public MedicalService(MedicalDAO medicalDAO) {
        this.medicalDAO = medicalDAO;
    }

    public List<Medical> getAllMedicals() {
        return medicalDAO.getAllMedicals();
    }

    public List<Medical> getMedicalsByStudent(int studentId) {
        return medicalDAO.getMedicalsByStudent(studentId);
    }

    public List<Medical> getMedicalsByCourse(int courseId) {
        return medicalDAO.getMedicalsByCourse(courseId);
    }

    public boolean addMedical(Medical med) throws ValidationException {
        ValidationUtil.validatePositive(med.getStudentId(), "Student");
        ValidationUtil.validatePositive(med.getCourseId(), "Course");
        ValidationUtil.validateNotEmpty(med.getReason(), "Medical reason");
        if (med.getSessionDate() == null) {
            throw new ValidationException("Session date is required.");
        }
        if (med.getSubmissionDate() == null) {
            throw new ValidationException("Submission date is required.");
        }
        return medicalDAO.addMedical(med);
    }

    public boolean updateMedicalStatus(int medicalId, String status, int reviewerId, String remarks) {
        return medicalDAO.updateMedicalStatus(medicalId, status, reviewerId, remarks);
    }

    public boolean deleteMedical(int medicalId) {
        return medicalDAO.deleteMedical(medicalId);
    }
}
