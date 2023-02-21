package com.ddf.boot.common.rocketmq.dto;

import java.util.concurrent.TimeUnit;
import lombok.Getter;

/**
 * <p>RocketMQ延迟消息时间对应的级别，这种定义方式方便业务方只关心时间，而不关心具体内部实现</p >
 * 注意，不支持任意级别的消息延迟，目前RocketMQ默认只预留了18个level的延迟，分别对应不同的延迟时间，0代表不延迟
 *
 *
 * 可以在服务端rocketmq-broker的配置文件中配置如下内容，可以自定义level对应的延迟时间。默认如下
 * messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
 * 时间单位支持：s、m、h、d，分别表示秒、分、时、天；
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/09/15 20:30
 */
public enum RocketMQDelayTimeMapping {

    /**
     * 延迟级别和对应延迟时间，这里只是方便对照而已，并不是说这里添加或者改变值就会按照里面的属性来自动适应
     */
    NO_DELAY(0, 0L, "实时不延迟"),
    S1(1, 1L, "延迟1秒"),
    S5(2, 5L, "延迟5秒"),
    S10(3, 10L, "延迟10秒"),
    S30(4, 30L, "延迟30秒"),
    M1(5, TimeUnit.MINUTES.toSeconds(1), "延迟1分钟"),
    M2(6, TimeUnit.MINUTES.toSeconds(2), "延迟2分钟"),
    M3(7, TimeUnit.MINUTES.toSeconds(3), "延迟3分钟"),
    M4(8, TimeUnit.MINUTES.toSeconds(4), "延迟4分钟"),
    M5(9, TimeUnit.MINUTES.toSeconds(5), "延迟5分钟"),
    M6(10, TimeUnit.MINUTES.toSeconds(6), "延迟6分钟"),
    M7(11, TimeUnit.MINUTES.toSeconds(7), "延迟7分钟"),
    M8(12, TimeUnit.MINUTES.toSeconds(8), "延迟8分钟"),
    M9(13, TimeUnit.MINUTES.toSeconds(9), "延迟9分钟"),
    M10(14, TimeUnit.MINUTES.toSeconds(10), "延迟10分钟"),
    M20(15, TimeUnit.MINUTES.toSeconds(20), "延迟20分钟"),
    M30(16, TimeUnit.MINUTES.toSeconds(30), "延迟30分钟"),
    H1(17, TimeUnit.HOURS.toSeconds(1), "延迟1小时"),
    H2(18, TimeUnit.HOURS.toSeconds(2), "延迟2小时"),

    ;

    @Getter
    private final Integer level;
    @Getter
    private final Long seconds;

    @Getter
    private final String desc;

    RocketMQDelayTimeMapping(Integer level, Long seconds, String desc) {
        this.level = level;
        this.seconds = seconds;
        this.desc = desc;
    }
}
