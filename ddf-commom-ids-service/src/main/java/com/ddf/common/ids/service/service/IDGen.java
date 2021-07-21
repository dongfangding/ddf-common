package com.ddf.common.ids.service.service;

import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;

/**
 * id生成器顶层接口
 *
 * @author dongfang.ding
 * @date 2021/7/20 15:54
 **/
public interface IDGen {

    /**
     * 获取id, key在雪花id的实现中是无效的
     *
     * @param key
     * @return
     */
    default Result get(String key) {
        return null;
    }

    /**
     * 批量获取id
     *
     * @param key
     * @param number
     * @return
     */
    default ResultList list(String key, int number) {
        return null;
    }

    /**
     * 初始化方法
     *
     * @return
     */
    boolean init();
}
