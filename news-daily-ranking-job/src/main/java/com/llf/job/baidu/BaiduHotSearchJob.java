package com.llf.job.baidu;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.llf.cache.hotSearch.HotSearchCacheManager;
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
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.llf.enums.HotSearchEnum.BAIDU;

/**
 * @author llf
 * @version BaiduHotSearchJob.java, 1.0.0
 * @description 百度热搜Java爬虫代码
 * @date 2024.09.07
 */
@Component
@Slf4j
public class BaiduHotSearchJob {

    @Resource
    private HotSearchService hotSearchService;

    @Resource
    private HotSearchCacheManager hotSearchCacheManager;

    /**
     * 定时触发爬虫方法，1个小时执行一次
     */
    //@Scheduled(fixedRate = 1000 * 60 * 60)
    @XxlJob(value = "baiduHotSearchJob")
    public ReturnT<String> hotSearch(String param) throws IOException {
        log.info("百度热搜爬虫任务开始");
        try {
            //获取百度热搜
            String url = "https://top.baidu.com/board?tab=realtime&sa=fyb_realtime_31065";
            List<HotSearchDO> hotSearchDOList = new ArrayList<>();
            Document doc = Jsoup.connect(url).get();
            //标题
            Elements titles = doc.select(".c-single-text-ellipsis");
            //图片
            Elements imgs = doc.select(".category-wrap_iQLoo .index_1Ew5p").next("img");
            //内容
            Elements contents = doc.select(".hot-desc_1m_jR.large_nSuFU");
            //推荐图
            Elements urls = doc.select(".category-wrap_iQLoo a.img-wrapper_29V76");
            //热搜指数
            Elements levels = doc.select(".hot-index_1Bl1a");
            for (int i = 0; i < levels.size(); i++) {
                HotSearchDO hotSearchDO = HotSearchDO.builder().hotSearchResource(BAIDU.getCode()).build();
                //设置文章标题
                hotSearchDO.setHotSearchTitle(titles.get(i).text().trim());
                //设置百度三方ID
                hotSearchDO.setHotSearchId(getHashId(BAIDU.getDesc() + hotSearchDO.getHotSearchTitle()));
                //设置文章封面
                hotSearchDO.setHotSearchCover(imgs.get(i).attr("src"));
                //设置文章摘要
                hotSearchDO.setHotSearchExcerpt(contents.get(i).text().replaceAll("查看更多>", ""));
                //设置文章连接
                hotSearchDO.setHotSearchUrl(urls.get(i).attr("href"));
                //设置热搜热度
                hotSearchDO.setHotSearchHeat(levels.get(i).text().trim());
                //按顺序排名
                hotSearchDO.setHotSearchOrder(i + 1);
                hotSearchDOList.add(hotSearchDO);
            }
            if (CollectionUtils.isEmpty(hotSearchDOList)) {
                return ReturnT.SUCCESS;
            }
            //数据加到缓存中
            hotSearchCacheManager.setCache(BAIDU.getCode(), HotSearchDetailDTO.builder()
                    //热搜数据
                    .hotSearchDTOList(
                            hotSearchDOList.stream().map(HotSearchConvert::toDTOWhenQuery).collect(Collectors.toList()))
                    //更新时间
                    .updateTime(Calendar.getInstance().getTime()).build());

            //数据持久化
            hotSearchService.saveCache2DB(hotSearchDOList);
            log.info("百度热搜爬虫任务结束");
        } catch (IOException e) {
            log.error("获取百度数据异常", e);
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
