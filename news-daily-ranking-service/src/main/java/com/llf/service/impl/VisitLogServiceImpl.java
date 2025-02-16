package com.llf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.llf.dao.AbstractBaseDO;
import com.llf.dao.entity.VisitLogDO;
import com.llf.dao.repository.VisitLogRepository;
import com.llf.model.VisitorCountDTO;
import com.llf.service.VisitLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class VisitLogServiceImpl implements VisitLogService {

    @Autowired
    private VisitLogRepository visitLogRepository;

    @Override
    public VisitorCountDTO queryVisitorCount(Date startTime, Date endTime) {
        int todayPv = visitLogRepository.count(
                new QueryWrapper<VisitLogDO>().lambda().between(AbstractBaseDO::getGmtCreate, startTime, endTime));
        int todayUv = visitLogRepository.queryUvByStartTimeAndEndTime(startTime, endTime);
        int allPv = visitLogRepository.count();
        int allUv = visitLogRepository.queryUv();
        return VisitorCountDTO.builder()
                .todayPv(todayPv)
                .todayUv(todayUv)
                .allPv(allPv)
                .allUv(allUv)
                .build();
    }
}
