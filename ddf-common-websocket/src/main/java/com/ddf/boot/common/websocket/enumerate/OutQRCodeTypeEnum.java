package com.ddf.boot.common.websocket.enumerate;



import java.util.HashMap;
import java.util.Map;

/**
 * 第三方二维码枚举类


 */
public enum OutQRCodeTypeEnum {

    /**
     * 二维码类型个人码
     */
    NORMAL_QRCODE(0),


    /**
     * 商户码
     */
    MERCHANT_QRCODE(1),


    ;
    /**
     * 订单状态
     */
    private Integer value;

    /**
     * 说明
     */
    private String intro;

    static Map<Integer, OutQRCodeTypeEnum> OutQRCodeTypeMap;

    static {
        OutQRCodeTypeEnum[] values = values();
        OutQRCodeTypeMap = new HashMap<>(values.length);
        for (OutQRCodeTypeEnum value : values) {
            OutQRCodeTypeMap.put(value.getValue(), value);
        }
    }

    OutQRCodeTypeEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static OutQRCodeTypeEnum getByValue(Integer value) {
        return OutQRCodeTypeMap.get(value);
    }
}
