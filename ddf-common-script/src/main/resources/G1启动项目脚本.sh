#!/bin/bash


# api service running script.

source /etc/profile
action=$1

if [ ${action} == 'start' ];then
  java -Xmx2048m -Xms2048m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:SurvivorRatio=8
  .. -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps
  .. -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=./ -Xloggc:./gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5
  .. -XX:GCLogFileSize=10M -Duser.timezone=GMT+08 -Dspring.profiles.active=dev
  .. -jar api.jar > /dev/null 2>&1 &
elif [ ${action} == 'stop' ];then
  ps -ef| grep api.jar | grep -v grep | awk '{print $2}' | xargs kill -15
elif [ ${action} == 'restart' ];then
  ps -ef| grep api.jar | grep -v grep
  ps -ef| grep api.jar | grep -v grep | awk '{print $2}' | xargs kill -9
  sleep 2
  java -Xmx2048m -Xms2048m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:SurvivorRatio=8
  .. -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps
  .. -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=./ -Xloggc:./gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5
  .. -XX:GCLogFileSize=10M -Duser.timezone=GMT+08 -Dspring.profiles.active=dev
  .. -jar api.jar > /dev/null 2>&1 &
fi