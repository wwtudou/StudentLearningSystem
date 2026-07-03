package com.sls.service;

import com.sls.dto.ExplainRowVO;
import com.sls.dto.GradeChangeLogVO;
import com.sls.dto.WindowStatVO;
import com.sls.repository.DbTechRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 高阶数据库技术演示服务
 */
@Service
public class DbTechService {

    private final DbTechRepository dbTechRepository;

    public DbTechService(DbTechRepository dbTechRepository) {
        this.dbTechRepository = dbTechRepository;
    }

    public List<WindowStatVO> majorRank(String semesterCode) {
        return dbTechRepository.majorRank(semesterCode);
    }

    public List<WindowStatVO> cumulativeCredit() {
        return dbTechRepository.cumulativeCredit();
    }

    public List<GradeChangeLogVO> gradeChangeLogs(String studentNo) {
        return dbTechRepository.gradeChangeLogs(studentNo);
    }

    public List<ExplainRowVO> explainAuditPartition(int year) {
        return dbTechRepository.explainPartitionAudit(year);
    }

    public List<ExplainRowVO> explainGradeIndex(String offeringNo) {
        return dbTechRepository.explainGradeByOffering(offeringNo);
    }

    public List<ExplainRowVO> explainEnrollmentIndex(String offeringNo) {
        return dbTechRepository.explainEnrollmentByOffering(offeringNo);
    }
}
