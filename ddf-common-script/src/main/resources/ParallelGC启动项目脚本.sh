#!/bin/bash


# api service running script.

action=$1

if [ ${action} == 'start' ];then
  java -Xmx512m -Xms512m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:SurvivorRatio=8
  .. -XX:+UseParallelGC -XX:ParallelGCThreads=8 -XX:+UseParallelOldGC -XX:+UseAdaptiveSizePolicy
  .. -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError
  .. -XX:HeapDumpPath=./ -Xloggc:./gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10M
  .. -Duser.timezone=GMT+08 -Dspring.profiles.active=dev
  .. -jar api.jar 2>&1 &
elif [ ${action} == 'stop' ];then
  ps -ef| grep api.jar | grep -v grep | awk '{print $2}' | xargs kill -15
elif [ ${action} == 'restart' ];then
  ps -ef| grep api.jar | grep -v grep
  ps -ef| grep api.jar | grep -v grep | awk '{print $2}' | xargs kill -9
sleep 2
  java -Xmx512m -Xms512m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:SurvivorRatio=8
  .. -XX:+UseParallelGC -XX:ParallelGCThreads=8 -XX:+UseParallelOldGC -XX:+UseAdaptiveSizePolicy
  .. -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError
  .. -XX:HeapDumpPath=./ -Xloggc:./gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10M
  .. -Duser.timezone=GMT+08 -Dspring.profiles.active=dev
  .. -jar api.jar 2>&1 &
fi