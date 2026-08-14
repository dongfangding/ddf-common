package com.ddf.common.ids.service.service;

import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;

/**
 * id生成器顶层接口
 *
 * @author dongfang.ding
 * @since 2021/7/20 15:54
 **/
public interface IDGen {

    /**
     * 获取id, key在雪花id的实现中是无效的
     *
     * @param key 目标键
     */
    default Result get(String key) {
        return null;
    }

    /**
     * 批量获取id
     *
     * @param key 目标键
     * @param number 数值
     */
    default ResultList list(String key, int number) {
        return null;
    }

    /**
     * 初始化方法
     */
    boolean init();

    /**
     * 是否基于业务 key 生成 ID（号段/Leaf 等 key-based 实现为 true；雪花等 keyless 实现为 false）。
     * IdGenRegistry 只收集 key-based 实现用于按 key 分发。
     */
    default boolean supportsKey() {
        return true;
    }
}
