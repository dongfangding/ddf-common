#!/bin/bash

# nacos mysql脚本下载
source nacos/nacos-standalone-mysql.env

# 创建目录
mkdir -p minio/{data,config,logs}

# 获取脚本所在目录的绝对路径
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 获取真正的启动用户 (即使在 sudo 环境下也能找回原用户)
# 如果没有 sudo 变量，则退而求其次使用当前 id
REAL_USER_ID=${SUDO_UID:-$(id -u)}
REAL_GROUP_ID=${SUDO_GID:-$(id -g)}
sudo chown -R ${REAL_USER_ID}:${REAL_GROUP_ID} ${SCRIPT_DIR}
# mysql内部用户uid是999，必须给对应权限，否则无法操作数据库
sudo chown -R 999:999 mysql
# nginx内部使用nginx用户启动，对应101，给与权限，否则无法访问新创建的文件
sudo chown -R 101:101 nginx
# emqx文件权限
sudo chown -R 1000:1000 emqx
# ES目录权限
sudo chown -R 1000:1000 elasticsearch

# 监控相关
mkdir -p monitor/{otelcol,prometheus/data,grafana/provisioning/datasources}

sudo chown -R 472:472 monitor/grafana
sudo chown -R 65534:65534 monitor/prometheus

# 创建集群网络
sudo docker network create \
    --driver bridge \
    --subnet 172.20.0.0/16 \
    --gateway 172.20.0.1 \
    middleware-network

sudo bash "${SCRIPT_DIR}/nacos/mysql-init.sh"
sudo bash "${SCRIPT_DIR}/start.sh"
