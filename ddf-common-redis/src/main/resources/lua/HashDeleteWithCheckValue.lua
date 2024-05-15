-- 传入的要判断的值
local member = ARGV[1]
local checkValue = ARGV[2]
local actualValue = redis.call('HGET', KEYS[1], member)
if (actualValue == checkValue) then
    redis.call("HDEL", KEYS[1], member)
    return tostring(1)
end
return tostring(0)
