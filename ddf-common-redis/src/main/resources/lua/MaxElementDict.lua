-- 该脚本的作用类似于对集合进行最大值判断，当达到最后值后，将组成当前最大值的所有子元素以及对应的数量返回
-- 例如， 每个用户完成一个任务累加10分，当分数达到1000分的时候，获得组成1000分的所有的用户以及用户对应的积分， 如果未达到1000分，返回空集合
-- 特殊逻辑，当积分超过1000分，因为分数是一直在变化的，超过部分的分数要保留，哪个用户触发的则哪个用户超出的分数要保留，而且总的分数也要保留

-- gets all fields from a hash as a dictionary
local hgetall = function (key)
  local bulk = redis.call('HGETALL', key)
	local result = {}
	local nextkey
	for i, v in ipairs(bulk) do
		if i % 2 == 1 then
			nextkey = v
		else
			result[nextkey] = v
		end
	end
	return result
end
-- 身份，即hash key
local identity = ARGV[1]
-- 本次要增加的数值
local increaseValue = tonumber(ARGV[2])
-- 当前集合允许的最大数值
local maxValue = tonumber(ARGV[3])
-- 递增最大数值
local currMaxValue = redis.call('INCRBY', KEYS[1], increaseValue)
-- 给组成集合的子元素也增加自身的数值
redis.call('HINCRBY', KEYS[2], identity, increaseValue)
local result = {}
-- 当前集合已满，达到上限, 需要将组成这个集合的子元素及对应数量拼接出来
if (currMaxValue >= maxValue) then
    -- 当前集合减去最大大小，超出的部分要保留
    redis.call('INCRBY', KEYS[1], -maxValue)
    -- 获取所有子元素原始字段
    local tab = hgetall(KEYS[2])
    -- 清空原始子元素集合
    redis.call('DEL', KEYS[2])
    -- 自增一次轮次序列号，代表满足了一次集合已满的条件,外部可以把这个序列号取出来当做一个标识
    redis.call('INCRBY', KEYS[3], 1)
    local diff = currMaxValue - maxValue
    -- 超出的部分要保留，这里不考虑一次增加超出一倍的量
    if (diff > 0) then
        -- 将本次用户多余的次数再累加进去
        redis.call('HINCRBY', KEYS[2], identity, diff)
    end

    if (currMaxValue == maxValue) then
        result = tab
    else
        for key, value in pairs(tab) do
            if (key == identity) then
                -- 本次多出来的不能算到上一个整体集合中
                result[key] = value - diff
            else
                result[key] = value
            end
        end
    end
end

local str = ""
for key, value in pairs(result) do
    str = str .. key .. ":" .. value .. ";"
end
return str
