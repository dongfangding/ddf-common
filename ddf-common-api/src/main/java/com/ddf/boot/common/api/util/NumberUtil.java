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
}
