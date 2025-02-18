package com.llf.service.impl;

import com.llf.cache.sys.SysConfigCacheManager;
import com.llf.dao.entity.SysConfigDO;
import com.llf.dao.repository.SysConfigRepository;
import com.llf.model.SkylineWebcamsDTO;
import com.llf.service.SysConfigService;
import com.llf.service.convert.SysConfigConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Override
    public List<SkylineWebcamsDTO> querySkylineWebcams() {
        List<SysConfigDO> skylineWebcams = SysConfigCacheManager.getConfigByGroupCode("SkylineWebcams");
        //随机排列顺序
        Collections.shuffle(skylineWebcams);
        return skylineWebcams.subList(0, 3).stream().map(SysConfigConvert::toSkylineWebcamsDTOWhenQuery).collect(
                Collectors.toList());
    }

}
