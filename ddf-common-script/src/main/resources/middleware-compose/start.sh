#!/bin/bash

# 启动文件
docker compose -f standalone-docker-compose.yml up -d
docker compose -f minio-compose.yml up -d
