-- 对积分进行最高分判断， 如果是上榜最高分，则删除当前分数。本质上是对zadd_with__max_score_check的删除操作

-- KEYS[1] zset的key
-- KEYS[2] 存这个榜单的业务数据的key
-- KEYS[3] 存这个榜单里所有score的汇总
-- ARGV[1] element
-- ARGV[2] zset的score
-- ARGV[3] scoreFactor      分数系数，如果score是小数, 需要这个系数放大到整数，因为小数要预留给第二维度的排序规则
-- ARGV[4] scoreDecimal     分数的小数值，直接就是小数位，比如0.1，不能是整数, 如果榜单累加值很大， 会丢失小数部分精度，暂不考虑
-- ARGV[5] expireSeconds     过期时间
local rankingKey = KEYS[1]
local detailKey = KEYS[2]
local sumKey = KEYS[3]
local element = ARGV[1]
local newOriginScore = tonumber(ARGV[2])
local scoreFactor = tonumber(ARGV[3])
local scoreDecimal = tonumber(ARGV[4]) or 0
local expireSeconds = tonumber(ARGV[5])

-- 查询旧值
local oldPersistScore = tonumber(redis.call('ZSCORE', rankingKey, element))
local oldOriginScore = 0
if oldPersistScore then
    oldOriginScore = math.floor(oldPersistScore / scoreFactor)
end
-- 总分值不用关心是否是最大值，只要进来就递减(目前业务决定，所以理论上这里和上榜上的积分不一定会相等，后续如果有需求可以定义参数决定行为)
redis.call('INCRBYFLOAT', sumKey, -newOriginScore)
if expireSeconds > 0 then
    redis.call('EXPIRE', sumKey, expireSeconds)
end
-- 说明是上榜分数， 要从榜单上删掉, 只要判断原始分数就可以，否则无法处理用时间戳作为二级排序的。
local matched = tonumber(oldOriginScore) == tonumber(newOriginScore)
if matched then
    local del = redis.call('ZREM', rankingKey, element)
    redis.call('HDEL', detailKey, element)
    return cjson.encode({ score = 0, updated = 1 })
end
return cjson.encode({ score = oldOriginScore, updated = 0 })





