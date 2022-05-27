package com.ddf.common.captcha.model.response;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * <p>验证码应用返回结果，屏蔽某些关键信息</p >
 *
 * @author Snowball
 * @version 1.0
     * @date 2021/03/02 16:`19`
 */
@Data
public class ApplicationCaptchaResult implements Serializable {

    /**
     * 图片的宽度
     */
    protected int width;
    /**
     * 图片的高度
     */
    protected int height;

    /**
     * 原始图片base64编码， 某些情况下与实际使用的不一样，比如滑块图片，因为实际使用的是缺少的
     */
    private String originalImageBase64;

    /**
     * 图片的base64编码
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

    public static ApplicationCaptchaResult fromCaptchaResult(CaptchaResult result) {
        final ApplicationCaptchaResult applicationCaptchaResult = new ApplicationCaptchaResult();
        applicationCaptchaResult.setWidth(result.getWidth());
        applicationCaptchaResult.setHeight(result.getHeight());
        applicationCaptchaResult.setOriginalImageBase64(result.getOriginalImageBase64());
        applicationCaptchaResult.setImageBase64(result.getImageBase64());
        applicationCaptchaResult.setWordList(result.getWordList());
        applicationCaptchaResult.setUuid(result.getUuid());
        applicationCaptchaResult.setPrefix(result.getPrefix());
        return applicationCaptchaResult;
    }

}
