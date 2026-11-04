package com.fot.ams.model;

/**
 * Represents an academic department within the Faculty of Technology.
 */
public class Department {
    private int departmentId;
    private String departmentCode;
    private String departmentName;
    private String description;

    public Department() {
    }

    public Department(int departmentId, String departmentCode, String departmentName, String description) {
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return departmentCode + " - " + departmentName;
    }
}
