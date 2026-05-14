# MinIO 生产环境部署

## 目录结构

```
minio/
├── docker-compose.yml    # 主配置文件
├── README.md             # 本说明文档
├── cert/                 # SSL 证书目录（可选）
│   ├── public.crt
│   └── private.key
└── backup/               # 备份脚本目录（可选）
    └── backup.sh
```

## 快速部署

```bash
# 1. 创建目录
mkdir -p /data/minio/{data,config,logs}

# 2. 修改配置文件
#    - 修改 MINIO_ROOT_USER
#    - 修改 MINIO_ROOT_PASSWORD（建议16位强密码）
#    - 修改数据卷路径

# 3. 启动服务
docker-compose up -d

# 4. 查看状态
docker-compose ps

# 5. 查看日志
docker-compose logs -f minio
```

## 访问地址

- **API 地址**: http://localhost:9000
- **控制台**: http://localhost:9001

## 配置说明

### 环境变量

| 变量                  | 说明       | 默认值            |
|---------------------|----------|----------------|
| MINIO_ROOT_USER     | Root 用户名 | admin          |
| MINIO_ROOT_PASSWORD | Root 密码  | ChangeMe@2024! |
| MINIO_REGION        | 区域名称     | cn-bj1         |
| MINIO_NODE_NAME     | 节点名称     | local          |

### 生产环境建议配置

```yaml
# 1. 使用强密码
MINIO_ROOT_PASSWORD: "YourStr0ng_P@ssw0rd!2024"

# 2. 资源限制（根据服务器配置调整）
deploy:
  resources:
    limits:
      cpus: '4'
      memory: 8G

# 3. 数据目录权限
chmod -R 1000:1000 /data/minio/data
```

## SSL/HTTPS 配置（生产环境推荐）

```yaml
# 添加证书挂载
volumes:
  - ./cert/public.crt:/root/.minio/certs/public.crt:ro
  - ./cert/private.key:/root/.minio/certs/private.key:ro

# 修改命令启用 TLS
command: server
  --address :9000
  --console-address :9001
  --config-dir /config
  --certs-dir /root/.minio/certs
  /data
```

## 常用操作

```bash
# 查看进程状态
docker exec minio mc admin info local

# 查看磁盘使用
docker exec minio mc admin info local | grep Disk

# 添加用户
docker exec minio mc admin user add local username password

# 创建桶
docker exec minio mc mb local/my-bucket

# 设置桶策略（只读）
docker exec minio mc anonymous set public local/my-bucket

# 查看告警
docker exec minio mc admin alert list local

# 重启服务
docker-compose restart minio

# 停止服务
docker-compose down

# 完全删除（保留数据卷）
docker-compose down -v
```

## 监控配置

### Prometheus 指标

MinIO 默认在 `9000/minio/v2/metrics` 提供 Prometheus 格式指标。

```yaml
# docker-compose 添加额外端口
ports:
  - "9000:9000"
  - "9001:9001"
  - "9090:9090"   # Prometheus 指标端口
```

### 控制台配置

```yaml
environment:
  MINIO_PROMETHEUS_URL: http://prometheus:9090
  MINIO_PROMETHEUS_AUTH_TYPE: public
```

## 备份策略

```bash
#!/bin/bash
# backup.sh - 每日备份脚本

BACKUP_DIR=/data/minio/backup
DATE=$(date +%Y%m%d)
BUCKET=my-bucket

# 创建备份目录
mkdir -p ${BACKUP_DIR}/${DATE}

# 同步数据到备份目录
docker exec minio mc cp --recursive local/${BUCKET}/ ${BACKUP_DIR}/${DATE}/

# 保留最近30天备份
find ${BACKUP_DIR} -type d -mtime +30 -exec rm -rf {} \;
```

## 故障排查

```bash
# 1. 检查健康状态
docker inspect --format='{{.State.Health.Status}}' minio

# 2. 查看详细日志
docker-compose logs minio --tail=100

# 3. 检查磁盘空间
df -h /data/minio/data

# 4. 检查权限
ls -la /data/minio/

# 5. 测试 API 连通性
curl http://localhost:9000/minio/health/live
```

## 安全加固

1. **网络隔离**: 使用自定义网络，限制外部访问
2. **防火墙**: 只开放 9000 和 9001 端口
3. **定期更新**: 定期更新 MinIO 镜像版本
4. **审计日志**: 启用访问日志记录
5. **最小权限**: 创建专用用户而非使用 root

```yaml
# 网络安全配置
networks:
  minio-network:
    driver: bridge
    attachable: true
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

## 资源规划建议

| 业务规模 | CPU | 内存   | 磁盘        |
|------|-----|------|-----------|
| 小型项目 | 2核  | 4GB  | 500GB SSD |
| 中型项目 | 4核  | 8GB  | 1TB SSD   |
| 大型项目 | 8核  | 16GB | 4TB SSD   |

## 注意事项

1. **数据目录权限**: 确保挂载目录owner为1000:1000
2. **磁盘性能**: 建议使用 SSD 硬盘以获得最佳性能
3. **单机限制**: erasure coding 最小需要4个驱动器
4. **监控告警**: 配置磁盘空间告警（低于20%时告警）
5. **定期维护**: 定期检查健康状态和磁盘使用情况
