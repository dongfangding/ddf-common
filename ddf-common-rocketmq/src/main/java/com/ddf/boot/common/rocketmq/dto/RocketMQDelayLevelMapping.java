package com.ddf.boot.common.rocketmq.dto;

import lombok.Getter;

/**
 * <p>RocketMQ延迟消息级别</p >
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
public enum RocketMQDelayLevelMapping {

    /**
     * 延迟级别和对应延迟时间，这里只是方便对照而已，并不是说这里添加或者改变值就会按照里面的属性来自动适应
     */
    NO_DELAY(0, "实时不延迟"),
    LEVEL1(1, "延迟1秒"),
    LEVEL2(2, "延迟5秒"),
    LEVEL3(3, "延迟10秒"),
    LEVEL4(4, "延迟30秒"),
    LEVEL5(5, "延迟1分钟"),
    LEVEL6(6, "延迟2分钟"),
    LEVEL7(7, "延迟3分钟"),
    LEVEL8(8, "延迟4分钟"),
    LEVEL9(9, "延迟5分钟"),
    LEVEL10(10, "延迟6分钟"),
    LEVEL11(11, "延迟7分钟"),
    LEVEL12(12, "延迟8分钟"),
    LEVEL13(13, "延迟9分钟"),
    LEVEL14(14, "延迟10分钟"),
    LEVEL15(15, "延迟20分钟"),
    LEVEL16(16, "延迟30分钟"),
    LEVEL17(17, "延迟1小时"),
    LEVEL18(18, "延迟2小时"),

    ;

    @Getter
    private final Integer level;

    @Getter
    private final String desc;

    RocketMQDelayLevelMapping(Integer level, String desc) {
        this.level = level;
        this.desc = desc;
    }
}
