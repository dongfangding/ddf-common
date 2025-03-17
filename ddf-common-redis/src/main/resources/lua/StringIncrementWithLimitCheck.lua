-- 该脚本的作用是在string自增时进行上限判定，如果超出上限则回退本次增加数值， 脚本提供自增和判断以及回退的整个原子性保证
local step = tonumber(ARGV[1]);
local limit = tonumber(ARGV[2]);
local result = redis.call('INCRBY', KEYS[1], step)
if (result > limit) then
    -- 超出限制将值减回去
    redis.call('INCRBY', KEYS[1], -step)
    return cjson.encode({ limited = 1, currentCount = limit, maxCount = limit })
end
-- 首次的话设置过期时间，result - step == 0简单表示首次，如果数值来回浮动也会造成表达式满足，比如+6 + 4 - 10
if (ARGV[3] ~= nil and tonumber(ARGV[3]) > 0 and (result - step) == 0) then
    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3]))
end
return cjson.encode({ limited = 0, currentCount = result, maxCount = limit })
