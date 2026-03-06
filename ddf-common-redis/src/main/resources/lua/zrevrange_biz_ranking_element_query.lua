-- 本质是zadd_with__max_score_check.lua脚本的查询版本， 这个是针对的单个元素的查询

-- KEYS[1]: ZSET key (排名榜)
-- KEYS[2]: 存这个榜单的业务数据的key
-- ARGV[1]: element 要查询的元素
-- ARGV[2]: ARGV[2] scoreFactor      分数系数，如果score是小数, 需要这个系数放大到整数，因为小数要预留给第二维度的排序规则

local rankingKey = KEYS[1]
local detailKey = KEYS[2]

local element = ARGV[1]
local scoreFactor = tonumber(ARGV[2])
local score = redis.call("ZSCORE", rankingKey, element)
if not score then
    return cjson.encode({ element = element, score = 0, rank = 0, detail = '' })
end
local rank = redis.call("ZREVRANK", rankingKey, element)
local realScore = math.floor(score / scoreFactor)
local detail
if detailKey and detailKey ~= '' and detailKey ~= nil then
    detail = redis.call('HGET', detailKey, element)
else
    detail = nil
end
return cjson.encode({ element = element, score = realScore, rank = rank + 1, detail = detail })
