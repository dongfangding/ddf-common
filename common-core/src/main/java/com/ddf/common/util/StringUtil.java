package com.ddf.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	private static final String UNION_PAY_LOGIN_PASSWORD_POOL = "abcdefghijklmnopqrstuvwxyz";
	private static final String UNION_PAY_LOGIN_PASSWORD_CHAR = "@#*";
	private static final String UNION_PAY_PASSWORD_NUMBER = "0123456789";
	private static final int MAX = 16;
	private static final int MIN = 6;

	private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String timeFormat = "yyyy-MM-dd HH:mm:ss";

	public static Date string2Date(String str) {
		Date aDate = null;
		try {
			if (StringUtil.contains(str, ConstUtil.STRING_COLON)) {
				aDate = new SimpleDateFormat(timeFormat).parse(str);
			} else {
				aDate = new SimpleDateFormat(DATE_FORMAT).parse(str);
			}
		} catch (ParseException e) {
			logger.error("xtream date converte failed: " + str);
		}
		return aDate;
	}

	public static String date2String(Date date) {
		return new SimpleDateFormat(DATE_FORMAT).format(date);
	}

	public static String time2String(Date date) {
		return new SimpleDateFormat(timeFormat).format(date);
	}

	public static boolean isBlank(String s) {
		if (s == null) {
			return true;
		}
		if (s.trim().length() == 0) {
			return true;
		}
		return false;
	}

	public static boolean isNotBlank(String s) {
		return !isBlank(s);
	}

	public static String capitalize(String s) {
		return StringUtils.capitalize(s);
	}

	public static String uncapitalize(String s) {
		return StringUtils.uncapitalize(s);
	}

	public static String utf82ascii(String s) {
		try {
			return new String(s.getBytes("UTF-8"), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	public static String ascii2utf8(String s) {
		try {
			return new String(s.getBytes("ISO8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	public static String formatTwoNumber(int n) {
		if (n < 10) {
			return "0" + n;
		}
		return "" + n;
	}

	public static Object parseValue(Type fieldType, String value) {
		Object objValue = value;
		if (!fieldType.equals(String.class)) {
			if (fieldType.equals(Integer.class)) {
				objValue = Integer.parseInt(value);
			} else if (fieldType.equals(Byte.class)) {
				objValue = Byte.parseByte(value);
			} else if (fieldType.equals(Long.class)) {
				objValue = Long.parseLong(value);
			} else if (fieldType.equals(Short.class)) {
				objValue = Short.parseShort(value);
			} else if (fieldType.equals(Float.class)) {
				objValue = Float.parseFloat(value);
			} else if (fieldType.equals(Double.class)) {
				objValue = Double.parseDouble(value);
			} else if (fieldType.equals(BigInteger.class)) {
				objValue = new BigInteger(value);
			} else if (fieldType.equals(BigDecimal.class)) {
				objValue = new BigDecimal(value);
			} else if (fieldType.equals(Date.class)) {
				objValue = string2Date(value);
			}
		}
		return objValue;
	}
	
	public static Double call2Double(String callDate){
		if(callDate!=null){
			try{
				return Double.parseDouble(callDate);
			}
			catch(Exception e){
				logger.error("callDate is not format date!");
				return 0.00;
			}
			
		}
		else{
			return 0.00;
		}
	}

	public static boolean contains(String s, String needle) {
		if (isNotBlank(s) && s.indexOf(needle) > -1) {
			return true;
		}
		return false;
	}

	public static boolean isEqual(String s, String t) {
		return Objects.equals(s, t);
	}

	public static String unescape(String s) {
		if (isBlank(s)) {
			return "";
		}

		s = s.replaceAll("&amp;", "&");
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt", ">;");
		return s;
	}
	
	/**
	 * 解码URL中文字符
	 * 
	 * @param param
	 * @return
	 */
	public static String decodeUrlString(String param) {
		String retString = "";
		try {
			retString = java.net.URLDecoder.decode(param, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return retString;
	}


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
	public static String exceptionToString(Exception e) {
		return exceptionToString(e, 2000);
	}

	/**
	 * 将异常栈输出为字符串
	 * @param e
	 * @return
	 */
	public static String exceptionToString(Exception e, int allowLength) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String errorMessage = sw.toString();
		if (allowLength == -1) {
			return errorMessage;
		}
		if (errorMessage.length() > allowLength) {
			errorMessage = errorMessage.substring(0, allowLength);
		}
		return errorMessage;
	}

	/**
	 * 避免字符串拼接出现null字符串
	 * @param string
	 * @return
	 */
	public static String nullToBlank(Object string) {
		if (string == null || org.apache.commons.lang3.StringUtils.isBlank(string + "")) {
			return "";
		}
		return string.toString();
	}


	/**
	 * 随机生成云闪付登录密码
	 *
	 * @param
	 * @return
	 * @author dongfang.ding
	 * @date 2019/9/25 17:51
	 */
	public static String randomUnionPayLoginPassword() {
		return randomUnionPayLoginPassword(MIN, MAX);
	}

	/**
	 * 随机生成云闪付登录密码
	 *
	 * @param
	 * @return
	 * @author dongfang.ding
	 * @date 2019/9/25 17:51
	 */
	public static String randomUnionPayLoginPassword(int minLength, int maxLength) {
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
	 * 生成云闪付6位支付密码
	 *
	 * @param
	 * @return
	 * @author dongfang.ding
	 * @date 2019/9/25 17:59
	 */
	public static String randomUnionPayPassword() {
		Random random = new Random();
		String pool = UNION_PAY_PASSWORD_NUMBER;
		int length = 6;
		StringBuilder sbl = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sbl.append(pool.charAt(random.nextInt(pool.length())));
		}
		return sbl.toString();
	}
}
