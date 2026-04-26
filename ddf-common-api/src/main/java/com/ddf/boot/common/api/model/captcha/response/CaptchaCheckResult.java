package com.ddf.boot.common.api.model.captcha.response;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>验证码校验结果</p>
 *
 * <p>一次校验成功后返回二次校验凭证，二次校验场景下该字段为空。</p>
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/04/11
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CaptchaCheckResult implements Serializable {

    private static final long serialVersionUID = 1L;

	/**
	 * 二次校验时携带的uuid
	 */
	private String uuid;

    /**
     * 二次校验凭证，用于登录等业务接口的服务端二次验证。
     */
    private String captchaVerification;
}
