package com.ddf.boot.common.stomp.repository;

import cn.hutool.core.util.RandomUtil;
import com.ddf.boot.common.redis.helper.RedisCommandHelper;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ws仓储层
 * stomp over websocket
 *
 * @author dongfang.ding
 * @since 2019/8/20 11:43
 */
@RequiredArgsConstructor
@Slf4j
public class ImTokenRepository {

    private final RedisCommandHelper redisCommandHelper;

    /**
     * 注册并刷新im token
     *
     * @param uid
     * @return
     */
    public String registerAndRefreshImToken(String uid) {
        final String token = RandomUtil.randomString(64);
        redisCommandHelper.setEx(String.format("im_token:%s", token), uid, TimeUnit.DAYS.toSeconds(1));
        return token;
    }

    /**
     * 判断im token是否匹配
     *
     * @param token
     * @return
     */
    public String imTokenIsMatched(String token) {
        return redisCommandHelper.get(String.format("im_token:%s", token));
    }
}
