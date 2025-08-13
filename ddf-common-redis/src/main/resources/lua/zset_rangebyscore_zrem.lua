-- 通过扫描截止时间点， 并且删除扫描到的数据，用来做一些数据清理并且返回扫描到的数据
-- KEYS:
-- 1: scanKey (要扫描的key)

-- ARGV
-- 1:scanLimitTime (扫描的截止点)
local scanKey = KEYS[1]

local min = ARGV[1]
local max = ARGV[2]
local tables = redis.call('ZRANGEBYSCORE', scanKey, min, max)

for i, table in ipairs(tables) do
    --
    redis.call('ZREM', scanKey, table)
end

return cjson.encode(tables)

