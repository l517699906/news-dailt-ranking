package com.llf.service.convert;

import com.llf.dao.entity.SysConfigDO;
import com.llf.model.SkylineWebcamsDTO;

public class SysConfigConvert {

    public static SkylineWebcamsDTO toSkylineWebcamsDTOWhenQuery(SysConfigDO sysConfigDO) {
        return SkylineWebcamsDTO.builder().placeName(sysConfigDO.getItemKey()).playUrl(
                sysConfigDO.getItemValue()).build();
    }
}
