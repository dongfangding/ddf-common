package com.ddf.common.exception;

import org.springframework.core.NestedRuntimeException;

/**
 * @author dongfang.ding on 2018/8/18
 * 自定义异常类
 */
public class GlobalCustomizeException extends NestedRuntimeException {
	private static final long serialVersionUID = 1L;
	private String code;
	private String message;
	/** 可替换消息参数，消息配置中不确定的值用大括号包着数组角标的方式，如{0} 占位，
	 * 抛出异常的时候使用带params的构造函数赋值，即可替换;仅支持使用资源文件定义的消息*/
	private Object[] params;


	public GlobalCustomizeException(Throwable e) {
		super(e.getMessage());
	}

	/**
	 * @param codeResolver
	 * @param params
	 */
	public GlobalCustomizeException(GlobalExceptionCodeResolver codeResolver, Object... params) {
		super(codeResolver.get());
		this.code = codeResolver.get();
		this.message = codeResolver.get();
		this.params = params;
	}

	public GlobalCustomizeException(GlobalExceptionCodeResolver codeResolver) {
		super(codeResolver.get());
		this.code = codeResolver.get();
		this.message = codeResolver.get();
	}


	public GlobalCustomizeException(String msg) {
		super(msg);
		this.code = msg;
		this.message = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}
}
