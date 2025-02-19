package com.llf.controller;

import com.llf.cache.hotSearch.HotSearchCacheManager;
import com.llf.model.HotSearchDTO;
import com.llf.model.HotSearchDetailDTO;
import com.llf.page.Page;
import com.llf.result.ResultModel;
import com.llf.service.HotSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author llf
 * @version HotSearchController.java, 1.0.0
 * @description 热搜接口
 * @date 2024.09.10
 */
@Slf4j
@RestController
@RequestMapping("/api/hotSearch")
@CrossOrigin
public class HotSearchController {

    @Autowired
    private HotSearchService hotSearchService;

    @Resource
    private HotSearchCacheManager hotSearchCacheManager;

    @GetMapping("/queryByType")
    public ResultModel<HotSearchDetailDTO> queryByType(@RequestParam String type) {
        String key = type.toUpperCase(); // 直接使用BAIDU等平台代码
        HotSearchDetailDTO detail = hotSearchCacheManager.getCache(key);
        return ResultModel.success(detail);
    }

    @GetMapping("/pageQueryHotSearchByType")
    public ResultModel<Page<HotSearchDTO>> pageQueryHotSearchByType(@RequestParam(required = true) String type) {
        return ResultModel.success(hotSearchService.pageQueryHotSearchByType(type,1,9));
    }

    @GetMapping("/pageQueryHotSearchByTitle")
    public ResultModel<Page<HotSearchDTO>> pageQueryHotSearchByTitle(@RequestParam(required = true) String title,
                                                                     @RequestParam(required = true) Integer pageNum, @RequestParam(required = true) Integer pageSize) {
        return ResultModel.success(hotSearchService.pageQueryHotSearchByTitle(title, pageNum, pageSize));
    }

}
