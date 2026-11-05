package com.fot.ams.service;

import com.fot.ams.dao.MedicalDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Medical;
import com.fot.ams.util.ValidationUtil;

import javax.swing.*;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    public boolean submitMedicalReport(Medical medical, File pdfFile) {
        try {
            String uploadDir = "uploads/medical/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String destinationPath = uploadDir + System.currentTimeMillis() + "_" + pdfFile.getName();
            Files.copy(pdfFile.toPath(), new File(destinationPath).toPath(), StandardCopyOption.REPLACE_EXISTING);

            medical.setFilePath(destinationPath);
            return medicalDAO.insertMedical(medical);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Implemented download method with JFileChooser support
    public void downloadMedicalReport(int medicalId, Component parentComponent) {
        String filePath = medicalDAO.getMedicalReportPath(medicalId);

        if (filePath != null && !filePath.isEmpty()) {
            File sourceFile = new File(filePath);

            if (!sourceFile.exists()) {
                JOptionPane.showMessageDialog(parentComponent,
                        "The medical report file could not be found on the server/storage.",
                        "File Not Found",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(sourceFile.getName()));
            int userSelection = fileChooser.showSaveDialog(parentComponent);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File destinationFile = fileChooser.getSelectedFile();
                try {
                    Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    JOptionPane.showMessageDialog(parentComponent,
                            "Medical report downloaded successfully to:\n" + destinationFile.getAbsolutePath(),
                            "Download Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(parentComponent,
                            "Failed to download file: " + e.getMessage(),
                            "Download Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(parentComponent,
                    "No medical report uploaded for this record.",
                    "Missing Report",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean addMedical(Medical med) throws ValidationException {
        ValidationUtil.validatePositive(med.getStudentId(), "Student");
        ValidationUtil.validatePositive(med.getCourseId(), "Course");
        ValidationUtil.validateNotEmpty(med.getReason(), "Medical reason");

        if (med.getFromDate() == null || med.getFromDate().trim().isEmpty()) {
            throw new ValidationException("From date is required.");
        }
        if (med.getToDate() == null || med.getToDate().trim().isEmpty()) {
            throw new ValidationException("To date is required.");
        }
        if (med.getSubmissionDate() == null) {
            throw new ValidationException("Submission date is required.");
        }

        try {
            LocalDate fromDate = LocalDate.parse(med.getFromDate().trim());
            LocalDate toDate = LocalDate.parse(med.getToDate().trim());

            if (toDate.isBefore(fromDate)) {
                throw new ValidationException("To Date cannot be earlier than From Date.");
            }

            long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
            if (daysBetween > 14) {
                throw new ValidationException("The medical absence date range cannot exceed 2 weeks (14 days).");
            }
        } catch (java.time.format.DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use YYYY-MM-DD.");
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