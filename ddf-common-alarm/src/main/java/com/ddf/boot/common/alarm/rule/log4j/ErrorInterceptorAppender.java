package com.ddf.boot.common.alarm.rule.log4j;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.ddf.boot.common.alarm.config.LarkProperties;
import com.ddf.boot.common.alarm.util.LarkUtil;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import com.ddf.boot.common.redis.response.AccessLimitResponse;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.message.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 先内部验证，如果没问题，要单独抽一个日志模块，将log4j的依赖，还有这些统一到一起
 */
@Plugin(name = "ErrorInterceptor", category = "Core", elementType = Appender.ELEMENT_TYPE)
public class ErrorInterceptorAppender extends AbstractAppender {
	protected ErrorInterceptorAppender(String name, Layout<? extends Serializable> layout) {
		super(name, null, layout, false);
	}

	@Override
	public void append(LogEvent event) {
		try {
			final Level level = event.getLevel();
			if (!level.equals(Level.ERROR)) {
				return;
			}
			final String loggerName = event.getLoggerName();
			final RedisTemplateHelper redisTemplateHelper = SpringContextHolder.getBean(RedisTemplateHelper.class);
			final Message message = event.getMessage();
			final String formattedMessage = message.getFormattedMessage();
			final LarkProperties larkProperties = SpringContextHolder.getBean(LarkProperties.class);
			final EnvironmentHelper environmentHelper = SpringContextHolder.getBean(EnvironmentHelper.class);
			if (formattedMessage.contains("全局异常捕获到请求异常") || Objects.isNull(redisTemplateHelper)
					|| Objects.isNull(larkProperties) || Objects.isNull(environmentHelper)) {
				return;
			}
			final String applicationName = environmentHelper.getApplicationName();
			final LarkProperties.Properties properties = larkProperties.getCodeProperties(applicationName);
			if (!properties.isEnabled()) {
				return;
			}
			final Object globalExceptionExecutor = SpringContextHolder.getBean("globalExceptionExecutor");
			if (Objects.isNull(globalExceptionExecutor)) {
				return;
			}
			final AccessLimitResponse response = redisTemplateHelper.sliderWindowAccess(
					applicationName + ":error-push:count:" + loggerName, 1, 1);
			if (response.isLimited()) {
				return;
			}
			final long epochSeconds = event
					.getInstant()
					.getEpochSecond();
			final String stackTrace = event.getThrown() != null ? ExceptionUtil.stacktraceToString(event.getThrown()) :
					"";
			final ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) globalExceptionExecutor;
			executor.execute(() -> {
				String host = "";
				try {
					host = InetAddress
							.getLocalHost()
							.getHostAddress();
				} catch (Exception ignore) {
				}
				String text = String.format(
						"[%s]-[%s(%s)]-[%s]: %s\n\n堆栈信息: %s", DateUtils.standardFormatSeconds(epochSeconds),
						applicationName, host, loggerName, formattedMessage, stackTrace
				);
				LarkUtil.sendTextMsgType(properties.getWebhookUrl(), properties.getSecret(), text, true);
			});
		} catch (Exception e) {
			getStatusLogger().info("ErrorInterceptorAppender出错", e);
		}
	}

	@PluginFactory
	public static ErrorInterceptorAppender createAppender(@PluginAttribute("name") String name,
			@PluginElement("Layout") Layout<? extends Serializable> layout) {
		return new ErrorInterceptorAppender(name, layout);
	}
}
