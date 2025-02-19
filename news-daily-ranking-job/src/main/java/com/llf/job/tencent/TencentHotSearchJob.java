package com.llf.job.tencent;

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
import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.llf.enums.HotSearchEnum.TENCENT;

/**
 * @author llf
 * @version TencentHotSearchJob.java, 1.0.0
 * @description 腾讯热搜Java爬虫代码
 * @date 2025.02.16
 */
@Component
@Slf4j
public class TencentHotSearchJob {
    
    @Autowired
    private HotSearchService hotSearchService;

    @Resource
    private HotSearchCacheManager hotSearchCacheManager;

    /**
     * 定时触发爬虫方法，1个小时执行一次
     */
    @XxlJob("TencentHotSearchJob")
    public ReturnT<String> hotSearch(String param) throws IOException {
        log.info("腾讯热搜爬虫任务开始");
        try {
            //查询腾讯热搜数据
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url("https://i.news.qq.com/gw/event/pc_hot_ranking_list?ids_hash=&offset=0&page_size=50&appver=15"
                            + ".5_qqnews_7.1.60&rank_id=hot").method("GET", null).build();
            Response response = client.newCall(request).execute();
            JSONObject jsonObject = JSON.parseObject(response.body().string());
            JSONArray array = jsonObject.getJSONArray("idlist").getJSONObject(0).getJSONArray("newslist");
            List<HotSearchDO> hotSearchDOList = Lists.newArrayList();
            for (int i = 1, len = array.size(); i < len; i++) {
                //获取腾讯热搜信息
                JSONObject object = (JSONObject)array.get(i);
                //构建热搜信息榜
                HotSearchDO hotSearchDO = HotSearchDO.builder().hotSearchResource(TENCENT.getCode()).build();
                //设置腾讯三方ID
                hotSearchDO.setHotSearchId(object.getString("id"));
                //设置文章连接
                hotSearchDO.setHotSearchUrl(object.getString("surl"));
                //设置文章标题
                hotSearchDO.setHotSearchTitle(object.getString("title"));
                //设置作者名称
                hotSearchDO.setHotSearchAuthor(object.getString("chlname"));
                //设置文章封面
                hotSearchDO.setHotSearchCover(object.getString("miniProShareImage"));
                //设置热搜热度
                hotSearchDO.setHotSearchHeat(object.getString("readCount"));
                hotSearchDOList.add(hotSearchDO);
            }
            // 使用自定义的Comparator进行排序
            AtomicInteger count = new AtomicInteger(1);
            // 先判断是否为空，然后转换成数值进行比较，按照倒序排序，空值放在最后。
            hotSearchDOList = hotSearchDOList.stream().sorted(Comparator.comparing((HotSearchDO hotSearch) -> hotSearch.getHotSearchHeat() != null).reversed().thenComparing(hotSearch -> {
                try {
                    return Integer.parseInt(hotSearch.getHotSearchHeat());
                } catch (NumberFormatException | NullPointerException e) {
                    // 将无法解析的字符串和空值视为最小值
                    return Integer.MIN_VALUE;
                }
                // 对解析的数值进行倒序排序
            }, Comparator.reverseOrder())).map(hotSearchDO -> {
                hotSearchDO.setHotSearchOrder(count.getAndIncrement());
                return hotSearchDO;
            }).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(hotSearchDOList)) {
                return ReturnT.SUCCESS;
            }
            //数据加到缓存中
            hotSearchCacheManager.setCache(TENCENT.getCode(), HotSearchDetailDTO.builder()
                    //热搜数据
                    .hotSearchDTOList(
                            hotSearchDOList.stream().map(HotSearchConvert::toDTOWhenQuery).collect(Collectors.toList()))
                    //更新时间
                    .updateTime(Calendar.getInstance().getTime()).build());

            //数据持久化
            hotSearchService.saveCache2DB(hotSearchDOList);
            log.info("腾讯热搜爬虫任务结束");
        } catch (IOException e) {
            log.error("获取腾讯数据异常", e);
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
