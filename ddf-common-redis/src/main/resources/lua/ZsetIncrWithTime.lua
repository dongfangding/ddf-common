-- 基于zset实现的排行榜计数，注意只支持整数位，因为这里的积分会在小数位根据时间进行拼接小数位，来实现整数位相同，最先到达的排名靠前
local member = ARGV[1]
-- 本次要增加的实际数值
local step = tonumber(ARGV[2])
-- 本次附加的基于时间计算的小数位
local decimal = tonumber(ARGV[3])
local scoreStr = redis.call('ZSCORE', KEYS[1], member)
local oldScore = tonumber(scoreStr)
if (oldScore == nil) then
    oldScore = 0
end
oldScore = math.floor(oldScore)
local newScore = step + oldScore + decimal
redis.call('ZADD', KEYS[1], newScore, member)
return redis.call('ZSCORE', KEYS[1], member)
