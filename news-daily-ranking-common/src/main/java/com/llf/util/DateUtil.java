package com.llf.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 */

public class DateUtil {

    public static final String FULL_TIME_PATTERN = "yyyyMMddHHmmss";

    public static final String FULL_TIME_SPLIT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String formatFullTime(LocalDateTime localDateTime) {
        return formatFullTime(localDateTime, FULL_TIME_PATTERN);
    }

    public static String formatFullTime(LocalDateTime localDateTime, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(dateTimeFormatter);
    }

    private static String getDateFormat(Date date, String dateFormatType) {
        SimpleDateFormat simformat = new SimpleDateFormat(dateFormatType);
        return simformat.format(date);
    }

    public static Date getStartOfDay(Date date) {
        if (date == null) {
            return null;
        } else {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),
                    ZoneId.systemDefault());
            LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
            return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    public static Date getEndOfDay(Date date) {
        if (date == null) {
            return null;
        } else {
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),
                    ZoneId.systemDefault());
            LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
            return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
        }
    }

    public static Date parseDate(String currentDate, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return simpleDateFormat.parse(currentDate);
        } catch (Exception e) {
            return null;
        }
    }

    public static String format(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Description: 判断一个时间是否在一个时间段内 </br>
     *
     * @param nowTime   当前时间 </br>
     * @param beginTime 开始时间 </br>
     * @param endTime   结束时间 </br>
     */
    public static Boolean betweenStartAndEnd(Date nowTime, Date beginTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return date.after(begin) && date.before(end);
    }

    /**
     * 当前日期-增加天数
     *
     * @param date 日期
     * @param day  增加的天数 例如：往前减一天 -1 / 往后加一天 1
     * @return 日期
     */
    public static Date addDay(Date date, int day) {
        // 初始化Calendar
        Calendar calendar = Calendar.getInstance();
        // 设置时间
        calendar.setTime(date);
        // 添加具体天数
        calendar.add(Calendar.DATE, day);
        // 返回日期
        return calendar.getTime();
    }
}
