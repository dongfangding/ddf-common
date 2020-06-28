/**
 * 
 */
package com.ddf.boot.common.exception;

/**
 *
 *  定义异常消息的代码，get方法返回实际值，这个值需要在exception.properties、exception_zh_CN.properties、
 *  exception_en_US中配置，请根据实际情况在对应的Locale资源文件中配置，至少配置exception.properties
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
 * @author dongfang.ding 2017年12月1日
 *
 */
public enum GlobalExceptionEnum implements GlobalExceptionCodeResolver {

	/**
	 * 请求正常
	 */
	CODE_COMPLETE("200"),

	/**
	 * 账户无效
	 */
	INVALID_ACCOUNT("INVALID_ACCOUNT"),
	/**
	 * 用户名已存在
	 */
	USERNAME_EXIST("USERNAME_EXIST"),
	/**
	 * 邮箱已经被注册
	 */
	EMAIL_HAD_REGISTERED("EMAIL_HAD_REGISTERED"),
	/**
	 * 用户名或密码错误
	 */
	USERNAME_OR_PASSWORD_INVALID("USERNAME_OR_PASSWORD_INVALID"),
	/**
	 * 账户已停用
	 */
	ACCOUNT_NOT_ENABLE("ACCOUNT_NOT_ENABLE"),
	/**
	 * 账号已在别处登录，当前认证信息无效
	 */
	CREDIT_CHANGED("CREDIT_CHANGED"),
	/**
	 * 登录状态过期
	 */
	LOGIN_EXPIRED("LOGIN_EXPIRED"),
	/**
	 * 数据重复
	 */
	DATA_REPEAT("DATA_REPEAT"),

	/**
	 * 更新失败
	 */
	UPDATE_ERROR("UPDATE_ERROR"),
	FIELD_NOT_MATCH("FIELD_NOT_MATCH"),

	OBJECT_OPTIMISTIC_LOCKING("OBJECT_OPTIMISTIC_LOCKING"),
	SERIAL_NO_GENERATE_FAILURE("SERIAL_NO_GENERATE_FAILURE"),
	USER_NOT_LOGIN("USER_NOT_LOGIN"),
	DB_ERROR("DB_ERROR"),
	LOGIN_ERROR("LOGIN_ERROR"),

	/** 该公司没有配置流水号规则 */
	SERIAL_RULE_NOT_EXISTS("SERIAL_RULE_NOT_EXISTS"),

	/** 流水号规则重复 */
	REPEAT_SERIAL_RULE("REPEAT_SERIAL_RULE");

	private String code;

	GlobalExceptionEnum (String code) {
		this.code = code;
	}

	@Override
	public String get() {
		return code;
	}
}