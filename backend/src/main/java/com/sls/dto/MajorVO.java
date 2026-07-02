package com.sls.dto;

/**
 * 专业视图
 */
public class MajorVO {
    private String majorCode;
    private String majorName;
    private String collegeCode;
    private String collegeName;
    private String status;

    public String getMajorCode() { return majorCode; }
    public void setMajorCode(String majorCode) { this.majorCode = majorCode; }
    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }
    public String getCollegeCode() { return collegeCode; }
    public void setCollegeCode(String collegeCode) { this.collegeCode = collegeCode; }
    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
