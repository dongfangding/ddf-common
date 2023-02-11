package com.ddf.boot.common.api.constraint.collect;

import java.util.Set;

/**
 * <p>收集用户id的接口标记类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/22 10:47
 */
public interface IUserIdCollection {

    /**
     * 收集用户id, 提供一个通用接口，方便做一个工具类直接通过这个接口收集用户信息
     *
     * @return
     */
    Set<String> getUserIds();
}
