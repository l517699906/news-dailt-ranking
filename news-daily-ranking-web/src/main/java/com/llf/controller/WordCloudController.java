package com.llf.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Sets;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.llf.cache.hotSearch.HotSearchCacheManager;
import com.llf.cache.sys.SysConfigCacheManager;
import com.llf.model.HotSearchDTO;
import com.llf.model.WordCloudDTO;
import com.llf.result.ResultModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: llf
 * @desc: 热搜标题分词接口
 *
 */
@RestController
@RequestMapping("/api/hotSearch/wordCloud")
public class WordCloudController {

    private static Set<String> STOP_WORDS;
    private static JSONArray WEIGHT_WORDS_ARRAY;

    @RequestMapping("/queryWordCloud")
    public ResultModel<List<WordCloudDTO>> queryWordCloud(@RequestParam(required = true) Integer topN) {
        List<HotSearchDTO> hotSearchDTOS = gatherHotSearchData();
        List<String> titleList = hotSearchDTOS.stream().map(HotSearchDTO::getHotSearchTitle).collect(Collectors.toList());
        return ResultModel.success(findTopFrequentNouns(titleList, topN));
    }

    /**
     * 获取停用词
     *
     * @return
     */
    private List<HotSearchDTO> gatherHotSearchData() {
        String stopWordsStr = SysConfigCacheManager.getConfigByGroupCodeAndKey("WordCloud", "StopWords");
        STOP_WORDS = Sets.newHashSet(stopWordsStr.split(","));
        WEIGHT_WORDS_ARRAY = JSONArray.parseArray(SysConfigCacheManager.getConfigByGroupCodeAndKey("WordCloud", "WeightWords"));
        List<HotSearchDTO> hotSearchDTOS = new ArrayList<>();
        HotSearchCacheManager.CACHE_MAP.forEach((key, detail) -> hotSearchDTOS.addAll(detail.getHotSearchDTOList()));
        return hotSearchDTOS;
    }

    /**
     * 分词
     *
     * @param titleList 标题列表
     * @param topN      截取指定长度的热词大小
     * @return
     */
    public static List findTopFrequentNouns(List<String> titleList, int topN) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        Map<String, Integer> wordCount = new HashMap<>();
        Iterator<String> var4 = titleList.iterator();

        while (var4.hasNext()) {
            String title = var4.next();
            List<String> words = segmenter.sentenceProcess(title.trim());
            Iterator<String> var7 = words.iterator();

            while (var7.hasNext()) {
                String word = var7.next();
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        return wordCount.entrySet().stream()
                //停用词过滤
                .filter(entry -> !STOP_WORDS.contains(entry.getKey()))
                //构建对象
                .map(entry -> WordCloudDTO.builder().word(entry.getKey()).rate(entry.getValue()).build())
                //权重替换
                .map(wordCloudDTO -> {
                    if (CollectionUtils.isEmpty(WEIGHT_WORDS_ARRAY)) {
                        return wordCloudDTO;
                    } else {
                        WEIGHT_WORDS_ARRAY.forEach(weightedWord -> {
                            JSONObject tempObject = (JSONObject) weightedWord;
                            if (wordCloudDTO.getWord().equals(tempObject.getString("originWord"))) {
                                wordCloudDTO.setWord(tempObject.getString("targetWord"));
                                if (tempObject.containsKey("weight")) {
                                    wordCloudDTO.setRate(tempObject.getIntValue("weight"));
                                }
                            }
                        });
                        return wordCloudDTO;
                    }
                })
                //按出现频率进行排序
                .sorted(Comparator.comparing(WordCloudDTO::getRate).reversed())
                //截取前topN的数据
                .limit(topN)
                .collect(Collectors.toList());
    }
}
