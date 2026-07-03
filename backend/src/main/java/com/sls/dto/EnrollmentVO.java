package com.sls.dto;

/**
 * 选课记录视图
 */
public class EnrollmentVO {
    private String studentNo;
    private String studentName;
    private String offeringNo;
    private String courseName;
    private String semesterName;
    private String teacherName;
    private String enrollTime;
    private boolean retake;
    /** 开课计划状态：开放选课 / 选课结束 / 已结束 等 */
    private String offeringStatus;

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
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getEnrollTime() { return enrollTime; }
    public void setEnrollTime(String enrollTime) { this.enrollTime = enrollTime; }
    public boolean isRetake() { return retake; }
    public void setRetake(boolean retake) { this.retake = retake; }
    public String getOfferingStatus() { return offeringStatus; }
    public void setOfferingStatus(String offeringStatus) { this.offeringStatus = offeringStatus; }
}
