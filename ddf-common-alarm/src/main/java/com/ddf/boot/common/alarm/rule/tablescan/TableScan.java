package com.ddf.boot.common.alarm.rule.tablescan;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.util.DateUtils;
import com.ddf.boot.common.api.util.PatternUtil;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.env.Environment;

/**
 * <p>description</p >
 *
 * @author rebot
 * @version 1.0
 * @since 2024/06/03 16:50
 */
@Slf4j
public class TableScan {

    /**
     * 匹配建表语句中自增起始值的部分
     */
    private static final String AUTO_INCREMENT_REGEX = "AUTO_INCREMENT=\\d+";

    /**
     * 重置后的自增起始值
     */
    private static final String AUTO_INCREMENT_RESET = " AUTO_INCREMENT=1 ";

    private final Environment environment;

    private final DataSourceProperties dataSourceProperties;

    private final TableNotify tableNotify;

    private final DataSource dataSource;

    public TableScan(Environment environment, Optional<DataSourceProperties> dataSourceProperties,
            Optional<TableNotify> tableNotify, Optional<DataSource> dataSource) {
        this.environment = environment;
        this.dataSourceProperties = dataSourceProperties.orElse(null);
        this.tableNotify = tableNotify.orElse(null);
        this.dataSource = dataSource.orElse(null);
    }

    @PostConstruct
    public void scan() {
        // 没有数据源对象
        if (Objects.isNull(dataSource)) {
            return;
        }
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "table-scan-notify");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(() -> {
            try {
                doScan();
            } catch (Exception e) {
                log.error("分表扫描告警-处理失败", e);
            }
        }, 0, 1, TimeUnit.DAYS);
    }

    private void doScan() {
        // 分表扫描优先走 shardingsphere 配置的连接信息，否则退回标准 DataSource 配置
        String url = resolveDatabaseUrl();
        if (StringUtils.isBlank(url)) {
            log.warn("分表扫描告警-未获取到数据库连接信息");
            return;
        }

        final String databaseName = PatternUtil.extractDatabaseName(url);
        final List<String> tables = listTables(databaseName);
        final LocalDateTime now = LocalDateTime.now();
        // 当月分表结尾形式
        final String currentMonthSuffix = "_" + DateUtils.formatYearMonth(now);
        // 下个月分表结尾形式
        final String nextMonthSuffix = "_" + DateUtils.formatYearMonth(now.plusMonths(1));
        // 下个月不存在的表
        final List<String> nextMonthNotExistTables = findNextMonthNotExistTables(tables, currentMonthSuffix,
                nextMonthSuffix);
        if (CollUtil.isEmpty(nextMonthNotExistTables)) {
            return;
        }
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
            createMissingTables(url, databaseName, currentMonthSuffix, nextMonthSuffix, nextMonthNotExistTables);
        }
    }

    private String resolveDatabaseUrl() {
        return StringUtils.defaultIfBlank(environment.getProperty("spring.shardingsphere.datasource.master.url"),
                dataSourceProperties != null ? dataSourceProperties.getUrl() : null);
    }

    private List<String> listTables(String databaseName) {
        final List<String> tables = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet tablesSet = metaData.getTables(databaseName, null, null, new String[] {"TABLE"})) {
                while (tablesSet.next()) {
                    tables.add(tablesSet.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            log.error("分表扫描告警-扫描表失败", e);
        }
        return tables;
    }

    private List<String> findNextMonthNotExistTables(List<String> tables, String currentMonthSuffix,
            String nextMonthSuffix) {
        final List<String> nextMonthNotExistTables = new ArrayList<>();
        for (String table : tables) {
            if (table.endsWith(currentMonthSuffix)) {
                String baseTableName = table.substring(0, table.length() - currentMonthSuffix.length());
                if (!tables.contains(baseTableName + nextMonthSuffix)) {
                    nextMonthNotExistTables.add(table);
                }
            }
        }
        return nextMonthNotExistTables;
    }

    private void createMissingTables(String url, String databaseName, String currentMonthSuffix,
            String nextMonthSuffix, List<String> nextMonthNotExistTables) {
        try (Connection conn = dataSource.getConnection()) {
            for (String table : nextMonthNotExistTables) {
                createMissingTable(conn, url, databaseName, table, currentMonthSuffix, nextMonthSuffix);
            }
        } catch (SQLException e) {
            log.error("分表扫描告警-获取数据源失败", e);
        }
    }

    private void createMissingTable(Connection conn, String url, String databaseName, String table,
            String currentMonthSuffix, String nextMonthSuffix) throws SQLException {
        String sql = "SHOW CREATE TABLE " + table;
        log.info("分表扫描告警-查询表结构语句, table = {}, sql = {}", table, sql);
        String createTableSql;
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                log.info("分表扫描告警-查询表结构失败, table = {}, sql = {}", table, sql);
                return;
            }
            // 这个表名是本月的表，比如7月份的表不存在，提示的是以6月份为基准的，因此表结构也是6月份的
            createTableSql = rs.getString(2);
        }
        // 将建表语句中的表名替换为下一个月的，并且重置自增开始值
        final String nextMonthTableName = table.replace(currentMonthSuffix, nextMonthSuffix);
        createTableSql = createTableSql.replace(table, nextMonthTableName);
        createTableSql = createTableSql.replaceAll(AUTO_INCREMENT_REGEX, AUTO_INCREMENT_RESET);
        String errorMsg = "";
        try (Statement createStatement = conn.createStatement()) {
            createStatement.executeUpdate(createTableSql);
        } catch (Exception e) {
            log.error("分表扫描告警-自动创建表失败, url = {}, createTableSql = {}", url, createTableSql, e);
            errorMsg = StringUtils.defaultIfBlank(e.getMessage(), "创建失败");
        }

        final TableAutoCreateNotifyInfo info = new TableAutoCreateNotifyInfo();
        info.setUrl(url);
        info.setDatabase(databaseName);
        info.setCurrentMonthTableName(table);
        info.setNextMonthTableName(nextMonthTableName);
        info.setCreateTableSql(createTableSql);
        sql = "SHOW CREATE TABLE " + nextMonthTableName;
        try (PreparedStatement ps2 = conn.prepareStatement(sql);
                ResultSet rs2 = ps2.executeQuery()) {
            if (rs2.next()) {
                info.setShowCreateTableSql(rs2.getString(2));
            } else {
                info.setShowCreateTableSql("");
            }
        }
        info.setErrorMsg(errorMsg);
        if (tableNotify != null) {
            tableNotify.notifyAuthCreateTable(info);
        }
    }
}
