package com.ddf.boot.common.websocket.model.entity;

import com.ddf.boot.common.core.entity.BaseDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 收款短信模板
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PlatformMessageTemplate extends BaseDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模板机构名称 */
    @ApiModelProperty(value = "模板标题")
    private String title;

    /** 模板内容 */
    @ApiModelProperty(value = "模板内容")
    private String templateContext;

    @ApiModelProperty("模板类型")
    private Integer templateType;

    @ApiModelProperty("模板描述")
    private String templateRemark;

    @ApiModelProperty("应用类型代码")
    private String clientChannel;

    @ApiModelProperty(value = "模板所属方标识，如发件号码")
    private String credit;

    @ApiModelProperty("排序，数字越小，优先级越高")
    private Integer sort;

    public enum Type {
        /** 个人码到账消息  */
        NORMAL_INCOME_TOPIC_MESSAGE(1),
        /** 商户码到账通知 */
        MERCHANT_INCOME_TOPIC_MESSAGE(2),
        /** 转账通知 */
        PAY_TOPIC_MESSAGE(3),
        /** 登录验证码 */
        LOGIN_VERIFY_CODE(4),
        /** 转账验证码 */
        PAY_VERIFY_CODE(5),
        /** 忽略处理 */
        IGNORE_MESSAGE(6),
        /** 收入短信模板 */
        BANK_INCOME_SMS(7),
        /** 支出短信模板 */
        BANK_PAY_SMS(8),
        /** 垃圾短信模板 */
        GARBAGE_SMS(9),
        /** 安全认证短信 */
        SAFETY_CERTIFICATION_SMS(10),
        /**
         * 短信回复确认
         */
        CONFIRM_SMG(11),
        /**
         * 注册验证码
         */
        REGISTRY_VERIFY_CODE(12)
        ;


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
