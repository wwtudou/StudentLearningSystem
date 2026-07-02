package com.sls.dto;

/**
 * 下拉选项：code + name（学院、专业等）
 */
public class OptionVO {

    private String code;
    private String name;

    public OptionVO(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
