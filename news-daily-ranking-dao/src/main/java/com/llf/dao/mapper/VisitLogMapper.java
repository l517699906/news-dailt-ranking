package com.llf.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.llf.dao.entity.VisitLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLogDO> {

    @Select("SELECT COUNT(DISTINCT ip) FROM visit_log WHERE gmt_create BETWEEN #{startTime} AND #{endTime}")
    int queryUvByStartTimeAndEndTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    @Select("SELECT COUNT(DISTINCT ip) FROM visit_log")
    int queryUv();
}
