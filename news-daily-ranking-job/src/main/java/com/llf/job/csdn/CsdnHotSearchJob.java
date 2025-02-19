package com.llf.job.csdn;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.llf.enums.HotSearchEnum.CSDN;

/**
 * @author llf
 * @version CsdnHotSearchJob.java, 1.0.0
 * @description Csdn热搜Java爬虫代码
 * @date 2025年02月16
 */
@Component
@Slf4j
public class CsdnHotSearchJob {

    @Autowired
    private HotSearchService hotSearchService;

    @Resource
    private HotSearchCacheManager hotSearchCacheManager;

    @XxlJob("csdnHotSearchJob")
    public ReturnT<String> hotSearch(String param) throws IOException {
        log.info("Csdn热搜爬虫任务开始");
        try {
            //查询CSDN热搜数据
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(
                    "https://blog.csdn.net/phoenix/web/blog/hot-rank?page=0&pageSize=25").addHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0"
                            + ".3770.142 Safari/537.36").method("GET", null).build();
            Response response = client.newCall(request).execute();
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            JSONArray array = jsonObject.getJSONArray("data");
            List<HotSearchDO> hotSearchDOList = Lists.newArrayList();
            for (int i = 0, len = array.size(); i < len; i++) {
                //获取知乎热搜信息
                JSONObject object = (JSONObject)array.get(i);
                //构建热搜信息榜
                HotSearchDO hotSearchDO = HotSearchDO.builder().hotSearchResource(CSDN.getCode()).build();
                //设置知乎三方ID
                hotSearchDO.setHotSearchId(object.getString("productId"));
                //设置文章标题
                hotSearchDO.setHotSearchTitle(object.getString("articleTitle"));
                //设置文章连接
                hotSearchDO.setHotSearchUrl(object.getString("articleDetailUrl"));
                //设置热搜热度
                hotSearchDO.setHotSearchHeat(object.getString("hotRankScore"));
                //设置热搜作者
                hotSearchDO.setHotSearchAuthor(object.getString("nickName"));
                //设置热搜作者头像
                hotSearchDO.setHotSearchAuthorAvatar(object.getString("avatarUrl"));

                //按顺序排名
                hotSearchDO.setHotSearchOrder(i + 1);
                hotSearchDOList.add(hotSearchDO);
            }
            if (CollectionUtils.isEmpty(hotSearchDOList)) {
                return ReturnT.SUCCESS;
            }
            //数据加到缓存中
            hotSearchCacheManager.setCache(CSDN.getCode(), HotSearchDetailDTO.builder()
                    //热搜数据
                    .hotSearchDTOList(hotSearchDOList.stream().map(HotSearchConvert::toDTOWhenQuery).collect(Collectors.toList()))
                    //更新时间
                    .updateTime(Calendar.getInstance().getTime()).build());
            //数据持久化
            hotSearchService.saveCache2DB(hotSearchDOList);
            log.info("Csdn热搜爬虫任务结束");
        } catch (IOException e) {
            log.error("获取Csdn数据异常", e);
        }
        return ReturnT.SUCCESS;
    }


    @PostConstruct
    public void init() {
        // 启动运行爬虫一次
        try {
            hotSearch(null);
        } catch (IOException e) {
            log.error("启动爬虫脚本失败", e);
        }
    }
}
