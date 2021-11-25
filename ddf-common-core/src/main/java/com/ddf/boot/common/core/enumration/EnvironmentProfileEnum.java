package com.ddf.boot.common.core.enumration;

import lombok.Getter;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/11/25 20:53
 */
public enum EnvironmentProfileEnum {

    /**
     * 环境信息
     */
    LOCAL("local", "本地开发环境"),
    DEV("dev", "开发环境"),
    TEST("test", "测试环境"),
    PRE("pre", "预发布环境"),
    PRO("pro", "生产环境"),

    ;

    @Getter
    private final String code;

    @Getter
    private final String desc;

    EnvironmentProfileEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }


}
