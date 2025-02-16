package com.llf.dao.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.llf.dao.entity.VisitLogDO;
import com.llf.dao.mapper.VisitLogMapper;
import com.llf.dao.repository.VisitLogRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class VisitLogRepositoryImpl extends ServiceImpl<VisitLogMapper, VisitLogDO> implements VisitLogRepository {

    @Override
    public int queryUvByStartTimeAndEndTime(Date startTime, Date endTime) {
        return this.baseMapper.queryUvByStartTimeAndEndTime(startTime, endTime);
    }

    @Override
    public int queryUv() {
        return this.baseMapper.queryUv();
    }
}
