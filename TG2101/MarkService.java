package com.fot.ams.service;

import com.fot.ams.dao.EvaluationDAO;
import com.fot.ams.dao.MarkDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Evaluation;
import com.fot.ams.model.Mark;
import com.fot.ams.util.ValidationUtil;

import java.util.List;

/**
 * Service managing evaluation definitions and student marks.
 */
public class MarkService {
    private final EvaluationDAO evaluationDAO;
    private final MarkDAO markDAO;

    public MarkService() {
        this.evaluationDAO = new EvaluationDAO();
        this.markDAO = new MarkDAO();
    }

    public MarkService(EvaluationDAO evaluationDAO, MarkDAO markDAO) {
        this.evaluationDAO = evaluationDAO;
        this.markDAO = markDAO;
    }

    // Evaluation definitions
    public List<Evaluation> getEvaluationsByCourse(int courseId) {
        return evaluationDAO.getEvaluationsByCourse(courseId);
    }

    public boolean addEvaluation(Evaluation eval) throws ValidationException {
        ValidationUtil.validateNotEmpty(eval.getEvaluationName(), "Evaluation Name");
        ValidationUtil.validatePositive(eval.getCourseId(), "Course");
        if (eval.getWeightagePercent() <= 0 || eval.getWeightagePercent() > 100) {
            throw new ValidationException("Weightage must be between 1% and 100%.");
        }
        return evaluationDAO.addEvaluation(eval);
    }

    public boolean updateEvaluation(Evaluation eval) throws ValidationException {
        ValidationUtil.validateNotEmpty(eval.getEvaluationName(), "Evaluation Name");
        if (eval.getWeightagePercent() <= 0 || eval.getWeightagePercent() > 100) {
            throw new ValidationException("Weightage must be between 1% and 100%.");
        }
        return evaluationDAO.updateEvaluation(eval);
    }

    public boolean deleteEvaluation(int evaluationId) {
        return evaluationDAO.deleteEvaluation(evaluationId);
    }

    // Marks Management
    public boolean saveMark(Mark mark) throws ValidationException {
        ValidationUtil.validatePositive(mark.getEvaluationId(), "Evaluation");
        ValidationUtil.validatePositive(mark.getStudentId(), "Student");
        ValidationUtil.validateMark(mark.getMarksObtained());
        return markDAO.saveOrUpdateMark(mark);
    }

    public boolean batchSaveMarks(List<Mark> marks) throws ValidationException {
        // Check all inputs before asking the DAO to write the batch transaction.
        for (Mark m : marks) {
            ValidationUtil.validateMark(m.getMarksObtained());
        }
        return markDAO.batchSaveMarks(marks);
    }

    public List<Mark> getMarksByEvaluation(int evaluationId) {
        return markDAO.getMarksByEvaluation(evaluationId);
    }

    public List<Mark> getMarksByStudentAndCourse(int studentId, int courseId) {
        return markDAO.getMarksByStudentAndCourse(studentId, courseId);
    }

    public double calculateCaMark(int studentId, int courseId) {
        return markDAO.calculateCaMark(studentId, courseId);
    }

    public double calculateFinalExamMark(int studentId, int courseId) {
        return markDAO.calculateFinalExamMark(studentId, courseId);
    }
}
