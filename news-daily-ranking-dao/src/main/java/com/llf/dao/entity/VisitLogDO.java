package com.llf.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.llf.dao.AbstractBaseDO;
import lombok.*;

import javax.persistence.Column;

@Data
@TableName("visit_log")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class VisitLogDO extends AbstractBaseDO<VisitLogDO> {

    /**
     * 物理主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备类型，手机还是电脑
     */
    @Column(name = "device_type")
    private String deviceType;

    /**
     * 访问
     */
    private String ip;

    /**
     * IP地址
     */
    private String address;

    /**
     * 耗时
     */
    private Integer time;

    /**
     * 调用方法
     */
    private String method;

    /**
     * 参数
     */
    private String params;
}
