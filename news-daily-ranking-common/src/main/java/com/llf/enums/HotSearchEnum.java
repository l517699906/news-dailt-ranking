package com.llf.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;

@Getter
@AllArgsConstructor
public enum HotSearchEnum {

    DOUYIN("DOUYIN", "抖音"),
    BAIDU("BAIDU", "百度"),
    ZHIHU("ZHIHU", "知乎"),
    BILIBILI("BILIBILI", "B站"),
    TIEBA("TIEBA", "贴吧"),
    SOUGOU("SOUGOU", "搜狗"),
    TENCENT("TENCENT", "腾讯"),
    TOUTIAO("TOUTIAO", "头条"),
    WEIBO("WEIBO", "微博"),
    HUPU("HUPU", "虎扑"),
    JUEJIN("JUEJIN", "掘金"),
    CSDN("CSDN", "CSDN");

    private String code;
    private String desc;

    public static HotSearchEnum of(byte code) {
        return EnumSet.allOf(HotSearchEnum.class).stream().filter(x -> x.code.equals(code)).findFirst().orElse(null);
    }
}
