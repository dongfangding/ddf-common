package com.ddf.boot.common.core.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    public static final String TIME_SPLIT = ":";

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

    /**
     * 获取指定日期对应时:分的毫秒值
     *
     * @param date
     * @return
     */
    public static long getMillionsOfHourMinute(Date date) {
        final Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        return instance.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 + instance.get(Calendar.MINUTE) * 60 * 1000;
    }


    /**
     * 获取时分对应的毫秒值， 字符格式固定为时:分
     *
     * @param hourMinuteStr
     * @return
     * @see DateUtils#TIME_SPLIT
     */
    public static long getMillionsOfHourMinuteStr(String hourMinuteStr) {
        final int[] hourMinute = checkHourMinute(hourMinuteStr);
        int hour = hourMinute[0];
        int minute = hourMinute[1];
        return hour * 60 * 60 * 1000 + minute * 60 * 1000;
    }


    /**
     * 校验时分格式
     *
     * @param hourMinuteStr 时分时间字符串
     * @return [0] 时  [1] 分
     */
    public static int[] checkHourMinute(String hourMinuteStr) {
        if (StringUtils.isBlank(hourMinuteStr) || !hourMinuteStr.contains(TIME_SPLIT)) {
            throw new IllegalArgumentException(String.format("[%s]没有格式有误，没有包含%s", hourMinuteStr, TIME_SPLIT));
        }
        final String[] split = hourMinuteStr.split(TIME_SPLIT);
        if (split.length != 2 || !NumberUtil.isNumber(split[0].trim()) || !NumberUtil.isNumber(split[1].trim())) {
            throw new IllegalArgumentException(hourMinuteStr);
        }
        final int hour = Integer.parseInt(split[0].trim());
        final int minute = Integer.parseInt(split[1].trim());
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(String.format("【%s】小时只能位于0到23之间", hour));
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(String.format("【%s】分钟只能位于0到59之间", minute));
        }
        return new int[] {hour, minute};
    }

    /**
     * 使用系统默认时区LocalDateTime转Instant
     *
     * @param localDateTime
     * @return
     */
    public static Instant toDefaultInstant(LocalDateTime localDateTime) {
        return Objects.isNull(localDateTime) ? null : localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * 使用系统默认时区LocalDateTime转Instant
     *
     * @param localDateTime
     * @return
     */
    public static Instant toZhCnInstant(LocalDateTime localDateTime) {
        return Objects.isNull(localDateTime) ? null : localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant();
    }


    /**
     * 使用系统默认时区LocalDateTime转时间戳
     *
     * @param localDateTime
     * @return
     */
    public static Long toDefaultMills(LocalDateTime localDateTime) {
        final Instant instant = toDefaultInstant(localDateTime);
        return Objects.isNull(instant) ? null : instant.toEpochMilli();
    }

    /**
     * 使用系统默认时区LocalDateTime转时间戳
     *
     * @param localDateTime
     * @return
     */
    public static Long toZhCnMills(LocalDateTime localDateTime) {
        final Instant instant = toZhCnInstant(localDateTime);
        return Objects.isNull(instant) ? null : instant.toEpochMilli();
    }

    public static void main(String[] args) {
        final LocalDateTime now = LocalDateTime.now();
        final Long aLong = toZhCnMills(now);
        System.out.println(aLong);
        System.out.println(new Date(aLong));
    }

    /**
     * 获取下个星期的第一天
     *
     * @return
     */
    public static Date nextWeekFirstDay() {
        return DateUtil.beginOfWeek(DateUtil.nextWeek());
    }

    /**
     * 获取下个星期的最后一天
     *
     * @return
     */
    public static Date nextWeekEndDay() {
        return DateUtil.endOfWeek(DateUtil.nextWeek());
    }

    /**
     * 获取下个月的第一天
     *
     * @return
     */
    public static Date nextMonthFirstDay() {
        return DateUtil.beginOfMonth(DateUtil.nextMonth());
    }

    /**
     * 获取下个月的最后一天
     *
     * @return
     */
    public static Date nextMonthEndDay() {
        return DateUtil.endOfDay(DateUtil.nextMonth());
    }

    /**
     * 获取下一年的第一天
     *
     * @return
     */
    public static Date nextYearFirstDay() {
        final Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, DateUtil.year(new Date()) + 1);
        return DateUtil.beginOfYear(instance).getTime();
    }

    /**
     * 获取下一年的第一天
     *
     * @return
     */
    public static Date nextYearEndDay() {
        final Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, DateUtil.year(new Date()) + 1);
        return DateUtil.endOfYear(instance).getTime();
    }

    /**
     * 获取本周的最后一天
     *
     * @return String
     **/
    static String getWeekEnd() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.DAY_OF_WEEK, 1);
        Date time = cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd").format(time);
    }

    /**
     * 获取当前秒时间戳
     *
     * @return
     */
    static Long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

}
