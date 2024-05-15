-- 传入的要判断的值
local checkValue = ARGV[1]
local actualValue = redis.call('GET', KEYS[1])
if (actualValue == actualValue) then
    redis.call("del", KEYS[1])
    return tostring(1)
end
return tostring(0)
