package com.ddf.common.captcha.properties;

import com.anji.captcha.model.common.CaptchaTypeEnum;
import lombok.Data;

@Data
public class DefaultAjCaptchaProperties {
    /**
     * 验证码类型.
     */
    private CaptchaTypeEnum type = CaptchaTypeEnum.CLICKWORD;

    /**
     * 滑动拼图底图路径.
     */
    private String jigsaw = "classpath:images/jigsaw";

    /**
     * 点选文字底图路径.
     */
    private String picClick = "classpath:images/pic-click";

    /**
     * 右下角水印文字.
     */
    private String waterMark = "";

    /**
     * 右下角水印字体(文泉驿正黑).
     */
    private String waterFont = "WenQuanZhengHei.ttf";

    /**
     * 点选文字验证码的文字字体(文泉驿正黑).
     */
    private String fontType = "WenQuanZhengHei.ttf";

    /**
     * 校验滑动拼图允许误差偏移量(默认5像素).
     */
    private String slipOffset = "5";

    /**
     * aes加密坐标开启或者禁用(true|false).
     * 开启的话前端需要对坐标数组进行aes加密然后回传
     */
    private Boolean aesStatus = false;

    /**
     * 滑块干扰项(0/1/2)
     */
    private String interferenceOptions = "2";

    /**
     * local缓存的阈值
     */
    private String cacheNumber = "1000";

    /**
     * 定时清理过期local缓存(单位秒)
     */
    private String timingClear = "180";

    /**
     * 历史数据清除开关
     */
    private boolean historyDataClearEnable = true;

    /**
     * 一分钟内接口请求次数限制 开关
     */
    private boolean reqFrequencyLimitEnable = false;

    /***
     * 一分钟内check接口失败次数
     */
    private int reqGetLockLimit = 3;

    /**
     * 一分钟内check失败超限锁定
     */
    private int reqGetLockSeconds = 300;

    /***
     * get接口一分钟内限制访问数
     */
    private int reqGetMinuteLimit = 100;
    private int reqCheckMinuteLimit = 100;
    private int reqVerifyMinuteLimit = 100;
}
