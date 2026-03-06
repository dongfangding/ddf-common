-- 该脚本的作用是在hash自增时进行上下限判定（递增的值可以是正负数），如果超出上限则数值为传入的最大最小限制值
local field = ARGV[1]
local step = tonumber(ARGV[2])
local result = tonumber(redis.call('HINCRBYFLOAT', KEYS[1], field, step))
local minValue = tonumber(ARGV[3]) or nil
local maxValue = tonumber(ARGV[4]) or nil
-- 最小值，可以为空，则代表不限制
if (minValue ~= nil and result < minValue) then
    -- 超出最小限制将值设置允许的最小值
    redis.call('hset', KEYS[1], field, minValue)
    -- actualStep代表实际递增的值
    return cjson.encode({ limited = 1, actualStep = minValue - (result - step), currentValue = minValue })
end
-- 最大值，可以为空，则代表不限制
if (maxValue ~= nil and result > maxValue) then
    -- 超出最大限制将值设置允许的最大值
    redis.call('hset', KEYS[1], field, maxValue)
    -- actualStep代表实际递增的值
    return cjson.encode({ limited = 1, actualStep = maxValue - (result - step), currentValue = maxValue })
end
-- 首次的话设置过期时间
if (ARGV[5] ~= "" and tonumber(ARGV[5]) > 0 and redis.call('HEXISTS', KEYS[1], field) == 0) then
    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[5]))
end
return cjson.encode({ actualStep = step, currentValue = result })
