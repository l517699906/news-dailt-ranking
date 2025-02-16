package com.llf.dao.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llf.dao.entity.VisitLogDO;

import java.util.Date;

public interface VisitLogRepository extends IService<VisitLogDO> {

    /**
     * 按时间查询uv
     *
     * @param startTime 开始时间
     * @param entTime   结束时间
     * @return uv
     */
    int queryUvByStartTimeAndEndTime(Date startTime, Date entTime);

    /**
     * 查询所有Uv
     * @return
     */
    int queryUv();
}
