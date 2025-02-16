package com.llf.service;

import com.llf.model.VisitorCountDTO;

import java.util.Date;

public interface VisitLogService {

    /**
     * 查询pv/uv
     *
     * @param startTime 开始时间
     * @param entTime   结束时间
     * @return pv/uv
     */
    VisitorCountDTO queryVisitorCount(Date startTime, Date entTime);
}
