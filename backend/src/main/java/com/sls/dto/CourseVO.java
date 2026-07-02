package com.sls.dto;

import java.math.BigDecimal;

/**
 * 课程视图
 */
public class CourseVO {
    private String courseCode;
    private String courseName;
    private BigDecimal credit;
    private Integer hours;
    private String collegeCode;
    private String collegeName;
    private String nature;
    private String status;

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }
    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }
    public String getCollegeCode() { return collegeCode; }
    public void setCollegeCode(String collegeCode) { this.collegeCode = collegeCode; }
    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public String getNature() { return nature; }
    public void setNature(String nature) { this.nature = nature; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
