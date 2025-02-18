package com.llf.cache.sys;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Maps;
import com.llf.dao.entity.SysConfigDO;
import com.llf.dao.repository.SysConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 系统配置读取
 */
@Component
@Slf4j
public class SysConfigCacheManager {

    @Resource
    private SysConfigRepository sysConfigRepository;

    /**
     * 加个锁，防止出现并发问题
     */
    private static final Lock LOCK = new ReentrantLock();

    /**
     * 底层缓存组件，可以使用ConcurrentMap也可以使用Redis，推荐使用Redis
     */
    private static ConcurrentMap<String, List<SysConfigDO>> CACHE = Maps.newConcurrentMap();
    /**
     * 底层缓存组件，可以使用ConcurrentMap也可以使用Redis，推荐使用Redis
     */
    private static ConcurrentMap<String, String> KEY_CACHE = Maps.newConcurrentMap();

    @Scheduled(fixedRate = 60 * 1000)
    public void loadingCache() {
        LOCK.lock();
        try {
            List<SysConfigDO> sysConfigDOS = sysConfigRepository.list();
            if (CollectionUtils.isEmpty(sysConfigDOS)) {
                return;
            }
            //按key缓存
            sysConfigDOS.forEach(sysConfigDO -> {
                KEY_CACHE.put(sysConfigDO.getGroupCode() + "_" + sysConfigDO.getItemKey(), sysConfigDO.getItemValue());
            });
            //按组缓存
            Map<String, List<SysConfigDO>> configMap = sysConfigDOS.stream().collect(Collectors.groupingBy(SysConfigDO::getGroupCode));
            configMap.forEach((key, value) -> {
                CACHE.put(key, value);
            });
        } catch (Exception e) {
            log.error("获取系统缓存数据异常", e);
        } finally {
            LOCK.unlock();
        }
    }

    public static List<SysConfigDO> getConfigByGroupCode(String groupCode) {
        return CACHE.get(groupCode);
    }

    public static String getConfigByGroupCodeAndKey(String groupCode, String key) {
        return KEY_CACHE.get(groupCode + "_" + key);
    }
}
