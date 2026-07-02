package com.sls.service;

import com.sls.dto.StatRowVO;
import com.sls.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 报表统计（FR-07）
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<StatRowVO> studentCount(String groupBy) {
        return reportRepository.studentCountBy(groupBy != null ? groupBy : "college");
    }

    public List<StatRowVO> enrollmentCount() {
        return reportRepository.enrollmentCount();
    }

    public List<StatRowVO> gradeStats(String offeringNo) {
        return reportRepository.gradeStats(offeringNo);
    }

    public List<StatRowVO> rewardStats() {
        return reportRepository.rewardStats();
    }
}
