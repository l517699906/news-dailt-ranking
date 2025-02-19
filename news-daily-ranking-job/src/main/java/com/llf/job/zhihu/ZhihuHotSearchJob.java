package com.llf.job.zhihu;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;

import com.llf.cache.hotSearch.HotSearchCacheManager;
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
import javax.annotation.Resource;

import static com.llf.enums.HotSearchEnum.ZHIHU;

/**
 * @author llf
 * @version ZhihuHotSearchJob.java, 1.0.0
 * @description 知乎热搜Java爬虫代码
 * @date 2024.09.10
 */
@Component
@Slf4j
public class ZhihuHotSearchJob {

    @Autowired
    private HotSearchService hotSearchService;

    @Resource
    private HotSearchCacheManager hotSearchCacheManager;

    /**
     * 定时触发爬虫方法，1个小时执行一次
     */
    @XxlJob("zhihuHotSearchJob")
    public ReturnT<String> hotSearch(String param) throws IOException {
        log.info("知乎热搜爬虫任务开始");
        try {
            //查询知乎热搜数据
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url("https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total")
                .method("GET", null).build();
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            JSONObject jsonObject = JSON.parseObject(response.body().string());
            JSONArray array = jsonObject.getJSONArray("data");
            List<HotSearchDO> hotSearchDOList = Lists.newArrayList();
            for (int i = 0, len = array.size(); i < len; i++) {
                //获取知乎热搜信息
                JSONObject object = (JSONObject)array.get(i);
                JSONObject target = object.getJSONObject("target");
                //构建热搜信息榜
                HotSearchDO hotSearchDO = HotSearchDO.builder().hotSearchResource(ZHIHU.getCode()).build();
                //设置知乎三方ID
                hotSearchDO.setHotSearchId(target.getString("id"));
                //设置文章连接
                hotSearchDO.setHotSearchUrl("https://www.zhihu.com/question/" + hotSearchDO.getHotSearchId());
                //设置文章标题
                hotSearchDO.setHotSearchTitle(target.getString("title"));
                //设置作者名称
                hotSearchDO.setHotSearchAuthor(target.getJSONObject("author").getString("name"));
                //设置作者头像
                hotSearchDO.setHotSearchAuthorAvatar(target.getJSONObject("author").getString("avatar_url"));
                //设置文章摘要
                hotSearchDO.setHotSearchExcerpt(target.getString("excerpt"));
                //设置热搜热度
                hotSearchDO.setHotSearchHeat(object.getString("detail_text").replace("热度", ""));
                //按顺序排名
                hotSearchDO.setHotSearchOrder(i + 1);
                hotSearchDOList.add(hotSearchDO);
            }
            if (CollectionUtils.isEmpty(hotSearchDOList)) {
                return ReturnT.SUCCESS;
            }
            //数据加到缓存中
            hotSearchCacheManager.setCache(ZHIHU.getCode(), HotSearchDetailDTO.builder()
                    //热搜数据
                    .hotSearchDTOList(
                            hotSearchDOList.stream().map(HotSearchConvert::toDTOWhenQuery).collect(Collectors.toList()))
                    //更新时间
                    .updateTime(Calendar.getInstance().getTime()).build());

            //数据持久化
            hotSearchService.saveCache2DB(hotSearchDOList);
            log.info("知乎热搜爬虫任务结束");
        } catch (IOException e) {
            log.error("获取知乎数据异常", e);
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

