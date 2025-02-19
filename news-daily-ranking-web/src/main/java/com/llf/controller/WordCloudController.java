package com.llf.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Sets;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.llf.cache.hotSearch.HotSearchCacheManager;
import com.llf.cache.sys.SysConfigCacheManager;
import com.llf.enums.HotSearchEnum;
import com.llf.model.HotSearchDTO;
import com.llf.model.HotSearchDetailDTO;
import com.llf.model.WordCloudDTO;
import com.llf.result.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: llf
 * @desc: 热搜标题分词接口
 *
 */
@Slf4j
@RestController
@RequestMapping("/api/hotSearch/wordCloud")
public class WordCloudController {

    @Resource
    @Qualifier("hotSearchRedisTemplate")
    private RedisTemplate<String, HotSearchDetailDTO> redisTemplate;

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
        // 1. 配置项获取（增强空值保护）
        String stopWordsStr = Optional.ofNullable(
                SysConfigCacheManager.getConfigByGroupCodeAndKey("WordCloud", "StopWords")
        ).orElse("");
        STOP_WORDS = Sets.newHashSet(stopWordsStr.split(","));

        String weightWordsStr = Optional.ofNullable(
                SysConfigCacheManager.getConfigByGroupCodeAndKey("WordCloud", "WeightWords")
        ).orElse("[]");
        WEIGHT_WORDS_ARRAY = JSONArray.parseArray(weightWordsStr);

        // 2. Redis缓存查询（带空值过滤）
        return getAllHotSearchWithMonitor()
                .stream()
                .filter(Objects::nonNull) // 过滤空DTO
                .collect(Collectors.toList());
    }

    /**
     * 从Redis获取所有平台的热搜数据
     */
    public List<HotSearchDTO> getAllHotSearchWithMonitor() {
        // 获取所有枚举项
        HotSearchEnum[] allPlatforms = HotSearchEnum.values();

        // 生成所有平台Redis键（直接使用枚举code）
        List<String> redisKeys = Arrays.stream(allPlatforms)
                .map(e -> "ndr:hotsearch:" + e.getCode())
                .collect(Collectors.toList());

        // 批量获取Redis缓存
        List<HotSearchDetailDTO> details = redisTemplate.opsForValue()
                .multiGet(redisKeys)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 找出缺失平台（通过枚举对比）
        Set<String> cachedPlatforms = details.stream()
                .flatMap(dto -> dto.getHotSearchDTOList().stream())
                .map(HotSearchDTO::getHotSearchResource) // 这里获取资源标识字段
                .collect(Collectors.toSet());

        List<String> missingPlatforms = Arrays.stream(allPlatforms)
                .filter(e -> !cachedPlatforms.contains(e.getCode()))
                .map(HotSearchEnum::getDesc)
                .collect(Collectors.toList());

        if (!missingPlatforms.isEmpty()) {
            log.warn("以下平台数据缺失: {}", missingPlatforms);
        }

        // 合并数据
        return details.stream()
                .map(HotSearchDetailDTO::getHotSearchDTOList)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(dto -> {
                    // 过滤无效平台数据
                    boolean valid = HotSearchEnum.of(Byte.parseByte(dto.getHotSearchResource())) != null;
                    if (!valid) {
                        log.error("发现未知平台数据: {}", dto.getHotSearchResource());
                    }
                    return valid;
                })
                .collect(Collectors.toList());
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
