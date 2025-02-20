package com.llf.job.bilibili;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.llf.enums.HotSearchEnum.BILIBILI;

/**
 * @author llf
 * @version BilibiliHotSearchJob.java, 1.0.0
 * @description B站热榜Java爬虫代码
 * @date 2025年02月09
 */
@Component
@Slf4j
public class BilibiliHotSearchJob {

    @Autowired
    private HotSearchService hotSearchService;

    @Resource
    private HotSearchCacheManager hotSearchCacheManager;

    @XxlJob("bilibiliHotSearchJob")
    public ReturnT<String> hotSearch(String param) throws IOException {
        log.info("B站热搜爬虫任务开始");
        try {
            //查询B站热搜数据
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url("https://api.bilibili.com/x/web-interface/ranking/v2")
                    .addHeader("User-Agent", "Mozilla/5.0 (compatible)").addHeader("Cookie", "b_nut=1712137652; "
                            + "buvid3=DBA9C433-8738-DD67-DCF5" + "-DDC780CA892052512infoc").method("GET", null).build();
            Response response = client.newCall(request).execute();
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            JSONArray array = jsonObject.getJSONObject("data").getJSONArray("list");
            List<HotSearchDO> sbmyHotSearchDOList = Lists.newArrayList();
            for (int i = 0, len = array.size(); i < len; i++) {
                //获取B站热搜信息
                JSONObject object = (JSONObject)array.get(i);
                //构建热搜信息榜
                HotSearchDO sbmyHotSearchDO = HotSearchDO.builder().hotSearchResource(BILIBILI.getCode())
                        .build();
                //设置B站三方ID
                sbmyHotSearchDO.setHotSearchId(object.getString("aid"));
                //设置文章连接
                sbmyHotSearchDO.setHotSearchUrl(object.getString("short_link_v2"));
                //设置文章标题
                sbmyHotSearchDO.setHotSearchTitle(object.getString("title"));
                //设置作者名称
                sbmyHotSearchDO.setHotSearchAuthor(object.getJSONObject("owner").getString("name"));
                //设置作者头像
                sbmyHotSearchDO.setHotSearchAuthorAvatar(object.getJSONObject("owner").getString("face"));
                //设置文章封面
                sbmyHotSearchDO.setHotSearchCover(object.getString("pic"));
                //设置热搜热度
                sbmyHotSearchDO.setHotSearchHeat(object.getJSONObject("stat").getString("view"));
                //按顺序排名
                sbmyHotSearchDO.setHotSearchOrder(i + 1);
                sbmyHotSearchDOList.add(sbmyHotSearchDO);
            }
            if (CollectionUtils.isEmpty(sbmyHotSearchDOList)) {
                return ReturnT.SUCCESS;
            }
            //数据加到缓存中
            hotSearchCacheManager.setCache("ndr:hotsearch:" + BILIBILI.getCode(), HotSearchDetailDTO.builder()
                    //热搜数据
                    .hotSearchDTOList(
                            sbmyHotSearchDOList.stream().map(HotSearchConvert::toDTOWhenQuery).collect(Collectors.toList()))
                    //更新时间
                    .updateTime(Calendar.getInstance().getTime()).build());
            //数据持久化
            hotSearchService.saveCache2DB(sbmyHotSearchDOList);
            log.info("B站热搜爬虫任务结束");
        } catch (IOException e) {
            log.error("获取B站数据异常", e);
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 根据文章标题获取一个唯一ID
     *
     * @param title 文章标题
     * @return 唯一ID
     */
    public static String getHashId(String title) {
        long seed = title.hashCode();
        Random rnd = new Random(seed);
        return new UUID(rnd.nextLong(), rnd.nextLong()).toString();
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

