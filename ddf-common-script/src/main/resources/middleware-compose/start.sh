#!/bin/bash
#
# 中间件启动脚本：合并三个 compose 文件成单次 up 调用，
# 让 docker compose 在同一上下文里解析 depends_on / 网络别名，避免分批启动
# 时跨 compose 的健康依赖失效。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "${SCRIPT_DIR}"

MIDDLEWARE_NETWORK="${MIDDLEWARE_NETWORK:-middleware-network}"

# 网络必须先于 compose 启动存在 (compose 文件声明为 external: true)
if ! docker network inspect "${MIDDLEWARE_NETWORK}" >/dev/null 2>&1; then
    echo "Network ${MIDDLEWARE_NETWORK} not found, creating it with defaults..."
    docker network create "${MIDDLEWARE_NETWORK}"
fi

# 启动：单次 docker compose 调用同时管理三套服务
docker compose \
    -f standalone-docker-compose.yml \
    -f minio-compose.yml \
    -f monitor-compose.yml \
    up -d
