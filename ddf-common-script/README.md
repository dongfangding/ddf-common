# ddf-common-script

开发常用脚本收集，提供各种开发辅助工具。

## 功能特性

- 数据库脚本
- 部署脚本
- 运维脚本

## 使用说明

脚本文件位于 `src/main/script/` 目录：

```bash
# 执行数据库迁移
sh script/db-migrate.sh

# 部署脚本
sh script/deploy.sh
```

## 脚本列表

| 脚本 | 功能 |
|------|------|
| `db-migrate.sh` | 数据库迁移 |
| `deploy.sh` | 应用部署 |
| `backup.sh` | 数据备份 |

## 注意事项

1. **脚本执行权限**：确保脚本有执行权限
2. **环境配置**：执行前检查环境变量配置
