-- KEYS[1]: Redis Hash Key
-- ARGV[1]: Hash Field Name
-- ARGV[2]: 增量 JSON 字符串 (例如 '{"reward":10, "sn":"123"}')

local oldJson = redis.call('HGET', KEYS[1], ARGV[1]);
local data = {};

if oldJson then
    data = cjson.decode(oldJson);
end

-- 解析传入的增量 JSON
local updates = cjson.decode(ARGV[2]);
-- 将增量内容合并到原数据中
for key, value in pairs(updates) do
    -- 只更新非 nil 值
    if value ~= nil and value ~= "" and value ~= cjson.null then
        data[key] = value;
    end
end

local newJson = cjson.encode(data);
redis.call('HSET', KEYS[1], ARGV[1], newJson);

return newJson;
