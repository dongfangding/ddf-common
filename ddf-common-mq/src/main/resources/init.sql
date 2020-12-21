CREATE TABLE `log_mq_listener`
(
    `id`                  bigint(20)                                                    NOT NULL,
    `message_id`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '消息的唯一标识符',
    `current_thread_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL     DEFAULT NULL COMMENT '处理线程',
    `creator`             varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL     DEFAULT NULL COMMENT '消息的创建人',
    `requeue_times`       int(11)                                                       NULL     DEFAULT 0 COMMENT '消息当前重投次数',
    `message_json`        json                                                          NOT NULL COMMENT '当前消息json串',
    `event`               varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '消息事件',
    `send_timestamp`      bigint(20)                                                    NOT NULL COMMENT '消息发送时间',
    `consumer_timestamp`  bigint(20)                                                    NULL     DEFAULT NULL COMMENT '消息消费时间',
    `event_timestamp`     mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NOT NULL COMMENT '消息事件发生的时间戳，实际上这个时间理论上就是要么就是发送时间要么就是消费时间',
    `exchange_name`       varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交换器名称',
    `exchange_type`       varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '交换器类型',
    `route_key`           varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '路由键名称',
    `target_queue`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预期队列名称（根据定义中获取的队列名）',
    `actual_queue`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '实际消费队列名称（根据@RabbitListener获取）',
    `container_factory`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL     DEFAULT NULL COMMENT '消息消费容器工厂beanName',
    `error_message`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '失败消息',
    `error_stack`         text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '失败错误堆栈消息',
    `create_by`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL     DEFAULT NULL,
    `create_time`         timestamp(0)                                                  NULL     DEFAULT NULL,
    `modify_by`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NULL     DEFAULT NULL,
    `modify_time`         timestamp(0)                                                  NULL     DEFAULT NULL,
    `removed`             int(11)                                                       NOT NULL DEFAULT 0,
    `version`             int(11)                                                       NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_message_id` (`message_id`) USING BTREE,
    INDEX `index_queue` (`target_queue`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = 'mq消息发送与消费日志（并不保证百分百数据记录）'
  ROW_FORMAT = Dynamic;