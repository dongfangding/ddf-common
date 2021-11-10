package com.ddf.common.captcha.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 16:19
 */
@Data
public class CaptchaResult implements Serializable {

    /**
     * 图片的宽度
     */
    protected int width;
    /**
     * 图片的高度
     */
    protected int height;

    /**
     * 验证码, 普通验证码和计算型验证码可用
     * 如果是普通验证码，图片上的验证码和这个值是一样的，用这个即可代表图片中存储的字符
     * 如果是计算型验证码， 这个值为计算结果
     */
    @JsonIgnore
    private String verifyCode;

    /**
     * 图片的base64编码
     */
    private String imageBase64;

    /**
     * 滑块图片base64， 图片滑动验证码可用
     */
    private String jigsawImageBase64;

    /**
     * 点选文字顺序， 文字点选验证码可用
     */
    private List<String> wordList;

    /**
     * 唯一表单token, 使用这个和验证码做对应关系， 在表单中需要回传这个值
     */
    private String token;

}
