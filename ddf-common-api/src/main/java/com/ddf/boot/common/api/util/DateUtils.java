package com.ddf.boot.common.api.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    public static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter STANDARD_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    /**
     * 时间转换为int的日格式
     */
    public static final DateTimeFormatter DAY_INTEGER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final DateTimeFormatter DAY_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 时间转换为int的日格式
     */
    public static final DateTimeFormatter HOUR_INTEGER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");
    /**
     * 时间转换为int的月格式
     */
    public static final DateTimeFormatter MONTH_INTEGER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 上海时区
     */
    public static final ZoneId GLOBAL_ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 全局默认东八区时区
     */
    public static final ZoneOffset DEFAULT_GMT = ZoneOffset.of("+8");

    /**
     * 获取本月第一天
     *
     * @return Date
     */
    public static Date getMonthFirstDate(Date time) {
        return DateUtil.beginOfMonth(time);
    }

    /**
     * 获取本月第一天秒时间戳
     *
     * @param timestampSeconds 秒时间戳
     * @return Date
     */
    public static Long getMonthFirstSeconds(Long timestampSeconds) {
        return getMonthFirstDate(new Date(timestampSeconds * 1000)).getTime() / 1000;
    }

    /**
     * 根据指定月份获取本月第一天秒时间戳
     *
     * @param month 月格式化形式，格式为yyyyMM
     * @return Date
     */
    public static Long getMonthFirstSecondsByDayFormatter(Integer month) {
        return LocalDate
                .parse(month + "01", DateUtils.DAY_INTEGER_FORMATTER)
                .atStartOfDay(ZoneOffset.ofHours(8))
                .toInstant()
                .getEpochSecond();
    }

    /**
     * 根据指定天获取本天第一天秒时间戳
     *
     * @param monthDay 天格式化形式，格式为yyyyMMdd
     * @return Date
     */
    public static Long getDayFirstSecondsByDayFormatter(Integer monthDay) {
        return LocalDate
                .parse(String.valueOf(monthDay), DateUtils.DAY_INTEGER_FORMATTER)
                .atStartOfDay(ZoneOffset.ofHours(8))
                .toInstant()
                .getEpochSecond();
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
     *
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
     * 今天的开始时间戳
     *
     * @return
     */
    public static Long getTodayStartTimeSeconds() {
        return LocalDate
                .now()
                .atStartOfDay()
                .atZone(GLOBAL_ZONE_ID)
                .toEpochSecond();
    }

    /**
     * 今天的结束时间戳
     *
     * @return
     */
    public static Long getTodayEndTimeSeconds() {
        return getTodayStartTimeSeconds() + TimeUnit.HOURS.toSeconds(24) - 1;
    }

    /**
     * 获取今天还剩多少秒
     *
     * @return
     */
    public static Long getRemainingSecondsOfToday() {
        return getTodayEndTimeSeconds() - DateUtils.currentTimeSeconds();
    }


    /**
     * 获取当天时间在今天开始时间后已过去多少秒
     *
     * @return
     */
    public static Long getPastSecondsOfStartToday() {
        return DateUtils.currentTimeSeconds() - getTodayStartTimeSeconds();
    }

    /**
     * 获取日期的结束时间 如某天 23:59:59
     *
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
        return (int) DateTime
                .now()
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
        return (long) hour * 60 * 60 * 1000 + (long) minute * 60 * 1000;
    }


    /**
     * 校验时分格式
     *
     * @param hourMinuteStr 时分时间字符串
     * @return [0] 时  [1] 分
     */
    public static int[] checkHourMinute(String hourMinuteStr) {
        if (StringUtils.isBlank(hourMinuteStr) || !hourMinuteStr.contains(TIME_SPLIT)) {
            throw new IllegalArgumentException("[%s]没有格式有误，没有包含%s".formatted(hourMinuteStr, TIME_SPLIT));
        }
        final String[] split = hourMinuteStr.split(TIME_SPLIT);
        if (split.length != 2 || !NumberUtil.isNumber(split[0].trim()) || !NumberUtil.isNumber(split[1].trim())) {
            throw new IllegalArgumentException(hourMinuteStr);
        }
        final int hour = Integer.parseInt(split[0].trim());
        final int minute = Integer.parseInt(split[1].trim());
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("【%s】小时只能位于0到23之间".formatted(hour));
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("【%s】分钟只能位于0到59之间".formatted(minute));
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
        return Objects.isNull(localDateTime) ? null : localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }

    /**
     * 使用全局设置时区LocalDateTime转Instant
     *
     * @param localDateTime
     * @return
     */
    public static Instant toZhCnInstant(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        } else {
            return localDateTime
                    .atZone(GLOBAL_ZONE_ID)
                    .toInstant();
        }
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


    public static Long toDefaultSeconds(LocalDateTime localDateTime) {
        Instant instant = toDefaultInstant(localDateTime);
        return Objects.isNull(instant) ? null : instant.toEpochMilli() / 1000;
    }

    /**
     * 使用北京时区LocalDateTime转秒时间戳
     *
     * @param localDateTime
     * @return
     */
    public static Long toZhCnSeconds(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(DEFAULT_GMT);
    }

    /**
     * 根据秒转换为标准北京时间
     *
     * @param seconds
     * @return
     */
    public static LocalDateTime ofSeconds(long seconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), DEFAULT_GMT);
    }

    /**
     * 时间戳转  LocalDate
     *
     * @param seconds
     * @return
     */
    public static LocalDate toLocalDate(long seconds) {
        Instant instant = Instant.ofEpochSecond(seconds);
        return instant
                .atZone(GLOBAL_ZONE_ID)
                .toLocalDate();
    }

    /**
     * 根据毫秒转换为标准北京时间
     *
     * @param millis
     * @return
     */
    public static LocalDateTime ofMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DEFAULT_GMT);
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
        return DateUtil.beginOfYear(instance.getTime());
    }

    /**
     * 获取下一年的第一天
     *
     * @return
     */
    public static Date nextYearEndDay() {
        final Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, DateUtil.year(new Date()) + 1);
        return DateUtil
                .endOfYear(instance.getTime());
    }

    /**
     * 获取本周的最后一天
     *
     * @return String
     **/
    public static LocalDateTime getWeekEnd() {
        return DateUtil
                .endOfWeek(new Date())
                .toLocalDateTime();
    }

    /**
     * 获取本周的最后一天
     *
     * @return String
     **/
    public static LocalDateTime getWeekEnd(Long currentSeconds) {
        return DateUtil
                .endOfWeek(new Date(currentSeconds * 1000))
                .toLocalDateTime();
    }

    /**
     * 获取本周的最后一天, 格式化为yyyyMMdd
     *
     * @return String
     **/
    public static Integer getWeekEndFormatYmd() {
        return Integer.parseInt(DateUtil
                .endOfWeek(new Date())
                .toLocalDateTime()
                .format(DAY_INTEGER_FORMATTER));
    }

    /**
     * 根据当前时间秒，获取本周的最后一天, 格式化为yyyyMMdd
     *
     * @return String
     **/
    public static Integer getWeekEndFormatYmd(Long currentSeconds) {
        return Integer.parseInt(getWeekEnd(currentSeconds).format(DAY_INTEGER_FORMATTER));
    }

    /**
     * 获取当前秒时间戳
     *
     * @return
     */
    public static Long currentTimeSeconds() {
        //        return System.currentTimeMillis() / 1000;
        return Instant
                .now()
                .getEpochSecond();
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
     * @param calibration 0 今天 -1 昨天 1 明天
     * @return
     */
    public static Integer currentYearMonthDay(int calibration) {
        return Integer.parseInt(DAY_INTEGER_FORMATTER.format(LocalDateTime
                .now()
                .plusDays(calibration)));
    }

    /**
     * 当前年月日 小时
     *
     * @param calibration 0 今天 -1 昨天 1 明天
     * @return
     */
    public static Integer currentYearMonthDayHour(int calibration) {
        return Integer.parseInt(HOUR_INTEGER_FORMATTER.format(LocalDateTime
                .now()
                .plusHours(calibration)));
    }

    /**
     * 当前年份周
     *
     * @param calibration 校准
     * @return {@link Integer}
     */
    public static Integer currentYearWeek(int calibration) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = LocalDate
                .now()
                .plusWeeks(calibration)
                .get(weekFields.weekOfWeekBasedYear());
        return weekNumber;
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
     * 当前年月日
     *
     * @return '20231212'
     */
    public static String formatYmd() {
        return DAY_INTEGER_FORMATTER.format(LocalDate.now());
    }

    /**
     * 当前年月
     *
     * @return '202312'
     */
    public static String currentYm() {
        return MONTH_INTEGER_FORMATTER.format(LocalDate.now());
    }

    /**
     * 根据秒时间戳格式化当前年月
     *
     * @return
     */
    public static Integer formatYearMonthDayBySeconds(Long seconds) {
        return Integer.parseInt(
                DAY_INTEGER_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("+8"))));
    }

    /**
     * 根据秒时间戳格式化当前年月
     *
     * @return
     */
    public static String formatYmdBySeconds(Long seconds) {
        return DAY_INTEGER_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("+8")));
    }

    /**
     * 根据毫秒时间戳格式化当前年月
     *
     * @param milli
     * @return
     */
    public static String formatYmdByMilli(Long milli) {
        return DAY_INTEGER_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneId.of("+8")));
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
     * 当前年月
     *
     * @param calibration 0 单月 -1 上个月 1 下个月
     * @return
     */
    public static Integer currentYearMonth(int calibration) {
        return Integer.parseInt(MONTH_INTEGER_FORMATTER.format(LocalDateTime
                .now()
                .plusMonths(calibration)));
    }

    /**
     * 根据秒时间戳格式化当前年月
     *
     * @return
     */
    public static Integer formatYearMonthBySeconds(Long seconds) {
        return Integer.parseInt(MONTH_INTEGER_FORMATTER.format(
                LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("+8"))));
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
    public static DateTimeFormatter getStandardFormatter() {
        return STANDARD_FORMATTER;
    }

    /**
     * 将秒标准格式化输出
     *
     * @param seconds
     * @return
     */
    public static String standardFormatSeconds(Long seconds) {
        return STANDARD_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), DEFAULT_GMT));
    }

    /**
     * 将秒标准格式化输出
     *
     * @param seconds
     * @return
     */
    public static String standardFormatSeconds(Long seconds, DateTimeFormatter formatter) {
        return formatter.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), DEFAULT_GMT));
    }

    /**
     * 将毫秒标准格式化输出
     *
     * @param millis
     * @return
     */
    public static String standardFormatMillis(Long millis) {
        return STANDARD_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DEFAULT_GMT));
    }


    /**
     * 将秒标准数字格式化输出
     *
     * @param seconds
     * @return
     */
    public static String standardNumberFormatSeconds(Long seconds) {
        return STANDARD_NUMBER_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), DEFAULT_GMT));
    }

    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 格式化时间
     *
     * @param time
     * @param format
     * @return
     */
    public static String formatDate(LocalDateTime time, DateTimeFormatter format) {
        return format.format(time);
    }

    /**
     * 格式化时间
     *
     * @return 'yyyy-MM-dd HH:mm:ss'
     */
    public static String formatDate(Date date) {
        return new SimpleDateFormat(DATE_TIME).format(date);
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
     *
     * @param time
     * @return
     */
    public static LocalDateTime getStartOfDay(LocalDateTime time) {
        return LocalDateTime.of(time.getYear(), time.getMonth(), time.getDayOfMonth(), 0, 0, 0, 0);
    }

    /**
     * 获取日期的结束时间 如某天 23:59:59
     *
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

    /**
     * 获取前后指定小时的 时间戳
     *
     * @param day
     * @param hour
     * @return
     */
    public static long assignDateSeconds(int day, int hour) {
        return LocalDateTime
                .of(
                        LocalDate
                                .now()
                                .plusDays(day), LocalTime.of(hour, 0)
                )
                .atZone(GLOBAL_ZONE_ID)
                .toEpochSecond();
    }

    /**
     * 今天日期往后推NUM天
     *
     * @param num
     * @param pattern
     * @return
     */
    public static String getDateFormatByPlus(int num, String pattern) {
        return LocalDateTime
                .now()
                .plusDays(num)
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param localDateTime
     * @return
     */
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime
                .atZone(GLOBAL_ZONE_ID)
                .toInstant());
    }

    /**
     * 判断两个时间戳是否是同一天
     *
     * @param startTimeSeconds
     * @param endTimeSeconds
     * @return
     */
    public static boolean isSameDay(long startTimeSeconds, long endTimeSeconds) {
        LocalDate day1 = Instant
                .ofEpochSecond(startTimeSeconds)
                .atZone(GLOBAL_ZONE_ID)
                .toLocalDate();
        LocalDate day2 = Instant
                .ofEpochSecond(endTimeSeconds)
                .atZone(GLOBAL_ZONE_ID)
                .toLocalDate();
        return day1.equals(day2);
    }

    /**
     * 计算两个秒时间戳之间的自然日间隔
     *
     * @param startTimeSeconds
     * @param endTimeSeconds
     * @return
     */
    public static long betweenDays(Long startTimeSeconds, Long endTimeSeconds) {
        // 转换为 LocalDate（自然日）
        LocalDate date1 = Instant
                .ofEpochSecond(startTimeSeconds)
                .atZone(GLOBAL_ZONE_ID)
                .toLocalDate();
        LocalDate date2 = Instant
                .ofEpochSecond(endTimeSeconds)
                .atZone(GLOBAL_ZONE_ID)
                .toLocalDate();
        return ChronoUnit.DAYS.between(date1, date2);
    }
}
