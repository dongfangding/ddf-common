package com.ddf.boot.common.redis.helper;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

/**
 * Redis工具类
 */

public class RedisCommandHelper {
    private StringRedisTemplate redisTemplate;
    public RedisCommandHelper(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public StringRedisTemplate getRedisTemplate() {
        return this.redisTemplate;
    }

    /** -------------------key相关操作--------------------- */

    /**
     * 删除key
     *
     * @param key 目标键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除key
     *
     * @param keys 键集合
     */
    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 序列化key
     *
     * @param key 目标键
     * @return
     */
    public byte[] dump(String key) {
        return redisTemplate.dump(key);
    }

    /**
     * 是否存在key
     *
     * @param key 目标键
     * @return
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     *
     * @param key 目标键
     * @param timeout 超时时长
     * @param unit 时间或距离单位
     * @return
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 设置过期时间
     *
     * @param key 目标键
     * @param timeout 超时时长
     * @return
     */
    public Boolean expire(String key, long timeout) {
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 注意:TimeUnit: 常用的颗粒度
     * TimeUnit.DAYS          //天
     * TimeUnit.HOURS         //小时
     * <p>
     * 设置过期时间
     *
     * @param key 目标键
     * @param date 日期时间
     * @return
     */
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }

    /**
     * 查找匹配的key
     *
     * @param pattern 匹配表达式
     * @return
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 将当前数据库的 key 移动到给定的数据库 db 当中
     *
     * @param key 目标键
     * @param dbIndex 数据库索引
     * @return
     */
    public Boolean move(String key, int dbIndex) {
        return redisTemplate.move(key, dbIndex);
    }

    /**
     * 移除 key 的过期时间，key 将持久保持
     *
     * @param key 目标键
     * @return
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    /**
     * 返回 key 的剩余的过期时间
     *
     * @param key 目标键
     * @param unit 时间或距离单位
     * @return
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 返回 key 的剩余的过期时间
     *
     * @param key 目标键
     * @return
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 从当前数据库中随机返回一个 key
     *
     * @return
     */
    public String randomKey() {
        return redisTemplate.randomKey();
    }

    /**
     * 修改 key 的名称
     *
     * @param oldKey 原键
     * @param newKey 新键
     */
    public void rename(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * 仅当 newkey 不存在时，将 oldKey 改名为 newkey
     *
     * @param oldKey 原键
     * @param newKey 新键
     * @return
     */
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    /**
     * 返回 key 所储存的值的类型
     *
     * @param key 目标键
     * @return
     */
    public DataType type(String key) {
        return redisTemplate.type(key);
    }

    /** -------------------string相关操作--------------------- */

    /**
     * 设置指定 key 的值
     *
     * @param key 目标键
     * @param value 参数值
     */
    public void set(String key, String value) {
        redisTemplate
                .opsForValue()
                .set(key, value);
    }

    /**
     * 设置指定 key 的值
     *
     * @param key 目标键
     * @param value 参数值
     * @param timeout 超时时长
     */
    public void set(String key, String value, long timeout) {
        redisTemplate
                .opsForValue()
                .set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取指定 key 的值
     *
     * @param key 目标键
     * @return
     */
    public String get(String key) {
        return redisTemplate
                .opsForValue()
                .get(key);
    }

    /**
     * 返回 key 中字符串值的子字符
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public String getRange(String key, long start, long end) {
        return redisTemplate
                .opsForValue()
                .get(key, start, end);
    }

    /**
     * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
     *
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public String getAndSet(String key, String value) {
        return redisTemplate
                .opsForValue()
                .getAndSet(key, value);
    }

    /**
     * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)
     *
     * @param key 目标键
     * @param offset 偏移量
     * @return
     */
    public Boolean getBit(String key, long offset) {
        return redisTemplate
                .opsForValue()
                .getBit(key, offset);
    }

    /**
     * 批量获取
     *
     * @param keys 键集合
     * @return
     */
    public List<String> multiGet(Collection<String> keys) {
        return redisTemplate
                .opsForValue()
                .multiGet(keys);
    }

    /**
     * 设置ASCII码, 字符串'a'的ASCII码是97, 转为二进制是'01100001', 此方法是将二进制第offset位值变为value
     *
     * @param key   目标键
     * @param value 参数值
     * @param offset 偏移量
     * @return
     */
    public boolean setBit(String key, long offset, boolean value) {
        return redisTemplate
                .opsForValue()
                .setBit(key, offset, value);
    }

    /**
     * 将值 value 关联到 key ，并将 key 的过期时间设为 timeout
     *
     * @param key 目标键
     * @param value 参数值
     * @param timeout 超时时长
     * @param unit    时间或距离单位
     *                秒:TimeUnit.SECONDS 毫秒:TimeUnit.MILLISECONDS
     */
    public void setEx(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate
                .opsForValue()
                .set(key, value, timeout, unit);
    }

    /**
     * 将值 value 关联到 key ，并将 key 的过期时间设为 timeout
     *
     * @param key 目标键
     * @param value 参数值
     * @param timeout 超时时长
     */
    public void setEx(String key, String value, long timeout) {
        redisTemplate
                .opsForValue()
                .set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 只有在 key 不存在时设置 key 的值
     *
     * @param key 目标键
     * @param value 参数值
     * @return 之前已经存在返回false, 不存在返回true
     */
    public boolean setIfAbsent(String key, String value) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(key, value);
    }

    /**
     * 只有在 key 不存在时设置 key 的值
     *
     * @param key 目标键
     * @param value 参数值
     * @param timeout 超时时长
     * @param unit 时间或距离单位
     * @return 之前已经存在返回false, 不存在返回true
     */
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始
     *
     * @param key 目标键
     * @param value 参数值
     * @param offset 偏移量
     */
    public void setRange(String key, String value, long offset) {
        redisTemplate
                .opsForValue()
                .set(key, value, offset);
    }

    /**
     * 获取字符串的长度
     *
     * @param key 目标键
     * @return
     */
    public Long size(String key) {
        return redisTemplate
                .opsForValue()
                .size(key);
    }

    /**
     * 批量添加
     *
     * @param maps 映射数据
     */
    public void multiSet(Map<String, String> maps) {
        redisTemplate
                .opsForValue()
                .multiSet(maps);
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
     *
     * @param maps 映射数据
     * @return 之前已经存在返回false, 不存在返回true
     */
    public boolean multiSetIfAbsent(Map<String, String> maps) {
        return redisTemplate
                .opsForValue()
                .multiSetIfAbsent(maps);
    }

    /**
     * 增加(自增长), 负数则为自减
     *
     * @param key 目标键
     * @param increment 增量值
     * @return
     */
    public Long incrBy(String key, long increment) {
        return redisTemplate
                .opsForValue()
                .increment(key, increment);
    }

    /**
     * @param key 目标键
     * @param increment 增量值
     * @return
     */
    public Double incrByFloat(String key, double increment) {
        return redisTemplate
                .opsForValue()
                .increment(key, increment);
    }

    /**
     * 追加到末尾
     *
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Integer append(String key, String value) {
        return redisTemplate
                .opsForValue()
                .append(key, value);
    }

    /** -------------------hash相关操作------------------------- */

    /**
     * 获取存储在哈希表中指定字段的值
     *
     * @param key 目标键
     * @param field 字段名
     * @return
     */
    public String hGet(String key, String field) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(key, field);
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key 目标键
     * @return
     */
    public Map<String, String> hGetAll(String key) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(key);
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key 目标键
     * @return
     */
    public Map<String, Object> hGetAllObject(String key) {
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(key);
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key 目标键
     * @param fields 字段集合
     * @return
     */
    public List<String> hMultiGet(String key, Collection<String> fields) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.multiGet(key, fields);
    }

    /**
     * h mget地图
     *
     * @param key    目标键
     * @param fields 字段集合
     */
    public Map<String, String> hMGetMap(String key, Collection<String> fields) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        List<String> values = hashOperations.multiGet(key, fields);
        if (values == null) {
            return new LinkedHashMap<>();
        }
        HashMap<String, String> data = new LinkedHashMap<>();
        List<String> fieldList = new ArrayList<>(fields);
        for (int i = 0; i < fieldList.size(); i++) {
            data.put(fieldList.get(i), values.get(i));
        }
        return data;
    }
    /**
     * @param key 目标键
     * @param hashKey 哈希字段键
     * @param value 参数值
     */
    public void hPut(String key, String hashKey, String value) {
        redisTemplate
                .opsForHash()
                .put(key, hashKey, value);
    }
    /**
     * @param key 目标键
     * @param maps 参数
     */
    public void hPutAll(String key, Map<String, String> maps) {
        redisTemplate
                .opsForHash()
                .putAll(key, maps);
    }
    /**
     * @param key 目标键
     * @param maps 参数
     */
    public void hPutAllObject(String key, Map<String, Object> maps) {
        redisTemplate
                .opsForHash()
                .putAll(key, maps);
    }

    /**
     * 仅当hashKey不存在时才设置
     *
     * @param key 目标键
     * @param hashKey 哈希字段键
     * @param value 参数值
     * @return
     */
    public Boolean hPutIfAbsent(String key, String hashKey, String value) {
        return redisTemplate
                .opsForHash()
                .putIfAbsent(key, hashKey, value);
    }

    /**
     * 删除一个或多个哈希表字段
     *
     * @param key 目标键
     * @param fields 字段集合
     * @return
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate
                .opsForHash()
                .delete(key, fields);
    }

    /**
     * 查看哈希表 key 中，指定的字段是否存在
     *
     * @param key 目标键
     * @param field 字段名
     * @return
     */
    public boolean hExists(String key, String field) {
        return redisTemplate
                .opsForHash()
                .hasKey(key, field);
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     *
     * @param key 目标键
     * @param field 字段名
     * @param increment 增量值
     * @return
     */
    public Long hIncrBy(String key, Object field, long increment) {
        return redisTemplate
                .opsForHash()
                .increment(key, field, increment);
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     *
     * @param key 目标键
     * @param field 字段名
     * @param delta 变化量
     * @return
     */
    public Double hIncrByFloat(String key, Object field, double delta) {
        return redisTemplate
                .opsForHash()
                .increment(key, field, delta);
    }

    /**
     * 获取所有哈希表中的字段
     *
     * @param key 目标键
     * @return
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate
                .opsForHash()
                .keys(key);
    }

    /**
     * 获取哈希表中字段的数量
     *
     * @param key 目标键
     * @return
     */
    public Long hSize(String key) {
        return redisTemplate
                .opsForHash()
                .size(key);
    }

    /**
     * 获取哈希表中所有值
     *
     * @param key 目标键
     * @return
     */
    public List<Object> hValues(String key) {
        return redisTemplate
                .opsForHash()
                .values(key);
    }

    /**
     * 迭代哈希表中的键值对
     *
     * @param key 目标键
     * @param options 操作参数
     * @return
     */
    public Cursor<Entry<Object, Object>> hScan(String key, ScanOptions options) {
        return redisTemplate
                .opsForHash()
                .scan(key, options);
    }

    /** ------------------------list相关操作---------------------------- */

    /**
     * 通过索引获取列表中的元素
     *
     * @param key 目标键
     * @param index 索引位置
     * @return
     */
    public String lIndex(String key, long index) {
        return redisTemplate
                .opsForList()
                .index(key, index);
    }

    /**
     * 获取列表指定范围内的元素
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end   结束位置
     * @return
     */
    public List<String> lRange(String key, long start, long end) {
        return redisTemplate
                .opsForList()
                .range(key, start, end);
    }

    /**
     * 存储在list头部
     *
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long lLeftPush(String key, String value) {
        return redisTemplate
                .opsForList()
                .leftPush(key, value);
    }

    /**
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long lLeftPushAll(String key, String... value) {
        return redisTemplate
                .opsForList()
                .leftPushAll(key, value);
    }

    /**
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long lLeftPushAll(String key, Collection<String> value) {
        return redisTemplate
                .opsForList()
                .leftPushAll(key, value);
    }

    /**
     * 当list存在的时候才加入
     *
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long lLeftPushIfPresent(String key, String value) {
        return redisTemplate
                .opsForList()
                .leftPushIfPresent(key, value);
    }

    /**
     * 如果pivot存在,再pivot前面添加
     *
     * @param key 目标键
     * @param pivot 基准元素
     * @param value 参数值
     * @return
     */
    public Long lLeftPush(String key, String pivot, String value) {
        return redisTemplate
                .opsForList()
                .leftPush(key, pivot, value);
    }

    /**
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long lRightPush(String key, String value) {
        return redisTemplate
                .opsForList()
                .rightPush(key, value);
    }

    /**
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long lRightPushAll(String key, String... value) {
        return redisTemplate
                .opsForList()
                .rightPushAll(key, value);
    }

    /**
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long lRightPushAll(String key, Collection<String> value) {
        return redisTemplate
                .opsForList()
                .rightPushAll(key, value);
    }

    /**
     * 为已存在的列表添加值
     *
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long lRightPushIfPresent(String key, String value) {
        return redisTemplate
                .opsForList()
                .rightPushIfPresent(key, value);
    }

    /**
     * 在pivot元素的右边添加值
     *
     * @param key 目标键
     * @param pivot 基准元素
     * @param value 参数值
     * @return
     */
    public Long lRightPush(String key, String pivot, String value) {
        return redisTemplate
                .opsForList()
                .rightPush(key, pivot, value);
    }

    /**
     * 通过索引设置列表元素的值
     *
     * @param key 目标键
     * @param index 索引位置
     * @param value 参数值
     */
    public void lSet(String key, long index, String value) {
        redisTemplate
                .opsForList()
                .set(key, index, value);
    }

    /**
     * 移出并获取列表的第一个元素
     *
     * @param key 目标键
     * @return 删除的元素
     */
    public String lLeftPop(String key) {
        return redisTemplate
                .opsForList()
                .leftPop(key);
    }

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param key 目标键
     * @param timeout 超时时长
     * @param unit    时间或距离单位
     * @return
     */
    public String lBLeftPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate
                .opsForList()
                .leftPop(key, timeout, unit);
    }

    /**
     * 移除并获取列表最后一个元素
     *
     * @param key 目标键
     * @return 删除的元素
     */
    public String lRightPop(String key) {
        return redisTemplate
                .opsForList()
                .rightPop(key);
    }

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param key 目标键
     * @param timeout 超时时长
     * @param unit    时间或距离单位
     * @return
     */
    public String lBRightPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate
                .opsForList()
                .rightPop(key, timeout, unit);
    }

    /**
     * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     *
     * @param sourceKey 源键
     * @param destinationKey 目标键
     * @return
     */
    public String lRightPopAndLeftPush(String sourceKey, String destinationKey) {
        return redisTemplate
                .opsForList()
                .rightPopAndLeftPush(sourceKey, destinationKey);
    }

    /**
     * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param sourceKey 源键
     * @param destinationKey 目标键
     * @param timeout 超时时长
     * @param unit 时间或距离单位
     * @return
     */
    public String lBRightPopAndLeftPush(String sourceKey, String destinationKey, long timeout, TimeUnit unit) {
        return redisTemplate
                .opsForList()
                .rightPopAndLeftPush(sourceKey, destinationKey, timeout, unit);
    }

    /**
     * 删除集合中值等于value得元素
     *
     * @param key 目标键
     * @param index 索引位置
     *              index&lt;0, 从尾部开始删除第一个值等于value的元素;
     * @param value 参数值
     * @return
     */
    public Long lRemove(String key, long index, String value) {
        return redisTemplate
                .opsForList()
                .remove(key, index, value);
    }

    /**
     * 裁剪list
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end 结束位置
     */
    public void lTrim(String key, long start, long end) {
        redisTemplate
                .opsForList()
                .trim(key, start, end);
    }

    /**
     * 获取列表长度
     *
     * @param key 目标键
     * @return
     */
    public Long lLen(String key) {
        return redisTemplate
                .opsForList()
                .size(key);
    }

    /** --------------------set相关操作-------------------------- */

    /**
     * set添加元素
     *
     * @param key 目标键
     * @param values 参数值集合
     * @return
     */
    public Long sAdd(String key, String... values) {
        return redisTemplate
                .opsForSet()
                .add(key, values);
    }

    /**
     * set移除元素
     *
     * @param key 目标键
     * @param values 参数值集合
     * @return
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate
                .opsForSet()
                .remove(key, values);
    }

    /**
     * 移除并返回集合的一个随机元素
     *
     * @param key 目标键
     * @return
     */
    public String sPop(String key) {
        return redisTemplate
                .opsForSet()
                .pop(key);
    }

    /**
     * 移除并返回集合的一个随机元素
     *
     * @param key 目标键
     * @param count 数量
     * @return
     */
    public List<String> sPop(String key, int count) {
        return redisTemplate
                .opsForSet()
                .pop(key, count);
    }

    /**
     * 将元素value从一个集合移到另一个集合
     *
     * @param key 目标键
     * @param value 参数值
     * @param destKey 目标键
     * @return
     */
    public Boolean sMove(String key, String value, String destKey) {
        return redisTemplate
                .opsForSet()
                .move(key, value, destKey);
    }

    /**
     * 获取集合的大小
     *
     * @param key 目标键
     * @return
     */
    public Long sSize(String key) {
        return redisTemplate
                .opsForSet()
                .size(key);
    }

    /**
     * 判断集合是否包含value
     *
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate
                .opsForSet()
                .isMember(key, value);
    }
    /**
     * @param key 目标键
     * @param values 参数值集合
     */
    public Map<Object, Boolean> sIsMember(String key, Object... values) {
        return redisTemplate
                .opsForSet()
                .isMember(key, values);
    }

    /**
     * 获取两个集合的交集
     *
     * @param key 目标键
     * @param otherKey 另一个键
     * @return
     */
    public Set<String> sIntersect(String key, String otherKey) {
        return redisTemplate
                .opsForSet()
                .intersect(key, otherKey);
    }

    /**
     * 获取key集合与多个集合的交集
     *
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @return
     */
    public Set<String> sIntersect(String key, Collection<String> otherKeys) {
        return redisTemplate
                .opsForSet()
                .intersect(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的交集存储到destKey集合中
     *
     * @param key 目标键
     * @param otherKey 另一个键
     * @param destKey 目标键
     * @return
     */
    public Long sIntersectAndStore(String key, String otherKey, String destKey) {
        return redisTemplate
                .opsForSet()
                .intersectAndStore(key, otherKey, destKey);
    }

    /**
     * key集合与多个集合的交集存储到destKey集合中
     *
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @param destKey 目标键
     * @return
     */
    public Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate
                .opsForSet()
                .intersectAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的并集
     *
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @return
     */
    public Set<String> sUnion(String key, String otherKeys) {
        return redisTemplate
                .opsForSet()
                .union(key, otherKeys);
    }

    /**
     * 获取key集合与多个集合的并集
     *
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @return
     */
    public Set<String> sUnion(String key, Collection<String> otherKeys) {
        return redisTemplate
                .opsForSet()
                .union(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的并集存储到destKey中
     *
     * @param key 目标键
     * @param otherKey 另一个键
     * @param destKey 目标键
     * @return
     */
    public Long sUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate
                .opsForSet()
                .unionAndStore(key, otherKey, destKey);
    }

    /**
     * key集合与多个集合的并集存储到destKey中
     *
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @param destKey 目标键
     * @return
     */
    public Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate
                .opsForSet()
                .unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的差集
     *
     * @param key 目标键
     * @param otherKey 另一个键
     * @return
     */
    public Set<String> sDifference(String key, String otherKey) {
        return redisTemplate
                .opsForSet()
                .difference(key, otherKey);
    }

    /**
     * 获取key集合与多个集合的差集
     *
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @return
     */
    public Set<String> sDifference(String key, Collection<String> otherKeys) {
        return redisTemplate
                .opsForSet()
                .difference(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的差集存储到destKey中
     *
     * @param key 目标键
     * @param otherKey 另一个键
     * @param destKey 目标键
     * @return
     */
    public Long sDifference(String key, String otherKey, String destKey) {
        return redisTemplate
                .opsForSet()
                .differenceAndStore(key, otherKey, destKey);
    }

    /**
     * key集合与多个集合的差集存储到destKey中
     *
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @param destKey 目标键
     * @return
     */
    public Long sDifference(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate
                .opsForSet()
                .differenceAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取集合所有元素
     *
     * @param key 目标键
     * @return
     */
    public Set<String> setMembers(String key) {
        return redisTemplate
                .opsForSet()
                .members(key);
    }

    /**
     * 随机获取集合中的一个元素
     *
     * @param key 目标键
     * @return
     */
    public String sRandomMember(String key) {
        return redisTemplate
                .opsForSet()
                .randomMember(key);
    }

    /**
     * 随机获取集合中count个元素
     *
     * @param key 目标键
     * @param count 数量
     * @return
     */
    public List<String> sRandomMembers(String key, long count) {
        return redisTemplate
                .opsForSet()
                .randomMembers(key, count);
    }

    /**
     * 随机获取集合中count个元素并且去除重复的
     *
     * @param key 目标键
     * @param count 数量
     * @return
     */
    public Set<String> sDistinctRandomMembers(String key, long count) {
        return redisTemplate
                .opsForSet()
                .distinctRandomMembers(key, count);
    }

    /**
     * @param key 目标键
     * @param options 操作参数
     * @return
     */
    public Cursor<String> sScan(String key, ScanOptions options) {
        return redisTemplate
                .opsForSet()
                .scan(key, options);
    }

    /**------------------zSet相关操作--------------------------------*/

    /**
     * 添加元素,有序集合是按照元素的score值由小到大排列
     *
     * @param key 目标键
     * @param value 参数值
     * @param score 分值
     * @return
     */
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate
                .opsForZSet()
                .add(key, value, score);
    }

    /**
     * @param key 目标键
     * @param values 参数值集合
     * @return
     */
    public Long zAdd(String key, Set<TypedTuple<String>> values) {
        return redisTemplate
                .opsForZSet()
                .add(key, values);
    }

    /**
     * @param key 目标键
     * @param values 参数值集合
     * @return
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate
                .opsForZSet()
                .remove(key, values);
    }

    /**
     * 增加元素的score值，并返回增加后的值
     *
     * @param key 目标键
     * @param value 参数值
     * @param delta 变化量
     * @return
     */
    public Double zIncrementScore(String key, String value, double delta) {
        return redisTemplate
                .opsForZSet()
                .incrementScore(key, value, delta);
    }

    /**
     * 返回元素在集合的排名,有序集合是按照元素的score值由小到大排列
     *
     * @param key 目标键
     * @param value 参数值
     * @return 0表示第一位
     */
    public Long zRank(String key, Object value) {
        return redisTemplate
                .opsForZSet()
                .rank(key, value);
    }

    /**
     * 返回元素在集合的排名,按元素的score值由大到小排列
     *
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Long zReverseRank(String key, Object value) {
        return redisTemplate
                .opsForZSet()
                .reverseRank(key, value);
    }

    /**
     * 获取集合的元素, 从小到大排序
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end   结束位置
     * @return
     */
    public Set<String> zRange(String key, long start, long end) {
        return redisTemplate
                .opsForZSet()
                .range(key, start, end);
    }

    /**
     * 获取集合元素, 并且把score值也获取
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public Set<TypedTuple<String>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate
                .opsForZSet()
                .rangeWithScores(key, start, end);
    }

    /**
     * 根据Score值查询集合元素
     *
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public Set<String> zRangeByScore(String key, double min, double max) {
        return redisTemplate
                .opsForZSet()
                .rangeByScore(key, min, max);
    }

    /**
     * 根据Score值查询集合元素, 从小到大排序
     *
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public Set<TypedTuple<String>> zRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate
                .opsForZSet()
                .rangeByScoreWithScores(key, min, max);
    }

    /**
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public Set<TypedTuple<String>> zRangeByScoreWithScores(String key, double min, double max, long start, long end) {
        return redisTemplate
                .opsForZSet()
                .rangeByScoreWithScores(key, min, max, start, end);
    }

    /**
     * 获取集合的元素, 从大到小排序
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        return redisTemplate
                .opsForZSet()
                .reverseRange(key, start, end);
    }

    /**
     * 获取集合的元素, 从大到小排序, 并返回score值
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public Set<TypedTuple<String>> zReverseRangeWithScores(String key, long start, long end) {
        return redisTemplate
                .opsForZSet()
                .reverseRangeWithScores(key, start, end);
    }

    /**
     * 批量获取多个集合的元素, 从大到小排序, 并返回score值
     *
     * @param keys 键集合
     * @param offset 偏移量
     * @param count 数量
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public List<Set<TypedTuple<String>>> batchZReverseRangeWithScores(List<String> keys, double min, double max,
            long offset, long count) {
        List<Object> resultList = executePipelined(connection -> {
            for (String key : keys) {
                connection.zRevRangeByScoreWithScores(key.getBytes(StandardCharsets.UTF_8), min, max, offset, count);
            }
            return null;
        });
        List<Set<TypedTuple<String>>> returnList = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            final Object o = resultList.get(i);
            if (o instanceof Set data) {
                returnList.add(data);
            } else {
                returnList.add(null);
            }
        }
        return returnList;
    }
    /**
     * @param keys 键集合
     * @param member 成员值
     */
    public Map<String, Double> batchZScore(List<String> keys, String member) {
        final List<Object> valueList = executePipelined(connection -> {
            for (String key : keys) {
                connection.zScore(key.getBytes(StandardCharsets.UTF_8), member.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
        Map<String, Double> scoreMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            scoreMap.put(keys.get(i), Double.parseDouble(Optional
                    .ofNullable(valueList.get(i))
                    .orElse(0)
                    .toString()));
        }
        return scoreMap;
    }

    /**
     * 批量获取多个集合的元素, 并返回score值
     * @param keys 键集合
     * @param min 最小值
     * @param max 最大值
     */
    public List<Set<TypedTuple<String>>> batchZRangWithScore (List<String> keys, Long min, Long max) {
        List<Object> resultList = executePipelined(connection -> {
            for (String key : keys) {
                connection.zRangeWithScores(key.getBytes(StandardCharsets.UTF_8), min, max);
            }
            return null;
        });
        List<Set<TypedTuple<String>>> returnList = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            final Object o = resultList.get(i);
            if (o instanceof Set data) {
                returnList.add(data);
            } else {
                returnList.add(Sets.newHashSetWithExpectedSize(0));
            }
        }
        return returnList;
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public Set<String> zReverseRangeByScore(String key, double min, double max) {
        return redisTemplate
                .opsForZSet()
                .reverseRangeByScore(key, min, max);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public Set<TypedTuple<String>> zReverseRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate
                .opsForZSet()
                .reverseRangeByScoreWithScores(key, min, max);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public Set<TypedTuple<String>> zReverseRangeByScoreWithScores(String key, double min, double max, long start,
            long end) {
        return redisTemplate
                .opsForZSet()
                .reverseRangeByScoreWithScores(key, min, max, start, end);
    }

    /**
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public Set<String> zReverseRangeByScore(String key, double min, double max, long start, long end) {
        return redisTemplate
                .opsForZSet()
                .reverseRangeByScore(key, min, max, start, end);
    }

    /**
     * 根据score值获取集合元素数量
     *
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public Long zCount(String key, double min, double max) {
        return redisTemplate
                .opsForZSet()
                .count(key, min, max);
    }

    /**
     * 获取集合大小
     *
     * @param key 目标键
     * @return
     */
    public Long zSize(String key) {
        return redisTemplate
                .opsForZSet()
                .size(key);
    }

    /**
     * 获取集合大小
     *
     * @param key 目标键
     * @return
     */
    public Long zZCard(String key) {
        return redisTemplate
                .opsForZSet()
                .zCard(key);
    }

    /**
     * 获取集合中value元素的score值
     *
     * @param key 目标键
     * @param value 参数值
     * @return
     */
    public Double zScore(String key, Object value) {
        return redisTemplate
                .opsForZSet()
                .score(key, value);
    }

    /**
     * 移除指定索引位置的成员
     *
     * @param key 目标键
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate
                .opsForZSet()
                .removeRange(key, start, end);
    }

    /**
     * 根据指定的score值的范围来移除成员
     *
     * @param key 目标键
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate
                .opsForZSet()
                .removeRangeByScore(key, min, max);
    }

    /**
     * 获取key和otherKey的并集并存储在destKey中
     *
     * @param key 目标键
     * @param otherKey 另一个键
     * @param destKey 目标键
     * @return
     */
    public Long zUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate
                .opsForZSet()
                .unionAndStore(key, otherKey, destKey);
    }

    /**
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @param destKey 目标键
     * @return
     */
    public Long zUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate
                .opsForZSet()
                .unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 交集
     *
     * @param key 目标键
     * @param otherKey 另一个键
     * @param destKey 目标键
     * @return
     */
    public Long zIntersectAndStore(String key, String otherKey, String destKey) {
        return redisTemplate
                .opsForZSet()
                .intersectAndStore(key, otherKey, destKey);
    }

    /**
     * 交集
     *
     * @param key 目标键
     * @param otherKeys 其他键集合
     * @param destKey 目标键
     * @return
     */
    public Long zIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate
                .opsForZSet()
                .intersectAndStore(key, otherKeys, destKey);
    }

    /**
     * @param key 目标键
     * @param options 操作参数
     * @return
     */
    public Cursor<TypedTuple<String>> zScan(String key, ScanOptions options) {
        return redisTemplate
                .opsForZSet()
                .scan(key, options);
    }

    /**
     * 管道原生命令
     *
     * @param action 执行动作
     * @return
     */
    public List<Object> executePipelined(RedisCallback<?> action) {
        return redisTemplate.executePipelined(action);
    }

    /**
     * 批量zscore
     *
     * @param key 目标键
     * @param members 成员列表
     * @return
     */
    public Map<String, Double> batchZScore(String key, List<String> members) {
        final List<Object> valueList = executePipelined(connection -> {
            for (String member : members) {
                connection.zScore(key.getBytes(StandardCharsets.UTF_8), member.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
        Map<String, Double> scoreMap = new HashMap<>();
        for (int i = 0; i < members.size(); i++) {
            scoreMap.put(members.get(i), Double.parseDouble(Optional
                    .ofNullable(valueList.get(i))
                    .orElse(0)
                    .toString()));
        }
        return scoreMap;
    }

    //====================================== GEO ========================================\\

    /**
     * 将具有给定成员名称的 Point 添加到键中。
     *
     * @param key 目标键
     * @param member 成员值
     * @param longitude 经度
     * @param latitude  纬度
     */
    public void addGeo(String key, String member, double longitude, double latitude) {
        Point point = new Point(longitude, latitude);
        redisTemplate
                .opsForGeo()
                .add(key, point, member);
    }

    /**
     * 删除成员
     *
     * @param key 目标键
     * @param member 成员值
     */
    public void removeGeo(String key, String... member) {
        redisTemplate
                .opsForGeo()
                .remove(key, member);
    }

    /**
     * 获取给定圆边界内的成员
     *
     * @param key 目标键
     * @param longitude 经度
     * @param latitude 纬度
     * @param radius 半径
     * @param unit 时间或距离单位
     * @param paArgs GEO 查询附加参数
     * @return
     */
    public GeoResults<GeoLocation<String>> geoRadius(String key, double longitude, double latitude, double radius,
            RedisGeoCommands.DistanceUnit unit, RedisGeoCommands.GeoRadiusCommandArgs paArgs) {
        return redisTemplate
                .opsForGeo()
                .radius(key, new Circle(new Point(longitude, latitude), new Distance(radius, unit)), paArgs);

    }

    /**
     * 批量zcard
     *
     * @param keys 键集合
     * @return
     */
    public Map<String, Long> batchZCard(List<String> keys) {
        final List<Object> valueList = executePipelined(connection -> {
            for (String key : keys) {
                connection.zCard(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
        Map<String, Long> sizeMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            sizeMap.put(keys.get(i), Long.parseLong(Optional
                    .ofNullable(valueList.get(i))
                    .orElse(0)
                    .toString()));
        }
        return sizeMap;
    }

    /**
     * 批量zrank
     *
     * @param members 成员列表
     * @param key 目标键
     * @return
     */
    public Map<String, Integer> batchZRank(String key, List<String> members) {
        final List<Object> valueList = executePipelined(connection -> {
            for (String member : members) {
                connection.zRevRank(key.getBytes(StandardCharsets.UTF_8), member.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });
        Map<String, Integer> sizeMap = new HashMap<>();
        for (int i = 0; i < members.size(); i++) {
            sizeMap.put(members.get(i), Integer.parseInt(Optional
                    .ofNullable(valueList.get(i))
                    .orElse(0)
                    .toString()));
        }
        return sizeMap;
    }
    /**
     * @param pattern 匹配表达式
     */
    public List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions
                    .scanOptions()
                    .match(pattern + "*")
                    .build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }
            return null;
        });
        return keys;
    }
    /**
     * @param keys 键集合
     */
    public Map<String, Long> getTtlForKeys(List<String> keys) {
        Map<String, Long> ttlMap = new HashMap<>();
        List<Object> objects = redisTemplate.executePipelined((RedisCallback<Long>) connection -> {
            for (String key : keys) {
                connection.ttl(key.getBytes());
            }
            return null;
        });
        for (int i = 0; i < keys.size(); i++) {
            ttlMap.put(keys.get(i), Long.parseLong(Optional
                    .ofNullable(objects.get(i))
                    .orElse(0)
                    .toString()));
        }
        return ttlMap;
    }

    /**
     * 批量获取map的所有数据
     *
     * @param keys 键集合
     * @return
     */
    public Map<String, Map<String, String>> hMultiMapGetAll(List<String> keys) {
        Map<String, Map<String, String>> allMap = new HashMap<>();
        List<Object> objects = redisTemplate.executePipelined((RedisCallback<Long>) connection -> {
            for (String key : keys) {
                connection.hGetAll(key.getBytes());
            }
            return null;
        });
        for (int i = 0; i < keys.size(); i++) {
            Object o = objects.get(i);
            if (Objects.isNull(o)) {
                allMap.put(keys.get(i), Maps.newHashMap());
            } else {
                allMap.put(keys.get(i), (Map<String, String>) o);
            }
        }
        return allMap;
    }

    /**
     * 使用Pipeline批量获取多个 ZSet 的数据
     *
     * @param keys      键集合
     * @param offsets   每个 key 对应的起始游标映射
     * @param batchSize 每个 key 批量拉取的数据量
     * @return 返回 Map，其中 key 为原始ZSet key，value 为该key对应的候选房间ID集合
     */
    public Map<String, Set<String>> pipelineZRange(List<String> keys, Map<String, Integer> offsets, int batchSize) {
        // 使用executePipelined执行Pipeline命令，返回结果列表，与keys顺序一致
        List<Object> pipelineResults = redisTemplate.executePipelined(new RedisCallback<Object>() {
            /**
             * @param connection 参数
             */
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 遍历每个 key，并发送zRange命令
                for (String key : keys) {
                    int offset = offsets.getOrDefault(key, 0);
                    // 将 key 序列化为字节数组
                    byte[] keyBytes = redisTemplate
                            .getStringSerializer()
                            .serialize(key);
                    connection.zRange(keyBytes, offset, offset + batchSize - 1);
                }
                return null; // 返回null，由executePipelined收集各命令结果
            }
        });

        // 构造结果 Map：keys 与 pipelineResults 是一一对应的
        Map<String, Set<String>> resultMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Set<String> resultSet = new LinkedHashSet<>();
            Object rawResult = pipelineResults.get(i);
            if (rawResult instanceof Set) {
                @SuppressWarnings("unchecked") Set<byte[]> bytesSet = (Set<byte[]>) rawResult;
                for (byte[] b : bytesSet) {
                    // 反序列化得到房间ID
                    String value = redisTemplate
                            .getStringSerializer()
                            .deserialize(b);
                    if (value != null) {
                        resultSet.add(value);
                    }
                }
            }
            resultMap.put(key, resultSet);
        }
        return resultMap;
    }

}
