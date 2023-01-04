#!/bin/bash

echo "开始执行部署脚本>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo "开始摘除8080的流量"
sed -i "s/server 192.168.0.41:8080 weight=1;/#server 192.168.0.41:8080 weight=1;/g" /usr/local/openresty/nginx/conf/conf.d/xiuxian_upstream.conf
openresty -s reload
echo "8080的流量摘除完毕, 执行应用重启命令"
supervisorctl restart xiuxian
#sleep 5
times=1
while(( times <= 20 ))
do
	pid=$(/usr/sbin/lsof -i :8080|grep -v "PID" | awk '{print $2}')
	if [ "$pid" != "" ];
	then
		 echo "8080服务启动完成"
		 break
    else
    	echo "8080服务正在启动~"
    fi
	(( times++ ))
	sleep 5
done

echo "开始上线8080服务"
sed -i "s/#server 192.168.0.41:8080 weight=1;/server 192.168.0.41:8080 weight=1;/g" /usr/local/openresty/nginx/conf/conf.d/xiuxian_upstream.conf
openresty -s reload
echo "8080服务上线完成"

echo "开始摘除8081的流量"
sed -i "s/server 192.168.0.41:8081 weight=1;/#server 192.168.0.41:8081 weight=1;/g" /usr/local/openresty/nginx/conf/conf.d/xiuxian_upstream.conf
openresty -s reload
echo "8081的流量摘除完毕, 执行应用重启命令"
supervisorctl restart xiuxian1
#sleep 5
times=1
while(( times <= 20 ))
do
  pid=$(/usr/sbin/lsof -i :8081|grep -v "PID" | awk '{print $2}')
	if [ "$pid" != "" ];
	then
		 echo "8081服务启动完成"
		 break
    else
    	echo "8081服务正在启动~"
    fi
	(( times++ ))
	sleep 5
done

echo "开始上线8081服务"
sed -i "s/#server 192.168.0.41:8081 weight=1;/server 192.168.0.41:8081 weight=1;/g" /usr/local/openresty/nginx/conf/conf.d/xiuxian_upstream.conf
openresty -s reload
echo "8081服务上线完成"
echo "部署脚本执行完毕>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
