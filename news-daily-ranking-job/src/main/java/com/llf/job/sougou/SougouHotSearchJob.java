package com.llf.job.sougou;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.llf.dao.entity.HotSearchDO;
import com.llf.model.HotSearchDetailDTO;
import com.llf.service.HotSearchService;
import com.llf.service.convert.HotSearchConvert;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.llf.cache.NdrHotSearchCache.CACHE_MAP;
import static com.llf.enums.HotSearchEnum.SOUGOU;

/**
 * @author llf
 * @version SougouHotSearchJob.java, 1.0.0
 * @description 搜狗热搜Java爬虫代码
 * @date 2025年02月16日
 */
@Component
@Slf4j
public class SougouHotSearchJob {

    @Autowired
    private HotSearchService hotSearchService;

    @XxlJob("sougouHotSearchJob")
    public ReturnT<String> hotSearch(String param) throws IOException {
        log.info("搜狗热搜爬虫任务开始");
        try {
            //查询搜狗热搜数据
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url("https://go.ie.sogou.com/hot_ranks").method("GET", null)
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            JSONArray array = jsonObject.getJSONArray("data");
            List<HotSearchDO> hotSearchDOList = Lists.newArrayList();
            for (int i = 0, len = array.size(); i < len; i++) {
                //获取搜狗热搜信息
                JSONObject object = (JSONObject)array.get(i);
                //构建热搜信息榜
                HotSearchDO hotSearchDO = HotSearchDO.builder().hotSearchResource(SOUGOU.getCode()).build();
                //设置搜狗三方ID
                hotSearchDO.setHotSearchId(object.getString("id"));
                //设置文章标题
                hotSearchDO.setHotSearchTitle(object.getJSONObject("attributes").getString("title"));
                //设置文章连接
                hotSearchDO.setHotSearchUrl(
                        "https://www.sogou.com/web?ie=utf8&query=" + hotSearchDO.getHotSearchTitle());
                //设置热搜热度
                hotSearchDO.setHotSearchHeat(object.getJSONObject("attributes").getString("num"));
                //按顺序排名
                hotSearchDO.setHotSearchOrder(i + 1);
                hotSearchDOList.add(hotSearchDO);
            }
            if (CollectionUtils.isEmpty(hotSearchDOList)) {
                return ReturnT.SUCCESS;
            }
            //数据加到缓存中
            CACHE_MAP.put(SOUGOU.getCode(), HotSearchDetailDTO.builder()
                    //热搜数据
                    .hotSearchDTOList(
                            hotSearchDOList.stream().map(HotSearchConvert::toDTOWhenQuery).collect(Collectors.toList()))
                    //更新时间
                    .updateTime(Calendar.getInstance().getTime()).build());
            //数据持久化
            hotSearchService.saveCache2DB(hotSearchDOList);
            log.info("搜狗热搜爬虫任务结束");
        } catch (IOException e) {
            log.error("获取搜狗数据异常", e);
        }
        return ReturnT.SUCCESS;
    }

    @PostConstruct
    public void init() {
        // 启动运行爬虫一次
        try {
            hotSearch(null);
        } catch (IOException e) {
            log.error("启动爬虫脚本失败",e);
        }
    }
}
