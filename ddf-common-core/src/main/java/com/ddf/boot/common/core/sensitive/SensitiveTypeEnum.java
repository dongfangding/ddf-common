package com.ddf.boot.common.core.sensitive;


/**
 * 数据脱敏类型
 *
 * @author dongfang.ding
 * @date 2020/9/25 0025 14:37
 **/
public enum SensitiveTypeEnum {

    /**
     * 中文名
     */
    CHINESE_NAME,

    /**
     * 身份证号
     */
    ID_CARD,

    /**
     * 座机号
     */
    FIXED_PHONE,

    /**
     * 手机号
     */
    MOBILE_PHONE,

    /**
     * 地址
     */
    ADDRESS,

    /**
     * 电子邮件
     */
    EMAIL,

    /**
     * 银行卡
     */
    BANK_CARD,

    /**
     * 密码
     */
    PASSWORD

}
