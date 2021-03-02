package com.ddf.common.captcha.model;

import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 16:19
 */
@Data
public class CaptchaResult {

    /**
     * 图片的宽度
     */
    protected int width;
    /**
     * 图片的高度
     */
    protected int height;

    /**
     * 验证码
     * 如果是普通验证码，图片上的验证码和这个值是一样的，用这个即可代表图片中存储的字符
     * 如果是计算型验证码， 这个值为计算结果
     */
    private String verifyCode;

    /**
     * 图片的base64编码
     */
    private String imageBase64;

}
