package com.sls.dto;

/**
 * 奖惩记录视图
 */
public class RewardPunishmentVO {
    private String recordNo;
    private String studentNo;
    private String studentName;
    private String type;
    private String level;
    private String reason;
    private String occurDate;
    private String recorderUsername;
    private boolean archived;

    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOccurDate() { return occurDate; }
    public void setOccurDate(String occurDate) { this.occurDate = occurDate; }
    public String getRecorderUsername() { return recorderUsername; }
    public void setRecorderUsername(String recorderUsername) { this.recorderUsername = recorderUsername; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
