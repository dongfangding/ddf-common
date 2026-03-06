-- 本质是zadd_with__max_score_check.lua脚本的查询版本

-- KEYS[1]: ZSET key (排名榜)
-- KEYS[2]: 存这个榜单的业务数据的key
-- KEYS[3]: 存这个榜单里所有score的汇总
-- ARGV[1]: start index (0-based)
-- ARGV[2]: end index (inclusive)
-- ARGV[3]: ARGV[3] scoreFactor      分数系数，如果score是小数, 需要这个系数放大到整数，因为小数要预留给第二维度的排序规则
-- 返回 JSON: {
--    totalElements = n,
--    totalScore = x,
--    list = [
--       { element = ..., score = ..., detail = ... },
--       ...
--    ]
-- }

local rankingKey = KEYS[1]
local detailKey = KEYS[2]
local sumKey = KEYS[3]

local startIdx = tonumber(ARGV[1])
local endIdx = tonumber(ARGV[2])
local scoreFactor = tonumber(ARGV[3])

-- 获取榜单总人数
local totalElements = redis.call('ZCARD', rankingKey)

-- 获取总分
local totalScore = 0
if sumKey and sumKey ~= '' then
    local s = redis.call('GET', sumKey)
    totalScore = s and tonumber(s) or 0
end

-- 获取分页数据，降序
local rawList = redis.call('ZREVRANGE', rankingKey, startIdx, endIdx, 'WITHSCORES')
local resultList = {}
if cjson.empty_array_mt then
    setmetatable(resultList, cjson.empty_array_mt)
end
local rank = startIdx;
for i = 1, #rawList, 2 do
    rank = rank + 1
    local element = rawList[i]
    local fs = tonumber(rawList[i + 1])
    -- 还原真实 score（整数部分）
    local realScore = math.floor(fs / scoreFactor)
    -- 获取业务数据
    local detail
    if detailKey and detailKey ~= '' then
        detail = redis.call('HGET', detailKey, element)
    else
        detail = nil
    end
    table.insert(resultList, { element = element, score = realScore, rank = (rank), detail = detail })
end

local result = {
    totalElements = totalElements,
    totalScore = totalScore,
    list = resultList
}

local jsonResult = cjson.encode(result)

if not cjson.empty_array_mt then
    -- 使用 string.gsub 替换空对象为空数组
    jsonResult = string.gsub(jsonResult, '"list":%s*{}', '"list":[]')
end

return jsonResult


