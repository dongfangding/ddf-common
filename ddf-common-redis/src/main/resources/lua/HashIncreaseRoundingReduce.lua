-- 该脚本的作用是在hash自增时进行取模运算， 为求尽量通用且与业务部绑定，需要外部传入要操作的key和hash key
-- 还需要传入递增的值以及取模的模数，最终返还取模后的值，算出来之后会减去取模用掉的数值，重新设置当前hash的value。

local hashKey = ARGV[1]
local step = tonumber(ARGV[2])
local modules = tonumber(ARGV[3])
local result = redis.call('HINCRBY', KEYS[1], hashKey, step)
local round = 0
if (result > 0 and modules > 0) then
    round = math.modf(result / modules)
    if (round > 0) then
        -- 将求整消耗的数值减掉
        redis.call('HINCRBY', KEYS[1], hashKey, -(round * modules))
    end
end
return tostring(round)
