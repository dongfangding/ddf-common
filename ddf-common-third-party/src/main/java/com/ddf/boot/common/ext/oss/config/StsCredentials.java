package com.ddf.boot.common.ext.oss.config;

import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * STS返回的认证信息
 * @author dongfang.ding
 * @date 2020/10/13 0013 16:52
 * @see AssumeRoleResponse.Credentials
 **/
@Data
public class StsCredentials implements Serializable {

	static final long serialVersionUID = 8603542924461775912L;

	private String securityToken;

	private String accessKeySecret;

	private String accessKeyId;

	private String expiration;

}