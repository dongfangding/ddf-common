package com.ddf.common.captcha.properties;

import lombok.Data;

/**
 * <p>基于google的Kaptcha验证码属性</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 15:07
 */
@Data
public class KaptchaProperties {

    /**
     * 宽度
     */
    private Integer width = 200;
    /**
     * 高度
     */
    private Integer height = 50;

    /**
     * 图片样式
     * 水纹 com.google.code.kaptcha.impl.WaterRipple   (1)
     * 阴影 com.google.code.kaptcha.impl.ShadowGimpy   (2)
     * 鱼眼 com.google.code.kaptcha.impl.FishEyeGimpy  (丑)
     */
    private String obscurificator = "com.google.code.kaptcha.impl.WaterRipple";

    /**
     * 内容
     */
    private Content content = new Content();
    /**
     * 背景色
     */
    private BackgroundColor backgroundColor = new BackgroundColor();
    /**
     * 字体
     */
    private Font font = new Font();
    /**
     * 边框
     */
    private Border border = new Border();


    @Data
    public static class BackgroundColor {

        /**
         * 开始渐变色
         */
        private String from = "lightGray";
        /**
         * 结束渐变色
         */
        private String to = "white";

    }


    @Data
    public static class Content {

        /**
         * 内容源
         */
        private String source = "abcdefghjklmnopqrstuvwxyz23456789";
        /**
         * 内容长度
         */
        private Integer length = 4;
        /**
         * 内容间隔
         */
        private Integer space = 2;

    }


    @Data
    public static class Border {

        /**
         * 是否开启
         */
        private Boolean enabled = true;
        /**
         * 颜色
         */
        private String color = "black";
        /**
         * 厚度
         */
        private Integer thickness = 1;

    }


    @Data
    public static class Font {

        /**
         * 名称
         */
        private String name = "Arial";
        /**
         * 颜色
         */
        private String color = "black";
        /**
         * 大小
         */
        private Integer size = 40;

    }

}
