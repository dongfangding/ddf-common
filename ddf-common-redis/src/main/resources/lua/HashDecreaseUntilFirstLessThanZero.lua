-- 该脚本的作用是在hash递减时进行允许递减至第一次为负值，后续递减时，如果还为负值，则将值累加回来，保证这个值，是第一次小于等于0的值
local field = ARGV[1]
local step = tonumber(ARGV[2]);
local result = redis.call('HINCRBY', KEYS[1], field, -step)
if (result < 0 and result + step <= 0) then
    -- 超出限制将值加回去
    redis.call('HINCRBY', KEYS[1], field, step)
    return cjson.encode({ limited = 1, currentValue = result + step })
end
return cjson.encode({ limited = 0, currentValue = result })
