package com.ddf.boot.common.alarm.rule.tablescan;

import java.util.List;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author rebot
 * @version 1.0
 * @since 2024/06/04 11:33
 */
@Data
public class TableNotExistNotifyInfo {

    /**
     * 连接地址
     */
    private String url;

    /**
     * 数据库
     */
    private String database;

    /**
     * 当月分表结尾形式
     */
    private String currentMonthSuffix;

    /**
     * 下月分表结尾形式
     */
    private String nextMonthSuffix;

    /**
     * 下个月不存在的表列表
     */
    private List<String> nextMonthNotExistTables;
}
