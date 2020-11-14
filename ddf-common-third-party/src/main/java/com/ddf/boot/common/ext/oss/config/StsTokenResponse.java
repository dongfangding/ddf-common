package com.ddf.boot.common.ext.oss.config;

import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import lombok.Builder;
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
@Builder
public class StsTokenResponse implements Serializable {

	private static final long serialVersionUID = 8603542924461775912L;

	/**
	 * STS临时授权信息
	 */
	private String securityToken;

	/**
	 * STS临时授权信息
	 */
	private String accessKeySecret;

	/**
	 * STS临时授权信息
	 */
	private String accessKeyId;

	/**
	 * STS临时授权信息
	 */
	private String expiration;

	/**
	 * 存储桶名称
	 */
	private String bucketName;

	/**
	 * 存储桶域名
	 */
	private String endPoint;

	/**
	 * oss访问前缀
	 */
	private String ossPrefix;

	/**
	 * 上传对象的前缀
	 */
	private String objectPrefix;

}