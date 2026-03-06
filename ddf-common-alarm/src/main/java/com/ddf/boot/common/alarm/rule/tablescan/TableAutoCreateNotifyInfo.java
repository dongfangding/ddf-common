package com.ddf.boot.common.alarm.rule.tablescan;

import lombok.Data;

/**
 * <p>自动创建表通知</p >
 *
 * @author rebot
 * @version 1.0
 * @date 2024/06/04 11:33
 */
@Data
public class TableAutoCreateNotifyInfo {

    /**
     * 连接地址
     */
    private String url;

    /**
     * 数据库
     */
    private String database;

    /**
     * 当月表名
     */
    private String currentMonthTableName;

    /**
     * 下月表名
     */
    private String nextMonthTableName;

    /**
     * 建表语句
     */
    private String createTableSql;

    /**
     * 重新从数据库反查出来的建表语句
     */
    private String showCreateTableSql;

    /**
     * 自动建表失败的原因
     */
    private String errorMsg;

}
