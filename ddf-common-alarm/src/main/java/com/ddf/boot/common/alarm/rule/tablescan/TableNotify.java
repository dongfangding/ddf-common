package com.ddf.boot.common.alarm.rule.tablescan;

/**
 * <p>description</p >
 *
 * @author rebot
 * @version 1.0
 * @since 2024/06/04 11:30
 */
public interface TableNotify {

    /**
     * 通知不存在的表名
     *
     * @param info
     */
    void notifyNotExistTables(TableNotExistNotifyInfo info);

    /**
     * 通知自动建表情况
     *
     * @param info
     */
    void notifyAuthCreateTable(TableAutoCreateNotifyInfo info);
}
