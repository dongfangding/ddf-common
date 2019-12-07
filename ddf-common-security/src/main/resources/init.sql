DROP TABLE IF EXISTS auth_user;

CREATE TABLE auth_user
(
	id BIGINT(20) NOT NULL COMMENT '主键ID' AUTO_INCREMENT,
	user_name VARCHAR(30) NOT NULL COMMENT '姓名',
	password VARCHAR(32) NOT NULL COMMENT '密码',
	email VARCHAR(50) NULL DEFAULT NULL COMMENT '邮箱',
	birthday DATE NULL DEFAULT NULL COMMENT '生日',
    last_modify_password bigint NOT NULL COMMENT '最后一次修改密码的时间',
    last_login_time bigint COMMENT '最后一次使用密码登录的时间',
    is_enable TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户是否有效， 0否1是',
    org_code VARCHAR(32) NULL COMMENT '用户所属组织代码',

	create_by VARCHAR(32) NULL,
	create_time TIMESTAMP NULL,
	modify_by VARCHAR(32) NULL,
	modify_time TIMESTAMP NULL,
	removed INT NOT NULL DEFAULT 0,
	version INT NOT NULL DEFAULT 1,

	PRIMARY KEY (id)

);