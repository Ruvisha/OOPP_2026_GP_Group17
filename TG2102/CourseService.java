package com.fot.ams.service;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.CourseEnrollmentDAO;
import com.fot.ams.dao.CourseMaterialDAO;
import com.fot.ams.exception.ValidationException;
import com.fot.ams.model.Course;
import com.fot.ams.model.CourseEnrollment;
import com.fot.ams.model.CourseMaterial;
import com.fot.ams.util.ValidationUtil;

import java.util.List;

/**
 * Service managing courses, enrollments, and course materials.
 */
public class CourseService {
    private final CourseDAO courseDAO;
    private final CourseMaterialDAO materialDAO;
    private final CourseEnrollmentDAO enrollmentDAO;

    public CourseService() {
        this.courseDAO = new CourseDAO();
        this.materialDAO = new CourseMaterialDAO();
        this.enrollmentDAO = new CourseEnrollmentDAO();
    }

    public List<Course> getAllCourses() {
        return courseDAO.getAllCourses();
    }

    public Course getCourseById(int courseId) {
        return courseDAO.getCourseById(courseId);
    }

    public List<Course> getCoursesForStudent(int studentId) {
        return courseDAO.getCoursesForStudent(studentId);
    }

    public List<Course> getCoursesByDepartment(int departmentId) {
        return courseDAO.getCoursesByDepartment(departmentId);
    }

    public boolean addCourse(Course course) throws ValidationException {
        ValidationUtil.validateNotEmpty(course.getCourseCode(), "Course Code");
        ValidationUtil.validateNotEmpty(course.getCourseName(), "Course Name");
        ValidationUtil.validatePositive(course.getCredits(), "Credits");
        if (course.getStudyYear() < 1 || course.getStudyYear() > 4) throw new ValidationException("Study year must be between 1 and 4.");
        if (course.getSemester() < 1 || course.getSemester() > 2) throw new ValidationException("Semester must be 1 or 2.");
        if (course.getDepartmentId() <= 0 || course.getLecturerId() <= 0) throw new ValidationException("Select a valid department and lecturer.");

        if (courseDAO.isCourseCodeTaken(course.getCourseCode().trim(), 0)) {
            throw new ValidationException("Course Code '" + course.getCourseCode() + "' is already registered.");
        }

        return courseDAO.addCourse(course);
    }

    public boolean updateCourse(Course course) throws ValidationException {
        ValidationUtil.validateNotEmpty(course.getCourseCode(), "Course Code");
        ValidationUtil.validateNotEmpty(course.getCourseName(), "Course Name");
        ValidationUtil.validatePositive(course.getCredits(), "Credits");
        if (course.getStudyYear() < 1 || course.getStudyYear() > 4) throw new ValidationException("Study year must be between 1 and 4.");
        if (course.getSemester() < 1 || course.getSemester() > 2) throw new ValidationException("Semester must be 1 or 2.");
        if (course.getDepartmentId() <= 0 || course.getLecturerId() <= 0) throw new ValidationException("Select a valid department and lecturer.");

        if (courseDAO.isCourseCodeTaken(course.getCourseCode().trim(), course.getCourseId())) {
            throw new ValidationException("Course Code '" + course.getCourseCode() + "' is already registered.");
        }

        return courseDAO.updateCourse(course);
    }

    public boolean deleteCourse(int courseId) {
        return courseDAO.deleteCourse(courseId);
    }

    // Material Management
    public List<CourseMaterial> getAllMaterials() {
        return materialDAO.getAllMaterials();
    }

    public List<CourseMaterial> getMaterialsByLecturer(int lecturerId) { return materialDAO.getMaterialsByLecturer(lecturerId); }
    public List<CourseMaterial> getMaterialsByCourseAndLecturer(int courseId, int lecturerId) { return materialDAO.getMaterialsByCourseAndLecturer(courseId, lecturerId); }

    public List<CourseMaterial> getMaterialsByCourse(int courseId) {
        return materialDAO.getMaterialsByCourse(courseId);
    }

    public boolean addMaterial(CourseMaterial material) throws ValidationException {
        ValidationUtil.validateNotEmpty(material.getTitle(), "Material Title");
        ValidationUtil.validateNotEmpty(material.getFilePath(), "File Path");
        ValidationUtil.validatePositive(material.getCourseId(), "Course");
        return materialDAO.addMaterial(material);
    }

    public boolean updateMaterial(CourseMaterial material) throws ValidationException {
        ValidationUtil.validateNotEmpty(material.getTitle(), "Material Title");
        ValidationUtil.validateNotEmpty(material.getFilePath(), "File Path");
        return materialDAO.updateMaterial(material);
    }

    public boolean deleteMaterial(int materialId, int lecturerId) {
        return materialDAO.deleteMaterial(materialId, lecturerId);
    }

    // Enrollment Management
    public List<CourseEnrollment> getEnrollmentsByCourse(int courseId) {
        return enrollmentDAO.getEnrollmentsByCourse(courseId);
    }

    public boolean enrollStudent(int studentId, int courseId, String academicYear, int semester, boolean isRepeat) throws ValidationException {
        if (studentId <= 0 || courseId <= 0) throw new ValidationException("Select a valid student and course.");
        ValidationUtil.validateNotEmpty(academicYear, "Academic Year");
        if (!academicYear.matches("^\\d{4}$")) {
            throw new ValidationException("Academic year must be in simple 4-digit YYYY format (e.g. 2026).");
        }
        if (semester < 1 || semester > 2) throw new ValidationException("Semester must be 1 or 2.");
        if (enrollmentDAO.isStudentEnrolled(studentId, courseId, academicYear.trim(), semester))
            throw new ValidationException("This student is already enrolled in this course for the selected academic year and semester.");
        return enrollmentDAO.enrollStudent(studentId, courseId, academicYear.trim(), semester, isRepeat);
    }

    public boolean cancelEnrollment(int enrollmentId) { return enrollmentDAO.deleteEnrollment(enrollmentId); }

    public List<String> getAvailableAcademicYears() {
        return enrollmentDAO.getAvailableAcademicYears();
    }

    public String getActiveAcademicYear() {
        return enrollmentDAO.getActiveAcademicYear();
    }

    public boolean addAcademicYear(String year, boolean isActive) throws ValidationException {
        if (year == null || !year.matches("^\\d{4}$")) {
            throw new ValidationException("Academic year must be in simple 4-digit YYYY format (e.g. 2026).");
        }
        return enrollmentDAO.addAcademicYear(year, isActive);
    }
}
