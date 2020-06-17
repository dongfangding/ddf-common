package com.ddf.boot.common.exception;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpStatus;
import com.ddf.boot.common.config.GlobalProperties;
import com.ddf.boot.common.helper.EnvironmentHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 处理全局异常，支持异常类占位符解析和国际化
 *
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding on 2019/1/2
 *
 */
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ErrorAttributesHandler extends DefaultErrorAttributes {

	@Autowired
	private MessageSource messageSource;
	@Autowired(required = false)
	private ErrorHttpStatusMapping errorHttpStatusMapping;
	@Autowired
	private EnvironmentHelper environmentHelper;
	@Autowired
	private GlobalProperties globalProperties;

	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
		Throwable error = getError(webRequest);
		if (error == null) {
			return errorAttributes;
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		error.printStackTrace(pw);
		log.error(sw.toString());

        // 为了定义自己的字段返回顺序，所以重新写了一个map
		Map<String, Object> errorOverrideMap = initErrorMap(errorAttributes);

		// 假如当前环境允许设置trace，则将详细错误堆栈信息返回
		if (!environmentHelper.checkIsExistOr(globalProperties.getIgnoreErrorTraceProfile())) {
			errorOverrideMap.put("trace", errorAttributes.get("trace"));
		}

		int httpStatus = HttpStatus.HTTP_INTERNAL_ERROR;
		GlobalCustomizeException exception;

		if (AccessDeniedException.class.getName().equals(error.getClass().getName())) {
			httpStatus = HttpStatus.HTTP_UNAUTHORIZED;
		} else if (IllegalArgumentException.class.getName().equals(error.getClass().getName())) {
			httpStatus = HttpStatus.HTTP_BAD_REQUEST;
		}

		// 预留用户自定义的异常对Http响应状态码的映射
		if (errorHttpStatusMapping != null) {
			httpStatus = errorHttpStatusMapping.getHttpStatus(error);
		}

		if (error instanceof GlobalCustomizeException) {
			exception = (GlobalCustomizeException) error;
		} else {
			exception = new GlobalCustomizeException(error.getMessage());
			// 没有定义异常code的异常使用http状态码标识
			exception.setCode(Convert.toStr(httpStatus));
		}
		// 解析异常类消息代码，并根据当前Local格式化资源文件
		Locale locale = webRequest.getLocale();
		// 没有定义资源文件的使用直接使用异常消息
		exception.setMessage(messageSource.getMessage(exception.getCode(), exception.getParams(),
				exception.getMessage(), locale));

		// org.springframework.web.context.request.RequestAttributes.REFERENCE_REQUEST
		HttpServletRequest httpServletRequest = (HttpServletRequest) webRequest.resolveReference(WebRequest.REFERENCE_REQUEST);
		if (httpServletRequest != null) {
			// 返回response状态码的时候会去判断这个属性里有没有值，如果有就用这个，如果没有，用默认的500
			// 看这个方法。org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.error
			httpServletRequest.setAttribute("javax.servlet.error.status_code", httpStatus);
//			httpServletRequest.setAttribute("javax.servlet.error.status_code", HttpStatus.HTTP_OK);
		}
		// 接口响应一直都有两种不同的方式，一种是使用标准的Http状态码，还有一种是保持接口请求为200，使用自定义的状态字段来标识
		// 目前是使用http状态码，但这个字段保留，如果需要切换的话，把http状态码赋值为200即可
        errorOverrideMap.put("status", httpStatus);
		errorOverrideMap.put("code", exception.getCode());
		errorOverrideMap.put("message", exception.getMessage());
		return errorOverrideMap;
	}


	/**
	 * 自定义错误返回字段顺序
	 * @param errorAttributes
	 * @return
	 */
	private Map<String, Object> initErrorMap(Map<String, Object> errorAttributes) {
		Map<String, Object> errorOverrideMap = new LinkedHashMap<>();
		errorOverrideMap.put("path", errorAttributes.get("path"));
		errorOverrideMap.put("status", errorAttributes.get("status"));
		errorOverrideMap.put("code", errorAttributes.get("message"));
		errorOverrideMap.put("message", errorAttributes.get("message"));
		errorOverrideMap.put("timestamp", System.currentTimeMillis());
		errorOverrideMap.put("error", errorAttributes.get("error"));
		errorOverrideMap.put("trace", "");
		return errorOverrideMap;
	}
}
