package com.ddf.boot.common.api.util;


import org.apache.commons.lang3.StringUtils;

/**
 * @author dongfang.ding on 2018/12/1
 */
public class ConstUtil {

    private ConstUtil() {
    }

    public static final String FALSE_STR = "0";
    public static final String TRUE_STR = "1";
    public static final Byte FALSE_BYTE = new Byte(FALSE_STR);
    public static final Byte TRUE_BYTE = new Byte(TRUE_STR);
    public static final String STRING_COLON = ":";
    public static final String STRING_DOT = ".";
    public static final String STRING_PERCENT = "%";


    public static boolean isBlank(Object obj) {
        return !isNotBlank(obj);
    }

    public static boolean isNotBlank(Object obj) {
        return obj != null && StringUtils.isNotBlank(obj.toString());
    }

}
