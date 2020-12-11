package com.ddf.boot.common.redis.constant;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/12/11 10:57
 */
public class ScriptConst {

    /**
     * 基于redis的lua分布式限流脚本
     *
     * https://www.infoq.cn/article/Qg2tX8fyw5Vt-f3HH673
     *
     * 令牌桶算法需要在 Redis 中存储桶的大小、当前令牌数量，并且实现每隔一段时间添加新的令牌。最简单的办法当然是每隔一段时间请求一次 Redis，将存储的令牌数量递增。
     *
     * 但实际上我们可以通过对限流两次请求之间的时间和令牌添加速度来计算得出上次请求之后到本次请求时，令牌桶应添加的令牌数量。因此我们在 Redis 中只需要存储上次请求的时间和令牌桶中的令牌数量，而桶的大小和令牌的添加速度可以通过参数传入实现动态修改。
     *
     * 由于第一次运行脚本时默认令牌桶是满的，因此可以将数据的过期时间设置为令牌桶恢复到满所需的时间，及时释放资源。
     */
    public static final String SCRIPT_DISTRIBUTED_RATE_LIMIT = "local ratelimit_info = redis.pcall('HMGET',KEYS[1],'last_time','current_token')\n" +
        "local last_time = ratelimit_info[1]\n" +
        "local current_token = tonumber(ratelimit_info[2])\n" +
        "local max_token = tonumber(ARGV[1])\n" +
        "local token_rate = tonumber(ARGV[2])\n" +
        "local current_time = tonumber(ARGV[3])\n" +
        "local reverse_time = 1000/token_rate\n" +
        "if current_token == nil then\n" +
        "  current_token = max_token\n" +
        "  last_time = current_time\n" +
        "else\n" +
        "  local past_time = current_time-last_time\n" +
        "  local reverse_token = math.floor(past_time/reverse_time)\n" +
        "  current_token = current_token+reverse_token\n" +
        "  last_time = reverse_time*reverse_token+last_time\n" +
        "  if current_token>max_token then\n" +
        "    current_token = max_token\n" +
        "  end\n" +
        "end\n" +
        "local result = 0\n" +
        "if(current_token>0) then\n" +
        "  result = 1\n" +
        "  current_token = current_token-1\n" +
        "end\n" +
        "redis.call('HMSET',KEYS[1],'last_time',last_time,'current_token',current_token)\n" +
        "redis.call('pexpire',KEYS[1],math.ceil(reverse_time*(max_token-current_token)+(current_time-last_time)))\n" +
        "return result\n";
}
