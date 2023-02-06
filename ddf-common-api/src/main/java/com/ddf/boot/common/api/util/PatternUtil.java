package com.ddf.boot.common.api.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>正则表达式工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/09/13 20:08
 */
public class PatternUtil {


    /**
     * 从html中解析出图片链接
     *
     * @param htmlStr
     * @return
     */
    public static Set<String> findImgSrcUrl(String htmlStr) {
        Set<String> pics = new HashSet<>();
        // 匹配<img>标签
        String imageTagRegex = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        Pattern imagePattern = Pattern.compile(imageTagRegex, Pattern.CASE_INSENSITIVE);
        Matcher imageMatcher = imagePattern.matcher(htmlStr);
        // 匹配<img>标签的src内容，即具体图片链接了
        String imageSrcRegex = "src\\s*=\\s*\"?(.*?)(\"|>|\\s+)";
        Pattern imageSrcPattern = Pattern.compile(imageSrcRegex, Pattern.CASE_INSENSITIVE);
        String img = "";
        while (imageMatcher.find()) {
            // 得到<img />数据
            img = imageMatcher.group();
            // 匹配<img>中的src数据
            Matcher m = imageSrcPattern.matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }

    /**
     * 找出一个子字符串在原字符串中出现的次数
     *
     * @param sourceStr
     * @param matchStr
     * @return
     */
    public static int findChildStrCount(String sourceStr, String matchStr) {
        Pattern p = Pattern.compile(matchStr, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sourceStr);
        int count = 0;
        while (m.find()) {
            count ++;
        }
        return count;
    }
}
