-- 基于令牌桶的限流脚本
-- Created by IntelliJ IDEA.
-- User: Administrator
-- Date: 2021/1/16 0016
-- Time: 18:53
-- To change this template use File | Settings | File Templates.

-- 举个例子解释一下逻辑： 为了方便计算，就不举例突发流量令牌桶最大数量大于恢复速率的问题
-- 假如最大令牌数量为10，恢复速率是1秒10个，则每100毫秒恢复1个。
-- 第一次获取令牌:
--     current_token == nil条件成立， 因为令牌桶还没初始化，current_token肯定是nil， 然后执行初始化，
--     current_token则为max_token，直接为最大令牌桶数量
--     current_time则为当前时间戳, 为方便这个例子中假设为10000
--     last_time 本意为上次恢复令牌时间， 在第一次获取令牌时值与当前时间戳相等， 毕竟初始化也算恢复令牌，而且是满桶。
--     执行扣减令牌数量之后，则
--     current_token - 1 为最新令牌桶数量， 当前例子中为9
--     计算令牌桶key的过期时间， 这个过期时间是看多久能够将已消耗的令牌数量能够恢复满桶， 然后用这个时间作为过期时间
--          reverse_time为恢复一个令牌所需时间, 这个例子中为1000 / 10 = 100, reverse_time * (max_token - current_token)， 计算全部已消耗令牌数恢复所需时间
--          current_time - last_time 计算从上一次恢复令牌到现在过了多久， 因为有可能某一次获取令牌时间与上次恢复令牌时间间隔太小，是不足以恢复令牌的， 在第一次获取这个计算结果为0
--          由上面两个推导可以得出，第一次获取令牌桶key的过期时间为100 * (10 - 9) + (10000 - 10000) = 100， 即恢复一个令牌桶所需时间
------------------------------------------------------------------------
-- 第二次获取令牌: 这里打比方本次获取令牌距离上次过去了90ms
--     past_time为从上次恢复令牌到现在过去了多久， 单位毫秒，注意是恢复令牌，如果上次执行的时候没有恢复令牌，则这个时间还是上上次的恢复令牌时间，
--              则当前，past_time = current_time - last_time = 100090 - 10000 = 90
--     reverse_token为本次需要恢复多少令牌， 则当前 reverse_token = past_time / reverse_time = 90 / 100 = 0， 本次不恢复令牌
--     current_token = current_token + reverse_token， 库存中令牌加当前恢复令牌数为最新令牌数，本次未恢复令牌， 则当前为 9 + 0 = 9，
--     last_time， 重新计算最后一次恢复令牌时间， 是个相对值， 由上次恢复时间 + 本次恢复的令牌桶耗时，
--              last_time = reverse_time * reverse_token + last_time， 本次未恢复，则 100 * 0 + 10000 = 10000， 与上次保持一致
--     计算令牌桶key的过期时间， 这个过期时间是看多久能够将已消耗的令牌数量能够恢复满桶， 然后用这个时间作为过期时间
--          reverse_time为恢复一个令牌所需时间, 这个例子中为1000 / 10 = 100, reverse_time * (max_token - current_token)， 计算全部已消耗令牌数恢复所需时间
--          current_time - last_time 计算从上一次恢复令牌到现在过了多久， 因为有可能某一次获取令牌时间与上次恢复令牌时间间隔太小，是不足以恢复令牌的， 在本次则为90
--          由上面两个推导可以得出，第二次获取令牌桶key的过期时间为100 * (10 - 8) + (10090 - 10000) = 290， 即恢复两个令牌桶的时间外加一个耗时但没恢复令牌的额外消耗
---------------------------------------------------------------------------------------------------------------
-- 第三次获取令牌: 这里打比方本次获取令牌距离上次又过去了50ms
--     past_time为从上次恢复令牌到现在过去了多久， 单位毫秒，注意是恢复令牌，如果上次执行的时候没有恢复令牌，则这个时间还是上上次的恢复令牌时间，
--              则当前，past_time = current_time - last_time = (10090 + 50) - 10000 = 140
--     reverse_token为本次需要恢复多少令牌， 则当前 reverse_token = past_time / reverse_time = 140 / 100 = 1， 本次恢复一个令牌
--     current_token = current_token + reverse_token， 库存中令牌加当前恢复令牌数为最新令牌数，本次恢复1个令牌， 则当前为 8 + 1 = 9，
--     last_time， 重新计算最后一次恢复令牌时间， 是个相对值， 由上次恢复时间 + 本次恢复的令牌桶耗时，
--              last_time = reverse_time * reverse_token + last_time， 恢复一个令牌，则 100 * 1 + 10000 = 10100
--     计算令牌桶key的过期时间， 这个过期时间是看多久能够将已消耗的令牌数量能够恢复满桶， 然后用这个时间作为过期时间
--          reverse_time为恢复一个令牌所需时间, 这个例子中为1000 / 10 = 100, reverse_time * (max_token - current_token)， 计算全部已消耗令牌数恢复所需时间
--          current_time - last_time 计算从上一次恢复令牌到现在过了多久， 因为有可能某一次获取令牌时间与上次恢复令牌时间间隔太小，是不足以恢复令牌的， 在本次则为140
--          由上面两个推导可以得出，第二次获取令牌桶key的过期时间为100 * (10 - 8) + (100140 - 10100) = 240， 即恢复两个令牌桶的时间外加一个耗时但没恢复令牌的额外消耗，
--                    上次额外消耗了90ms，这次又过去了50ms， 其中10ms补贴上次了90ms凑成100ms恢复一个令牌， 然后还额外消耗40ms
---------------------------------------------------------------------------------------------------------------

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
    -- 获取从上次恢复令牌之后到现在一共过去了多久，单位毫秒，注意是恢复令牌，如果上次执行的时候没有恢复令牌，则这个时间还是上上次的恢复令牌时间
    local past_time = current_time - last_time
    -- 获取从上次获取令牌之后到现在应该要恢复多少令牌数量
    local reverse_token = math.floor(past_time / reverse_time)
    -- 将缓存中的旧的令牌数加上这段时间恢复的，即为当前最新令牌数量
    current_token = current_token + reverse_token
    -- 获取恢复令牌的最后时间， 如果本次获取令牌并没有恢复令牌，则这个时间不变
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

-- 计算过期时间， 注意这个过期时间不是以窗口时间来算过期，即不是计算还剩多少时间过期，而是计算，已消耗的令牌数会在多久内能够恢复， 如果恢复的话，则这个key就过期，
-- 再次获取如果过期的话，说明这个令牌桶是满的，则重新进行最大化令牌桶初始化
redis.call('pexpire', KEYS[1], math.ceil(reverse_time * (max_token - current_token) + (current_time - last_time)))
return tostring(result);

