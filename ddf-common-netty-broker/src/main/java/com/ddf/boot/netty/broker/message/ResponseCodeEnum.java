package com.ddf.boot.netty.broker.message;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>指令响应码枚举</p >
 *
 * @author
 * @version 1.0
 * @date 2020/05/11 19:07
 */
public enum ResponseCodeEnum {

    /**
     * 成功
     */
    CODE_COMPLETE(200),


    /**
     * 通用错误状态码
     */
    CODE_ERROR(500),

    /**
     * 异步指令暂时不关心执行结果，仅仅是告诉指令发送方指令发送成功
     */
    CODE_RECEIVED(202),


    /**
     * 设备繁忙，不处理当前指令
     */
    DEVICE_IS_BUSY(106),


    /**
     * 响应请求资源不存在
     */
    RESOURCE_NOT_EXIST(404),

    /**
     * 格式无效
     */
    DATA_FORMAT_INVALID(422),

    /**
     * 指令调用方针对同一个业务主键数据重复下达指令
     */
    REPEAT_REQUEST(400),

    /**
     * 无权限
     */
    UNAUTHORIZED(401);

    private static final Map<Integer, ResponseCodeEnum> MAPPING_MAP;

    static {
        MAPPING_MAP = new HashMap<>(ResponseCodeEnum.values().length);
        for (ResponseCodeEnum value : ResponseCodeEnum.values()) {
            MAPPING_MAP.put(value.code, value);
        }
    }


    private final Integer code;

    ResponseCodeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }

    /**
     * 根据状态码构建枚举对象
     *
     * @param code code码
     * @return
     */
    public ResponseCodeEnum getByCode(Integer code) {
        return MAPPING_MAP.get(code);
    }

}
