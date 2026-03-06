-- 对zset进行zadd操作， 当传入的score大于已有值时才会更新

-- KEYS[1] zset的key
-- KEYS[2] 存这个榜单的业务数据的key
-- KEYS[3] 存这个榜单里所有score的汇总
-- ARGV[1] element
-- ARGV[2] zset的score
-- ARGV[3] scoreFactor      分数系数，如果score是小数, 需要这个系数放大到整数，因为小数要预留给第二维度的排序规则
-- ARGV[4] scoreDecimal     分数的小数值，直接就是小数位，比如0.1，不能是整数, 如果榜单累加值很大， 会丢失小数部分精度，暂不考虑
-- ARGV[5] expireSeconds     过期时间
-- ARGV[6] detailJson      (可选，业务数据)
local rankingKey = KEYS[1]
local detailKey = KEYS[2]
local sumKey = KEYS[3]
local element = ARGV[1]
local newOriginScore = tonumber(ARGV[2])
local scoreFactor = tonumber(ARGV[3])
local scoreDecimal = tonumber(ARGV[4]) or 0
local expireSeconds = tonumber(ARGV[5])
local detailJson = ARGV[6]

-- 查询旧值
local oldPersistScore = tonumber(redis.call('ZSCORE', rankingKey, element))
local oldOriginScore = 0
if oldPersistScore then
    oldOriginScore = math.floor(oldPersistScore / scoreFactor)
end
-- 总分值不用关心是否是最大值，只要进来就递增(目前业务决定，所以理论上这里和上榜上的积分不一定会相等，后续如果有需求可以定义参数决定行为)
redis.call('INCRBYFLOAT', sumKey, newOriginScore)
if expireSeconds > 0 then
    redis.call('EXPIRE', sumKey, expireSeconds)
end
local finalScore = (newOriginScore * scoreFactor) + scoreDecimal
-- 如果旧值不存在，或者新值更大，就更新...增加计算二级排序更大也更新值
if not oldPersistScore or newOriginScore > oldOriginScore or finalScore > oldPersistScore then
    redis.call('ZADD', rankingKey, finalScore, element)
    -- 这种写法只统计最高分的汇总，如果覆盖的话，加的是差值，要把以前旧的低分数减掉
    -- redis.call('INCRBYFLOAT', sumKey, newOriginScore - oldOriginScore)
    -- 只有在 detailJson 真的传了，才更新业务数据
    if detailJson and detailJson ~= nil and detailJson ~= '' and detailJson ~= 'null' then
        redis.call('HSET', detailKey, element, detailJson)
    end
    -- 设置过期时间
    if expireSeconds > 0 then
        redis.call('EXPIRE', rankingKey, expireSeconds)
        redis.call('EXPIRE', detailKey, expireSeconds)
    end
    return cjson.encode({ score = newOriginScore, updated = 1 })
else
    return cjson.encode({ score = oldOriginScore, updated = 0 })
end





