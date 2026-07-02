package com.sls.dto;

/**
 * 教师视图
 */
public class TeacherVO {
    private String teacherNo;
    private String name;
    private String collegeCode;
    private String collegeName;
    private String status;

    public String getTeacherNo() { return teacherNo; }
    public void setTeacherNo(String teacherNo) { this.teacherNo = teacherNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCollegeCode() { return collegeCode; }
    public void setCollegeCode(String collegeCode) { this.collegeCode = collegeCode; }
    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
