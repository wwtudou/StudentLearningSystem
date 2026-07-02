package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.CollegeVO;
import com.sls.dto.MajorVO;
import com.sls.dto.OptionVO;
import com.sls.repository.OrgRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 院系专业管理（FR-03）
 */
@Service
public class OrgService {

    private final OrgRepository orgRepository;

    public OrgService(OrgRepository orgRepository) {
        this.orgRepository = orgRepository;
    }

    public List<CollegeVO> tree() {
        List<CollegeVO> colleges = orgRepository.listColleges(null);
        for (CollegeVO c : colleges) {
            c.setMajors(orgRepository.listMajors(c.getCollegeCode(), null));
        }
        return colleges;
    }

    public List<CollegeVO> listColleges() {
        return orgRepository.listColleges(null);
    }

    public void createCollege(String code, String name) {
        validateCode(code);
        if (name == null || name.isBlank()) throw new BusinessException("学院名称不能为空");
        if (orgRepository.collegeExists(code)) throw new BusinessException("学院编号已存在");
        orgRepository.insertCollege(code.trim(), name.trim());
    }

    public void updateCollege(String code, String name, String status) {
        if (orgRepository.findCollege(code) == null) throw new BusinessException("学院不存在");
        validateStatus(status);
        orgRepository.updateCollege(code, name.trim(), status);
    }

    public List<MajorVO> listMajors(String collegeCode) {
        return orgRepository.listMajors(collegeCode, null);
    }

    public void createMajor(String code, String name, String collegeCode) {
        validateCode(code);
        if (name == null || name.isBlank()) throw new BusinessException("专业名称不能为空");
        if (orgRepository.findCollege(collegeCode) == null) throw new BusinessException("学院不存在");
        if (orgRepository.majorExists(code)) throw new BusinessException("专业编号已存在");
        orgRepository.insertMajor(code.trim(), name.trim(), collegeCode);
    }

    public void updateMajor(String code, String name, String collegeCode, String status) {
        if (orgRepository.findMajor(code) == null) throw new BusinessException("专业不存在");
        if (orgRepository.findCollege(collegeCode) == null) throw new BusinessException("学院不存在");
        validateStatus(status);
        orgRepository.updateMajor(code, name.trim(), collegeCode, status);
    }

    public List<OptionVO> collegeOptions() {
        return orgRepository.collegeOptions();
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) throw new BusinessException("编号不能为空");
    }

    private void validateStatus(String status) {
        if (status == null || (!status.equals("启用") && !status.equals("停用"))) {
            throw new BusinessException("状态须为启用或停用");
        }
    }
}
