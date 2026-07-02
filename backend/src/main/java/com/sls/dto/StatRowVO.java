package com.sls.dto;

/**
 * 统计报表行
 */
public class StatRowVO {
    private String groupKey;
    private String groupName;
    private Long count;
    private Double avgScore;
    private Double passRate;

    public String getGroupKey() { return groupKey; }
    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
    public Double getAvgScore() { return avgScore; }
    public void setAvgScore(Double avgScore) { this.avgScore = avgScore; }
    public Double getPassRate() { return passRate; }
    public void setPassRate(Double passRate) { this.passRate = passRate; }
}
