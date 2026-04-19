package com.ddf.common.captcha.producer;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数学运算验证码文本生成器
 * 
 * @author ruoyi
 */
public class MathKaptchaTextCreator extends DefaultTextCreator
{
    private static final String[] CNUMBERS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    @Override
    public String getText()
    {
        Integer result = 0;
        int x = ThreadLocalRandom.current().nextInt(10);
        int y = ThreadLocalRandom.current().nextInt(10);
        StringBuilder suChinese = new StringBuilder();
        int randomoperands = (int) Math.round(ThreadLocalRandom.current().nextDouble() * 2);
        if (randomoperands == 0)
        {
            result = x * y;
            suChinese.append(CNUMBERS[x]);
            suChinese.append("*");
            suChinese.append(CNUMBERS[y]);
        }
        else if (randomoperands == 1)
        {
            if (!(x == 0) && y % x == 0)
            {
                result = y / x;
                suChinese.append(CNUMBERS[y]);
                suChinese.append("/");
                suChinese.append(CNUMBERS[x]);
            }
            else
            {
                result = x + y;
                suChinese.append(CNUMBERS[x]);
                suChinese.append("+");
                suChinese.append(CNUMBERS[y]);
            }
        }
        else if (randomoperands == 2)
        {
            if (x >= y)
            {
                result = x - y;
                suChinese.append(CNUMBERS[x]);
                suChinese.append("-");
                suChinese.append(CNUMBERS[y]);
            }
            else
            {
                result = y - x;
                suChinese.append(CNUMBERS[y]);
                suChinese.append("-");
                suChinese.append(CNUMBERS[x]);
            }
        }
        else
        {
            result = x + y;
            suChinese.append(CNUMBERS[x]);
            suChinese.append("+");
            suChinese.append(CNUMBERS[y]);
        }
        suChinese.append("=?@" + result);
        return suChinese.toString();
    }

    /**
     * 生成的数据类型
     *
     */
    @lombok.Data
    public static class Data {

        /**
         * 生成的计算表达式, 要放到图形验证码中的
         */
        private String calcCode;

        /**
         * 计算表达式的结果， 这个是验证码结果， 需要校验的
         *
         */
        private String calcResult;
    }

    /**
     * 解析该类生成的计算表达式
     *
     * @param mathText 数学验证码文本
     * @return
     */
    public static Data parse(String mathText) {
        final Data data = new Data();
        int atIndex = mathText.lastIndexOf("@");
        data.setCalcCode(mathText.substring(0, atIndex));
        data.setCalcResult(mathText.substring(atIndex + 1));
        return data;
    }
}
