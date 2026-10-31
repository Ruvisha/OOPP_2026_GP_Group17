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

        if (courseDAO.isCourseCodeTaken(course.getCourseCode().trim(), 0)) {
            throw new ValidationException("Course Code '" + course.getCourseCode() + "' is already registered.");
        }

        return courseDAO.addCourse(course);
    }

    public boolean updateCourse(Course course) throws ValidationException {
        ValidationUtil.validateNotEmpty(course.getCourseCode(), "Course Code");
        ValidationUtil.validateNotEmpty(course.getCourseName(), "Course Name");
        ValidationUtil.validatePositive(course.getCredits(), "Credits");

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

    public boolean deleteMaterial(int materialId) {
        return materialDAO.deleteMaterial(materialId);
    }

    // Enrollment Management
    public List<CourseEnrollment> getEnrollmentsByCourse(int courseId) {
        return enrollmentDAO.getEnrollmentsByCourse(courseId);
    }

    public boolean enrollStudent(int studentId, int courseId, String academicYear, int semester, boolean isRepeat) {
        return enrollmentDAO.enrollStudent(studentId, courseId, academicYear, semester, isRepeat);
    }
}
