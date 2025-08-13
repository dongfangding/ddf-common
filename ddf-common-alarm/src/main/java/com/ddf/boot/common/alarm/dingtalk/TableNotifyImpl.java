package com.ddf.boot.common.alarm.dingtalk;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.alarm.config.DingTalkProperties;
import com.ddf.boot.common.alarm.rule.tablescan.TableAutoCreateNotifyInfo;
import com.ddf.boot.common.alarm.rule.tablescan.TableNotExistNotifyInfo;
import com.ddf.boot.common.alarm.rule.tablescan.TableNotify;
import com.ddf.boot.common.alarm.util.DingTalkUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/06/04 14:52
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class TableNotifyImpl implements TableNotify {

    private final DingTalkProperties dingTalkProperties;

    @Override
    public void notifyNotExistTables(TableNotExistNotifyInfo info) {
        final DingTalkProperties.Properties propertiesBizResource = dingTalkProperties.getBizResource();
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

    @Override
    public void notifyAuthCreateTable(TableAutoCreateNotifyInfo info) {
        final DingTalkProperties.Properties propertiesBizResource = dingTalkProperties.getBizResource();
        StringBuilder sbl = new StringBuilder();
        sbl.append("# 数据表自动创建: \n");
        sbl.append("## 数据库: \n").append(">").append(info.getDatabase()).append(" \n");
        sbl.append("## 所属连接: \n").append(">").append(info.getUrl()).append(" \n");
        sbl.append("## 当月表名: \n").append(">").append(info.getCurrentMonthTableName()).append(" \n");
        sbl.append("## 下月表名: \n").append(">").append(info.getNextMonthTableName()).append(" \n");
        sbl.append("## 预建表语句: \n").append(">").append(info.getCreateTableSql()).append(" \n");
        sbl.append("## 建表结果: \n").append(">").append(StringUtils.isBlank(info.getErrorMsg()) ? "创建成功 \n" : "创建失败 \n").append(StringUtils.defaultIfBlank(info.getErrorMsg(), info.getShowCreateTableSql())).append(" \n");
        DingTalkUtil.sendMarkdownMsgToAll(propertiesBizResource.getSecret(), propertiesBizResource.getAccessToken(),
                "自动创建表结构", sbl.toString());
    }
}
