-- 对一个key进行ttl的带有上限值的累加操作， key不存在，则初始化， 存在则续期ttl
local step = tonumber(ARGV[1])
local maxValue = tonumber(ARGV[2])
local ttl = redis.call('TTL', KEYS[1])
if (ttl == -2) then
    if (step < 1) then
        return '0-0'
    end
    redis.call('SETEX', KEYS[1], step, '0')
    return '0-' .. step
end

local newTtl = ttl + step
if (newTtl > maxValue) then
    newTtl = maxValue
    -- 如果到达最大值， 更改值为1， 暂时就当预留一个标志，根据value的0和1进行一下业务上场景的是和否的判定
    redis.call('SETEX', KEYS[1], newTtl, '1')
    return '1-' .. newTtl
end

redis.call('EXPIRE', KEYS[1], newTtl)
return redis.call('GET', KEYS[1]) .. '-' .. newTtl
