#!/bin/bash
echo "开始执行部署脚本>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
source /etc/profile
source /opt/services/boot-quick/.env

DATE=$(date +%Y%m%d%H%M)
# 基础路径
BASE_PATH=/opt/services/better-together
# 服务名称。同时约定部署服务的 jar 包名字也为它。
SERVER_NAME=better-together-exec
# 环境
PROFILES_ACTIVE=dev
# 健康检查 URL
HEALTH_CHECK_URL=http://127.0.0.1:8083/better-together/actuator/health/

# heapError 存放路径
TEMP_LOG_PATH=$BASE_PATH/logs
# JVM 参数
JAVA_OPS="-Dnocos_server=localhost:8848 -Dnacos_username=nacos -Dnacos_username=nacos -Dserver.port=8083 -Xmx512m -Xms256m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -XX:SurvivorRatio=8 -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:MaxGCPauseMillis=150 -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$TEMP_LOG_PATH -Xloggc:$TEMP_LOG_PATH/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=30M -Duser.timezone=GMT+08"

# 备份
function backup() {
    # 如果不存在，则无需备份
    if [ ! -f "$BASE_PATH/$SERVER_NAME.jar" ]; then
        echo "[backup] $BASE_PATH/$SERVER_NAME.jar 不存在，跳过备份"
    # 如果存在，则备份到 backup 目录下，使用时间作为后缀
    else
        echo "[backup] 开始备份 $SERVER_NAME ..."
        cp $BASE_PATH/$SERVER_NAME.jar $BASE_PATH/backup/$SERVER_NAME-$DATE.jar
        echo "[backup] 备份 $SERVER_NAME 完成"
    fi
}

# 停止
function stop() {
    echo "[stop] 开始停止 $BASE_PATH/$SERVER_NAME"
    PID=$(ps -ef | grep $BASE_PATH/$SERVER_NAME | grep -v "grep" | awk '{print $2}')
    # 如果 Java 服务启动中，则进行关闭
    if [ -n "$PID" ]; then
        # 正常关闭
        echo "[stop] $BASE_PATH/$SERVER_NAME 运行中，开始 kill [$PID]"
        kill -15 $PID
        # 等待最大 60 秒，直到关闭完成。
        for ((i = 0; i < 60; i++))
            do
                sleep 1
                PID=$(ps -ef | grep $BASE_PATH/$SERVER_NAME | grep -v "grep" | awk '{print $2}')
                if [ -n "$PID" ]; then
                    echo -e ".\c"
                else
                    echo "[stop] 停止 $BASE_PATH/$SERVER_NAME 成功"
                    break
                fi
		    done

        # 如果正常关闭失败，那么进行强制 kill -9 进行关闭
        if [ -n "$PID" ]; then
            echo "[stop] $BASE_PATH/$SERVER_NAME 失败，强制 kill -9 $PID"
            kill -9 $PID
        fi
    # 如果 Java 服务未启动，则无需关闭
    else
        echo "[stop] $BASE_PATH/$SERVER_NAME 未启动，无需停止"
    fi
}

# 启动
function start() {
    # 开启启动前，打印启动参数
    echo "[start] 开始启动 $BASE_PATH/$SERVER_NAME"
    echo "[start] JAVA_OPS: $JAVA_OPS"
    echo "[start] PROFILES: $PROFILES_ACTIVE"

    # 开始启动
    nohup java -server $JAVA_OPS -jar $BASE_PATH/$SERVER_NAME.jar --spring.profiles.active=$PROFILES_ACTIVE >/dev/null 2>&1 &
    echo "[start] 启动 $BASE_PATH/$SERVER_NAME 完成"
}

# 重启
function restart() {
    stop
    start
    healthCheck
}

# 健康检查
function healthCheck() {
    # 如果配置健康检查，则进行健康检查
    if [ -n "$HEALTH_CHECK_URL" ]; then
        # 健康检查最大 60 秒，直到健康检查通过
        echo "[healthCheck] 开始通过 $HEALTH_CHECK_URL 地址，进行健康检查";
        for ((i = 0; i < 60; i++))
            do
                # 请求健康检查地址，只获取状态码。
                result=`curl -I -m 10 -o /dev/null -s -w %{http_code} $HEALTH_CHECK_URL || echo "000"`
                # 如果状态码为 200，则说明健康检查通过
                if [ "$result" == "200" ]; then
                    echo "[healthCheck] 健康检查通过";
                    break
                # 如果状态码非 200，则说明未通过。sleep 1 秒后，继续重试
                else
                    echo -e ".\c"
                    sleep 1
                fi
            done

        # 健康检查未通过，则异常退出 shell 脚本，不继续部署。
        if [ ! "$result" == "200" ]; then
            echo "[healthCheck] 健康检查不通过，可能部署失败。查看日志，自行判断是否启动成功";
            tail -n 100 nohup.out
        # 健康检查通过，打印最后 10 行日志，可能部署的人想看下日志。
        else
            tail -n 10 nohup.out
        fi
    # 如果未配置健康检查，则 slepp 60 秒，人工看日志是否部署成功。
    else
        echo "[healthCheck] HEALTH_CHECK_URL 未配置，开始 sleep 60 秒";
        sleep 60
        echo "[healthCheck] sleep 60 秒完成，查看日志，自行判断是否启动成功";
        tail -n 100 nohup.out
    fi
    # delete nohup.out
    echo "收尾----------------"
    sleep 30
    echo "开始删除nohup.out........."
    rm -rf nohup.out
    exit 1;
}

# 部署
function deploy() {
    cd $BASE_PATH || exit
    # 备份原 jar
    backup
    # 停止 Java 服务
    stop
    # 启动 Java 服务
    start
    # 健康检查
    healthCheck
}

function is_exist() {
  pid=$(ps -ef|grep $SERVER_NAME|grep -v grep|awk '{print $2}')
  #如果不存在返回1，存在返回0
  if [ -z "${pid}" ]; then
   return 1
  else
    return 0
  fi
}

function status() {
  is_exist
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} is running. Pid is ${pid}"
  else
    echo "${APP_NAME} is NOT running."
  fi
}


usage() {
    echo "Usage: sh deploy.sh [deploy|start|stop|restart|status]"
    exit 1
}

#根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$1" in
  "deploy")
    deploy
    ;;
  "start")
    start
    ;;
  "stop")
    stop
    ;;
  "status")
    status
    ;;
  "restart")
    restart
    ;;
  *)
    usage
    ;;
esac
