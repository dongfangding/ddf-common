
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
  `create_date` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `merchant_create_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户创建人',
  `merchant_update_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户修改人',
  `platform_create_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '总台创建人',
  `platform_update_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户修改人',
  `is_del` tinyint(4) NOT NULL DEFAULT 0,
  `version` bigint(20) NOT NULL DEFAULT 1 COMMENT '乐观锁',
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
  `current_list_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '当前版本清单号',
  `target_list_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标版本清单号',
  `flush_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '云闪付运行状态 0未启动 1正常',
  `flush_login_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '云闪付登录状态 0未登录 1登录',
  `app_running_status` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备应用运行状态',
  `version` bigint(20) NOT NULL DEFAULT 1 COMMENT '乐观锁',
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
  `create_date` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_date` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改日期',
  `merchant_create_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户创建人',
  `merchant_update_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户修改人',
  `platform_create_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '总台创建人',
  `platform_update_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户修改人',
  `is_del` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  `version` bigint(20) NOT NULL DEFAULT 1 COMMENT '乐观锁',
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
  `create_date` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `merchant_create_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户创建人',
  `merchant_update_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户修改人',
  `platform_create_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '总台创建人',
  `platform_update_user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商户修改人',
  `is_del` tinyint(1) NOT NULL DEFAULT 0,
  `version` bigint(20) NOT NULL DEFAULT 1 COMMENT '乐观锁',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收款短信模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of platform_message_template
-- ----------------------------
INSERT INTO `platform_message_template` VALUES ('1', '光大银行到账短信模板-1', '尊敬的客户：您尾号${bankCardNo}账户${hour}:${minute}存入${amount}元，余额${balance}元，摘要:${payType}—付款方姓名:${payName},付款方账号后四位:${payNo}。[${payBankName}]', 2, 2, '95595', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('10', '中国邮政到账短信模板-1', '【${bankName}】${year}年${month}月${day}日${hour}:${minute}您尾号${bankCardNo}账户${payType}金额${amount}元，余额${balance}元。', 2, 2, '95580', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('11', '工商银行到账短信模板-1', '您尾号${bankCardNo}卡${month}月${day}日${hour}:${minuteContactPayType})${amount}元，余额${balance}元。【${bankName}】', 2, 2, '95588', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('12', '交通银行到账短信模板-1', '您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minuteContactPayType}转入${amount}元,交易后余额为${balance}元。【${bankName}】', 2, 2, '95559', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('13', '建设银行到账短信模板-1', '${payNameContactMonth}月${day}日${hour}时${minute}分向您尾号${bankCardNo}的储蓄卡账户${payType}收入人民币${amount}元,活期余额${balance}元。[${bankName}]', 2, 2, '95533', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('14', '建设银行到账短信模板-1', '您尾号${bankCardNo}的储蓄卡账户${month}月${day}日${hour}时${minute}分${payType}收入人民币${amount}元,活期余额${balance}元。[${bankName}]', 2, 2, '95533', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('15', '光大银行支出短信模板-1', '尾号${bankCardNo}账户${hour}:${minute}支出${amount}元，余额${balance}元，摘要:${payType} 二维码快速收款码专用。[${bankName}]', 5, 2, '95595', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('16', '广发银行支出短信模板-1', '【${bankName}】您尾号${bankCardNo}卡${day}日${hour}:${minute}支出人民币${amount}元(${payType})。', 5, 2, '95508', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('17', '上海浦东发展银行支出短信模板-1', '您尾号${bankCardNo}卡${hour}:${minute}消费${amount}[${payType}],可用余额${balance}【${bankName}】', 5, 2, '95528', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('18', '中信银行支出短信模板-1', '【${bankName}】您尾号${bankCardNo}的中信卡于${month}月${day}日${hour}:${minute}，${payType}人民币${amount}元，当前余额为人民币${balance}元。', 5, 2, '9555801', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('19', '民生银行支出短信模板-1', '账户${bankCardNo}于${month}月${day}日${hour}:${minute}支出￥${amount}元，可用余额${balance}元。${payType}。【${bankName}】', 5, 2, '95568', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('2', '广发银行到账短信模板-1', '【${bankName}】您尾号${bankCardNo}卡${day}日${hour}:${minute}收入人民币${amount}元（${payType}-${payName}）。', 2, 2, '95508', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('20', '平安银行支出短信模板-1', '您尾号${bankCardNo}的账户于${month}月${day}日${hour}:${minuteContactPayType}转出人民币${amount}元。【${bankName}】', 5, 2, '106927995511', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('21', '招商银行支出短信模板-1', '您账户${bankCardNo}于${month}月${day}日${hour}:${minute}向${payName}做${payType}，人民币${amount}元[${bankName}]', 5, 2, '95555', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('210', '招商银行支出短信模板-2', '您账户${bankCardNo}于${month}月${day}日${hour}:${minute}银联扣款人民币${amount}元（${payType}）[${bankName}]', 5, 2, '95555', '2019-10-25 11:42:22', '2019-10-25 11:42:22', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('22', '华夏银行支出短信模板-1', '您的账户${bankCardNo}于${month}月${day}日${hour}:${minute}支出人民币${amount}元，余额${balance}元。${payType}。【${bankName}】', 5, 2, '95577', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('222', '广发银行到账短信模板-2', '【${bankName}】您尾号${bankCardNo}卡${day}日${hour}:${minute}收入人民币${amount}元(${payType})。', 2, 2, '95508', '2019-11-02 14:36:38', '2019-11-02 14:36:38', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('229', '华夏银行到账短信模板-2', '${bankCardNo}于${month}月${day}日${hour}:${minute}收入人民币${amount}元，余额${balance}元。${payType}。【${bankName}】', 2, 2, '95577', '2019-11-30 17:27:21', '2019-11-30 17:29:18', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('23', '中国邮政支出短信模板-1', '【${bankName}】${year}年${month}月${day}日${hour}:${minute}您尾号${bankCardNo}账户消费金额${amount}元，余额${balance}元。', 5, 2, '95580', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('24', '工商银行支出短信模板-1', '您尾号${bankCardNo}卡${month}月${day}日${hour}:${minute}工商银行支出(${payType})${amount}元，余额${balance}元。【${bankName}】', 5, 2, '95588', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('25', '交通银行支出短信模板-1', '您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minute}网络支付异地消费${amount}元,交易后余额为${balance}元。【${bankName}】', 5, 2, '95559', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('26', '交通银行支出短信模板-1', '您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minute}网络支付转出${amount}元,交易后余额为${balance}元。【${bankName}】', 5, 2, '95559', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('27', '建设银行支出短信模板-1', '您尾号${bankCardNo}的储蓄卡账户${month}月${day}日${hour}时${minute}分向${payType}支出人民币${amount}元,活期余额${balance}元。[建设银行]', 5, 2, '95533', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('28', '垃圾短信模板-1', '{\"condition\": \"NOT_CONTAINS\", \"value\": \"银行\"}', 3, 999, NULL, '2019-10-16 17:39:54', '2019-10-29 09:22:58', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('3', '上海浦东发展银行到账短信模板-1', '您尾号${bankCardNo}卡人民币活期${hour}:${minute}存入${amount}[${payType}:${payName}]，可用余额${balance}。【${bankName}】', 2, 2, '95528', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('30', '云闪付到账消息模板-2', '${payName}通过扫码向您付款${amount}元,您的收款卡尾号为${payBankName}(尾号${bankCardNo})', 0, 1, NULL, '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('31', '云闪付登录验证码短信模板-1', '验证码${verifyCode}，有效期5分钟。您正在进行账户安全验证，请勿将短信验证码告知他人！【中国银联】', 4, 1, '95516', '2019-10-16 17:39:54', '2019-10-17 17:43:43', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('32', '招商银行到账短信模板-2', '您账户${bankCardNo}于${month}月${day}日${hour}:${minute}收到${payType}人民币${amount}，付方${payName}，账号尾号${payNo}，备注：转账[${bankName}]', 2, 2, '95555', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('33', '云闪付转账验证码短信模板-1', '手机验证码为：${verifyCode}，您正在进行转账交易，验证码1分钟内有效，泄露验证码会影响资金安全。【中国银联】', 6, 0, '95516', '2019-10-17 17:31:12', '2019-10-17 17:43:41', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('330', '云闪付商户码到账消息模板-1', '云闪付收款${amount}元。', 7, 0, NULL, '2019-10-24 18:48:57', '2019-10-24 19:27:41', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('331', '云闪付转账消息模板', '您向${receiverName}、${bankName}、${bankCardNo}转账${amount}元，已于${year}-${month}-${day} ${hour}:${minute}成功到账！', 8, 0, NULL, '2019-10-24 18:48:57', '2019-10-26 16:20:18', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('34', '安全认证短信模板-1', '【${companyName}】您现在正在进行安全验证，验证码为${verifyCode}。', 9, 998, '106914919913127032', '2019-11-21 14:15:11', '2019-11-23 14:46:03', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('4', '中信银行到账短信模板-1', '【${bankName}】您尾号${bankCardNo}的中信卡于${month}月${day}日${hour}:${minute}，${payType}存入人民币${amount}元，当前余额为人民币${balance}元。', 2, 2, '9555801', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('5', '民生银行到账短信模板-1', '账户${bankCardNo}于${month}月${day}日${hour}:${minute}存入￥${amount}元，可用余额${balance}元。${payType}。【${bankName}】', 2, 2, '95568', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('6', '平安银行到账短信模板-1', '您尾号${bankCardNo}的账户于${month}月${day}日${hour}:${minuteContactPayType}转入人民币${amount}元。【${bankName}】', 2, 2, '106927995511', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('7', '平安银行到账短信模板-2', '您存款账户${bankCardNo}于${month}月${day}日${hour}:${minuteContactPayType}转入人民币${amount}元，详见 pingan.com/foMI【${bankName}】', 2, 2, '106927995511', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('8', '招商银行到账短信模板-1', '您账户${bankCardNo}于${month}月${day}日${hour}:${minuteContactPayType}（${payName}），人民币${amount}元[${bankName}]', 2, 2, '95555', '2019-10-16 17:39:54', '2019-11-26 09:51:38', NULL, NULL, NULL, NULL, 0, 1);
INSERT INTO `platform_message_template` VALUES ('9', '华夏银行到账短信模板-1', '您的账户${bankCardNo}于${month}月${day}日${hour}:${minute}收入人民币${amount}元，余额${balance}元。${payType}，付款方${payName}。【${bankName}】', 2, 2, '95577', '2019-10-16 17:39:54', '2019-10-16 17:39:54', NULL, NULL, NULL, NULL, 0, 1);

SET FOREIGN_KEY_CHECKS = 1;
