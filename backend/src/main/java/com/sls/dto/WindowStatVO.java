package com.sls.dto;

import java.math.BigDecimal;

/**
 * 窗口函数统计行（DB-Tech-06）
 */
public class WindowStatVO {
    private String studentNo;
    private String studentName;
    private String majorName;
    private String courseName;
    private String semesterCode;
    private BigDecimal credit;
    private BigDecimal totalScore;
    private Integer majorRank;
    private BigDecimal cumulativeCredit;

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getSemesterCode() { return semesterCode; }
    public void setSemesterCode(String semesterCode) { this.semesterCode = semesterCode; }
    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public Integer getMajorRank() { return majorRank; }
    public void setMajorRank(Integer majorRank) { this.majorRank = majorRank; }
    public BigDecimal getCumulativeCredit() { return cumulativeCredit; }
    public void setCumulativeCredit(BigDecimal cumulativeCredit) { this.cumulativeCredit = cumulativeCredit; }
}
