package com.sls.dto;

import java.util.List;

/**
 * 学院视图（含下属专业）
 */
public class CollegeVO {
    private String collegeCode;
    private String collegeName;
    private String status;
    private List<MajorVO> majors;

    public String getCollegeCode() { return collegeCode; }
    public void setCollegeCode(String collegeCode) { this.collegeCode = collegeCode; }
    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<MajorVO> getMajors() { return majors; }
    public void setMajors(List<MajorVO> majors) { this.majors = majors; }
}
