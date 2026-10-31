package com.fot.ams.model;

import java.sql.Time;

/**
 * Represents a timetable slot entry for a department and course.
 */
public class Timetable {
    private int timetableId;
    private int departmentId;
    private String departmentCode;
    private String departmentName;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String dayOfWeek; // MONDAY, TUESDAY, etc.
    private Time startTime;
    private Time endTime;
    private String room;
    private Integer lecturerId;
    private String lecturerName;
    private String sessionType = "THEORY"; // THEORY, PRACTICAL
    private int semester = 1;
    private String academicYear = "2024/2025";

    public Timetable() {
    }

    public Timetable(int timetableId, int departmentId, int courseId, String dayOfWeek,
                     Time startTime, Time endTime, String room, Integer lecturerId,
                     String sessionType, int semester, String academicYear) {
        this.timetableId = timetableId;
        this.departmentId = departmentId;
        this.courseId = courseId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.lecturerId = lecturerId;
        this.sessionType = sessionType;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    public int getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(int timetableId) {
        this.timetableId = timetableId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Integer getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Integer lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime + "-" + endTime + " [" + courseCode + " - " + room + "]";
    }
}
