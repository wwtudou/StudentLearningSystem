package com.sls.repository;

import com.sls.dto.DictItemVO;
import com.sls.dto.DictTypeVO;
import com.sls.dto.OptionVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典数据访问
 */
@Repository
public class DictRepository {

    private final JdbcTemplate jdbc;

    public DictRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DictTypeVO> listTypes() {
        return jdbc.query("SELECT type_code, type_name FROM dict_type ORDER BY type_code",
                (rs, rowNum) -> {
                    DictTypeVO vo = new DictTypeVO();
                    vo.setTypeCode(rs.getString("type_code"));
                    vo.setTypeName(rs.getString("type_name"));
                    return vo;
                });
    }

    public boolean typeExists(String typeCode) {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM dict_type WHERE type_code=?",
                Long.class, typeCode);
        return c != null && c > 0;
    }

    public void insertType(String typeCode, String typeName) {
        jdbc.update("INSERT INTO dict_type (type_code, type_name) VALUES (?, ?)", typeCode, typeName);
    }

    public void updateType(String typeCode, String typeName) {
        jdbc.update("UPDATE dict_type SET type_name=? WHERE type_code=?", typeName, typeCode);
    }

    public void deleteType(String typeCode) {
        jdbc.update("DELETE FROM dict_type WHERE type_code=?", typeCode);
    }

    public List<DictItemVO> listItems(String typeCode) {
        return jdbc.query(
                "SELECT type_code, item_code, label, status FROM dict_item WHERE type_code=? ORDER BY item_code",
                (rs, rowNum) -> {
                    DictItemVO vo = new DictItemVO();
                    vo.setTypeCode(rs.getString("type_code"));
                    vo.setItemCode(rs.getString("item_code"));
                    vo.setLabel(rs.getString("label"));
                    vo.setStatus(rs.getString("status"));
                    return vo;
                }, typeCode);
    }

    public boolean itemExists(String typeCode, String itemCode) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dict_item WHERE type_code=? AND item_code=?",
                Long.class, typeCode, itemCode);
        return c != null && c > 0;
    }

    public void insertItem(String typeCode, String itemCode, String label) {
        jdbc.update("INSERT INTO dict_item (type_code, item_code, label, status) VALUES (?, ?, ?, '启用')",
                typeCode, itemCode, label);
    }

    public void updateItem(String typeCode, String itemCode, String label, String status) {
        jdbc.update("UPDATE dict_item SET label=?, status=? WHERE type_code=? AND item_code=?",
                label, status, typeCode, itemCode);
    }

    public void deleteItem(String typeCode, String itemCode) {
        jdbc.update("DELETE FROM dict_item WHERE type_code=? AND item_code=?", typeCode, itemCode);
    }

    /** 按类型查询启用项，供下拉框使用 */
    public List<OptionVO> listEnabledOptions(String typeCode) {
        return jdbc.query(
                "SELECT item_code, label FROM dict_item WHERE type_code=? AND status='启用' ORDER BY item_code",
                (rs, rowNum) -> new OptionVO(rs.getString("item_code"), rs.getString("label")),
                typeCode);
    }
}
