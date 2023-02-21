package com.ddf.boot.common.api.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    public static final String TIME_SPLIT = ":";

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 时间转换为int的日格式
     */
    public static final DateTimeFormatter DAY_INTEGER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    /**
     * 时间转换为int的月格式
     */
    public static final DateTimeFormatter MONTH_INTEGER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

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
     * 使用北京时区LocalDateTime转时间戳
     *
     * @param localDateTime
     * @return
     */
    public static Long toZhCnMills(LocalDateTime localDateTime) {
        final Instant instant = toZhCnInstant(localDateTime);
        return Objects.isNull(instant) ? null : instant.toEpochMilli();
    }

    /**
     * 使用北京时区LocalDateTime转秒时间戳
     *
     * @param localDateTime
     * @return
     */
    public static Long toZhCnSeconds(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZoneOffset.of("+8"));
    }

    /**
     * 根据秒转换为标准北京时间
     *
     * @param seconds
     * @return
     */
    public static LocalDateTime ofSeconds(long seconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.of("+8"));
    }

    /**
     * 根据毫秒转换为标准北京时间
     *
     * @param millis
     * @return
     */
    public static LocalDateTime ofMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.of("+8"));
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
    public static String getWeekEnd() {
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
    public static Long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 当前年月日
     *
     * @return
     */
    public static Integer currentYearMonthDay() {
        return Integer.parseInt(DAY_INTEGER_FORMATTER.format(LocalDateTime.now()));
    }

    /**
     * 当前年月日
     *
     * @return
     */
    public static Integer formatYearMonthDay(LocalDateTime localDateTime) {
        return Integer.parseInt(DAY_INTEGER_FORMATTER.format(localDateTime));
    }

    /**
     * 当前年月
     *
     * @return
     */
    public static Integer currentYearMonth() {
        return Integer.parseInt(MONTH_INTEGER_FORMATTER.format(LocalDateTime.now()));
    }

    /**
     * 当前年月日
     *
     * @return
     */
    public static Integer formatYearMonth(LocalDateTime localDateTime) {
        return Integer.parseInt(MONTH_INTEGER_FORMATTER.format(localDateTime));
    }

    /**
     * 返回时间格式化类
     *
     * @return
     */
    public static DateTimeFormatter getFormatter() {
        return FORMATTER;
    }

    /**
     * 将秒标准格式化输出
     *
     * @param seconds
     * @return
     */
    public static String standardFormatSeconds(Long seconds) {
        return FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.of("+8")));
    }

    /**
     * 将毫秒标准格式化输出
     *
     * @param millis
     * @return
     */
    public static String standardFormatMillis(Long millis) {
        return FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.of("+8")));
    }

    /**
     * date转中国标准时间LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalDateTime date2CnLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("+8"));
    }


    /**
     * 获取日期的起始时间 如某天 00:00:00
     * @param time
     * @return
     */
    public static LocalDateTime getStartOfDay(LocalDateTime time) {
        return LocalDateTime.of(time.getYear(), time.getMonth(), time.getDayOfMonth(), 0, 0, 0, 0);
    }

    /**
     * 获取日期的结束时间 如某天 23:59:59
     * @param time
     * @return
     */
    public static LocalDateTime getEndOfDay(LocalDateTime time) {
        return LocalDateTime.of(time.getYear(), time.getMonth(), time.getDayOfMonth(), 23, 59, 59);
    }

    /**
     * 判定指定时间已经过去了今天多久
     *
     * @param timeSeconds
     * @return
     */
    public static long calcPassedTodaySeconds(long timeSeconds) {
        final LocalDateTime localDateTime = ofSeconds(timeSeconds);
        return timeSeconds - toZhCnSeconds(getStartOfDay(localDateTime));
    }
}
