package com.llf.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.File;
import java.text.MessageFormat;
import java.util.Objects;

@Slf4j
public class AddressUtil {

    /**
     * 当前记录地址的本地DB
     */
    private static final String TEMP_FILE_DIR = "/home/admin/app/";

    /**
     * 根据IP地址查询登录来源
     *
     * @param ip
     * @return
     */
    public static String getAddress(String ip) {
        try {
            // 获取当前记录地址位置的文件
            String dbPath = Objects.requireNonNull(AddressUtil.class.getResource("/ip2region/ip2region.xdb")).getPath();
            File file = new File(dbPath);
            //如果当前文件不存在，则从缓存中复制一份
            if (!file.exists()) {
                dbPath = TEMP_FILE_DIR + "ip.db";
                log.info(MessageFormat.format("当前目录为:[{0}]", dbPath));
                file = new File(dbPath);
                FileUtils.copyInputStreamToFile(Objects.requireNonNull(
                        AddressUtil.class.getClassLoader().getResourceAsStream("classpath:ip2region/ip2region.xdb")), file);
            }
            //创建查询对象
            Searcher searcher = Searcher.newWithFileOnly(dbPath);
            //开始查询
            return searcher.searchByStr(ip);
        } catch (Exception e) {
            return ip;
        }
    }
}
