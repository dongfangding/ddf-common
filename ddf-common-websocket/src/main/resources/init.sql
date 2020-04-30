
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
`client_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指令想要在哪个应用上执行\r\n1. UPAY 云闪付\r\n2. ALIPAY 支付宝\r\n3. WECHAT_PAY 微信\r\n4. ICBC_APP 工行app\r\n5. CCB_APP 建行app',
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
`is_enable` tinyint(2) NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
create_by VARCHAR(32) NULL,
create_time TIMESTAMP NULL,
modify_by VARCHAR(32) NULL,
modify_time TIMESTAMP NULL,
removed INT NOT NULL DEFAULT 0,
version INT NOT NULL DEFAULT 1,
PRIMARY KEY (`id`) USING BTREE,
UNIQUE INDEX `request_id`(`request_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通道传输报文日志记录' ROW_FORMAT = Dynamic;


SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for merchant_base_device
-- ----------------------------
DROP TABLE IF EXISTS `merchant_base_device`;
CREATE TABLE `merchant_base_device`  (
`id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '信息id',
`merchant_user_base_info_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
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
`connect_server_address` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '连接到服务器的地址',
`online_change_time` bigint(20) NULL DEFAULT NULL COMMENT '在线状态最后一次变化时间',
`is_allot` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 未分配  1 已分配 ',
`sequence` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '序列号',
`is_remote_disable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '远程启用禁用状态 0 否 1 是',
`current_list_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '当前版本清单号',
`target_list_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标版本清单号',
`is_enable` tinyint(2) NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
create_by VARCHAR(32) NULL,
create_time TIMESTAMP NULL,
modify_by VARCHAR(32) NULL,
modify_time TIMESTAMP NULL,
removed INT NOT NULL DEFAULT 0,
version INT NOT NULL DEFAULT 1,
PRIMARY KEY (`id`) USING BTREE,
UNIQUE INDEX `random_code`(`random_code`) USING BTREE,
UNIQUE INDEX `number_random`(`number`) USING BTREE,
INDEX `index_user_info`(`merchant_user_base_info_id`) USING BTREE COMMENT '用户允许有多个设备，不要唯一'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '设备基础管理表' ROW_FORMAT = Dynamic;




DROP TABLE IF EXISTS `merchant_base_device_running_state`;
CREATE TABLE `merchant_base_device_running_state`  (
`id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`device_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备id',
`cmd` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '指令码',
`request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '对应请求日志',
`request_time` bigint(20) NOT NULL COMMENT '指令最新发送时间,毫秒值',
`response_time` bigint(20) NULL DEFAULT NULL COMMENT '指令最近响应时间，毫秒值',
`status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 执行中 2 未执行 3（超长时间未响应的需要额外代码支持，暂缓开发）',
`is_enable` tinyint(2) NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
create_by VARCHAR(32) NULL,
create_time TIMESTAMP NULL,
modify_by VARCHAR(32) NULL,
modify_time TIMESTAMP NULL,
removed INT NOT NULL DEFAULT 0,
version INT NOT NULL DEFAULT 1,
PRIMARY KEY (`id`) USING BTREE,
UNIQUE INDEX `uk_device_cmd`(`device_id`, `cmd`) USING BTREE,
UNIQUE INDEX `uk_request_id`(`request_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;



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
`client_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '指令想要在哪个应用上执行\r\n1. UPAY 云闪付\r\n2. ALIPAY 支付宝\r\n3. WECHAT_PAY 微信\r\n4. ICBC_APP 工行app\r\n5. CCB_APP 建行app',
`single_message_payload` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报文中接收的body主体数据,如果报文中有多个数据，对应多条记录，分开存储',
`description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '对报文中的内容进行字符串拼接解释',
`source_type` tinyint(2) NOT NULL DEFAULT 0 COMMENT '消息来源 0 默认未知，即未解析成功，无法判别数据 \r\n1 云闪付普通码到账消息\r\n2 云闪付普通码入账账单记录\r\n3 银行收入短信\r\n4. 垃圾短信\r\n5 云闪付登录验证码短信\r\n6. 银行支出短信\r\n7. 云闪付支出消息\r\n8. 云闪付转账支出交易记录\r\n9. 云闪付商户码到账消息\r\n10. 云闪付商户码入账账单记录\r\n11. 云闪付转账安全认证短信验证码\r\n12.安全认证短信\r\n13. 消息为忽略类型的数据',
`order_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '0 数据错误 1 收款 2 转账',
`status` tinyint(1) NULL DEFAULT 0 COMMENT '处理状态 0 未处理 1 处理成功 2 模板未匹配 3 业务处理错误 4 未匹配订单 5 数据格式有误 6 订单重复匹配',
`error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理错误原因,只取异常的getMessage,给前端展示用',
`error_stack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '取异常栈信息，方便查错',
`bill_time` datetime(0) NULL DEFAULT NULL COMMENT '账单时间，这个时间可能时app里标准的交易时间，也有可能是建立在短信里不标准的时间修正后的交易记录时间',
`parse_content` json NULL COMMENT '消息的解析内容json串，由于有一些数据需要解析，这里将解析后的数据放进去，方便直接取用',
`trade_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '到账消息的唯一标识符，或为云闪付订单号或消息的唯一标识符',
`receive_time` datetime(0) NULL DEFAULT NULL COMMENT '交易记录接收时间，这个时间晚于交易时间。',
`order_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自己系统的订单id,用以维系该表记录匹配到了哪条订单。merchant_order_info的id',
`match_by_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '匹配完成人，如果是系统匹配的则为0，否则为操作人的id',
`match_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '匹配完成人，如果时系统匹配的则为System,否则为操作人的用户名',
`is_enable` tinyint(2) NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
`create_date` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
`update_date` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改日期',
create_by VARCHAR(32) NULL,
create_time TIMESTAMP NULL,
modify_by VARCHAR(32) NULL,
modify_time TIMESTAMP NULL,
removed INT NOT NULL DEFAULT 0,
version INT NOT NULL DEFAULT 1,
PRIMARY KEY (`id`) USING BTREE,
UNIQUE INDEX `uk_order_id`(`order_id`, `cmd`) USING BTREE,
UNIQUE INDEX `uk_device_trade_cmd`(`trade_no`, `cmd`, `device_number`, `client_channel`) USING BTREE,
INDEX `index_merchant_device`(`merchant_id`, `device_id`, `client_channel`) USING BTREE,
INDEX `index_source_type`(`source_type`) USING BTREE,
INDEX `index_cmd`(`cmd`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '云闪付收款到账消息' ROW_FORMAT = Dynamic;



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
`template_type` tinyint(3) NOT NULL COMMENT '模板功能, 详见platform_template_config的含义',
`template_remark` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板功能描述',
`client_channel` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端应用，如果必须关联则让前端传入，如果为否使用配置表的默认值',
`sort` int(9) NULL DEFAULT 999 COMMENT '优先级 数字越小，优先级越高',
`credit` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板所属者标识，如发件号码',
`is_enable` tinyint(2) NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
create_by VARCHAR(32) NULL,
create_time TIMESTAMP NULL,
modify_by VARCHAR(32) NULL,
modify_time TIMESTAMP NULL,
removed INT NOT NULL DEFAULT 0,
version INT NOT NULL DEFAULT 1,
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '模板' ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `platform_template_config`;
CREATE TABLE `platform_template_config`  (
`id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`template_type` tinyint(3) NOT NULL COMMENT '模板功能',
`template_remark` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模板功能描述',
`required_related_channel` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必须关联应用， 是的话必须前端传参，否的话配置里给默认值',
`default_client_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户端应用',
`is_enable` tinyint(2) NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
create_by VARCHAR(32) NULL,
create_time TIMESTAMP NULL,
modify_by VARCHAR(32) NULL,
modify_time TIMESTAMP NULL,
removed INT NOT NULL DEFAULT 0,
version INT NOT NULL DEFAULT 1
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;



INSERT INTO `platform_template_config`(`id`, `template_type`, `template_remark`, `required_related_channel`, `default_client_channel`)
VALUES
('1', 1, '个人码到账消息', 1, NULL),
('2', 2, '商户码到账通知', 1, NULL),
('3', 3, '转账通知', 1, NULL),
('4', 4, '登录验证码', 1, NULL),
('5', 5, '转账验证码', 1, NULL),
('6', 6, '忽略处理模板', 0, NULL),
('7', 7, '收入短信模板', 0, NULL),
('8', 8, '支出短信模板', 0, NULL),
('9', 9, '垃圾短信模板', 0, 'PAY_MAIN'),
('10', 10, '安全认证短信', 0, NULL),
('11', 11, '短信回复确认', 0, NULL),
('12', 12, '注册验证码', 1, NULL);



insert into platform_message_template(id, title, template_type, client_channel, sort, credit, template_context) VALUES

-- 银行到账短信模板
(1, '光大银行到账短信模板-1', 7, NULL, 2, '95595', '尊敬的客户：您尾号${bankCardNo}账户${hour}:${minute}存入${amount}元，余额${balance}元，摘要:${payType}—付款方姓名:${payName},付款方账号后四位:${payNo}。[${payBankName}]'),
(2, '广发银行到账短信模板-1', 7, NULL, 2, '95508', '【${bankName}】您尾号${bankCardNo}卡${day}日${hour}:${minute}收入人民币${amount}元（${payType}-${payName}）。'),
(222, '广发银行到账短信模板-2', 7, NULL, 2, '95508', '【${bankName}】您尾号${bankCardNo}卡${day}日${hour}:${minute}收入人民币${amount}元(${payType})。'),

(3, '上海浦东发展银行到账短信模板-1', 7, NULL, 2, '95528', '您尾号${bankCardNo}卡人民币活期${hour}:${minute}存入${amount}[${payType}:${payName}]，可用余额${balance}。【${bankName}】'),
(4, '中信银行到账短信模板-1', 7, NULL, 2, '9555801', '【${bankName}】您尾号${bankCardNo}的中信卡于${month}月${day}日${hour}:${minute}，${payType}存入人民币${amount}元，当前余额为人民币${balance}元。'),
(5, '民生银行到账短信模板-1', 7, NULL, 2, '95568', '账户${bankCardNo}于${month}月${day}日${hour}:${minute}存入￥${amount}元，可用余额${balance}元。${payType}。【${bankName}】'),
(6, '平安银行到账短信模板-1', 7, NULL, 2, '106927995511', '您尾号${bankCardNo}的账户于${month}月${day}日${hour}:${minuteContactPayType}转入人民币${amount}元。【${bankName}】'),
(7, '平安银行到账短信模板-2', 7, NULL, 2, '106927995511', '您存款账户${bankCardNo}于${month}月${day}日${hour}:${minuteContactPayType}转入人民币${amount}元，详见 pingan.com/foMI【${bankName}】'),
(8, '招商银行到账短信模板-1', 7, NULL, 2, '95555', '您账户${bankCardNo}于${month}月${day}日${hour}:${minuteContactPayType}（${payName}），人民币${amount}元[${bankName}]'),
(32, '招商银行到账短信模板-2', 7, NULL, 2, '95555', '您账户${bankCardNo}于${month}月${day}日${hour}:${minute}收到${payType}人民币${amount}，付方${payName}，账号尾号${payNo}，备注：转账[${bankName}]'),
(9, '华夏银行到账短信模板-1', 7, NULL, 2, '95577', '您的账户${bankCardNo}于${month}月${day}日${hour}:${minute}收入人民币${amount}元，余额${balance}元。${payType}，付款方${payName}。【${bankName}】'),
(999, '华夏银行到账短信模板-2', 7, NULL, 2, '95577', '${bankCardNo}于${month}月${day}日${hour}:${minute}收入人民币${amount}元，余额${balance}元。${payType}。【${bankName}】'),
(10, '中国邮政到账短信模板-1', 7, NULL, 2, '95580', '【${bankName}】${year}年${month}月${day}日${hour}:${minute}您尾号${bankCardNo}账户${payType}金额${amount}元，余额${balance}元。'),
(11, '工商银行到账短信模板-1', 7, NULL, 2, '95588', '您尾号${bankCardNo}卡${month}月${day}日${hour}:${minuteContactPayType})${amount}元，余额${balance}元。【${bankName}】'),
(12, '交通银行到账短信模板-1', 7, NULL, 2, '95559', '您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minuteContactPayType}转入${amount}元,交易后余额为${balance}元。【${bankName}】'),
(13, '建设银行到账短信模板-1', 7, NULL, 2, '95533', '${payNameContactMonth}月${day}日${hour}时${minute}分向您尾号${bankCardNo}的储蓄卡账户${payType}收入人民币${amount}元,活期余额${balance}元。[${bankName}]'),
(14, '建设银行到账短信模板-1', 7, NULL, 2, '95533', '您尾号${bankCardNo}的储蓄卡账户${month}月${day}日${hour}时${minute}分${payType}收入人民币${amount}元,活期余额${balance}元。[${bankName}]'),

-- 银行支付短信模板
(15, '光大银行支出短信模板-1', 8, NULL, 2, '95595', '尾号${bankCardNo}账户${hour}:${minute}支出${amount}元，余额${balance}元，摘要:${payType} 二维码快速收款码专用。[${bankName}]'),
(16, '广发银行支出短信模板-1', 8, NULL, 2, '95508', '【${bankName}】您尾号${bankCardNo}卡${day}日${hour}:${minute}支出人民币${amount}元(${payType})。'),
(17, '上海浦东发展银行支出短信模板-1', 5, NULL, 2, '95528', '您尾号${bankCardNo}卡${hour}:${minute}消费${amount}[${payType}],可用余额${balance}【${bankName}】'),
(18, '中信银行支出短信模板-1', 8, NULL, 2, '9555801', '【${bankName}】您尾号${bankCardNo}的中信卡于${month}月${day}日${hour}:${minute}，${payType}人民币${amount}元，当前余额为人民币${balance}元。'),
(19, '民生银行支出短信模板-1', 8, NULL, 2, '95568', '账户${bankCardNo}于${month}月${day}日${hour}:${minute}支出￥${amount}元，可用余额${balance}元。${payType}。【${bankName}】'),
(20, '平安银行支出短信模板-1', 8, NULL, 2, '106927995511', '您尾号${bankCardNo}的账户于${month}月${day}日${hour}:${minuteContactPayType}转出人民币${amount}元。【${bankName}】'),
(21, '招商银行支出短信模板-1', 8, NULL, 2, '95555', '您账户${bankCardNo}于${month}月${day}日${hour}:${minute}向${payName}做${payType}，人民币${amount}元[${bankName}]'),
(210, '招商银行支出短信模板-2', 8, NULL, 2, '95555', '您账户${bankCardNo}于${month}月${day}日${hour}:${minute}银联扣款人民币${amount}元（${payType}）[${bankName}]'),


(22, '华夏银行支出短信模板-1', 8, NULL, 2, '95577', '您的账户${bankCardNo}于${month}月${day}日${hour}:${minute}支出人民币${amount}元，余额${balance}元。${payType}。【${bankName}】'),
(23, '中国邮政支出短信模板-1', 8, NULL, 2, '95580', '【${bankName}】${year}年${month}月${day}日${hour}:${minute}您尾号${bankCardNo}账户消费金额${amount}元，余额${balance}元。'),
(24, '工商银行支出短信模板-1', 8, NULL, 2, '95588', '您尾号${bankCardNo}卡${month}月${day}日${hour}:${minute}工商银行支出(${payType})${amount}元，余额${balance}元。【${bankName}】'),
(25, '交通银行支出短信模板-1', 8, NULL, 2, '95559', '您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minute}网络支付异地消费${amount}元,交易后余额为${balance}元。【${bankName}】'),
(26, '交通银行支出短信模板-1', 8, NULL, 2, '95559', '您尾号${bankCardNo}的卡于${month}月${day}日${hour}:${minute}网络支付转出${amount}元,交易后余额为${balance}元。【${bankName}】'),
(27, '建设银行支出短信模板-1', 8, NULL, 2, '95533', '您尾号${bankCardNo}的储蓄卡账户${month}月${day}日${hour}时${minute}分向${payType}支出人民币${amount}元,活期余额${balance}元。[建设银行]'),

-- 垃圾短信模板
(28, '垃圾短信模板-1', 9, NULL, 999, null, '{"condition": "NOT_CONTAINS", "value": "[\"银行\"]"}'),

-- 云闪付到账消息模板-1
(29, '云闪付个人码到账消息模板', 1, 'UPAY', 1, null, '${payName}通过扫码向您付款${amount}元,您的收款卡尾号为${payBankName}(尾号${bankCardNo})'),
(34, '云闪付商户码到账消息模板-1', 2, 'UPAY', 0, null, '云闪付收款${amount}元。'),
(35, '云闪付转账消息模板', 3, 'UPAY', 0, null, '您向${receiverName}、${bankName}、${bankCardNo}转账${amount}元，已于${year}-${month}-${day} ${hour}:${minute}成功到账！'),

-- 云闪付登录验证码
(31, '云闪付登录验证码短信模板-1', 4, 'UPAY', 1, '95516', '验证码${verifyCode}，有效期5分钟。您正在进行账户安全验证，请勿将短信验证码告知他人！【中国银联】'),

-- 云闪付转账验证码
(33, '云闪付转账验证码短信模板-1', 5, 'UPAY', 0, '95516', '手机验证码为：${verifyCode}，您正在进行转账交易，验证码1分钟内有效，泄露验证码会影响资金安全。【中国银联】'),

-- SIM卡安全认证短信
(36, '安全认证短信模板-1', 10, NULL, 998, null, '手机验证码为：${verifyCode}，您正在进行转账交易，验证码1分钟内有效，泄露验证码会影响资金安全。【中国银联】'),

-- 忽略处理消息模板
(36666666, '忽略无用云闪付消息模板-1', 6, NULL, 998, null, '您尾号为${bankCardNo}的银行卡于${day}日${hour}时${minute}分消费${amount}元'),

-- 忽略处理消息模板
('20200221134801', '云闪付风控自动回复短信模板-1', 6, NULL, 2, '95516', '【$/{title}】尾号$/{bankCardNo}银行卡于$/{orderTimeStr}转账$/{amount}元。为保障您的资金安全，请立即回复“$/{verifyCode}”确认交易，如不是本人交易，请立即联系发卡行挂失并拨打$/{bankTel}。（20分钟内回复有效）【中国银联】'),

-- 注册验证码
(202004261609001, '建行app注册验证码-1', 12, 'CCB_APP', 999, '95533', '验证码${verifyCode}，有效期5分钟。您正在注册云闪付账号，请勿泄露！如非您本人操作，请忽略。【中国银联】'),

-- 建行转账验证码
(202004261658001, '建行app转账验证码-1', 5, 'CCB_APP', 999, '95533', '序号${ignore}的验证码${verifyCode}，您向${targetAccountName}尾号${targetAccountNo}账户转账${amount}元。任何索要验证码的都是骗子，千万别给！[建设银行]');

