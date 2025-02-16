package com.llf.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class CacheRedisUtil {

    /**
     * 先查询Redis，再调用接口
     *
     * @param cacheSelector    查询缓存的方法
     * @param databaseSelector 数据库查询方法
     * @return T
     */
    public static <T> T selectCacheByTemplate(Supplier<T> cacheSelector, Supplier<T> databaseSelector) {
        try {
            log.info("query data from redis ······");
            // 先查 Redis缓存
            T t = cacheSelector.get();
            if (t == null) {
                // 没有记录再查询数据库
                return databaseSelector.get();
            } else {
                return t;
            }
        } catch (Exception e) {
            // 缓存查询出错，则去数据库查询
            log.info("query data from database ······");
            return databaseSelector.get();
        }
    }
}
