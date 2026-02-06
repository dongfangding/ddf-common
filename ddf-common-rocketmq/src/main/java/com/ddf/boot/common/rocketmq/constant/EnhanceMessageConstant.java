package com.ddf.boot.common.rocketmq.constant;

import lombok.extern.slf4j.Slf4j;

/**
 * @author YiMing
 * @description:
 * @since 2023/10/9 14:02
 */
@Slf4j
public class EnhanceMessageConstant {
    /**
     * 实体类型描述标签
     */
    private static final String TAG = "";


    /**
     * 重试前缀
     */
    public static final String RETRY_PREFIX = "RETRY_MSG-";

    /**
     * 五秒
     */
    public static final Long FIVE_SECOND = 5L;

}
