package com.sls.dto;

/**
 * 成绩变更日志
 */
public class GradeChangeLogVO {
    private Long logNo;
    private String studentNo;
    private String offeringNo;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private String changedAt;

    public Long getLogNo() { return logNo; }
    public void setLogNo(Long logNo) { this.logNo = logNo; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getOfferingNo() { return offeringNo; }
    public void setOfferingNo(String offeringNo) { this.offeringNo = offeringNo; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public String getChangedAt() { return changedAt; }
    public void setChangedAt(String changedAt) { this.changedAt = changedAt; }
}
