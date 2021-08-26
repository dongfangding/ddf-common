package com.ddf.common.ons.redis;

/**
 * <p>ONS Redis key生成规则</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/31 13:21
 */
public interface RedisKeyConstants {

    String KEY_SPLIT = ":";

    String ONS_KEY_PREFIX = "ons";

    /**
     * 拼接ons消费幂等key规则
     *
     * @param groupId
     * @param bizId
     * @return
     */
    static String getEnsureIdempotentKey(String groupId, String bizId) {
        return String.join(KEY_SPLIT, new String[] {ONS_KEY_PREFIX, groupId, bizId});
    }
}
