package com.llf.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.llf.dao.AbstractBaseDO;
import lombok.*;

import javax.persistence.Column;

@Getter
@Setter
@TableName("sys_config")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SysConfigDO extends AbstractBaseDO<SysConfigDO> {

    /**
     * 物理主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组
     */
    @Column(name = "group_code")
    private String groupCode;

    /**
     * KEY
     */
    @Column(name = "item_key")
    private String itemKey;

    /**
     * 逻辑删除字段
     */
    @Column(name = "is_deleted")
    private Byte isDeleted;

    /**
     * VALUE
     */
    @Column(name = "item_value")
    private String itemValue;
}
