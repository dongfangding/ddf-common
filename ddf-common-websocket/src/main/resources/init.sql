
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for log_channel_transfer
-- ----------------------------
DROP TABLE IF EXISTS `log_channel_transfer`;
CREATE TABLE `log_channel_transfer`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `logic_primary_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '针对某些指令根据业务主键发送，需要记录业务主键，服务端根据这个字段来判断客户端是否发起过该业务主键的某个指令。如果没有发送成功，服务端需要重试',
  `operator_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '下发指令的操作人',
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求id，用来响应和判断客户端是否重复请求;如果消息解析失败那个这个值为空，照样保存，以便排错',
  `device_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '安卓设备号',
  `cmd` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指令码',
  `token` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '安卓绑定的随机码',
  `server_address` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '服务端地址',
  `client_address` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端地址',
  `business_data` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务端用来存储发送时携带的业务主键，方便数据回传时，不依赖于客户端将业务数据回传',
  `send_flag` tinyint(255) NOT NULL DEFAULT 0 COMMENT '主动发送方标识 0 服务端  1 客户端',
  `request` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '传输内容',
  `response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应内容',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0 已发送 1 已接收 2 已响应 3 已处理 4 处理失败',
  `error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败消息',
  `full_request_response` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '交互的完整报文，使用json数组包裹原始报文',
	create_by VARCHAR(32) NULL,
	create_time TIMESTAMP NULL,
	modify_by VARCHAR(32) NULL,
	modify_time TIMESTAMP NULL,
	removed INT NOT NULL DEFAULT 0,
	version INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `request_id`(`request_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通道传输报文日志记录' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;



SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for merchant_base_device
-- ----------------------------
DROP TABLE IF EXISTS `merchant_base_device`;
CREATE TABLE `merchant_base_device`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '信息id',
  `merchant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户id',
  `number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备号',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '未知' COMMENT '设备名称',
  `random_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备随机码',
  `model` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备型号',
  `memory` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '设备内存',
  `os_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '设备系统版本',
  `binding_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '绑定状态 0 未绑定 1已绑定',
  `is_online` tinyint(1) NOT NULL DEFAULT 0 COMMENT '设备是否在线 0 离线 1在线',
  `longitude` decimal(20, 10) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(20, 10) NULL DEFAULT NULL COMMENT '纬度\n\n',
  `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '经纬度地址',
  `binding_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定ip',
  `restart_flag` tinyint(1) NULL DEFAULT 0 COMMENT '0 未重启  1 已重启  每次发指令之前需要把该值置为0',
  `restart_time` datetime(0) NULL DEFAULT NULL COMMENT '最后一次设备重启时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_date` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `connect_server_address` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '连接到服务器的地址',
  `online_change_time` bigint(20) NULL DEFAULT NULL COMMENT '在线状态最后一次变化时间',
  `is_insert_card` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否插卡  0：未插卡  1：已插卡',
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'C端用户ID',
  `is_allot` tinyint(1) NOT NULL DEFAULT 1 COMMENT '分配商户 1/未分配 0已分配',
  `update_date` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改日期',
  `merchant_create_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户创建人',
  `merchant_update_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户修改人',
  `platform_create_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '总台创建人',
  `platform_update_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户修改人',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0保留 1删除',
  `is_remote_disable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '远程启用禁用状态 0 否 1 是',
  `is_enable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0启用 1禁用',
  `sequence` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '序列号',
create_by VARCHAR(32) NULL,
	create_time TIMESTAMP NULL,
	modify_by VARCHAR(32) NULL,
	modify_time TIMESTAMP NULL,
	removed INT NOT NULL DEFAULT 0,
	version INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `random_code`(`random_code`) USING BTREE,
  UNIQUE INDEX `number_random`(`number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '设备基础管理表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;



SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for merchant_message_info
-- ----------------------------
DROP TABLE IF EXISTS `merchant_message_info`;
CREATE TABLE `merchant_message_info`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `merchant_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户id',
  `merchant_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户名',
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报文中的request_id',
  `device_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备id',
  `sequence` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `device_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备号',
  `cmd` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `single_message_payload` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报文中接收的body主体数据,如果报文中有多个数据，对应多条记录，分开存储',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '对报文中的内容进行字符串拼接解释',
  `source_type` tinyint(2) NOT NULL DEFAULT 0 COMMENT '消息来源  0 默认未知，即未解析成功，无法判别数据 \r\n1 云闪付普通码到账消息\r\n2 云闪付普通码入账账单记录\r\n3 银行收入短信\r\n4. 垃圾短信\r\n5 云闪付登录验证码短信\r\n6. 银行支出短信\r\n7. 云闪付支出消息\r\n8. 云闪付转账支出交易记录\r\n9. 云闪付商户码到账消息\r\n10. 云闪付商户码入账账单记录\r\n11. 云闪付转账安全认证短信验证码 12.安全认证短信',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '处理状态 0 未处理 1 处理成功 2 模板未匹配 3 业务处理错误 4 未匹配订单 5 数据格式有误 6 订单重复匹配\r\n7 伪造认证方式',
  `error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理错误原因,只取异常的getMessage,给前端展示用',
  `error_stack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '取异常栈信息，方便查错',
  `trade_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '到账消息的唯一标识符，或为云闪付订单号或消息的唯一标识符',
  `receive_time` datetime(0) NULL DEFAULT NULL COMMENT '到账时间',
  `order_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自己系统的订单id,用以维系该表记录匹配到了哪条订单。merchant_order_info的id',
  `order_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 数据错误 1 收款 2 转账',
create_by VARCHAR(32) NULL,
	create_time TIMESTAMP NULL,
	modify_by VARCHAR(32) NULL,
	modify_time TIMESTAMP NULL,
	removed INT NOT NULL DEFAULT 0,
	version INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_trade_cmd`(`trade_no`, `cmd`, `device_number`) USING BTREE,
  UNIQUE INDEX `uk_order_id`(`order_id`, `cmd`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '云闪付收款到账消息' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;



SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for platform_message_template
-- ----------------------------
DROP TABLE IF EXISTS `platform_message_template`;
CREATE TABLE `platform_message_template`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '模板标题',
  `template_context` varchar(510) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '模板内容',
  `type` tinyint(1) NULL DEFAULT 1 COMMENT '模板类型 0：云闪付个人码到账通知  1：系统消息 2 到账短信模板 3 垃圾信息模板 4 云闪付登录验证码 5 支出短信模板 6 云闪付转账验证码 7 云闪付商户码到账通知 8 云闪付转账通知 9安全认证短信',
  `sort` int(9) NULL DEFAULT 999 COMMENT '优先级 数字越小，优先级越高',
  `credit` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板所属者标识，如发件号码',
create_by VARCHAR(32) NULL,
	create_time TIMESTAMP NULL,
	modify_by VARCHAR(32) NULL,
	modify_time TIMESTAMP NULL,
	removed INT NOT NULL DEFAULT 0,
	version INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收款短信模板' ROW_FORMAT = Dynamic;


SET FOREIGN_KEY_CHECKS = 1;
