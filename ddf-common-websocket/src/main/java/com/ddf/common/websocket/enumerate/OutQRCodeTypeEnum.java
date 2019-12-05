package com.ddf.common.websocket.enumerate;



import java.util.HashMap;
import java.util.Map;

/**
 * 第三方二维码枚举类
 * @author honglin.jiang
 * @create 2019年09月19日
 */
public enum OutQRCodeTypeEnum {

    /**
     * 二维码类型
     */
    NORMAL_QRCODE(0, "个人码"),

    MERCHANT_QRCODE(1, "商户码"),


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

    OutQRCodeTypeEnum(Integer value, String intro) {
        this.value = value;
        this.intro = intro;
    }

    public Integer getValue() {
        return value;
    }

    public String getIntro() {
        return intro;
    }

    public static OutQRCodeTypeEnum getByValue(Integer value) {
        return OutQRCodeTypeMap.get(value);
    }
}
