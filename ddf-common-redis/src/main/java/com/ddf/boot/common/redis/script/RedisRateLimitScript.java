package com.ddf.boot.common.redis.script;

import com.ddf.boot.common.redis.constant.ScriptConst;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * <p>Redis 分布式限流脚本类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/12/11 11:00
 */
public class RedisRateLimitScript implements RedisScript<Boolean> {


    @Override
    public String getSha1() {
        return DigestUtils.sha1DigestAsHex(ScriptConst.SCRIPT_DISTRIBUTED_RATE_LIMIT);
    }

    @Override
    public Class<Boolean> getResultType() {
        return Boolean.class;
    }

    @Override
    public String getScriptAsString() {
        return ScriptConst.SCRIPT_DISTRIBUTED_RATE_LIMIT;
    }
}
