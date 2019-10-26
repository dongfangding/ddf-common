package com.ddf.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

public class NumberUtil {
	private static final NumberFormat nf = new DecimalFormat("#0.######");
	private static final NumberFormat nf2 = new DecimalFormat("#0.00");
	private static final NumberFormat nf3 = new DecimalFormat("#0.000");
	private static final NumberFormat nf4 = new DecimalFormat("#0.0000");
	private static final String NUM = "零壹贰叁肆伍陆柒捌玖";
	private static final String UNIT = "仟佰拾个";
	private static final String GRADEUNIT = "仟万亿兆";
	private static final String DOTUNIT = "角分厘";
	private static final int GRADE = 4;
	private static final double EQUAL_DELTA = 0.10;
	private static final Logger logger = LoggerFactory.getLogger(NumberUtil.class);

	/**
	 * 1.如果小数位数>=4, 格式化成4位; 2.如果小数位数==3, 格式化成3位; 3.其他, 格式化成2位
	 * 
	 * @param num
	 * @return 格式化的字符串
	 */
	public static String numberFormat2(double num) {
		String s = String.valueOf(num);
		if (StringUtil.contains(s, ConstUtil.STRING_DOT)) {
			if (s.indexOf(ConstUtil.STRING_DOT) + 5 <= s.length()) {
				return nf4.format(num);
			} else if (s.indexOf(ConstUtil.STRING_DOT) + 4 == s.length()) {
				return nf3.format(num);
			}
		}
		return nf2.format(num);
	}

	public static String removeDot(double num) {
		return nf.format(num);
	}

	public static String numberFormat(double num) {
		return nf2.format(num);
	}

	public static String numberFormat(Double num) {
		return nf2.format(num);
	}

	public static String numberFormat4(Double num) {
		return nf4.format(num);
	}

	public static Double null2Zero(Double d) {
		if (d == null) {
			return (double) 0;
		}
		return d;
	}

	private static String toBigAmt(double amount) {
		String amt = nf2.format(amount);
		String dotPart = ""; // 取小数位
		String intPart = ""; // 取整数位
		int dotPos;

		if ((dotPos = amt.indexOf(ConstUtil.STRING_DOT)) != -1) {
			intPart = amt.substring(0, dotPos);
			dotPart = amt.substring(dotPos + 1);
		} else {
			intPart = amt;
		}
		if (intPart.length() > 16) {
			throw new InternalError("The amount is too big.");
		}
		String intBig = intToBig(intPart);

		if ("0".equals(intPart)) {
			intBig = "零" + intBig;
		}

		String dotBig = dotToBig(dotPart);
		if (dotBig.length() == 0) {
			return intBig + "元整";
		} else {
			return intBig + "元" + dotBig;
		}
	}

	private static String dotToBig(String dotPart) {
		// 得到转换后的大写（小数部分）
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < dotPart.length() && i < 3; i++) {
			int num;
			if ((num = Integer.parseInt(dotPart.substring(i, i + 1))) != 0) {
				sb.append(NUM, num, num + 1).append(DOTUNIT, i, i + 1);
			}
		}
		return sb.toString();
	}

	private static String intToBig(String intPart) {
		// 得到转换后的大写（整数部分）
		int grade; // 级长
		String result = "";
		String strTmp = "";

		// 得到当级长
		grade = intPart.length() / GRADE;
		// 调整级次长度
		if (intPart.length() % GRADE != 0) {
			grade += 1;
		}

		// 对每级数字处理
		for (int i = grade; i >= 1; i--) {
			strTmp = getNowGradeVal(intPart, i);// 取得当前级次数字
			result += getSubUnit(strTmp);// 转换大写
			result = dropZero(result);// 除零
			// 加级次单位
			if (i > 1) // 末位不加单位
				// 单位不能相连续
			{
				result += GRADEUNIT.substring(i - 1, i);
			}
		}
		return result;
	}

	private static String getNowGradeVal(String strVal, int grade) {
		// 得到当前级次的串
		String rst;
		if (strVal.length() <= grade * GRADE) {
			rst = strVal.substring(0, strVal.length() - (grade - 1) * GRADE);
		} else {
			rst = strVal.substring(strVal.length() - grade * GRADE, strVal.length() - (grade - 1) * GRADE);
		}
		return rst;
	}

	private static String getSubUnit(String strVal) {
		// 数值转换
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < strVal.length(); i++) {
			String s = strVal.substring(i, i + 1);
			int num = Integer.parseInt(s);
			if (num == 0) {
				// “零”作特殊处理
				if (i != strVal.length()) // 转换后数末位不能为零
				{
					sb.append("零");
				}
			} else {
				// If IntKey = 1 And i = 2 Then
				// “壹拾”作特殊处理
				// “壹拾”合理
				// Else
				sb.append(NUM, num, num + 1);
				// End If
				// 追加单位
				if (i != strVal.length() - 1)// 个位不加单位
				{
					sb.append(UNIT, i + 4 - strVal.length(), i + 4 - strVal.length() + 1);
				}
			}
		}
		return sb.toString();
	}

	private static String dropZero(String strVal) {
		// 去除连继的“零”
		StringBuilder sb = new StringBuilder();
		String strBefore; // 前一位置字符
		String strNow; // 现在位置字符

		strBefore = strVal.substring(0, 1);
		sb.append(strBefore);

		for (int i = 1; i < strVal.length(); i++) {
			strNow = strVal.substring(i, i + 1);
			if ("零".equals(strNow) && "零".equals(strBefore)) {
				;// 同时为零
			} else {
				sb.append(strNow);
			}
			strBefore = strNow;
		}

		// 末位去零
		if ("零".equals(sb.substring(sb.length() - 1, sb.length()))) {
			return sb.substring(0, sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * 
	 * 功能： 将阿拉伯数字转换为中文金额大写,支持两位小数,超过两位小数的四舍五入 返回： 1、中文金额大写 说明：
	 * 
	 * @param num
	 *            要转换的数字
	 * @return String 返回的中文金额大写
	 * @exception Exception
	 */
	public static String numberToCapital(double num) {
		String ret = "";
		boolean isLessZero = false;
		try {
			if (num < 0) {
				num *= -1;
				isLessZero = true;
			}
			ret = toBigAmt(num);
			if (isLessZero) {
				ret = "负" + ret;
			}
		} catch (Exception ex) {
			logger.error("阿拉伯数字转换为中文金额大写错误");
			ret = "";
		}

		return ret;
	} // end of method NumberToCapital(double)

	public static boolean amountEqual(Double num1, Double num2) {
		return amountEqual(num1, num2, EQUAL_DELTA);
	}

	public static boolean amountEqual(double num1, double num2) {
		return amountEqual(num1, num2, EQUAL_DELTA);
	}

	public static boolean amountEqual(double num1, double num2, double delta) {
		return Math.abs(num1 - num2) < delta;
	}

	public static Integer frontId2DbId(Map<Integer, Integer> idMap, Integer oldId) {
		Integer newId = idMap.get(oldId);
		if (newId == null) {
			return oldId;
		}
		return newId;
	}

	public static String num2EnWords(Number n) {
		if (n == null) {
			return "";
		}
		String[] digits = { null, "ONE ", "TWO ", "THREE ", "FOUR ", "FIVE ", "SIX ", "SEVEN ", "EIGHT ", "NINE " };
		String[] tens = { null, null, "TWENTY ", "THIRTY ", "FORTY ", "FIFTY ", "SIXTY ", "SEVENTY ", "EIGHTY ",
				"NINETY " };
		String[] teens = { "TEN ", "ELEVEN ", "TWELVE ", "THIRTEEN ", "FOURTEEN ", "FIFTEEN ", "SIXTEEN", "SEVENTEEN ",
				"EIGHTEEN ", "NINETEEN " };
		boolean teen = false;
		String s = "" + n;
		int len = s.length();
		int[] d = new int[len + 1];
		for (int i = 0; i < len; i++) {
			d[len - i] = Integer.parseInt("" + s.charAt(i));
		}
		String[] store = new String[10];
		for (int i = len; i > 0; i--) {
			int reminder = i % 3;
			switch (reminder) {
			case 2:
				teen = d[i] == 1;
				store[i] = tens[d[i]];
				break;
			case 1:
				if (teen) {
					teen = false;
					store[i] = teens[d[i]];
				} else {
					store[i] = digits[d[i]];
				}
				break;
			case 0:
				store[i] = digits[d[i]];
				break;
			}
		}
		String a1 = "", a2 = "", a3 = "", a4 = "", a5 = "";
		if (store[9] != null) {
			a1 = "HUNDRED ";
		}
		if ((store[9] != null) || (store[8] != null) || (store[7] != null)) {
			a2 = "MILLION ";
		}
		if (store[6] != null) {
			a3 = "HUNDRED ";
		}
		if ((store[6] != null) || (store[5] != null) || (store[4] != null)) {
			a4 = "THOUSAND ";
		}
		if (store[3] != null) {
			a5 = "HUNDRED ";
			if (store[2] != null || store[1] != null) {
				a5 += "AND ";
			}
		}
		for (int i = 0; i < store.length; i++) {
			if (store[i] == null) {
				store[i] = "";
			}
		}
		return store[9] + a1 + store[8] + store[7] + a2 + store[6] + a3 + store[5] + store[4] + a4 + store[3] + a5
				+ store[2] + store[1];
	}

	public static Long frontId2DbId(Map<Long, Long> idMap, Long oldId) {
		Long newId = idMap.get(oldId);
		if (newId == null) {
			return oldId;
		}
		return newId;
	}

}
