#!/bin/bash
echo "开始执行部署脚本>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
source /etc/profile
source /opt/services/xiu-xian-config-server/.env

DATE=$(date +%Y%m%d%H%M)
# 基础路径
BASE_PATH=/opt/services/xiu-xian-config-server
# 服务名称。同时约定部署服务的 jar 包名字也为它。
SERVER_NAME=xiu-xian-config-server-exec
# 环境
PROFILES_ACTIVE=dev
SERVER_PORT=$1
# heapError 存放路径
TEMP_LOG_PATH=$BASE_PATH/logs
# JVM 参数
JAVA_OPS="-Dserver.port=$SERVER_PORT -Xmx512m -Xms512m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:SurvivorRatio=8 -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:MaxGCPauseMillis=150 -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$TEMP_LOG_PATH -Xloggc:$TEMP_LOG_PATH/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=30M -Duser.timezone=GMT+08 -Dredis_host=${redis_host} -Dredis_port=${redis_port} -Dredis_database=${redis_database} -Dredis_password=${redis_password} -Dmysql_host=${mysql_host} -Dmysql_port=${mysql_port} -Dmysql_db=${mysql_db} -Dmysql_username=${mysql_username} -Dmysql_password=${mysql_password} -Drsa_primaryKey=${rsa_primaryKey} -Drsa_publicKey=${rsa_publicKey} -Ddruid_stat_enable=${druid_stat_enable} -Ddruid_state_username=${druid_state_username} -Ddruid_state_password=${druid_state_password}"
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

function is_exist() {
  pid=$(ps -ef|grep $SERVER_NAME|grep -v grep|awk '{print $2}')
  #如果不存在返回1，存在返回0
  if [ -z "${pid}" ]; then
   return 1
  else
    return 0
  fi
}

java -server $JAVA_OPS -jar $BASE_PATH/$SERVER_NAME.jar --spring.profiles.active=$PROFILES_ACTIVE
