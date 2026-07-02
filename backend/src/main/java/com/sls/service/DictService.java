package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.DictItemVO;
import com.sls.dto.DictTypeVO;
import com.sls.dto.OptionVO;
import com.sls.repository.DictRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典维护业务（FR-04）
 */
@Service
public class DictService {

    private final DictRepository dictRepository;

    public DictService(DictRepository dictRepository) {
        this.dictRepository = dictRepository;
    }

    public List<DictTypeVO> listTypes() {
        return dictRepository.listTypes();
    }

    public void createType(String typeCode, String typeName) {
        validateCode(typeCode);
        if (typeName == null || typeName.isBlank()) {
            throw new BusinessException("类型名称不能为空");
        }
        if (dictRepository.typeExists(typeCode)) {
            throw new BusinessException("类型编码已存在");
        }
        dictRepository.insertType(typeCode.trim(), typeName.trim());
    }

    public void updateType(String typeCode, String typeName) {
        if (!dictRepository.typeExists(typeCode)) {
            throw new BusinessException("字典类型不存在");
        }
        if (typeName == null || typeName.isBlank()) {
            throw new BusinessException("类型名称不能为空");
        }
        dictRepository.updateType(typeCode, typeName.trim());
    }

    public void deleteType(String typeCode) {
        if (!dictRepository.typeExists(typeCode)) {
            throw new BusinessException("字典类型不存在");
        }
        if (!dictRepository.listItems(typeCode).isEmpty()) {
            throw new BusinessException("该类型下仍有字典项，无法删除");
        }
        dictRepository.deleteType(typeCode);
    }

    public List<DictItemVO> listItems(String typeCode) {
        return dictRepository.listItems(typeCode);
    }

    public void createItem(String typeCode, String itemCode, String label) {
        if (!dictRepository.typeExists(typeCode)) {
            throw new BusinessException("字典类型不存在");
        }
        validateCode(itemCode);
        if (label == null || label.isBlank()) {
            throw new BusinessException("显示名称不能为空");
        }
        if (dictRepository.itemExists(typeCode, itemCode)) {
            throw new BusinessException("字典项编码已存在");
        }
        dictRepository.insertItem(typeCode, itemCode.trim(), label.trim());
    }

    public void updateItem(String typeCode, String itemCode, String label, String status) {
        if (!dictRepository.itemExists(typeCode, itemCode)) {
            throw new BusinessException("字典项不存在");
        }
        if (label == null || label.isBlank()) {
            throw new BusinessException("显示名称不能为空");
        }
        if (status == null || (!status.equals("启用") && !status.equals("停用"))) {
            throw new BusinessException("状态须为启用或停用");
        }
        dictRepository.updateItem(typeCode, itemCode, label.trim(), status);
    }

    public void deleteItem(String typeCode, String itemCode) {
        if (!dictRepository.itemExists(typeCode, itemCode)) {
            throw new BusinessException("字典项不存在");
        }
        dictRepository.deleteItem(typeCode, itemCode);
    }

    public List<OptionVO> options(String typeCode) {
        return dictRepository.listEnabledOptions(typeCode);
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("编码不能为空");
        }
    }
}
