package com.ddf.common.security.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 路径匹配类
 *
 * @author dongfang.ding
 * @date 2019/8/30 15:03
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
