package com.llf.controller;

import com.llf.model.HotSearchDTO;
import com.llf.model.HotSearchDetailDTO;
import com.llf.page.Page;
import com.llf.result.ResultModel;
import com.llf.service.HotSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.llf.cache.NdrHotSearchCache.CACHE_MAP;

/**
 * @author llf
 * @version HotSearchController.java, 1.0.0
 * @description 热搜接口
 * @date 2024.09.10
 */
@RestController
@RequestMapping("/api/hotSearch")
@CrossOrigin
public class HotSearchController {

    @Autowired
    private HotSearchService hotSearchService;

    @GetMapping("/queryByType")
    public ResultModel<HotSearchDetailDTO> queryByType(@RequestParam(required = true) String type) {
        return ResultModel.success(CACHE_MAP.get(type.toUpperCase()));
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
