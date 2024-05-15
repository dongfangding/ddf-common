package com.ddf.common.ons.console.model;

import java.util.Set;

/**
 * <p>多环境请求参数</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/21 13:08
 */
public interface EnvRequest {

    /**
     * 获取要操作的环境
     *
     * @return
     */
    Set<String> getEnvList();

}
