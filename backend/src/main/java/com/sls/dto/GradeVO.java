package com.sls.dto;

import java.math.BigDecimal;

/**
 * 成绩视图
 */
public class GradeVO {
    private String studentNo;
    private String studentName;
    private String offeringNo;
    private String courseName;
    private String semesterName;
    private BigDecimal totalScore;
    private String examType;
    private boolean locked;
    private Integer academicYear;

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getOfferingNo() { return offeringNo; }
    public void setOfferingNo(String offeringNo) { this.offeringNo = offeringNo; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }
}
