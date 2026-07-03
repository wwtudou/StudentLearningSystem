package com.sls.dto;

/**
 * EXPLAIN 结果行（DB-Tech-04 / DB-Tech-07 演示）
 */
public class ExplainRowVO {
    private String id;
    private String selectType;
    private String tableName;
    private String partitions;
    private String type;
    private String possibleKeys;
    private String key;
    private String rows;
    private String extra;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSelectType() { return selectType; }
    public void setSelectType(String selectType) { this.selectType = selectType; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getPartitions() { return partitions; }
    public void setPartitions(String partitions) { this.partitions = partitions; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPossibleKeys() { return possibleKeys; }
    public void setPossibleKeys(String possibleKeys) { this.possibleKeys = possibleKeys; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getRows() { return rows; }
    public void setRows(String rows) { this.rows = rows; }
    public String getExtra() { return extra; }
    public void setExtra(String extra) { this.extra = extra; }
}
