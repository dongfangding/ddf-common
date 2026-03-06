-- 该脚本的作用是在hash递减时进行下限判定，如果小于下限则回退本次减少数值， 脚本提供递减和判断以及回退的整个原子性保证
local field = ARGV[1]
local step = tonumber(ARGV[2]);
local limit = tonumber(ARGV[3]);
local result = redis.call('HINCRBY', KEYS[1], field, -step)
if (result < limit) then
    -- 超出限制将值加回去
    redis.call('HINCRBY', KEYS[1], field, step)
    return cjson.encode({ limited = 1, currentCount = limit, maxCount = limit })
end
-- 首次的话设置过期时间，result - step == 0简单表示首次，如果数值来回浮动也会造成表达式满足，比如+6 + 4 - 10
if (ARGV[4] ~= nil and tonumber(ARGV[4]) > 0 and (result - step) == 0) then
    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[4]))
end
return cjson.encode({ limited = 0, currentCount = result, maxCount = limit })
