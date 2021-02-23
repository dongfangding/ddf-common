-- 基于滑动时间窗口
-- Created by IntelliJ IDEA.
-- User: Administrator
-- Date: 2021/2/23 0023
-- Time: 16:52
-- To change this template use File | Settings | File Templates.

-- 获取单位时间内定义允许最大访问次数
local maxCount = tonumber(ARGV[1])
-- 获取定义的窗口时间, 单位秒
local windowIntervalSeconds = tonumber(ARGV[2])
-- 获取当前时间戳
local currentTime = tonumber(ARGV[3])
-- 当前存入的value, 应该保证唯一不重复且随机， 脚本中不好产生，由外部传入
local currentValue = tostring(ARGV[4])

-- 移除已过期数据
redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, currentTime - windowIntervalSeconds * 1000)
-- 获取当前key已存在数据大小
local currentCount = redis.call('ZCARD', KEYS[1])
if (currentCount >= maxCount) then
    return 0
end

-- 增加一个成员数据
redis.call('ZADD', KEYS[1], currentTime, currentValue)
redis.call('EXPIRE', KEYS[1], windowIntervalSeconds)

return 1



