package com.ddf.common.captcha.properties;

import com.anji.captcha.model.common.CaptchaTypeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(AjCaptchaProperties.PREFIX)
public class AjCaptchaProperties {
    public static final String PREFIX = "aj.captcha";

    /**
     * 验证码类型.
     */
    private CaptchaTypeEnum type = CaptchaTypeEnum.DEFAULT;

    /**
     * 滑动拼图底图路径.
     */
    private String jigsaw = "";

    /**
     * 点选文字底图路径.
     */
    private String picClick = "";


    /**
     * 右下角水印文字(我的水印).
     */
    private String waterMark = "我的水印";

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
     */
    private Boolean aesStatus = true;

    /**
     * 滑块干扰项(0/1/2)
     */
    private String interferenceOptions = "0";

    /**
     * local缓存的阈值
     */
    private String cacheNumber = "1000";

    /**
     * 定时清理过期local缓存(单位秒)
     */
    private String timingClear = "180";

    /**
     * 缓存类型redis/local/....
     */
    private StorageType cacheType = StorageType.local;
    /**
     * 历史数据清除开关
     */
    private boolean historyDataClearEnable = false;

    /**
     * 一分钟内接口请求次数限制 开关
     */
    private boolean reqFrequencyLimitEnable = false;

    /***
     * 一分钟内check接口失败次数
     */
    private int reqGetLockLimit = 5;
    /**
     *
     */
    private int reqGetLockSeconds = 300;

    /***
     * get接口一分钟内限制访问数
     */
    private int reqGetMinuteLimit = 100;
    private int reqCheckMinuteLimit = 100;
    private int reqVerifyMinuteLimit = 100;

    public boolean isHistoryDataClearEnable() {
        return historyDataClearEnable;
    }
    /**
     * @param historyDataClearEnable 参数
     */
    public void setHistoryDataClearEnable(boolean historyDataClearEnable) {
        this.historyDataClearEnable = historyDataClearEnable;
    }

    public boolean isReqFrequencyLimitEnable() {
        return reqFrequencyLimitEnable;
    }

    public boolean getReqFrequencyLimitEnable() {
        return reqFrequencyLimitEnable;
    }
    /**
     * @param reqFrequencyLimitEnable 参数
     */
    public void setReqFrequencyLimitEnable(boolean reqFrequencyLimitEnable) {
        this.reqFrequencyLimitEnable = reqFrequencyLimitEnable;
    }

    public int getReqGetLockLimit() {
        return reqGetLockLimit;
    }
    /**
     * @param reqGetLockLimit 参数
     */
    public void setReqGetLockLimit(int reqGetLockLimit) {
        this.reqGetLockLimit = reqGetLockLimit;
    }

    public int getReqGetLockSeconds() {
        return reqGetLockSeconds;
    }
    /**
     * @param reqGetLockSeconds 参数
     */
    public void setReqGetLockSeconds(int reqGetLockSeconds) {
        this.reqGetLockSeconds = reqGetLockSeconds;
    }

    public int getReqGetMinuteLimit() {
        return reqGetMinuteLimit;
    }
    /**
     * @param reqGetMinuteLimit 参数
     */
    public void setReqGetMinuteLimit(int reqGetMinuteLimit) {
        this.reqGetMinuteLimit = reqGetMinuteLimit;
    }

    public int getReqCheckMinuteLimit() {
        return reqGetMinuteLimit;
    }
    /**
     * @param reqCheckMinuteLimit 参数
     */
    public void setReqCheckMinuteLimit(int reqCheckMinuteLimit) {
        this.reqCheckMinuteLimit = reqCheckMinuteLimit;
    }

    public int getReqVerifyMinuteLimit() {
        return reqVerifyMinuteLimit;
    }
    /**
     * @param reqVerifyMinuteLimit 参数
     */
    public void setReqVerifyMinuteLimit(int reqVerifyMinuteLimit) {
        this.reqVerifyMinuteLimit = reqVerifyMinuteLimit;
    }

    public enum StorageType {
        /**
         * 内存.
         */
        local,
        /**
         * redis.
         */
        redis,
        /**
         * 其他.
         */
        other,
    }

    public static String getPREFIX() {
        return PREFIX;
    }

    public CaptchaTypeEnum getType() {
        return type;
    }
    /**
     * @param type 参数
     */
    public void setType(CaptchaTypeEnum type) {
        this.type = type;
    }

    public String getJigsaw() {
        return jigsaw;
    }
    /**
     * @param jigsaw 参数
     */
    public void setJigsaw(String jigsaw) {
        this.jigsaw = jigsaw;
    }

    public String getPicClick() {
        return picClick;
    }
    /**
     * @param picClick 参数
     */
    public void setPicClick(String picClick) {
        this.picClick = picClick;
    }

    public String getWaterMark() {
        return waterMark;
    }
    /**
     * @param waterMark 参数
     */
    public void setWaterMark(String waterMark) {
        this.waterMark = waterMark;
    }

    public String getWaterFont() {
        return waterFont;
    }
    /**
     * @param waterFont 参数
     */
    public void setWaterFont(String waterFont) {
        this.waterFont = waterFont;
    }

    public String getFontType() {
        return fontType;
    }
    /**
     * @param fontType 参数
     */
    public void setFontType(String fontType) {
        this.fontType = fontType;
    }

    public String getSlipOffset() {
        return slipOffset;
    }
    /**
     * @param slipOffset 参数
     */
    public void setSlipOffset(String slipOffset) {
        this.slipOffset = slipOffset;
    }

    public Boolean getAesStatus() {
        return aesStatus;
    }
    /**
     * @param aesStatus 参数
     */
    public void setAesStatus(Boolean aesStatus) {
        this.aesStatus = aesStatus;
    }

    public StorageType getCacheType() {
        return cacheType;
    }
    /**
     * @param cacheType 参数
     */
    public void setCacheType(StorageType cacheType) {
        this.cacheType = cacheType;
    }

    public String getInterferenceOptions() {
        return interferenceOptions;
    }
    /**
     * @param interferenceOptions 参数
     */
    public void setInterferenceOptions(String interferenceOptions) {
        this.interferenceOptions = interferenceOptions;
    }

    public String getCacheNumber() {
        return cacheNumber;
    }
    /**
     * @param cacheNumber 参数
     */
    public void setCacheNumber(String cacheNumber) {
        this.cacheNumber = cacheNumber;
    }

    public String getTimingClear() {
        return timingClear;
    }
    /**
     * @param timingClear 参数
     */
    public void setTimingClear(String timingClear) {
        this.timingClear = timingClear;
    }

    @Override
    public String toString() {
        return "\nAjCaptchaProperties{" +
                "type=" + type +
                ", jigsaw='" + jigsaw + '\'' +
                ", picClick='" + picClick + '\'' +
                ", waterMark='" + waterMark + '\'' +
                ", waterFont='" + waterFont + '\'' +
                ", fontType='" + fontType + '\'' +
                ", slipOffset='" + slipOffset + '\'' +
                ", aesStatus=" + aesStatus +
                ", interferenceOptions='" + interferenceOptions + '\'' +
                ", cacheNumber='" + cacheNumber + '\'' +
                ", timingClear='" + timingClear + '\'' +
                ", cacheType=" + cacheType +
                ", reqFrequencyLimitEnable=" + reqFrequencyLimitEnable +
                ", reqGetLockLimit=" + reqGetLockLimit +
                ", reqGetLockSeconds=" + reqGetLockSeconds +
                ", reqGetMinuteLimit=" + reqGetMinuteLimit +
                ", reqCheckMinuteLimit=" + reqCheckMinuteLimit +
                ", reqVerifyMinuteLimit=" + reqVerifyMinuteLimit +
                '}';
    }
}