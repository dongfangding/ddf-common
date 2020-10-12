package com.ddf.boot.common.ext.oss.helper;

import com.aliyun.oss.OSS;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/12 13:33
 */
@Component
public class OssHelper {

    /**
     * 存储OSS实例的map, key为bucket name
     */
    private final Map<String, OSS> ossMap = new ConcurrentHashMap<>();

    /**
     * 放入实例
     * @param bucketName
     * @param oss
     */
    public void putOss(String bucketName, OSS oss) {
        ossMap.put(bucketName, oss);
    }

    /**
     * 根据bucket name 获取存储的实例
     * @param bucketName
     * @return
     */
    public OSS getOss(String bucketName) {
        return ossMap.get(bucketName);
    }

}
