package com.ddf.boot.common.core.util;

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
    private AntPathMatcher antPathMatcher;

    /**
     * 构造
     */
    GlobalAntMatcher() {
        create();
    }

    /**
     * 构造实例
     */
    public void create() {
        if (antPathMatcher == null) {
            antPathMatcher = new AntPathMatcher();
        }
    }

    /**
     * 将实例返回
     *
     * @return
     */
    public AntPathMatcher getAntPathMatcher() {
        return antPathMatcher;
    }


    /**
     * 转接方法
     *
     * @param pattern
     * @param path
     * @return
     */
    public boolean match(String pattern, String path) {
        return antPathMatcher.match(pattern, path);
    }
}
