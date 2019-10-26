package com.ddf.common.websocket.model.entity;

import com.ddf.common.entity.BaseDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 收款短信模板
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PlatformMessageTemplate extends BaseDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模板类型 0：云闪付个人码到账通知  1：系统消息 2 到账短信模板 3 垃圾信息模板" +
            " 4 云闪付登录验证码 5 支出短信模板 6 云闪付转账验证码 7 云闪付商户码到账通知 8 云闪付转账通知")
    private Integer type;

    /** 模板机构名称 */
    @ApiModelProperty(value = "模板标题")
    private String title;

    /** 模板内容 */
    @ApiModelProperty(value = "模板内容")
    private String templateContext;

    @ApiModelProperty(value = "模板所属方标识，如发件号码")
    private String credit;

    @ApiModelProperty("排序，数字越小，优先级越高")
    private Integer sort;

    public enum Type {
        /** 云闪付个人码到账通知  */
        UNION_PAY_NORMAL_INCOME_MESSAGE(0),
        /** 系统模板 */
        SYSTEM(1),
        /** 收入短信模板 */
        BANK_INCOME_SMS(2),
        /** 垃圾短信模板 */
        GARBAGE_SMS(3),
        /** 云闪付登录验证码 */
        UNION_PAY_LOGIN(4),
        /** 支出短信模板 */
        BANK_PAY_SMS(5),
        /** 云闪付转账验证码 */
        UNION_PAY_VERIFY_CODE(6),
        /** 云闪付商户码到账通知 */
        UNION_PAY_MERCHANT_INCOME_MESSAGE(7),
        /** 云闪付转账通知 */
        UNION_PAY_PAY_MESSAGE(8);




        Integer value;
        static Map<Integer, Type> map = new HashMap<>();

        static {
            Type[] values = values();
            for (Type value : values) {
                map.put(value.value, value);
            }
        }

        Type(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public static Type getByValue(Integer value) {
            return map.get(value);
        }
    }
}
