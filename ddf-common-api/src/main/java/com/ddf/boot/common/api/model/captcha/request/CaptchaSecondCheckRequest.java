package com.ddf.boot.common.api.model.captcha.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>二次校验参数，该接口主要是给后端调用再次校验的，需要的参数是前端首次验证通过后，后端接口给的，再后续接口再带回来让服务端校验</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/04/25 21:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaptchaSecondCheckRequest {

	/**
	 * 获取验证码接口返回的唯一标识
	 */
	@NotBlank(message = "uuid不能为空")
	private String uuid;


	/**
	 * 二次校验参数值，由前端控件生成
	 */
	@NotBlank(message = "captchaVerification不能为空")
	private String captchaVerification;
}
