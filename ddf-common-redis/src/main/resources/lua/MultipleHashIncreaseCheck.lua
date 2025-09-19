-- 多KEY + 固定fields + 上限判定 + 回滚
-- KEYS: Redis哈希Key数组，比如 {"user_limit:1001", "user_limit:1002"}
-- ARGV[1]: fields，固定的哈希字段，逗号分隔，比如 "1,2"
-- ARGV[2]: steps，每个字段的步长，逗号分隔，比如 "100,200"
-- ARGV[3]: limits，每个字段的上限，逗号分隔，比如 "5000,10000"
-- ARGV[4]: expire，可选过期时间，秒

local field = ARGV[1]
local stepsStr = ARGV[2]
local limitsStr = ARGV[3]
local expire = tonumber(ARGV[4])

-- 高性能 split 实现
local function fastSplit(str, delimiter)
    local result = {}
    local start = 1
    local delimLen = #delimiter
    local delimStart, delimEnd = string.find(str, delimiter, start)
    local i = 1
    while delimStart do
        result[i] = string.sub(str, start, delimStart - 1)
        i = i + 1
        start = delimEnd + 1
        delimStart, delimEnd = string.find(str, delimiter, start)
    end
    result[i] = string.sub(str, start)
    return result
end

-- 解析steps, limits
local stepsStrArr = fastSplit(stepsStr, ',')
local limitsStrArr = fastSplit(limitsStr, ',')

local steps = {}
local limits = {}
for k = 1, #KEYS do
    steps[k] = tonumber(stepsStrArr[k])
    limits[k] = tonumber(limitsStrArr[k])
end

-- 存放结果
local results = {}
local rollbackNeeded = false

-- 遍历每个Redis Key
for k = 1, #KEYS do
    local redisKey = KEYS[k]
    local newVal = redis.call('HINCRBY', redisKey, field, steps[k])
    results[k] = newVal
    if newVal > limits[k] then
        rollbackNeeded = true
    end
end

-- 如果有超限，回滚所有KEYS的所有字段
if rollbackNeeded then
    for k = 1, #KEYS do
        local redisKey = KEYS[k]
        redis.call('HINCRBY', redisKey, field, -steps[k])
    end
    return cjson.encode({
        limited = 1,
        currentCount = results
    })
end

-- 如果设置了过期时间 -> 给所有KEYS设置TTL
if expire and expire > 0 then
    for k = 1, #KEYS do
        if redis.call("TTL", KEYS[k]) < 0 then
            redis.call('EXPIRE', KEYS[k], expire)
        end
    end
end

return cjson.encode({
    limited = 0,
    currentCount = results
})
