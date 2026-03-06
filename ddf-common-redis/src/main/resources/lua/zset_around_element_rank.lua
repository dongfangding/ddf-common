-- KEYS[1] = key
-- ARGV[1] = element
-- ARGV[2] = beforeFetchSize
-- ARGV[3] = afterFetchSize

local key = KEYS[1]
local element = ARGV[1]
local before = tonumber(ARGV[2])
local after = tonumber(ARGV[3])

-- 获取用户排名 (0-based)
local rank = redis.call("ZREVRANK", key, element)
if not rank then
    return "[]"
end

-- 计算 start/stop
local startIndex = rank - before
if startIndex < 0 then
    startIndex = 0
end
local stopIndex = rank + after

-- 查询范围
local arr = redis.call("ZREVRANGE", key, startIndex, stopIndex, "WITHSCORES")

-- 构造结果表
local res = {}
local idx = startIndex
for i = 1, #arr, 2 do
    local m = arr[i]
    local s = tonumber(arr[i + 1])
    table.insert(res, { element = m, score = s, rank = idx + 1 })
    idx = idx + 1
end

return cjson.encode(res)
