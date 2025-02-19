package com.llf.job.tieba;

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
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.llf.enums.HotSearchEnum.TIEBA;

/**
 * @author llf
 * @version TiebaHotSearchJob.java, 1.0.0
 * @description B站热榜Java爬虫代码
 * @date 2025年02月15日
 */
@Component
@Slf4j
public class TiebaHotSearchJob {

    @Autowired
    private HotSearchService hotSearchService;

    @Resource
    private HotSearchCacheManager hotSearchCacheManager;

    @PostConstruct
    public void init() {
        try {
            // 调用 hotSearch 方法
            hotSearch(null);
        } catch (IOException e) {
            log.error("启动时调用热搜爬虫任务异常", e);
        }
    }

    @XxlJob("tiebaHotSearchJob")
    public ReturnT<String> hotSearch(String param) throws IOException {
        log.info("贴吧热搜爬虫任务开始");
        try {
            String url = "https://tieba.baidu.com/hottopic/browse/topicList?res_type=1";
            List<HotSearchDO> hotSearchDOList = Lists.newArrayList();
            Document doc = Jsoup.connect(url).get();
            //标题
            Elements titles = doc.select(".topic-top-item-desc");
            //热搜链接
            Elements urls = doc.select(".topic-text");
            //热搜指数
            Elements levels = doc.select(".topic-num");
            for (int i = 0; i < levels.size(); i++) {
                HotSearchDO hotSearchDO = HotSearchDO.builder().hotSearchResource(TIEBA.getCode()).build();
                //设置文章标题
                hotSearchDO.setHotSearchTitle(titles.get(i).text().trim());
                //设置文章连接
                hotSearchDO.setHotSearchUrl(urls.get(i).attr("href"));
                //设置贴吧三方ID
                hotSearchDO.setHotSearchId(getValueFromUrl(hotSearchDO.getHotSearchUrl(), "topic_id"));
                //设置热搜热度
                hotSearchDO.setHotSearchHeat(levels.get(i).text().trim().replace("W实时讨论", "") + "万");
                //按顺序排名
                hotSearchDO.setHotSearchOrder(i + 1);
                hotSearchDOList.add(hotSearchDO);
            }
            if (CollectionUtils.isEmpty(hotSearchDOList)) {
                return ReturnT.SUCCESS;
            }
            //数据加到缓存中
            hotSearchCacheManager.setCache(TIEBA.getCode(), HotSearchDetailDTO.builder()
                    //热搜数据
                    .hotSearchDTOList(
                            hotSearchDOList.stream().map(HotSearchConvert::toDTOWhenQuery).collect(Collectors.toList()))
                    //更新时间
                    .updateTime(Calendar.getInstance().getTime()).build());
            //数据持久化
            hotSearchService.saveCache2DB(hotSearchDOList);
            log.info("贴吧热搜爬虫任务结束");
        } catch (IOException e) {
            log.error("获取贴吧数据异常", e);
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 从链接中获取参数
     *
     * @param url   链接
     * @param param 想要提取出值的参数
     * @return
     * @throws Exception
     */
    public static String getValueFromUrl(String url, String param) {
        if (StringUtils.isAnyBlank(url, param)) {
            throw new RuntimeException("从链接中获取参数异常，url或param为空");
        }
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            Map<String, String> queryPairs = new HashMap<>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                queryPairs.put(key, value);
            }
            return queryPairs.get(param);
        } catch (Exception e) {
            log.error("提取参数发生异常", e);
            throw new RuntimeException("从链接中获取参数异常");
        }
    }
}

