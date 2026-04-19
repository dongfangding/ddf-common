package com.ddf.boot.common.alarm.notify;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.alarm.config.DingTalkProperties;
import com.ddf.boot.common.alarm.config.LarkProperties;
import com.ddf.boot.common.alarm.model.LarkContentRequest;
import com.ddf.boot.common.alarm.model.LarkTag;
import com.ddf.boot.common.alarm.rule.tablescan.TableAutoCreateNotifyInfo;
import com.ddf.boot.common.alarm.rule.tablescan.TableNotExistNotifyInfo;
import com.ddf.boot.common.alarm.rule.tablescan.TableNotify;
import com.ddf.boot.common.alarm.util.DingTalkUtil;
import com.ddf.boot.common.alarm.util.LarkUtil;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * <p>description</p >
 *
 * @author rebot
 * @version 1.0
 * @since 2024/06/04 14:52
 */
@RequiredArgsConstructor
@Slf4j
public class TableNotifyImpl implements TableNotify {

    private final DingTalkProperties dingTalkProperties;
    private final LarkProperties larkProperties;
    private final SmartInitializingSingleton loadBalancedAsyncRestTemplateInitializer;
    @Override
    public void notifyNotExistTables(TableNotExistNotifyInfo info) {
        notifyNotExistTablesToDingTalk(info);
        notifyNotExistTablesToLark(info);
    }
    /**
     * @param info 参数
     */
    private void notifyNotExistTablesToDingTalk(TableNotExistNotifyInfo info) {
        final DingTalkProperties.Properties propertiesBizResource = dingTalkProperties.getBizResource();
        if (Objects.isNull(propertiesBizResource) || !propertiesBizResource.isEnabled()) {
            return;
        }
        final List<String> nextMonthNotExistTables = info.getNextMonthNotExistTables();
        if (CollUtil.isEmpty(nextMonthNotExistTables)) {
            return;
        }
        StringBuilder sbl = new StringBuilder();
        sbl.append("# 数据表不存在: \n");
        sbl.append("## 数据库: \n").append(">").append(info.getDatabase()).append(" \n");
        sbl.append("## 所属连接: \n").append(">").append(info.getUrl()).append(" \n");
        sbl.append("## 下月不存在的表列表: \n").append(" ");
        for (String table : nextMonthNotExistTables) {
            sbl.append("* ").append(table).append(" \n");
        }
        DingTalkUtil.sendMarkdownMsgToAll(propertiesBizResource.getSecret(), propertiesBizResource.getAccessToken(),
                "数据库表不存在", sbl.toString());
    }



    /**
     * 发送到Lark机器人
     *
     * @param info 参数
     */
    private void notifyNotExistTablesToLark(TableNotExistNotifyInfo info) {
        try {
            final List<String> nextMonthNotExistTables = info.getNextMonthNotExistTables();
            if (CollUtil.isEmpty(nextMonthNotExistTables)) {
                return;
            }
            final LarkProperties.Properties propertiesBizResource = larkProperties.getBizResource();
            if (Objects.isNull(propertiesBizResource) || !propertiesBizResource.isEnabled()) {
                return;
            }
            LarkContentRequest request = new LarkContentRequest();
            final List<List<LarkTag>> lists = Lists.newArrayList();
            lists.add(List.of(LarkTag.buildText("数据表不存在: ")));
            lists.add(List.of(LarkTag.buildText("数据库: " + info.getDatabase())));
            lists.add(List.of(LarkTag.buildText("所属连接: : " + info.getUrl())));
            final List<List<LarkTag>> tablesList = Lists.newArrayList();
            tablesList.add(List.of(LarkTag.buildText("下月不存在的表列表: ")));
            for (String table : nextMonthNotExistTables) {
                tablesList.add(List.of(LarkTag.buildText("* " + table + " \n")));
            }
            lists.addAll(tablesList);
            request.setContent(lists);
            LarkUtil.sendPostMsgType(
                propertiesBizResource.getWebhookUrl(), propertiesBizResource.getSecret(),
                "自动创建表结构", request
            );
        } catch (Exception e) {
            log.error("发送lark异常告警失败", e);
        }
    }
    /**
     * @param info 参数
     */
    @Override
    public void notifyAuthCreateTable(TableAutoCreateNotifyInfo info) {
        notifyAuthCreateTableToLark(info);
        notifyAuthCreateTableToDingTalk(info);
    }
    /**
     * @param info 参数
     */
    private void notifyAuthCreateTableToDingTalk(TableAutoCreateNotifyInfo info) {
        final DingTalkProperties.Properties propertiesBizResource = dingTalkProperties.getBizResource();
        if (Objects.isNull(propertiesBizResource) || !propertiesBizResource.isEnabled()) {
            return;
        }
        StringBuilder sbl = new StringBuilder();
        sbl.append("# 数据表自动创建: \n");
        sbl.append("## 数据库: \n").append(">").append(info.getDatabase()).append(" \n");
        sbl.append("## 所属连接: \n").append(">").append(info.getUrl()).append(" \n");
        sbl.append("## 当月表名: \n").append(">").append(info.getCurrentMonthTableName()).append(" \n");
        sbl.append("## 下月表名: \n").append(">").append(info.getNextMonthTableName()).append(" \n");
        sbl.append("## 预建表语句: \n").append(">").append(info.getCreateTableSql()).append(" \n");
        sbl.append("## 建表结果: \n").append(">").append(StringUtils.isBlank(info.getErrorMsg()) ? "创建成功 \n" : "创建失败 \n").append(StringUtils.defaultIfBlank(
            info.getErrorMsg(), info.getShowCreateTableSql())).append(" \n");
        DingTalkUtil.sendMarkdownMsgToAll(propertiesBizResource.getSecret(), propertiesBizResource.getAccessToken(),
                "自动创建表结构", sbl.toString());
    }


    /**
     * 使用lark通知建表结果
     * @param info 参数
     */
    private void notifyAuthCreateTableToLark(TableAutoCreateNotifyInfo info) {
        try {
            final LarkProperties.Properties propertiesBizResource = larkProperties.getBizResource();
            if (Objects.isNull(propertiesBizResource) || !propertiesBizResource.isEnabled()) {
                return;
            }
            LarkContentRequest request = new LarkContentRequest();
            final List<List<LarkTag>> lists = List.of(
                List.of(LarkTag.buildText("数据表自动创建: ")),
                List.of(LarkTag.buildText("数据库: " + info.getDatabase())),
                List.of(LarkTag.buildText("所属连接: : " + info.getCurrentMonthTableName())),
                List.of(LarkTag.buildText("当月表名: " + info.getDatabase())),
                List.of(LarkTag.buildText("下月表名: " + info.getNextMonthTableName())),
                List.of(LarkTag.buildText("预建表语句: " + info.getCreateTableSql())),
                List.of(LarkTag.buildText("建表结果: " + (StringUtils.isBlank(info.getErrorMsg()) ? "创建成功" : "创建失败" + StringUtils.defaultIfBlank(info.getErrorMsg(), info.getShowCreateTableSql())))),
                List.of(LarkTag.buildText("数据库: " + info.getDatabase()))
            );
            request.setContent(lists);
            LarkUtil.sendPostMsgType(
                propertiesBizResource.getWebhookUrl(), propertiesBizResource.getSecret(),
                "自动创建表结构", request
            );
        } catch (Exception e) {
            log.error("发送lark异常告警失败", e);
        }
    }
}
