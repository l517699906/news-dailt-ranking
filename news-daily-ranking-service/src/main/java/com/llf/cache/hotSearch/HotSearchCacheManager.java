package com.llf.cache.hotSearch;

import com.llf.model.HotSearchDetailDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class HotSearchCacheManager {

    /**
     * 热搜缓存
     */
    @Resource
    @Qualifier("hotSearchRedisTemplate")
    private RedisTemplate<String, HotSearchDetailDTO> hotSearchRedisTemplate;

    /**
     * 操作Redis缓存
     */
    public void setCache(String key, HotSearchDetailDTO detail) {
        hotSearchRedisTemplate.opsForValue().set(key, detail);
    }

    /**
     * 获取Redis缓存
     */
    public HotSearchDetailDTO getCache(String key) {
        return hotSearchRedisTemplate.opsForValue().get(key);
    }
}
