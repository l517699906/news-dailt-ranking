package com.llf.job.hupu;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.llf.dao.entity.HotSearchDO;
import com.llf.model.HotSearchDetailDTO;
import com.llf.service.HotSearchService;
import com.llf.service.convert.HotSearchConvert;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.llf.cache.NdrHotSearchCache.CACHE_MAP;
import static com.llf.enums.HotSearchEnum.HUPU;

/**
 * @author llf
 * @version HupuHotSearchJob.java, 1.0.0
 * @description 虎扑热搜Java爬虫代码
 * @date 2025年02月16
 */
@Component
@Slf4j
public class HupuHotSearchJob {

    @Autowired
    private HotSearchService hotSearchService;

    /**
     * 定时触发爬虫方法，1个小时执行一次
     */
    @XxlJob("hupuHotSearchJob")
    public ReturnT<String> hotSearch(String param) throws IOException {
        log.info("虎扑热搜爬虫任务开始");
        try {
            //查询虎扑热搜数据
            String url = "https://bbs.hupu.com/love-hot";
            List<HotSearchDO> hotSearchDOList = new ArrayList<>();
            Document doc = Jsoup.connect(url).get();
            //元素列表
            Elements elements = doc.select(".p-title");
            for (int i = 0, len = elements.size(); i < len; i++) {
                //构建热搜信息榜
                HotSearchDO hotSearchDO = HotSearchDO.builder().hotSearchResource(HUPU.getCode()).build();
                //设置虎扑三方ID
                hotSearchDO.setHotSearchId(getHashId(HUPU.getCode() + hotSearchDO.getHotSearchTitle()));
                //设置文章连接
                hotSearchDO.setHotSearchUrl("https://bbs.hupu.com/" + doc.select(".p-title").get(i).attr("href"));
                //设置文章标题
                hotSearchDO.setHotSearchTitle(elements.get(i).text().trim());
                //设置作者名称
                hotSearchDO.setHotSearchAuthor(doc.select(".post-auth").get(i).text());
                //设置热搜热度
                hotSearchDO.setHotSearchHeat(doc.select(".post-datum").get(i).text().split("/")[1].trim());
                hotSearchDOList.add(hotSearchDO);
            }
            AtomicInteger count = new AtomicInteger(1);
            hotSearchDOList = hotSearchDOList.stream().sorted(Comparator.comparingInt((HotSearchDO hotSearch) -> Integer.parseInt(hotSearch.getHotSearchHeat())).reversed()).map(hotSearchDO -> {
                hotSearchDO.setHotSearchOrder(count.getAndIncrement());
                return hotSearchDO;
            }).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(hotSearchDOList)) {
                return ReturnT.SUCCESS;
            }
            //数据加到缓存中
            CACHE_MAP.put(HUPU.getCode(), HotSearchDetailDTO.builder()
                    //热搜数据
                    .hotSearchDTOList(
                            hotSearchDOList.stream().map(HotSearchConvert::toDTOWhenQuery).collect(Collectors.toList()))
                    //更新时间
                    .updateTime(Calendar.getInstance().getTime()).build());

            //数据持久化
            hotSearchService.saveCache2DB(hotSearchDOList);
            log.info("虎扑热搜爬虫任务结束");
        } catch (IOException e) {
            log.error("获取虎扑数据异常", e);
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 根据文章标题获取一个唯一ID
     *
     * @param title 文章标题
     * @return 唯一ID
     */
    private String getHashId(String title) {
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
