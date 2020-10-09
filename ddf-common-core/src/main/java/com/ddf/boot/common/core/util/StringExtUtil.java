package com.ddf.boot.common.core.util;


import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 * 聚合String功能
 *
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
 * @author dongfang.ding on 2018/12/31
 *
 */
@Slf4j
public class StringExtUtil {

	private static final String UNION_PAY_LOGIN_PASSWORD_POOL = "abcdefghijklmnopqrstuvwxyz";
	private static final String UNION_PAY_LOGIN_PASSWORD_CHAR = "@#*";
	private static final String UNION_PAY_PASSWORD_NUMBER = "0123456789";
	private static final int MAX = 16;
	private static final int MIN = 6;

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String timeFormat = "yyyy-MM-dd HH:mm:ss";



	/**
	 * 判断是否是英文字母，不区分大小写
	 */
	public static boolean isEn(String str) {
		String regex = "^[a-zA-Z]+$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	/**
	 * 随机生成登录密码
	 *
	 * @param
	 * @return
	 * @author dongfang.ding
	 * @date 2019/9/25 17:51
	 */
	public static String randomLoginPassword() {
		return randomLoginPassword(MIN, MAX);
	}

	/**
	 * 随机生成云闪付登录密码
	 *
	 * @param
	 * @return
	 * @author dongfang.ding
	 * @date 2019/9/25 17:51
	 */
	public static String randomLoginPassword(int minLength, int maxLength) {
		if (minLength < MIN) {
			minLength = MIN;
		}
		if (maxLength > MAX) {
			maxLength = MAX;
		}
		Random random = new Random();
		int length = random.nextInt(maxLength - minLength + 1) + minLength;
		StringBuilder sbl = new StringBuilder(length);
		char randomStr;
		String pool = UNION_PAY_LOGIN_PASSWORD_POOL + UNION_PAY_LOGIN_PASSWORD_CHAR + UNION_PAY_PASSWORD_NUMBER;
		for (int i = 0; i < length; i++) {
			randomStr = pool.charAt(random.nextInt(pool.length()));
			if (i == 0) {
				while (UNION_PAY_LOGIN_PASSWORD_CHAR.indexOf(randomStr) != -1) {
					randomStr = pool.charAt(random.nextInt(pool.length()));
				}
				sbl.append(Character.toString(randomStr).toUpperCase());
			}
			sbl.append(randomStr);
		}
		return sbl.toString();
	}

	/**
	 * 生成6位支付密码
	 *
	 * @param
	 * @return
	 * @author dongfang.ding
	 * @date 2019/9/25 17:59
	 */
	public static String randomPayPassword() {
		Random random = new Random();
		String pool = UNION_PAY_PASSWORD_NUMBER;
		int length = 6;
		StringBuilder sbl = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sbl.append(pool.charAt(random.nextInt(pool.length())));
		}
		return sbl.toString();
	}


	/**
	 * 根据当前时间生成指定长度的随机数
	 * @param length
	 * @return
	 */
	public static String randomString(int length) {
		String timeMillis = System.currentTimeMillis() + "";
		if (length <= timeMillis.length()) {
			return timeMillis;
		}
		// UUID长度是32，如果长度过长，生成一次UUID长度是不够的
		int loop = length % 32 == 0 ? length / 32 : length / 32 + 1;
		StringBuilder sbl = new StringBuilder();
		if (loop > 1) {
			for (int i = 0; i < loop; i++) {
				sbl.append(UUID.randomUUID().toString().replaceAll("-", ""));
			}
		} else {
			sbl.append(UUID.randomUUID().toString().replaceAll("-", ""));
		}
		return timeMillis + sbl.toString().substring(0, (length - timeMillis.length()));
	}

	/**
	 * 将异常栈输出为字符串
	 * @param e
	 * @return
	 */
	public static String exceptionToStringNoLimit(Throwable e) {
		return ExceptionUtil.stacktraceToString(e, -1);
	}
}