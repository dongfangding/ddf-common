package com.ddf.boot.common.api.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.commons.lang3.math.NumberUtils;

/***
 * 数字精度处理工具类
 */
public class NumberUtil {

    public static BigDecimal disposeAccuracy(Double value){
        return BigDecimal.valueOf(value).setScale(BigDecimal.ROUND_CEILING, RoundingMode.DOWN);
    }

    /**
     * 小于等于9999时正常显示，大于1万后格式改为1.001万，11.11万...，大于9999万后显示1.001亿，11.11
     *
     * @param score
     * @return
     */
    public static String formatNumberForScore(long score) {
        if (score < 0) {
            return "0";
        }
        if (score < 10000) {
            return String.valueOf(score);
        }

        if (score < 1000 * 10000) {
            return formatNumber(score, 10000) + "万";
        }

        if (score < 10000 * 10000) {
            return score / 10000 + "万";
        }

        return formatNumber(score, 100000000) + "亿";
    }

    private static String formatNumber(long number, long dividend) {
        long zheng = number / dividend;
        long yu = number % dividend;
        if (yu == 0 || zheng > 10000) {
            return String.valueOf(zheng);
        }
        String numberStr = zheng + "." + String.valueOf(number).substring(String.valueOf(zheng).length());
        if (numberStr.length() > 5) {
            numberStr = numberStr.substring(0, 5);
        }

        return subZeroAndDot(numberStr);
    }

    /**
     * 使用java正则表达式去掉多余的.与0
     *
     * @param s
     * @return
     */
    public static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            // 去掉多余的0
            s = s.replaceAll("0+?$", "");
            // 如最后一位是.则去掉
            s = s.replaceAll("[.]$", "");
        }
        return s;
    }

    /**
     * 是否 null 或  0
     * @param number
     * @return
     */
    public static boolean isNullOrZero(Number number){
        return null == number || number.longValue() == 0;
    }

     /**
     * 是否是数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str){
        return NumberUtils.isCreatable(str);
    }


	/**
	 * 将整数 X 转换为 0.X 的形式。
	 * * @param originalNumber 原始整数，例如 1234567
	 * @return 转换后的浮点数，例如 0.1234567
	 */
	public static double convertToFractional(long originalNumber) {
		if (originalNumber == 0) {
			return 0.0;
		}
		// 步骤 1: 确定原始数字的位数 N
		// 使用 Math.log10 并取底来实现，这是比转换为字符串更快的纯数学方法
		int N = (int) Math.floor(Math.log10(originalNumber)) + 1;

		// 步骤 2: 计算除数 (10^N)
		// Math.pow 返回 double，精确度高
		double divisor = Math.pow(10, N);

		// 步骤 3: 执行除法
		// 必须将原始数字转换为 double/float 进行浮点数除法
		return (double) originalNumber / divisor;
	}

	/**
	 * 从 double 数字中提取所有非零的小数部分，并转换为整数。
	 * 123.4567 -> 4567
	 * 987.0001 -> 1
	 * 543.2 -> 2
	 * 1.0 -> 0
	 * 3.1415926535 -> 1415926535
	 *
	 * @param originalDouble 原始 double 值，例如 123.456789
	 * @return 转换后的小数部分整数，例如 456789
	 */
	public static long getFractionalPartAll(double originalDouble) {
		if (originalDouble == 0.0) {
			return 0L;
		}

		// 1. 将 double 转换为其标准的字符串表示
		// 注意：这会包含科学计数法和精度问题，但通常是获取完整表示的最快方式。
		String fullString = String.valueOf(originalDouble);

		// 2. 找到小数点位置
		int decimalIndex = fullString.indexOf('.');

		if (decimalIndex == -1) {
			// 如果没有小数点 (例如 123.0)，则返回 0
			return 0L;
		}

		// 3. 提取小数点后面的子字符串
		String fractionalStr = fullString.substring(decimalIndex + 1);

		// 4. 清除尾随的 'E' 或其他科学计数法字符（如果数字过大或过小）
		int E_index = fractionalStr.indexOf('E');
		if (E_index != -1) {
			fractionalStr = fractionalStr.substring(0, E_index);
		}

		// 5. 将字符串转换为 long
		try {
			if (fractionalStr.isEmpty()) {
				return 0L;
			}

			// 移除可能存在的尾随零，以获取最小的整数表示，例如 0.20 -> 2
			while (fractionalStr.length() > 1 && fractionalStr.endsWith("0")) {
				fractionalStr = fractionalStr.substring(0, fractionalStr.length() - 1);
			}

			return Long.parseLong(fractionalStr);

		} catch (NumberFormatException e) {
			// 如果转换失败（例如小数部分过长，超出了 Long 的范围），
			// 可以选择抛出异常，或者返回一个默认值。
			System.err.println("警告：提取的小数部分 '" + fractionalStr + "' 超出 Long 范围或格式错误。");
			return 0L;
		}
	}
}
