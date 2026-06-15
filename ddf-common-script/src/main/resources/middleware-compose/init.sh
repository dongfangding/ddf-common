#!/bin/bash
#
# 中间件初始化脚本：负责创建宿主机映射目录、按容器内 UID 修正权限、创建外部网络，
# 最后触发 nacos 库初始化与 docker compose 启动。
#
# 设计原则：
#   1) 不再对整个脚本目录做 `chown -R ${当前用户}`：这会与下文针对具体容器 UID 的
#      chown 形成"先全部改成宿主用户、再逐个改回容器用户"的反复，没有意义还容易掩盖
#      bug；改为只在需要的子目录上 mkdir -p 后单独 chown 到具体 UID。
#   2) 子网 / 网关 / 网络名通过环境变量覆盖，避免与宿主机已有的 docker 网络冲突。
#   3) 创建网络前先 inspect，已存在则跳过，使脚本可重复执行 (idempotent)。

set -euo pipefail

# 获取脚本所在目录的绝对路径并切换过去，保证相对路径稳定
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "${SCRIPT_DIR}"

# nacos mysql脚本下载
# shellcheck disable=SC1091
source nacos/nacos-standalone-mysql.env

# 网络名 / 子网 / 网关：可通过外部环境变量覆盖，默认沿用历史值
MIDDLEWARE_NETWORK="${MIDDLEWARE_NETWORK:-middleware-network}"
MIDDLEWARE_SUBNET="${MIDDLEWARE_SUBNET:-172.20.0.0/16}"
MIDDLEWARE_GATEWAY="${MIDDLEWARE_GATEWAY:-172.20.0.1}"

# 容器内 UID / GID 常量（与镜像约定保持一致，集中放这里方便审计）
MYSQL_UID=999          # mysql:8.0.x 内部 mysql 用户
NGINX_UID=101          # nginx:alpine 内部 nginx 用户
EMQX_UID=1000          # emqx:5.x 内部 emqx 用户
ES_UID=1000            # elasticsearch:7.x 内部 elasticsearch 用户
GRAFANA_UID=472        # grafana/grafana:11.x 内部 grafana 用户
PROMETHEUS_UID=65534   # prom/prometheus 内部 nobody 用户

# 一、创建宿主机映射目录
mkdir -p minio/{data,config,logs}
mkdir -p monitor/{otelcol,prometheus/data,grafana/provisioning/datasources}
mkdir -p mysql/{data,logs}
mkdir -p redis
mkdir -p nginx/{conf.d,snippets,ssl,log,html}
mkdir -p emqx/{data,log}
mkdir -p elasticsearch/{data,logs,config}

# 二、按容器内 UID 设置权限
#    只在需要写入的数据 / 日志目录上 chown，配置文件 (./*.conf, ./config/*.yml)
#    通过只读挂载提供给容器，宿主机权限保持原样即可。
sudo chown -R ${MYSQL_UID}:${MYSQL_UID} mysql redis
sudo chown -R ${NGINX_UID}:${NGINX_UID} nginx/log nginx/html 2>/dev/null || true
sudo chown -R ${EMQX_UID}:${EMQX_UID} emqx
sudo chown -R ${ES_UID}:${ES_UID} elasticsearch/data elasticsearch/logs
sudo chown -R ${GRAFANA_UID}:${GRAFANA_UID} monitor/grafana
sudo chown -R ${PROMETHEUS_UID}:${PROMETHEUS_UID} monitor/prometheus

# 三、创建集群网络 (幂等)
if ! sudo docker network inspect "${MIDDLEWARE_NETWORK}" >/dev/null 2>&1; then
    echo "Creating docker network ${MIDDLEWARE_NETWORK} (${MIDDLEWARE_SUBNET})..."
    sudo docker network create \
        --driver bridge \
        --subnet "${MIDDLEWARE_SUBNET}" \
        --gateway "${MIDDLEWARE_GATEWAY}" \
        "${MIDDLEWARE_NETWORK}"
else
    echo "Docker network ${MIDDLEWARE_NETWORK} already exists, skip."
fi

# 四、nacos 数据库 schema 初始化 + 拉起 compose
sudo bash "${SCRIPT_DIR}/nacos/mysql-init.sh"
sudo bash "${SCRIPT_DIR}/start.sh"
