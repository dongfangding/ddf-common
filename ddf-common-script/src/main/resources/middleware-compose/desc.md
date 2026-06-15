# middleware-compose 使用说明

本目录是一套以 docker compose 拉起的本地/开发中间件栈，分三个 compose 文件：

- `standalone-docker-compose.yml`：核心业务中间件（MySQL / Redis / ZooKeeper / Mongo / ES /
  RocketMQ / xxl-job / EMQX / Nacos / Nginx）
- `minio-compose.yml`：对象存储 MinIO
- `monitor-compose.yml`：可观测性栈（OTel Collector / Jaeger / Prometheus / Grafana /
  node_exporter）

入口脚本：

- `init.sh`：**首次部署**执行一次。创建宿主机映射目录、按容器内 UID 修正权限、
  创建外部 docker 网络、初始化 nacos schema，最后调用 `start.sh`。
- `start.sh`：日常拉起/重启所有 compose（合并为单次 `docker compose -f ... -f ... up -d`）。

---

## 1. 启动流程

```bash
# 首次部署
cp .env.example .env       # 按需修改密码等敏感值
sh init.sh                 # 仅在首次部署、目录权限丢失或网络被删除后执行

# 日常重启
sh start.sh

# 停止
docker compose -f standalone-docker-compose.yml -f minio-compose.yml -f monitor-compose.yml down

# 仅停止某个服务
docker compose -f standalone-docker-compose.yml stop mysql
```

可通过环境变量覆盖网络配置（`init.sh` 支持）：

```bash
MIDDLEWARE_NETWORK=my-net \
MIDDLEWARE_SUBNET=10.99.0.0/16 \
MIDDLEWARE_GATEWAY=10.99.0.1 \
sh init.sh
```

## 2. 端口映射

| 服务                 | 宿主机端口             | 容器端口           | 用途                   |
|--------------------|-------------------|----------------|----------------------|
| mysql              | 3306              | 3306           | JDBC                 |
| redis              | 6379              | 6379           | RESP                 |
| zookeeper          | 2181              | 2181           | 客户端                  |
| mongo              | 27017             | 27017          | 数据库                  |
| elasticsearch      | 127.0.0.1:9200    | 9200           | REST，仅本机访问           |
| elasticsearch      | 127.0.0.1:9300    | 9300           | Transport，仅本机访问      |
| rocketmq-namesrv   | 9876              | 9876           | NameServer           |
| rocketmq-broker    | 10909/10911/10912 | 同              | Broker / Listen / HA |
| rocketmq-proxy     | 8081/10901/10902  | 同              | gRPC / Remoting / 管理 |
| rocketmq-dashboard | 8803              | 8082           | 控制台                  |
| xxl-job-admin      | 8802              | 8080           | 调度中心                 |
| emqx               | 11883             | 1883           | MQTT TCP             |
| emqx               | 18083/18084/18883 | 8083/8084/8883 | WS / WSS / MQTTS     |
| emqx               | 19083             | 18083          | Dashboard            |
| nacos              | 8801              | 8080           | Nacos v3 Console     |
| nacos              | 8848              | 8848           | OpenAPI              |
| nacos              | 9848              | 9848           | gRPC                 |
| nginx              | 8080 / 8443       | 80 / 443       | HTTP/HTTPS           |
| minio              | 9000 / 9001       | 9000 / 9001    | S3 API / Console     |
| otelcol            | 4317 / 4318       | 同              | OTLP gRPC / HTTP     |
| otelcol            | 13133 / 8888      | 同              | 健康检查 / 内部指标          |
| jaeger             | 16686             | 16686          | Jaeger UI            |
| prometheus         | 9090              | 9090           | UI / OTLP receiver   |
| grafana            | 3000              | 3000           | UI                   |
| node_exporter      | 127.0.0.1:9100    | 9100           | 节点指标，仅本机访问           |

## 3. UI 地址

- Grafana：http://localhost:3000
- Jaeger UI：http://localhost:16686
- Prometheus：http://localhost:9090
- Collector 健康检查：http://localhost:13133
- Collector 内部指标：http://localhost:8888/metrics
- RocketMQ Dashboard：http://localhost:8803
- xxl-job-admin：http://localhost:8802/xxl-job-admin （默认 admin / 123456）
- EMQX Dashboard：http://localhost:19083
- Nacos Console：http://localhost:8801/nacos
- MinIO Console：http://localhost:9001

## 4. 账号与密码

均由 `.env` 注入，下面给出对应环境变量名；**生产环境务必替换默认值**。

| 服务                  | 用户                    | 密码来源                                        |
|---------------------|-----------------------|---------------------------------------------|
| MySQL               | root                  | `MYSQL_ROOT_PASSWORD`                       |
| Redis               | -                     | `redis.conf` → requirepass (`ItIsSnowball`) |
| MongoDB             | `MONGO_ROOT_USERNAME` | `MONGO_ROOT_PASSWORD`                       |
| Elasticsearch       | elastic               | `ELASTIC_PASSWORD`                          |
| EMQX                | admin                 | `EMQX_PASS`                                 |
| Grafana             | admin                 | `GRAFANA_ADMIN_PASSWORD`                    |
| xxl-job-admin       | admin                 | 默认 123456，首次登录后修改                           |
| Nacos               | nacos                 | 默认 nacos，请在控制台修改                            |
| MinIO               | `MINIO_ROOT_USER`     | `MINIO_ROOT_PASSWORD`                       |
| xxl-job AccessToken | -                     | `XXL_JOB_TOKEN`                             |

## 5. 服务依赖顺序

`depends_on: condition: service_healthy` 已经在 compose 中显式声明，
人工启停时按这个图也能避免"看似启动了但还在握手"的问题：

```
mysql ─┬─► xxl-job-admin
       └─► nacos

rocketmq-namesrv ─┬─► rocketmq-broker ─► rocketmq-proxy
                  ├─► rocketmq-dashboard
                  └─► (broker 必须先就绪 proxy 才能拉起)

jaeger ─┐
        ├─► otelcol
prometheus ──┘
        ├─► grafana
jaeger ─┘
```

## 6. 数据目录与备份

所有持久化数据都在本目录下，**整目录复制即完整备份**：

| 服务            | 数据目录                                |
|---------------|-------------------------------------|
| mysql         | `./mysql/data`、日志 `./mysql/logs`    |
| redis         | `./redis`（含 AOF / RDB）              |
| mongo         | `./mongo/db`                        |
| elasticsearch | `./elasticsearch/data`              |
| rocketmq      | `./rocketmq/{namesrv,broker}/store` |
| emqx          | `./emqx/data`                       |
| minio         | `./minio/data`                      |
| grafana       | docker volume `grafana_data`        |
| prometheus    | docker volume `prometheus_data`     |
| otelcol       | docker volume `otelcol_data`        |

**重置某服务**：`down` 该服务 → 删除上面对应目录 → `init.sh` 重新建目录与权限 → `start.sh`。

## 7. 安全注意事项

1. **node_exporter `/:/host:ro,rslave` 风险**
   `monitor-compose.yml` 中 node_exporter 把宿主机根目录以只读方式映射给容器，
   容器内可读到宿主机上所有文件的元数据（含 `/etc/shadow` 大小、`/root` 目录结构等）。
   生产环境务必收紧：
    - `--collector.disable-defaults` + 仅启用必要 collector；
    - 或仅挂载 `/proc /sys /run` 等必需子目录；
    - 或直接以 systemd 方式部署到宿主机，不进入容器栈。

2. **Elasticsearch 默认仅监听 127.0.0.1**
   `9200 / 9300` 通过 `127.0.0.1:9200:9200` 绑定回环，禁止公网直连；
   如需对外暴露，请改回 `9200:9200` 并配置 TLS + X-Pack 用户证书。

3. **xxl-job-admin / Nacos 默认密码**
   首次启动后请立即在控制台修改 `admin / 123456` 与 `nacos / nacos`。

4. **EMQX Cookie 与节点名**
   集群模式（`cluster-docker-compose.yml`）下两个节点必须共用同一
   `EMQX_NODE__COOKIE`，否则无法成簇；当前模板已统一为
   `emqx_cluster_secret_cookie`，生产环境务必改成强随机值。
   节点名当前统一为 `emqx@emqx` / `emqx@emqx1` / `emqx@emqx2`（与 compose
   服务名一致，省掉假域名 alias）；如果从早期版本（`emqx@node1.emqx.com`
   等）升级上来，必须先清空对应 `./emqx{,1,2}/data` 目录，否则 mnesia
   会以旧节点身份启动并拒绝加入集群。

5. **`.env` 文件**
   不要提交到版本库；CI / 生产环境通过 secret manager 注入。

## 8. 常见排错

- **ES 启动失败 `AccessDeniedException: /usr/share/elasticsearch/data/nodes`**
  宿主机数据目录所有者不是 1000:1000。重新执行 `init.sh` 或手动
  `sudo chown -R 1000:1000 ./elasticsearch/data ./elasticsearch/logs`。
- **MySQL 容器反复重启 / 报错权限不足**
  类似上面，应为 999:999。`sudo chown -R 999:999 mysql`。
- **`docker network create` 报 already exists**
  `init.sh` 已加 inspect 守护；若手动 create 失败可先 `docker network rm middleware-network` 再执行。
- **`depends_on` 等待超时**
  优先看被依赖服务 healthcheck 是否反复失败（`docker inspect <c> | jq '.[].State.Health'`）。
