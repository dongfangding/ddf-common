-- 该脚本的作用是对hash的hashKey进行incr操作， 当key是第一次操作时，设置过期时间，后续不会设置过期时间
local member = ARGV[1]
local val = ARGV[2]
local ttl = ARGV[3]
local keyExist = redis.call("EXISTS", KEYS[1])
local ret = redis.call("HINCRBY", KEYS[1], member, val)
if (keyExist < 1) then
    -- 设置过期时间
    redis.call("EXPIRE", KEYS[1], ttl)
end
return tostring(ret)
