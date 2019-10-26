package com.ddf.common.util;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class DateUtil {

    /**
     * 将任意格式的日期字符串转为标准日期格式(2017-01-01)
     *
     * @param str
     * @return
     */
    public static Date parseDateString(String str) {
        SimpleDateFormat format = null;

        if (StringUtil.isBlank(str)) {
            return null;
        }
        try {
            // 01-Feb-17
            if (Pattern.matches("(\\d)(\\d)(-)([a-zA-Z])([a-zA-Z])([a-zA-Z])(-)(\\d)(\\d)", str)) {
                format = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
            }
            //15-Jan-2018
            else if (Pattern.matches("(\\d)(\\d)(-)([a-zA-Z])([a-zA-Z])([a-zA-Z])(-)(\\d)(\\d)(\\d)(\\d)", str)) {
                format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            }
            //11.JUN
            else if (Pattern.matches("(\\d)(\\d)(-)([a-zA-Z])([a-zA-Z])([a-zA-Z])", str)) {
                Calendar c = Calendar.getInstance();
                str = str + '.' + c.get(Calendar.YEAR);
                format = new SimpleDateFormat("dd.MMM.yyyy", Locale.ENGLISH);
            }
            // 01/02/2017
            else if (Pattern.matches("(\\d)(\\d)(\\/)(\\d)(\\d)(\\/)(\\d)(\\d)(\\d)(\\d)", str)) {
                format = new SimpleDateFormat("dd/MM/yyyy");
            }
            // 2017年12月02日
            else if (Pattern.matches("(\\d){2,4}年(\\d){1,2}月(\\d){1,2}日", str)) {
                format = new SimpleDateFormat("yyyy年M月d日");
            }
            // 2017-12-02
            else if (Pattern.matches("(\\d){2,4}-(\\d){1,2}-(\\d){1,2}", str)) {
                format = new SimpleDateFormat("yyyy-M-d");
            }
            // 2017-12-02
            else if (Pattern.matches("(\\d){2,4}/(\\d){1,2}/(\\d){1,2}", str)) {
                format = new SimpleDateFormat("yyyy/M/d");
            }
            // 12月02日
            else if (Pattern.matches("(\\d){1,2}月(\\d){1,2}(日|号)?", str)) {
                Calendar c = Calendar.getInstance();
                str = c.get(Calendar.YEAR) + "年" + str;
                if (str.length() < 10) {
                    str = str + "日";
                }
                format = new SimpleDateFormat("yyyy年M月d日");

            }
            // 12.02
            else if (Pattern.matches("(\\d){1,2}(\\.)(\\d){1,2}", str)) {
                Calendar c = Calendar.getInstance();
                str = c.get(Calendar.YEAR) + "." + str;

                format = new SimpleDateFormat("yyyy.M.d");
            }
            // 12/02
            else if (Pattern.matches("(\\d){1,2}(\\/)(\\d){1,2}", str)) {
                Calendar c = Calendar.getInstance();
                str = c.get(Calendar.YEAR) + "/" + str;
                format = new SimpleDateFormat("yyyy/M/d");
            }
            // 2017.12.02
            else if (Pattern.matches("(\\d){2,4}(\\.)(\\d){1,2}(\\.)(\\d){1,2}", str)) {
                format = new SimpleDateFormat("yyyy.M.d");
            }
            // 12-02
            else if (Pattern.matches("(\\d){1,2}(-)(\\d){1,2}", str)) {
                Calendar c = Calendar.getInstance();
                str = c.get(Calendar.YEAR) + "-" + str;
                format = new SimpleDateFormat("yyyy-M-d");
            }
            // Nov.16,2017
            else if (Pattern.matches("([a-zA-Z]){3}(\\.)(\\d){1,2}(,|\\s)(\\d){2,4}", str)) {
                try {
                    format = new SimpleDateFormat("MMM.dd yyyy", Locale.ENGLISH);
                } catch (Exception e) {
                    format = new SimpleDateFormat("MMM.dd,yyyy", Locale.ENGLISH);
                }
            }


            if (null != format) {
                Date date = format.parse(str);
                return date;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
