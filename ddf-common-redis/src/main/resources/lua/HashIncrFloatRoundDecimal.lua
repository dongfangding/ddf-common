-- 该脚本的作用是对hash的field进行incrFloat操作， 自增完成之后返回整数并扣除整数部分，保留小数位
-- KEYS[1]：hash 的 key
-- ARGV[1]：hash 字段
-- ARGV[2]：增量（float）

local newVal = redis.call('HINCRBYFLOAT', KEYS[1], ARGV[1], tonumber(ARGV[2]))
local intPart = math.floor(newVal)

if intPart > 0 then
    redis.call('HINCRBYFLOAT', KEYS[1], ARGV[1], -intPart)
end

return tostring(intPart)
