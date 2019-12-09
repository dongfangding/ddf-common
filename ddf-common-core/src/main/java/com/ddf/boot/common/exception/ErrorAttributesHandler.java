package com.ddf.boot.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author dongfang.ding on 2019/1/2
 * 处理全局异常，支持异常类占位符解析和国际化
 */
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE + 10)
public class ErrorAttributesHandler extends DefaultErrorAttributes {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private MessageSource messageSource;

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
		Throwable error = getError(webRequest);
		if (error == null) {
			return errorAttributes;
		}
		error.printStackTrace();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		error.printStackTrace(pw);
		logger.error(sw.toString());

		GlobalCustomizeException exception;
		if (error instanceof GlobalCustomizeException) {
			exception = (GlobalCustomizeException) error;
		} else {
			exception = new GlobalCustomizeException(error.getMessage());
			exception.setCode(error.getMessage());
		}
		// 解析异常类消息代码，并根据当前Local格式化资源文件
		Locale locale = webRequest.getLocale();
		exception.setMessage(messageSource.getMessage(exception.getCode(), exception.getParams(),
				exception.getCode(), locale));

		// 为了定义自己的字段返回顺序，所以重新写了一个map
		Map<String, Object> errorOverrideMap = new LinkedHashMap<>();
		errorOverrideMap.put("path", errorAttributes.get("path"));
		errorOverrideMap.put("status", errorAttributes.get("status"));
		errorOverrideMap.put("code", exception.getCode());
		errorOverrideMap.put("message", exception.getMessage());
		errorOverrideMap.put("timestamp", System.currentTimeMillis());
		errorOverrideMap.put("error", errorAttributes.get("error"));
		errorOverrideMap.put("trace", errorAttributes.get("trace"));
		return errorOverrideMap;
	}
}
