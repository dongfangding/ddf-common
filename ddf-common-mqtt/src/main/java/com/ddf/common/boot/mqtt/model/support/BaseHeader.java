package com.ddf.common.boot.mqtt.model.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * <p>mqtt 基础请求头</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 11:29
 */
@Data
public class BaseHeader implements Serializable {

    private static final long serialVersionUID = 4813011310202804454L;

    /**
     * 扩展头每个键值对之间的分隔符，注意空格
     */
    private transient static final String SPLIT_LINE = "; ";
    /**
     * 扩展头键值对之间的分隔符，注意空格
     */
    private transient static final String SPLIT_KEY_VALUE = ": ";

    /**
     * 扩展字段
     * 添加扩展字段时请使用{@link this#addExtra}方法
     * 获取扩展字段时请从{@link this#getExtraMap()}中获取
     */
    private String extra;

    /**
     * 发送方信息，预留字段，根据场景决定是否使用
     */
    private String fromClientId;

    /**
     * 发送方uid
     */
    private String fromUserId;

    /**
     * 发送方用户名
     */
    private String fromUserName;

    /**
     * 发送方用户头像
     */
    private String fromUserAvatarUrl;

    /**
     * 发送方时间戳，单位毫秒
     */
    private String fromClientTimestamp;

    /**
     * 客户端可预留业务id追溯消息
     */
    private String bizId;

    /**
     * 解析请求头存放的对象，非报文传输字段
     */
    @JsonIgnore
    private transient Map<String, String> extraMap;

    /**
     * 添加扩展字段
     *
     * @param key
     * @param value
     * @return
     */
    public BaseHeader addExtra(String key, String value) {
        if (extra == null) {
            extra = "";
        } else if (extra.length() > 0) {
            extra += SPLIT_LINE;
        }
        extra += key + SPLIT_KEY_VALUE + value;
        parseExtra();
        return this;
    }


    public void setExtra(String extra) {
        this.extra = extra;
        parseExtra();
    }

    /**
     * 解析扩展字段,注意空格
     */
    private BaseHeader parseExtra() {
        if (null == extra || "".equals(extra)) {
            return this;
        }
        try {
            String[] keyValueArr = extra.split(SPLIT_LINE);
            if (keyValueArr.length > 0) {
                Map<String, String> extraMap = getExtraMap() == null ? new HashMap<>() : getExtraMap();
                String[] keyValue;
                for (String s : keyValueArr) {
                    keyValue = s.split(SPLIT_KEY_VALUE);
                    extraMap.put(keyValue[0], keyValue[1]);
                }
                setExtraMap(extraMap);
            }
        } catch (Exception e) {
            extraMap = new HashMap<>();
        }
        return this;
    }
}
