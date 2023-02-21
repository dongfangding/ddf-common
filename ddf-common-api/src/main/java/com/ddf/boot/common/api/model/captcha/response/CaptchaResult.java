package com.ddf.boot.common.api.model.captcha.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * <p>验证码返回结果</p >
 *
 * @author Snowball
 * @version 1.0
     * @date 2021/03/02 16:`19`
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
     * 原始图片base64编码， 验证码图片一般是这个字段。
     * 滑块验证码时，为滑块验证码底图图片，即一张图里多个缺失的图形文案
     */
    private String originalImageBase64;

    /**
     * 滑块验证码时可用，其它暂无用处。为滑块图片，即滑块验证码底图中缺失的那一块图案。
     */
    private String imageBase64;

    /**
     * 点选文字顺序， 文字点选验证码可用
     */
    private List<String> wordList;

    /**
     * 唯一表单标识, 使用这个和验证码做对应关系， 在表单中需要回传这个值
     */
    private String uuid;

    /**
     * 图片编码base64的前缀，如`data:image/jpeg;base64,` + 真实的base64图片编码为一个完整版的格式，可以还原成图片
     */
    private String prefix = "data:image/jpeg;base64,";

}
