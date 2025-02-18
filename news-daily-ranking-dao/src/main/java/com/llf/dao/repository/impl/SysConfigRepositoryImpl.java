package com.llf.dao.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.llf.dao.entity.SysConfigDO;
import com.llf.dao.mapper.SysConfigMapper;
import com.llf.dao.repository.SysConfigRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SysConfigRepositoryImpl extends ServiceImpl<SysConfigMapper, SysConfigDO> implements SysConfigRepository {
}
