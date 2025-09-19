-- 基于hash结构的批量自增并增加上限判定的通用脚本, 对同一个key的多个hash key进行自增判断, 如果有一个超出上限，则回滚之前的
-- KEYS[1]: Redis 哈希 Key
-- ARGV[1]: fields (逗号分隔，比如 "1,2")
-- ARGV[2]: steps    (逗号分隔，比如 "100,200")
-- ARGV[3]: limits   (逗号分隔，比如 "5000,10000")
-- ARGV[4]: expire   (可选，秒)

local fields = ARGV[1]
local steps = ARGV[2]
local limits = ARGV[3]
local expire = tonumber(ARGV[4])

-- 高性能字符串 split
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

-- 拆分数组
local fieldArr = fastSplit(fields, ',')
local stepArrStr = fastSplit(steps, ',')
local limitArrStr = fastSplit(limits, ',')

local size = #fieldArr
local stepArr = {}
local limitArr = {}
for i = 1, size do
    stepArr[i] = tonumber(stepArrStr[i])
    limitArr[i] = tonumber(limitArrStr[i])
end

-- 存放自增后的结果
local results = {}
local rollbackNeeded = false

-- 批量自增
for i = 1, size do
    local newVal = redis.call('HINCRBY', KEYS[1], fieldArr[i], stepArr[i])
    results[i] = newVal
    if newVal > limitArr[i] then
        rollbackNeeded = true
    end
end

-- 如果有任意一个超限，回滚所有操作
if rollbackNeeded then
    for i = 1, size do
        redis.call('HINCRBY', KEYS[1], fieldArr[i], -stepArr[i])
    end
    return cjson.encode({
        limited = 1,
        currentCount = results
    })
end

-- 首次设置过期时间
if expire and expire > 0 and redis.call("TTL", KEYS[1]) < 0 then
    redis.call('EXPIRE', KEYS[1], expire)
end

-- 返回结果
return cjson.encode({
    limited = 0,
    currentCount = results
})

