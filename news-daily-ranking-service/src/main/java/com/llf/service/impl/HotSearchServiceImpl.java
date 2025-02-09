package com.llf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.page.PageMethod;
import com.llf.model.HotSearchDTO;
import com.llf.page.Page;
import com.llf.service.convert.HotSearchConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import com.llf.dao.entity.HotSearchDO;
import com.llf.dao.repository.HotSearchRepository;
import com.llf.service.HotSearchService;
import org.springframework.stereotype.Service;
import com.llf.dao.AbstractBaseDO;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HotSearchServiceImpl implements HotSearchService {

    @Resource
    private HotSearchRepository hotSearchRepository;

    @Override
    public Boolean saveCache2DB(List<HotSearchDO> hotSearchDOS) {
        if (CollectionUtils.isEmpty(hotSearchDOS)) {
            return Boolean.TRUE;
        }
        //查询当前数据是否已经存在
        List<String> searchIdList = hotSearchDOS.stream().map(HotSearchDO::getHotSearchId).collect(
                Collectors.toList());
        List<HotSearchDO> hotSearchDOList = hotSearchRepository.list(
                new QueryWrapper<HotSearchDO>().lambda().in(HotSearchDO::getHotSearchId, searchIdList));
        //过滤已经存在的数据
        if (CollectionUtils.isNotEmpty(hotSearchDOList)) {
            List<String> tempIdList = hotSearchDOList.stream().map(HotSearchDO::getHotSearchId).collect(
                    Collectors.toList());
            hotSearchDOS = hotSearchDOS.stream().filter(
                    hotSearchDO -> !tempIdList.contains(hotSearchDO.getHotSearchId())).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(hotSearchDOS)) {
            return Boolean.TRUE;
        }
        log.info("本次新增[{}]条数据", hotSearchDOS.size());
        //批量添加
        return hotSearchRepository.saveBatch(hotSearchDOS);
    }

    @Override
    public Page<HotSearchDTO> pageQueryHotSearchByTitle(String title, Integer pageNum, Integer pageSize) {
        //设置分页参数
        PageMethod.startPage(pageNum, pageSize);
        //查询热搜
        List<HotSearchDO> hotSearchDOS = hotSearchRepository.list(
                new QueryWrapper<HotSearchDO>().lambda().like(HotSearchDO::getHotSearchTitle, "%" + title + "%")
                        .orderByDesc(AbstractBaseDO::getGmtCreate));
        if (CollectionUtils.isEmpty(hotSearchDOS)) {
            return Page.emptyPage();
        }
        //对象转换
        return Page.resetPage(hotSearchDOS, hotSearchDOS.stream().map(HotSearchConvert::toDTOWhenQuery)
                .collect(Collectors.toList()));
    }

    @Override
    public Page<HotSearchDTO> pageQueryHotSearchByType(String hotSearchResource, Integer pageNum, Integer pageSize) {
        //设置分页参数
        PageMethod.startPage(pageNum, pageSize);
        //查询热搜
        List<HotSearchDO> hotSearchDOS = hotSearchRepository.list(
                new QueryWrapper<HotSearchDO>().lambda().eq(HotSearchDO :: getHotSearchResource,hotSearchResource)
                        .orderByDesc(AbstractBaseDO::getGmtCreate));
        if (CollectionUtils.isEmpty(hotSearchDOS)) {
            return Page.emptyPage();
        }
        //对象转换
        return Page.resetPage(hotSearchDOS, hotSearchDOS.stream().map(HotSearchConvert::toDTOWhenQuery)
                .collect(Collectors.toList()));
    }
}
