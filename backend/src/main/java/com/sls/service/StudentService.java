package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.PageResult;
import com.sls.dto.StudentRequest;
import com.sls.dto.StudentVO;
import com.sls.repository.StudentRepository;
import com.sls.security.AccessScope;
import com.sls.util.IdCardUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生信息业务逻辑层
 */
@Service
public class StudentService {

    private static final List<String> VALID_STATUS = List.of("在读", "休学", "毕业", "退学");
    private static final List<String> VALID_GENDER = List.of("男", "女");

    private final StudentRepository studentRepository;
    private final IdCardUtil idCardUtil;
    private final AccessScope accessScope;

    public StudentService(StudentRepository studentRepository, IdCardUtil idCardUtil, AccessScope accessScope) {
        this.studentRepository = studentRepository;
        this.idCardUtil = idCardUtil;
        this.accessScope = accessScope;
    }

    /** 分页查询，并为每条记录填充脱敏身份证 */
    public PageResult<StudentVO> page(String studentNo, String name, String collegeCode,
                                      String majorCode, String studentStatus,
                                      int page, int pageSize) {
        studentNo = accessScope.resolveStudentNoFilter(studentNo);
        collegeCode = accessScope.resolveCollegeFilter(collegeCode);
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = studentRepository.count(studentNo, name, collegeCode, majorCode, studentStatus, false);
        List<StudentVO> list = studentRepository.findPage(studentNo, name, collegeCode, majorCode,
                studentStatus, false, offset, pageSize);
        list.forEach(this::fillMaskedIdCard);
        return new PageResult<>(list, total, page, pageSize);
    }

    /** 按学号查询详情 */
    public StudentVO getByStudentNo(String studentNo) {
        accessScope.assertStudentAccess(studentNo);
        StudentVO vo = studentRepository.findByStudentNo(studentNo);
        if (vo == null) {
            throw new BusinessException("学生不存在");
        }
        fillMaskedIdCard(vo);
        return vo;
    }

    /** 新增学生 */
    public void create(StudentRequest req) {
        validateRequest(req, true);
        accessScope.assertCollegeScope(req.getCollegeCode());
        if (studentRepository.existsByStudentNo(req.getStudentNo())) {
            throw new BusinessException("学号已存在");
        }
        idCardUtil.validate(req.getIdCard());
        byte[] enc = idCardUtil.encrypt(req.getIdCard());
        studentRepository.insert(
                req.getStudentNo().trim(),
                req.getName().trim(),
                req.getCollegeCode(),
                req.getMajorCode(),
                req.getAge(),
                req.getGender(),
                enc,
                req.getEnrollYear(),
                req.getStudentStatus()
        );
    }

    /** 修改学生；身份证留空则保持原值 */
    public void update(String studentNo, StudentRequest req) {
        accessScope.assertStudentAccess(studentNo);
        if (studentRepository.findByStudentNo(studentNo) == null) {
            throw new BusinessException("学生不存在");
        }
        validateRequest(req, false);
        accessScope.assertCollegeScope(req.getCollegeCode());
        byte[] enc;
        if (req.getIdCard() != null && !req.getIdCard().isBlank()) {
            idCardUtil.validate(req.getIdCard());
            enc = idCardUtil.encrypt(req.getIdCard());
        } else {
            enc = studentRepository.findIdCardEncrypted(studentNo);
            if (enc == null) {
                throw new BusinessException("身份证号不能为空");
            }
        }
        studentRepository.update(
                studentNo,
                req.getName().trim(),
                req.getCollegeCode(),
                req.getMajorCode(),
                req.getAge(),
                req.getGender(),
                enc,
                req.getEnrollYear(),
                req.getStudentStatus()
        );
    }

    /** 逻辑删除；有选课记录则不允许删除 */
    public void delete(String studentNo) {
        accessScope.assertStudentAccess(studentNo);
        if (studentRepository.findByStudentNo(studentNo) == null) {
            throw new BusinessException("学生不存在");
        }
        if (studentRepository.countEnrollment(studentNo) > 0) {
            throw new BusinessException("该学生存在选课记录，无法删除");
        }
        studentRepository.logicalDelete(studentNo);
    }

    /** 校验请求参数 */
    private void validateRequest(StudentRequest req, boolean isCreate) {
        if (isCreate && (req.getStudentNo() == null || req.getStudentNo().isBlank())) {
            throw new BusinessException("学号不能为空");
        }
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BusinessException("姓名不能为空");
        }
        if (req.getCollegeCode() == null || req.getCollegeCode().isBlank()) {
            throw new BusinessException("请选择学院");
        }
        if (req.getMajorCode() == null || req.getMajorCode().isBlank()) {
            throw new BusinessException("请选择专业");
        }
        if (!studentRepository.majorBelongsToCollege(req.getMajorCode(), req.getCollegeCode())) {
            throw new BusinessException("所选专业不属于该学院");
        }
        if (req.getAge() == null || req.getAge() < 15 || req.getAge() > 50) {
            throw new BusinessException("年龄须在15到50之间");
        }
        if (req.getGender() == null || !VALID_GENDER.contains(req.getGender())) {
            throw new BusinessException("性别须为男或女");
        }
        if (isCreate && (req.getIdCard() == null || req.getIdCard().isBlank())) {
            throw new BusinessException("身份证号不能为空");
        }
        if (req.getEnrollYear() == null || req.getEnrollYear() < 2000 || req.getEnrollYear() > 2100) {
            throw new BusinessException("入学年份不合法");
        }
        if (req.getStudentStatus() == null || !VALID_STATUS.contains(req.getStudentStatus())) {
            throw new BusinessException("学籍状态不合法");
        }
    }

    /** 解密身份证并设置脱敏字段 */
    private void fillMaskedIdCard(StudentVO vo) {
        byte[] enc = studentRepository.findIdCardEncrypted(vo.getStudentNo());
        if (enc != null) {
            String plain = idCardUtil.decrypt(enc);
            vo.setIdCardMasked(idCardUtil.mask(plain));
        }
    }
}
