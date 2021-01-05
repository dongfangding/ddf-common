package com.ddf.boot.common.core.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

/**
 * <p>描述</p>
 *
 * @author network
 * @version 1.0: DateUtils.java
 * @date 2020/11/13 10:27
 */
@Slf4j
public class DateUtils {


    public static final String MM_DD = "MM/dd";

    /**
     * 获取本月第一天
     *
     * @return Date
     */
    public static Date getMonthFirstDate(Date time) {
        return DateUtil.beginOfMonth(time);
    }

    /**
     * 获取本月最后一天
     *
     * @return Date
     */
    public static Date getMonthLastDate(Date time) {
        return DateUtil.endOfMonth(time);
    }

    /**
     * 获取日期的起始时间 如某天 00:00:00
     * @param time
     * @return
     */
    public static Date getStartOfDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取日期的结束时间 如某天 23:59:59
     * @param time
     * @return
     */
    public static Date getEndOfDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 基于生日，计算星座
     *
     * @param birthDay
     * @return
     */
    public static String getZodiac(@Nullable Date birthDay) {
        if (Objects.isNull(birthDay)) {
            return null;
        }
        DateTime dateTime = new DateTime(birthDay.getTime());
        int day = dateTime.dayOfMonth();
        int month = dateTime.month();
        return DateUtil.getZodiac(month, day);
    }

    /**
     * 基于生日，计算当前年龄
     *
     * @param birthDay
     * @return
     */
    public static Integer getAge(@Nullable Date birthDay) {
        if (Objects.isNull(birthDay)) {
            return 0;
        }
        DateTime dateTime = new DateTime(birthDay.getTime());
        return (int) DateTime.now()
                .between(dateTime)
                .betweenYear(false);
    }

}
