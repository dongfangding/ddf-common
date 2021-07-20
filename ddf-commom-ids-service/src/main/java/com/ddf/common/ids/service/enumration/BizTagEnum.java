package com.ddf.common.ids.service.enumration;

/**
 * <p>id 业务标签 统一在此声明!!!!!</p >
 *
 * @author YUNTAO
 * @version 1.0
 * @date 2020/09/16 13:47
 */
public enum BizTagEnum {

    NUWA_LIVE_LIVE_NO("NUWA_LIVE_LIVE_NO", "直播服务-直播间编号"),

    NUWA_CONFIG_TEMPLATE_NO("NUWA_CONFIG_TEMPLATE_NO", "配置-模板编号"),

    NUWA_CONFIG_APPLIACTION_NO("NUWA_CONFIG_APPLIACTION_NO", "配置-应用编号"),
    NUWA_CONFIG_APPLIACTION_APPID("NUWA_CONFIG_APPLIACTION_APPID", "配置-应用APPID编号"),
    NUWA_CONFIG_APPLIACTION_SCREATE("NUWA_CONFIG_APPLIACTION_SCREATE", "配置-应用SCREATE"),

    NUWA_CONFIG_MODULE_NO("NUWA_CONFIG_MODULE_NO", "配置-模块编号"),

    NUWA_CONFIG_MODULE_CONFIG_NO("NUWA_CONFIG_MODULE_CONFIG_NO", "配置-配置信息编号"),

    NUWA_CONFIG_MODULE_SUPPLIER_NO("NUWA_CONFIG_MODULE_SUPPLIER_NO", "配置-供应商编号"),

    NUWA_CONFIG_MERCHANT_NO("NUWA_CONFIG_MERCHANT_NO", "配置-商户编号"),
    NUWA_ROLE_NO("NUWA_ROLE_NO", "角色编号"),
    NUWA_USER_NO("NUWA_USER_NO", "用户编号"),
    NUWA_ROLE_USER_NO("NUWA_ROLE_USER_NO", "用户角色编号"),
    NUWA_RESOURCE_NO("NUWA_RESOURCE_NO", "资源编号"),
    NUWA_PERMISSION_NO("NUWA_PERMISSION_NO", "角色-资源编号"),
    ;

    BizTagEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取枚举描述
     *
     * @param type
     * @return
     */
    public static String value(String type) {
        for (BizTagEnum status : values()) {
            if (status.code.equals(type)) {
                return status.desc;
            }
        }
        return null;
    }

    /**
     * 获取枚举实例
     *
     * @param type
     * @return
     */
    public static BizTagEnum valueOfType(String type) {
        for (BizTagEnum status : values()) {
            if (status.code.equals(type)) {
                return status;
            }
        }
        return null;
    }


    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;

    }

    private String code;

    private String desc;

}
