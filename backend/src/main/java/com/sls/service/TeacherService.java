package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.PageResult;
import com.sls.dto.TeacherVO;
import com.sls.repository.OrgRepository;
import com.sls.repository.TeacherRepository;
import org.springframework.stereotype.Service;

/**
 * 教师管理
 */
@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final OrgRepository orgRepository;

    public TeacherService(TeacherRepository teacherRepository, OrgRepository orgRepository) {
        this.teacherRepository = teacherRepository;
        this.orgRepository = orgRepository;
    }

    public PageResult<TeacherVO> page(String teacherNo, String name, String collegeCode, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = teacherRepository.count(teacherNo, name, collegeCode);
        return new PageResult<>(teacherRepository.findPage(teacherNo, name, collegeCode, offset, pageSize),
                total, page, pageSize);
    }

    public void create(String teacherNo, String name, String collegeCode) {
        if (teacherNo == null || teacherNo.isBlank()) throw new BusinessException("工号不能为空");
        if (name == null || name.isBlank()) throw new BusinessException("姓名不能为空");
        if (orgRepository.findCollege(collegeCode) == null) throw new BusinessException("学院不存在");
        if (teacherRepository.exists(teacherNo.trim())) throw new BusinessException("工号已存在");
        teacherRepository.insert(teacherNo.trim(), name.trim(), collegeCode);
    }

    public void update(String teacherNo, String name, String collegeCode, String status) {
        if (teacherRepository.findByNo(teacherNo) == null) throw new BusinessException("教师不存在");
        if (orgRepository.findCollege(collegeCode) == null) throw new BusinessException("学院不存在");
        if (status == null || (!status.equals("在职") && !status.equals("停用"))) {
            throw new BusinessException("状态须为在职或停用");
        }
        teacherRepository.update(teacherNo, name.trim(), collegeCode, status);
    }
}
