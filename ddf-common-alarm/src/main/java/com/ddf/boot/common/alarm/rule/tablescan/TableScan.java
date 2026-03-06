package com.ddf.boot.common.alarm.rule.tablescan;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.api.util.PatternUtil;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * <p>description</p >
 *
 * @author rebot
 * @version 1.0
 * @date 2024/06/03 16:50
 */
@RequiredArgsConstructor
@Slf4j
@Configuration
public class TableScan {

    private final Environment environment;
    @Autowired(required = false)
    private DataSourceProperties dataSourceProperties;
    @Autowired(required = false)
    private TableNotify tableNotify;
    @Autowired(required = false)
    private DataSource dataSource;

    @PostConstruct
    public void scan() {
        // 没有数据源对象
        if (Objects.isNull(dataSource)) {
            return;
        }
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                // 这是正常项目写法，直接获取数据源，但是不能很好的适配sharding sphere, 无法获取到分表的那些表
                //                final Connection connection = SpringContextHolder.getBean(DataSource.class).getConnection();
                //                final DatabaseMetaData databaseMetaData = connection.getMetaData();
                //                final String url = databaseMetaData.getURL();
                // 特定写法，直接按照目前已有的数据源配置，硬编码，获取数据库连接信息
                String url = StringUtils.defaultString(
                        environment.getProperty("spring.shardingsphere.datasource.master.url"),
                        dataSourceProperties.getUrl()
                );
                String username = StringUtils.defaultString(
                        environment.getProperty("spring.shardingsphere.datasource.master.username"),
                        dataSourceProperties.getUsername()
                );
                String password = StringUtils.defaultString(
                        environment.getProperty("spring.shardingsphere.datasource.master.password"),
                        dataSourceProperties.getPassword()
                );
                if (StringUtils.isAnyBlank(url, username, password)) {
                    log.warn("分表扫描告警-未获取到数据库连接信息");
                    return;
                }

                final String databaseName = PatternUtil.extractDatabaseName(url);
                List<String> tables = new ArrayList<>();

                try (Connection connection = DriverManager.getConnection(url, username, password)) {
                    DatabaseMetaData metaData = connection.getMetaData();
                    try (ResultSet tablesSet = metaData.getTables(databaseName, null, null, new String[] {"TABLE"})) {
                        while (tablesSet.next()) {
                            tables.add(tablesSet.getString("TABLE_NAME"));
                        }
                    }
                } catch (SQLException e) {
                    log.error("分表扫描告警-扫描表失败", e);
                }
                final LocalDateTime now = LocalDateTime.now();
                // 当月分表结尾形式
                String currentMonthSuffix = "_" + DateUtils.formatYearMonth(now);
                // 下个月分表结尾形式
                String nextMonthSuffix = "_" + DateUtils.formatYearMonth(now.plusMonths(1));
                // 下个月不存在的表
                List<String> nextMonthNotExistTables = new ArrayList<>();
                for (String table : tables) {
                    if (table.endsWith(currentMonthSuffix) && !tables.contains(
                            table.split(currentMonthSuffix)[0] + nextMonthSuffix)) {
                        nextMonthNotExistTables.add(table);
                    }
                }
                if (CollUtil.isNotEmpty(nextMonthNotExistTables)) {
                    log.error("分表扫描告警-不存在的表有: {}", nextMonthNotExistTables);
                    if (tableNotify != null) {
                        final TableNotExistNotifyInfo info = new TableNotExistNotifyInfo();
                        info.setUrl(url);
                        info.setDatabase(databaseName);
                        info.setCurrentMonthSuffix(currentMonthSuffix);
                        info.setNextMonthSuffix(nextMonthSuffix);
                        info.setNextMonthNotExistTables(nextMonthNotExistTables);
                        tableNotify.notifyNotExistTables(info);
                    }
                    // 自动执行建表语句
                    if (dataSource != null) {
                        String sql;
                        String createTableSql;
                        // 匹配自增部分的sql
                        String autoIncrementRegex = "AUTO_INCREMENT=\\d+";
                        String autoIncrementReset = " AUTO_INCREMENT=1 ";
                        String errorMsg = "";
                        String nextMonthTableName;
                        try (Connection conn = DriverManager.getConnection(url, username, password)) {
                            for (String table : nextMonthNotExistTables) {
                                errorMsg = "";
                                sql = "SHOW CREATE TABLE " + table;
                                log.info("分表扫描告警-查询表结构语句, table = {}, sql = {}", table, sql);
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ResultSet rs = ps.executeQuery();
                                if (rs.next()) {
                                    // 这个表名是本月的表，比如7月份的表不存在，提示的是以6月份为基准的，因此表结构也是6月份的
                                    createTableSql = rs.getString(2);
                                } else {
                                    log.info("分表扫描告警-查询表结构失败, table = {}, sql = {}", table, sql);
                                    continue;
                                }
                                nextMonthTableName = table.replace(currentMonthSuffix, nextMonthSuffix);
                                // 将建表语句中的表名替换为下一个月的，并且重置自增开始值
                                createTableSql = createTableSql.replace(table, nextMonthTableName);
                                createTableSql = createTableSql.replaceAll(autoIncrementRegex, autoIncrementReset);
                                try {
                                    final Statement createStatement = conn.createStatement();
                                    createStatement.executeUpdate(createTableSql);
                                } catch (Exception e) {
                                    log.error("分表扫描告警-自动创建表失败, url = {}, createTableSql = {}", url,
                                            createTableSql, e
                                    );
                                    errorMsg = StringUtils.defaultString(e.getMessage(), "创建失败");
                                }

                                final TableAutoCreateNotifyInfo info = new TableAutoCreateNotifyInfo();
                                info.setUrl(url);
                                info.setDatabase(databaseName);
                                info.setCurrentMonthTableName(table);
                                info.setNextMonthTableName(nextMonthTableName);
                                info.setCreateTableSql(createTableSql);
                                sql = "SHOW CREATE TABLE " + nextMonthTableName;
                                PreparedStatement ps2 = conn.prepareStatement(sql);
                                ResultSet rs2 = ps2.executeQuery();
                                if (rs2.next()) {
                                    info.setShowCreateTableSql(rs2.getString(2));
                                } else {
                                    info.setShowCreateTableSql("");
                                }
                                info.setErrorMsg(errorMsg);
                                tableNotify.notifyAuthCreateTable(info);
                            }
                        } catch (SQLException e) {
                            log.error("分表扫描告警-获取数据源失败", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("分表扫描告警-处理失败", e);
            }
        }, 0, 1, TimeUnit.DAYS);
    }
}
