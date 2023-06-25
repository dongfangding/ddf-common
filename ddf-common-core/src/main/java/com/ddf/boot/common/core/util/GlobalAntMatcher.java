package com.ddf.boot.common.core.util;

import java.util.List;
import org.springframework.util.AntPathMatcher;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/27 09:59
 */
public enum GlobalAntMatcher {

    /**
     * 实例对象
     */
    INSTANCE;

    /**
     * 对外提供的单例对象
     */
    private static AntPathMatcher antPathMatcher;


    /**
     * 构造实例
     */
    public static void create() {
        if (antPathMatcher == null) {
            antPathMatcher = new AntPathMatcher();
        }
    }

    static {
        create();
    }


    /**
     * 将实例返回
     *
     * @return
     */
    public static AntPathMatcher getAntPathMatcher() {
        return antPathMatcher;
    }


    /**
     * 转接方法
     *
     * @param pattern
     * @param path
     * @return
     */
    public static boolean match(String pattern, String path) {
        return antPathMatcher.match(pattern, path);
    }

    /**
     * 转接方法
     *
     * @param patterns
     * @param path
     * @return
     */
    public static boolean match(List<String> patterns, String path) {
        for (String pattern : patterns) {
            if (antPathMatcher.match(pattern, path)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
}
