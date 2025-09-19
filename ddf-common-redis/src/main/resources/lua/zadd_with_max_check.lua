-- 对zset进行zadd操作， 当传入的score大于已有值时才会更新

local element = ARGV[1]
local score = tonumber(ARGV[2])

-- 查询旧值
local oldScore = redis.call('ZSCORE', KEYS[1], element)
if oldScore then
    oldScore = tonumber(oldScore)
end

-- 如果旧值不存在，或者新值更大，就更新
if not oldScore or score > oldScore then
    redis.call('ZADD', KEYS[1], score, element)
    return cjson.encode({ score = score, updated = 1 })
else
    return cjson.encode({ score = oldScore, updated = 0 })
end





