package com.ddf.boot.common.api.util;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DateUtils {

    public static final String TIME_SPLIT = ":";

    public static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter STANDARD_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final DateTimeFormatter DAY_INTEGER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final DateTimeFormatter DAY_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter HOUR_INTEGER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    public static final DateTimeFormatter MONTH_INTEGER_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 全局默认时区（Asia/Shanghai），所有无显式时区参数的方法统一使用此时区
     */
    public static final ZoneId GLOBAL_ZONE_ID = ZoneId.of("Asia/Shanghai");

    /**
     * 全局默认时区时钟对象
     */
    public static final Clock CLOCK = Clock.system(GLOBAL_ZONE_ID);

    /**
     * 获取本月第一天
     */
    public static Date getMonthFirstDate(Date time) {
        return DateUtil.beginOfMonth(time);
    }

    /**
     * 获取本月第一天秒时间戳
     *
     * @param timestampSeconds 秒时间戳
     */
    public static Long getMonthFirstSeconds(Long timestampSeconds) {
        return getMonthFirstDate(new Date(timestampSeconds * 1000)).getTime() / 1000;
    }

    /**
     * 根据指定月份获取本月第一天秒时间戳
     *
     * @param month 月格式化形式，格式为yyyyMM
     */
    public static Long getMonthFirstSecondsByDayFormatter(Integer month) {
        return LocalDate.parse(month + "01", DAY_INTEGER_FORMATTER)
                .atStartOfDay(GLOBAL_ZONE_ID)
                .toEpochSecond();
    }

    /**
     * 根据指定天获取本天第一天秒时间戳
     *
     * @param monthDay 天格式化形式，格式为yyyyMMdd
     */
    public static Long getDayFirstSecondsByDayFormatter(Integer monthDay) {
        return LocalDate.parse(String.valueOf(monthDay), DAY_INTEGER_FORMATTER)
                .atStartOfDay(GLOBAL_ZONE_ID)
                .toEpochSecond();
    }

    /**
     * 根据指定天获取本天最后一秒时间戳
     *
     * @param monthDay 天格式化形式，格式为yyyyMMdd
     */
    public static Long getDayLastSecondsByDayFormatter(Integer monthDay) {
        return LocalDate.parse(String.valueOf(monthDay), DAY_INTEGER_FORMATTER)
                .atTime(LocalTime.MAX)
                .atZone(GLOBAL_ZONE_ID)
                .toEpochSecond();
    }

    /**
     * 获取本月最后一天
     */
    public static Date getMonthLastDate(Date time) {
        return DateUtil.endOfMonth(time);
    }

    /**
     * 获取日期的起始时间 如某天 00:00:00
     */
    public static Date getStartOfDay(Date time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(GLOBAL_ZONE_ID));
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 今天的开始时间戳（秒）
     */
    public static Long getTodayStartTimeSeconds() {
        return LocalDate.now(GLOBAL_ZONE_ID).atStartOfDay(GLOBAL_ZONE_ID).toEpochSecond();
    }

    /**
     * 今天的开始时间戳（毫秒）
     */
    public static Long getTodayStartTimeMillis() {
        return LocalDate.now(GLOBAL_ZONE_ID).atStartOfDay(GLOBAL_ZONE_ID).toInstant().toEpochMilli();
    }

    /**
     * 今天的结束时间戳（秒）
     */
    public static Long getTodayEndTimeSeconds() {
        return getTodayStartTimeSeconds() + TimeUnit.HOURS.toSeconds(24) - 1;
    }

    /**
     * 获取今天还剩多少秒
     */
    public static Long getRemainingSecondsOfToday() {
        return getTodayEndTimeSeconds() - currentTimeSeconds();
    }

    /**
     * 获取当天时间在今天开始时间后已过去多少秒
     */
    public static Long getPastSecondsOfStartToday() {
        return currentTimeSeconds() - getTodayStartTimeSeconds();
    }

    /**
     * 获取日期的结束时间 如某天 23:59:59
     */
    public static Date getEndOfDay(Date time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(GLOBAL_ZONE_ID));
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * 基于生日，计算星座
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
     */
    public static Integer getAge(@Nullable Date birthDay) {
        if (Objects.isNull(birthDay)) {
            return 0;
        }
        DateTime dateTime = new DateTime(birthDay.getTime());
        return (int) DateTime.now().between(dateTime).betweenYear(false);
    }

    /**
     * 获取指定日期对应时:分的毫秒值
     */
    public static long getMillionsOfHourMinute(Date date) {
        final Calendar instance = Calendar.getInstance(TimeZone.getTimeZone(GLOBAL_ZONE_ID));
        instance.setTime(date);
        return instance.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L
                + instance.get(Calendar.MINUTE) * 60 * 1000L;
    }

    /**
     * 获取时分对应的毫秒值，字符格式固定为时:分
     *
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
     * @return [0] 时 [1] 分
     */
    public static int[] checkHourMinute(String hourMinuteStr) {
        if (StringUtils.isBlank(hourMinuteStr) || !hourMinuteStr.contains(TIME_SPLIT)) {
            throw new IllegalArgumentException(String.format("[%s]格式有误，没有包含%s", hourMinuteStr, TIME_SPLIT));
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
     * 使用全局默认时区 LocalDateTime 转 Instant
     */
    public static Instant toDefaultInstant(LocalDateTime localDateTime) {
        return Objects.isNull(localDateTime) ? null : localDateTime.atZone(GLOBAL_ZONE_ID).toInstant();
    }

    /**
     * 使用全局默认时区 LocalDateTime 转毫秒时间戳
     */
    public static Long toDefaultMills(LocalDateTime localDateTime) {
        final Instant instant = toDefaultInstant(localDateTime);
        return Objects.isNull(instant) ? null : instant.toEpochMilli();
    }

    /**
     * 使用全局默认时区 LocalDateTime 转秒时间戳
     */
    public static Long toDefaultSeconds(LocalDateTime localDateTime) {
        Instant instant = toDefaultInstant(localDateTime);
        return Objects.isNull(instant) ? null : instant.getEpochSecond();
    }

    /**
     * 根据秒时间戳转换为全局默认时区 LocalDateTime
     */
    public static LocalDateTime ofSeconds(long seconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), GLOBAL_ZONE_ID);
    }

    /**
     * 秒时间戳转全局默认时区 LocalDate
     */
    public static LocalDate toLocalDate(long seconds) {
        return Instant.ofEpochSecond(seconds).atZone(GLOBAL_ZONE_ID).toLocalDate();
    }

    /**
     * 根据毫秒时间戳转换为全局默认时区 LocalDateTime
     */
    public static LocalDateTime ofMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), GLOBAL_ZONE_ID);
    }

    /**
     * 获取下个星期的第一天
     */
    public static Date nextWeekFirstDay() {
        return DateUtil.beginOfWeek(DateUtil.nextWeek());
    }

    /**
     * 获取下个星期的最后一天
     */
    public static Date nextWeekEndDay() {
        return DateUtil.endOfWeek(DateUtil.nextWeek());
    }

    /**
     * 获取下个月的第一天
     */
    public static Date nextMonthFirstDay() {
        return DateUtil.beginOfMonth(DateUtil.nextMonth());
    }

    /**
     * 获取下个月的最后一天
     */
    public static Date nextMonthEndDay() {
        return DateUtil.endOfMonth(DateUtil.nextMonth());
    }

    /**
     * 获取下一年的第一天
     */
    public static Date nextYearFirstDay() {
        final Calendar instance = Calendar.getInstance(TimeZone.getTimeZone(GLOBAL_ZONE_ID));
        instance.set(Calendar.YEAR, DateUtil.year(new Date()) + 1);
        return DateUtil.beginOfYear(instance).getTime();
    }

    /**
     * 获取下一年的最后一天
     */
    public static Date nextYearEndDay() {
        final Calendar instance = Calendar.getInstance(TimeZone.getTimeZone(GLOBAL_ZONE_ID));
        instance.set(Calendar.YEAR, DateUtil.year(new Date()) + 1);
        return DateUtil.endOfYear(instance).getTime();
    }

    /**
     * 获取本周的最后一天
     */
    public static LocalDateTime getWeekEnd() {
        return DateUtil.endOfWeek(new Date()).toLocalDateTime();
    }

    /**
     * 获取指定时间所在周的最后一天
     */
    public static LocalDateTime getWeekEnd(Long currentSeconds) {
        return DateUtil.endOfWeek(new Date(currentSeconds * 1000)).toLocalDateTime();
    }

    /**
     * 获取本周的最后一天，格式化为 yyyyMMdd
     */
    public static Integer getWeekEndFormatYmd() {
        return Integer.parseInt(DateUtil.endOfWeek(new Date()).toLocalDateTime().format(DAY_INTEGER_FORMATTER));
    }

    /**
     * 根据当前时间秒，获取所在周的最后一天，格式化为 yyyyMMdd
     */
    public static Integer getWeekEndFormatYmd(Long currentSeconds) {
        return Integer.parseInt(getWeekEnd(currentSeconds).format(DAY_INTEGER_FORMATTER));
    }

    /**
     * 获取当前毫秒时间戳
     */
    public static Long currentTimeMills() {
        return CLOCK.instant().toEpochMilli();
    }

    /**
     * 获取当前秒时间戳
     */
    public static Long currentTimeSeconds() {
        return CLOCK.instant().getEpochSecond();
    }

    /**
     * 获取指定时间所在月的最后一天
     */
    public static LocalDateTime getMonthEnd(Long currentSeconds) {
        return DateUtil.endOfMonth(new Date(currentSeconds * 1000)).toLocalDateTime();
    }

    /**
     * 获取本月的最后一天，格式化为 yyyyMMdd
     */
    public static Integer getMonthEndFormatYmd() {
        return Integer.parseInt(DateUtil.endOfMonth(new Date()).toLocalDateTime().format(DAY_INTEGER_FORMATTER));
    }

    /**
     * 根据当前时间秒，获取所在月的最后一天，格式化为 yyyyMMdd
     */
    public static Integer getMonthEndFormatYmd(Long currentSeconds) {
        return Integer.parseInt(getMonthEnd(currentSeconds).format(DAY_INTEGER_FORMATTER));
    }

    /**
     * 当前年月日，格式 yyyyMMdd
     */
    public static Integer currentYearMonthDay() {
        return Integer.parseInt(DAY_INTEGER_FORMATTER.format(LocalDateTime.now(GLOBAL_ZONE_ID)));
    }

    /**
     * 当前年月日（校准偏移）
     *
     * @param calibration 0 今天, -1 昨天, 1 明天
     */
    public static Integer currentYearMonthDay(int calibration) {
        return Integer.parseInt(DAY_INTEGER_FORMATTER.format(LocalDateTime.now(GLOBAL_ZONE_ID).plusDays(calibration)));
    }

    /**
     * 当前年月日小时（校准偏移）
     *
     * @param calibration 0 当前小时, -1 前一小时, 1 后一小时
     */
    public static Integer currentYearMonthDayHour(int calibration) {
        return Integer.parseInt(HOUR_INTEGER_FORMATTER.format(LocalDateTime.now(GLOBAL_ZONE_ID).plusHours(calibration)));
    }

    /**
     * 当前年份周（校准偏移）
     *
     * @param calibration 0 本周, -1 上周, 1 下周
     */
    public static Integer currentYearWeek(int calibration) {
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        return LocalDate.now(GLOBAL_ZONE_ID).plusWeeks(calibration).get(weekFields.weekOfWeekBasedYear());
    }

    /**
     * 格式化 LocalDateTime 为 yyyyMMdd 整数
     */
    public static Integer formatYearMonthDay(LocalDateTime localDateTime) {
        return Integer.parseInt(DAY_INTEGER_FORMATTER.format(localDateTime));
    }

    /**
     * 当前年月日，格式 yyyyMMdd 字符串
     */
    public static String formatYmd() {
        return DAY_INTEGER_FORMATTER.format(LocalDate.now(GLOBAL_ZONE_ID));
    }

    /**
     * 当前年月，格式 yyyyMM 字符串
     */
    public static String currentYm() {
        return MONTH_INTEGER_FORMATTER.format(LocalDate.now(GLOBAL_ZONE_ID));
    }

    /**
     * 根据秒时间戳格式化为 yyyyMMdd 整数
     */
    public static Integer formatYearMonthDayBySeconds(Long seconds) {
        return Integer.parseInt(DAY_INTEGER_FORMATTER.format(
                LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), GLOBAL_ZONE_ID)));
    }

    /**
     * 根据秒时间戳格式化为 yyyyMMdd 字符串
     */
    public static String formatYmdBySeconds(Long seconds) {
        return DAY_INTEGER_FORMATTER.format(
                LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), GLOBAL_ZONE_ID));
    }

    /**
     * 根据毫秒时间戳格式化为 yyyyMMdd 整数
     */
    public static Integer formatYearMonthDayByMillis(Long milli) {
        return Integer.parseInt(DAY_INTEGER_FORMATTER.format(
                LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), GLOBAL_ZONE_ID)));
    }

    /**
     * 根据毫秒时间戳格式化为 yyyyMMdd 字符串
     */
    public static String formatYmdByMilli(Long milli) {
        return DAY_INTEGER_FORMATTER.format(
                LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), GLOBAL_ZONE_ID));
    }

    /**
     * 当前年月，格式 yyyyMM 整数
     */
    public static Integer currentYearMonth() {
        return Integer.parseInt(MONTH_INTEGER_FORMATTER.format(LocalDateTime.now(GLOBAL_ZONE_ID)));
    }

    /**
     * 当前年月（校准偏移）
     *
     * @param calibration 0 当月, -1 上月, 1 下月
     */
    public static Integer currentYearMonth(int calibration) {
        return Integer.parseInt(MONTH_INTEGER_FORMATTER.format(LocalDateTime.now(GLOBAL_ZONE_ID).plusMonths(calibration)));
    }

    /**
     * 根据秒时间戳格式化为 yyyyMM 整数
     */
    public static Integer formatYearMonthBySeconds(Long seconds) {
        return Integer.parseInt(MONTH_INTEGER_FORMATTER.format(
                LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), GLOBAL_ZONE_ID)));
    }

    /**
     * 格式化 LocalDateTime 为 yyyyMM 整数
     */
    public static Integer formatYearMonth(LocalDateTime localDateTime) {
        return Integer.parseInt(MONTH_INTEGER_FORMATTER.format(localDateTime));
    }

    /**
     * 返回标准日期时间格式化器
     */
    public static DateTimeFormatter getStandardFormatter() {
        return STANDARD_FORMATTER;
    }

    /**
     * 将秒时间戳标准格式化输出
     */
    public static String standardFormatSeconds(Long seconds) {
        return STANDARD_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), GLOBAL_ZONE_ID));
    }

    /**
     * 将秒时间戳按指定格式输出
     */
    public static String standardFormatSeconds(Long seconds, DateTimeFormatter formatter) {
        return formatter.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), GLOBAL_ZONE_ID));
    }

    /**
     * 将毫秒时间戳标准格式化输出
     */
    public static String standardFormatMillis(Long millis) {
        return STANDARD_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), GLOBAL_ZONE_ID));
    }

    /**
     * 将秒时间戳标准数字格式化输出
     */
    public static String standardNumberFormatSeconds(Long seconds) {
        return STANDARD_NUMBER_FORMATTER.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), GLOBAL_ZONE_ID));
    }

    /**
     * 使用全局默认时区格式化 Date
     */
    public static String formatDate(Date date, String format) {
        return LocalDateTime.ofInstant(date.toInstant(), GLOBAL_ZONE_ID)
                .format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 格式化 LocalDateTime
     */
    public static String formatDate(LocalDateTime time, DateTimeFormatter format) {
        return format.format(time);
    }

    /**
     * 使用全局默认时区标准格式化 Date
     */
    public static String formatDate(Date date) {
        return STANDARD_FORMATTER.format(LocalDateTime.ofInstant(date.toInstant(), GLOBAL_ZONE_ID));
    }

    /**
     * Date 转全局默认时区 LocalDateTime
     */
    public static LocalDateTime date2CnLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), GLOBAL_ZONE_ID);
    }

    /**
     * 获取日期的起始时间 如某天 00:00:00
     */
    public static LocalDateTime getStartOfDay(LocalDateTime time) {
        return time.with(LocalTime.MIN);
    }

    /**
     * 获取日期的结束时间 如某天 23:59:59
     */
    public static LocalDateTime getEndOfDay(LocalDateTime time) {
        return time.with(LocalTime.MAX);
    }

    /**
     * 获取前后指定小时的秒时间戳
     */
    public static long assignDateSeconds(int day, int hour) {
        return LocalDateTime.of(LocalDate.now(GLOBAL_ZONE_ID).plusDays(day), LocalTime.of(hour, 0))
                .atZone(GLOBAL_ZONE_ID)
                .toEpochSecond();
    }

    /**
     * 今天日期往后推 N 天，按指定格式返回
     */
    public static String getDateFormatByPlus(int num, String pattern) {
        return LocalDateTime.now(GLOBAL_ZONE_ID).plusDays(num)
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * LocalDateTime 转 Date
     */
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(GLOBAL_ZONE_ID).toInstant());
    }

    /**
     * 判断两个秒时间戳是否是同一天（全局默认时区）
     */
    public static boolean isSameDay(long startTimeSeconds, long endTimeSeconds) {
        LocalDate day1 = Instant.ofEpochSecond(startTimeSeconds).atZone(GLOBAL_ZONE_ID).toLocalDate();
        LocalDate day2 = Instant.ofEpochSecond(endTimeSeconds).atZone(GLOBAL_ZONE_ID).toLocalDate();
        return day1.equals(day2);
    }

    /**
     * 计算两个秒时间戳之间的自然日间隔（全局默认时区）
     */
    public static long betweenDays(Long startTimeSeconds, Long endTimeSeconds) {
        LocalDate date1 = Instant.ofEpochSecond(startTimeSeconds).atZone(GLOBAL_ZONE_ID).toLocalDate();
        LocalDate date2 = Instant.ofEpochSecond(endTimeSeconds).atZone(GLOBAL_ZONE_ID).toLocalDate();
        return ChronoUnit.DAYS.between(date1, date2);
    }

    /**
     * 工作日还是休息日
     *
     * @param i 1: 昨天, 0: 今天
     */
    public static String isDayWorkDay(Integer i) {
        LocalDate target = LocalDate.now(GLOBAL_ZONE_ID).minusDays(i);
        DayOfWeek dayOfWeek = target.getDayOfWeek();
        // 1: 周末, 0: 工作日
        int seed = dayOfWeek.getValue() > 5 ? 1 : 0;
        return switch (seed) {
            case 0 -> "weekday";
            case 1 -> "weekend";
            default -> "";
        };
    }
}
