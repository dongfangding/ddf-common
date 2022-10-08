-- 该脚本的作用是在hash自增时进行上限判定，如果超出上限则回退本次增加数值， 脚本提供自增和判断以及回退的整个原子性保证
-- 需要注意的是返回的result并不一定是实际的value数值，否则外部无法进行超限判定。
-- 如果外部再超限后需要知道最新值， 则需要再外部拿result - step反推即可，这本身也是这个脚本当时数据的真实数值

local hashKey = ARGV[1]
local step = tonumber(ARGV[2]);
local limit = tonumber(ARGV[3]);
local result = redis.call('HINCRBY', KEYS[1], hashKey, step)
if (result > limit) then
    -- 超出限制将值减回去
    redis.call('HINCRBY', KEYS[1], hashKey, -step)
end
-- 允许设置过期时间，不设置的话maxTtl给0即可
if (ARGV[4] ~= nil and tonumber(ARGV[4]) > 0 and redis.call('TTL', KEYS[1]) == -1) then
    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[4]))
end
-- 如果走了上面超限的代码，这里返回这个值就不是实际存储的value值了，这是为了让外部可以拿返回值和Limit判断是否超限
return tostring(result);
