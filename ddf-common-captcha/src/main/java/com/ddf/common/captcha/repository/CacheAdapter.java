package com.ddf.common.captcha.repository;

import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>description</p >
 * <p>
 * 适配器
 *
 * @author Snowball
 * @version 1.0
 * @since 2021/11/06 16:27
 */
public class CacheAdapter {

	/**
	 * key 取名与三方库保持一致，便于统一管理。
	 */
	public final static String CAPTCHA_KEY_PREFIX = "RUNNING:CAPTCHA:";

	private final StringRedisTemplate stringRedisTemplate;

	private final RedisTemplateHelper redisTemplateHelper;

	public CacheAdapter(StringRedisTemplate stringRedisTemplate,
			RedisTemplateHelper redisTemplateHelper) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.redisTemplateHelper = redisTemplateHelper;
	}

	/**
	 * 设置二次校验参数
	 *
	 * @param uuid
	 * @param captchaVerification
	 */
	public void setCaptchaVerification(String uuid, String captchaVerification) {
		stringRedisTemplate
				.opsForValue()
				.set(
						String.format("%s:%s:verification", CAPTCHA_KEY_PREFIX, uuid), captchaVerification,
						Duration.ofMinutes(5)
				);
	}

	/**
	 * 确认二次验证码是否匹配，匹配后直接删除，如果用户连点会有问题，需要在业务层处理
	 *
	 * @param uuid
	 * @param captchaVerification
	 * @return
	 */
	public boolean hasCaptchaVerification(String uuid, String captchaVerification) {
		return redisTemplateHelper.stringDeleteWithCheckValue(
				String.format("%s:%s:verification", CAPTCHA_KEY_PREFIX, uuid), captchaVerification);
	}


}
