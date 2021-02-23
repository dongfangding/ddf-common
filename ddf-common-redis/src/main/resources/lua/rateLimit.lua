-- 基于令牌桶的限流脚本
-- Created by IntelliJ IDEA.
-- User: Administrator
-- Date: 2021/1/16 0016
-- Time: 18:53
-- To change this template use File | Settings | File Templates.
--

-- 获取上次获取令牌的时间和当前剩余token， 这个剩余token只是目前redis中存的，并不是真实准确的，因为没有定时在获取之后实时去恢复令牌， 这个在后面会重新计算
local ratelimit_info = redis.pcall('HMGET', KEYS[1], 'last_time', 'current_token')
local last_time = ratelimit_info[1]
local current_token = tonumber(ratelimit_info[2])

-- 获取单位时间内最大令牌数量
local max_token = tonumber(ARGV[1])
-- 获取每秒钟恢复的令牌数量
local token_rate = tonumber(ARGV[2])
local current_time = tonumber(ARGV[3])
-- 获取恢复一个令牌所需毫秒值
local reverse_time = 1000 / token_rate

-- 如果当前令牌数量为空，说明之前不存在，则直接填充最大令牌数
if current_token == nil then
    current_token = max_token
    last_time = current_time
else
    -- 获取从上次获取令牌之后到现在一共过去了多久，单位毫秒
    local past_time = current_time - last_time
    -- 获取从上次获取令牌之后到现在应该要恢复多少令牌数量
    local reverse_token = math.floor(past_time / reverse_time)
    -- 将缓存中的旧的令牌数加上这段时间恢复的，即为当前最新令牌数量
    current_token = current_token + reverse_token
    -- 再次获取令牌的最后时间， 以恢复令牌和数量为耗时
    last_time = reverse_time * reverse_token + last_time
    if current_token > max_token then
        current_token = max_token
    end
end
local result = 0

-- 令牌足够， 递减即可
if (current_token > 0) then
    result = 1
    current_token = current_token - 1
end
redis.call('HMSET', KEYS[1], 'last_time', last_time, 'current_token', current_token)
redis.call('pexpire', KEYS[1], math.ceil(reverse_time * (max_token - current_token) + (current_time - last_time)))
return result;

