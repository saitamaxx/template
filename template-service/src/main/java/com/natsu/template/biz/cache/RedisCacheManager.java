package com.natsu.template.biz.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by sunyu on 2021-10-25
 */
@Component
public class RedisCacheManager {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public <T> T assembleRedisObject(Supplier<T> supplier, Class<T> resultClazz, String key, Long time) {
        return assembleRedisObject(supplier, resultClazz, key, time, TimeUnit.SECONDS);
    }

    /**
     * redis获取object结果
     */
    public <T> T assembleRedisObject(Supplier<T> supplier, Class<T> resultClazz, String key, Long time, TimeUnit timeUnit) {
        boolean isString = String.class == resultClazz;
        // 判断是否为空
        if (StringUtils.isBlank(key)) {
            return null;
        }
        String value = get(key);
        if (StringUtils.isBlank(value)) {
            T result = supplier.get();
            if (Objects.nonNull(result)) {
                if (isString) {
                    set(key, (String) result, time, timeUnit);
                } else {
                    set(key, JSON.toJSONString(result), time, timeUnit);
                }
            }
            return result;
        }
        // 解决字符串中包含%导致json序列号失败的问题
        return isString ? (T) value : JSON.parseObject(value, resultClazz);
    }

    public <T> List<T> assembleRedisList(Supplier<List<T>> supplier, Class<T> resultClazz, String key, Long time) {
        return assembleRedisList(supplier, resultClazz, key, false, time, TimeUnit.SECONDS);
    }

    public <T> List<T> assembleRedisList(Supplier<List<T>> supplier, Class<T> resultClazz, String key, Long time, TimeUnit timeUnit) {
        return assembleRedisList(supplier, resultClazz, key, false, time, timeUnit);
    }

    public <T> List<T> assembleRedisList(Supplier<List<T>> supplier, Class<T> resultClazz, String key, boolean cacheNull, Long time) {
        return assembleRedisList(supplier, resultClazz, key, cacheNull, time, TimeUnit.SECONDS);
    }

    /**
     * redis获取list结果
     */
    public <T> List<T> assembleRedisList(Supplier<List<T>> supplier, Class<T> resultClazz, String key, boolean cacheNull, Long time, TimeUnit timeUnit) {
        // 判断是否为空
        if (StringUtils.isBlank(key)) {
            return Lists.newArrayList();
        }
        String value = get(key);
        if (StringUtils.isBlank(value)) {
            List<T> result = supplier.get();
            if (!Objects.isNull(result)) {
                set(key, JSON.toJSONString(result), time, timeUnit);
            } else if (cacheNull) {
                set(key, "[]", time, timeUnit);
            }
            return Optional.ofNullable(result).orElse(Lists.newArrayList());
        }
        return JSON.parseArray(value, resultClazz);
    }

    public Map<String, String> assembleRedisMap(Supplier<Map<String, String>> supplier, String key, Long time) {
        return assembleRedisMap(supplier, String.class, String.class, key, false, time, TimeUnit.SECONDS);
    }

    public <K, V> Map<K, V> assembleRedisMap(Supplier<Map<K, V>> supplier, Class<K> keyType,
                                             Class<V> valueType, String key, Long time) {
        return assembleRedisMap(supplier, keyType, valueType, key, false, time, TimeUnit.SECONDS);
    }

    public <K, V> Map<K, V> assembleRedisMap(Supplier<Map<K, V>> supplier, Class<K> keyType,
                                             Class<V> valueType, String key, boolean cacheNull, Long time) {
        return assembleRedisMap(supplier, keyType, valueType, key, cacheNull, time, TimeUnit.SECONDS);
    }

    public <K, V> Map<K, V> assembleRedisMap(Supplier<Map<K, V>> supplier, Class<K> keyType,
                                             Class<V> valueType, String key, boolean cacheNull, Long time, TimeUnit timeUnit) {
        // 判断是否为空
        if (StringUtils.isBlank(key)) {
            return Maps.newHashMap();
        }
        String value = get(key);
        if (StringUtils.isBlank(value)) {
            Map<K, V> result = supplier.get();
            if (!Objects.isNull(result)) {
                set(key, JSON.toJSONString(result), time, timeUnit);
            } else if (cacheNull) {
                set(key, "{}", time, timeUnit);
            }
            return Optional.ofNullable(result).orElse(Maps.newHashMap());
        }
        return JSON.parseObject(value, new TypeReference<Map<K, V>>(keyType, valueType) {
        });
    }


    public <T> List<T> assembleRedisHashForList(Supplier<List<T>> supplier, Class<T> resultClazz, String key, String hashKey, boolean cacheNull, Long time) {
        if (StringUtils.isBlank(key)) {
            return Lists.newArrayList();
        }
        String value = getHash(key, hashKey);
        if (StringUtils.isBlank(value)) {
            List<T> result = supplier.get();
            if (!CollectionUtils.isEmpty(result)) {
                putHash(key, hashKey, JSON.toJSONString(result));
                expire(key, time);
            } else if (cacheNull) {
                putHash(key, hashKey, "[]");
                expire(key, time);
            }
            return Optional.ofNullable(result).orElse(Lists.newArrayList());
        }
        return JSON.parseArray(value, resultClazz);
    }

    /**
     * 获取KEY对应的缓存值
     *
     * @param key
     * @return
     */
    public String get(String key) {
        try {
            return key == null ? null : stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
            return null;
        }
    }

    public String getHash(String key, String hashKey) {
        if (StringUtils.isAnyBlank(key, hashKey)) {
            return null;
        }
        try {
            return (String) stringRedisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
            return null;
        }
    }

    public void putHash(String key, String hashKey, String hashValue) {
        if (StringUtils.isAnyBlank(key, hashKey, hashValue)) {
            return;
        }
        try {
            stringRedisTemplate.opsForHash().put(key, hashKey, hashValue);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
        }
    }

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     * @param timeout
     * @param unit
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        if (StringUtils.isAnyBlank(key, value)) {
            return;
        }
        try {
            if (timeout > 0) {
                stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
            } else {
                stringRedisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
        }
    }

    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        if (StringUtils.isAnyBlank(key, value)) {
            return false;
        }
        boolean result = false;
        try {
            result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
        }
        return result;
    }

    public void rightPush(String key, String value) {
        if (StringUtils.isAnyBlank(key, value)) {
            return;
        }
        try {
            stringRedisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
        }
    }

    public void rightPushAll(String key, List<String> values) {
        if (Objects.isNull(key) || CollectionUtils.isEmpty(values)) {
            return;
        }
        try {
            stringRedisTemplate.opsForList().rightPushAll(key, values);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
        }
    }

    public String leftPop(String key) {
        if (Objects.isNull(key)) {
            return null;
        }
        try {
            return stringRedisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
            return null;
        }
    }

    /**
     * 设置KEY过期时间
     *
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            return stringRedisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
            return false;
        }
    }

    public boolean expire(String key, long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    public List<String> range(String key, Integer start, Integer end) {
        if (Objects.isNull(key)) {
            return null;
        }
        try {
            return stringRedisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
            return null;
        }
    }

    public long increment(String key, long delta) {
        try {
            return stringRedisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
        }
        return 0L;
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            if (StringUtils.isNotBlank(key)) {
                return stringRedisTemplate.hasKey(key);
            }
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
        }
        return false;
    }

    public void delString(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            // LoggerUtil.error(logger, e, "redis异常");
        }
    }
}
