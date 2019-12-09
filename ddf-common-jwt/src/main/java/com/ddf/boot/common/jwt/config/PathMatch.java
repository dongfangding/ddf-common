package com.ddf.boot.common.jwt.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 路径匹配类
 *
 */
@Setter
@Getter
public class PathMatch {


    /**
     * http方法名
     */
    private String httpMethod;

    /**
     * 路径
     */
    private String path;
}
