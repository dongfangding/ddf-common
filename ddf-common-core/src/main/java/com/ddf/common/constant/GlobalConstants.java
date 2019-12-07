package com.ddf.common.constant;

/**
 * @author dongfang.ding
 * @date 2019/8/28 13:52
 */
public class GlobalConstants {

    /**
     * 本地IP
     */
    public static final String LOCALHOST = "127.0.0.1";
    /**
     * 逗号
     */
    public static final String COMMA = ",";
    /**
     * 基础包
     */
    public static final String BASE_PACKAGE = "com.ddf.scaffold";
    /**
     * mybatis mapper服务包扫描包路径
     */
    public static final String FW_TCP_MAPPER_SCAN = BASE_PACKAGE + ".fw.tcp.mapper";
    /**
     * mybatis mapper业务包扫描包路径
     */
    public static final String LOGIC_MAPPER_SCAN = BASE_PACKAGE + ".*.mapper";

    /**
     * byte类型的true,适用于所有Byte类型是否的判断
     */
    public static final Byte BYTE_TRUE = 1;

    /**
     * byte类型的false,适用于所有Byte类型是否的判断
     */
    public static final Byte BYTE_FALSE = 0;


    /**
     * byte类型的true,适用于所有Byte类型是否的判断
     */
    public static final Integer INTEGER_TRUE = 1;

    /**
     * byte类型的false,适用于所有Byte类型是否的判断
     */
    public static final Integer INTEGER_FALSE = 0;

}
