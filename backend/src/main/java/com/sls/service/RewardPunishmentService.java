package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.PageResult;
import com.sls.dto.RewardPunishmentVO;
import com.sls.repository.RewardPunishmentRepository;
import com.sls.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 奖惩管理（FR-02）
 */
@Service
public class RewardPunishmentService {

    private static final List<String> TYPES = List.of("奖励", "惩罚");
    private static final List<String> LEVELS = List.of("校级", "院级", "班级");

    private final RewardPunishmentRepository repository;
    private final StudentRepository studentRepository;

    public RewardPunishmentService(RewardPunishmentRepository repository, StudentRepository studentRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
    }

    public PageResult<RewardPunishmentVO> page(String studentNo, String type, String level,
                                             String startDate, String endDate, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        int offset = (page - 1) * pageSize;
        long total = repository.count(studentNo, type, level, start, end);
        return new PageResult<>(repository.findPage(studentNo, type, level, start, end, offset, pageSize),
                total, page, pageSize);
    }

    public void create(String studentNo, String type, String level, String reason,
                       String occurDate, String recorder) {
        if (studentRepository.findByStudentNo(studentNo) == null) {
            throw new BusinessException("学生不存在");
        }
        validate(type, level, reason, occurDate);
        String recordNo = "RP" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        repository.insert(recordNo, studentNo, type, level, reason.trim(),
                LocalDate.parse(occurDate), recorder);
    }

    public void update(String recordNo, String type, String level, String reason, String occurDate) {
        RewardPunishmentVO existing = repository.findByNo(recordNo);
        if (existing == null) throw new BusinessException("记录不存在");
        if (existing.isArchived()) throw new BusinessException("已归档记录不可修改");
        validate(type, level, reason, occurDate);
        repository.update(recordNo, type, level, reason.trim(), LocalDate.parse(occurDate));
    }

    public void archive(String recordNo) {
        RewardPunishmentVO existing = repository.findByNo(recordNo);
        if (existing == null) throw new BusinessException("记录不存在");
        repository.archive(recordNo);
    }

    private void validate(String type, String level, String reason, String occurDate) {
        if (type == null || !TYPES.contains(type)) throw new BusinessException("类型须为奖励或惩罚");
        if (level == null || !LEVELS.contains(level)) throw new BusinessException("级别不合法");
        if (reason == null || reason.isBlank()) throw new BusinessException("原因不能为空");
        if (occurDate == null || occurDate.isBlank()) throw new BusinessException("发生日期不能为空");
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s);
    }
}
