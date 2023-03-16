-- 该脚本的作用是提供一个保留最大长度的容器，如果达到最大容器大小最开始存储的数据会丢失。
-- 场景举例，比如保留用户进房历史，最多保留20个。那么member就是主播的id, score就可以用进房时间

-- 允许保留的最大长度
local maxLength = tonumber(ARGV[1])
local member = ARGV[2]
local score = ARGV[3]

redis.call('ZADD', KEYS[1], score, member)
local currSize = redis.call('ZCARD', KEYS[1])
if (currSize > maxLength) then
    redis.call('ZREMRANGEBYRANK', KEYS[1], 0, currSize - maxLength)
end
return tostring(currSize)


