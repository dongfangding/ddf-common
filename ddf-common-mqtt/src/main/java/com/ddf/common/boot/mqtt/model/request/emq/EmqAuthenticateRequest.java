package com.ddf.common.boot.mqtt.model.request.emq;

import com.ddf.common.boot.mqtt.util.EmqHttpResponseUtil;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>emq http连接认证和ACL参数</p >
 * 由于emq认证是通过HTTP响应状态码来判断认证结果的，因此无需响应对象。
 * 提供了一个类来处理http相关响应{@link EmqHttpResponseUtil}
 *
 * https://www.emqx.io/docs/zh/v4.4/advanced/auth-http.html#%E8%AE%A4%E8%AF%81%E8%AF%B7%E6%B1%82
 * https://www.emqx.io/docs/zh/v4.4/advanced/acl-http.html#acl-%E6%8E%88%E6%9D%83%E5%8E%9F%E7%90%86
 *
 * # etc/plugins/emqx_auth_http.conf
 *
 * ## 连接认证
 * auth.http.auth_req = http://127.0.0.1:80/mqtt/auth
 * auth.http.auth_req.method = post
 * auth.http.auth_req.headers.content-type = application/json
 * auth.http.auth_req.params = clientId=%c,username=%u,password=%P
 *
 * ## 超级用户ACL认证
 * auth.http.super_req = http://127.0.0.1:8991/mqtt/superuser
 * auth.http.super_req.method = post
 * auth.http.super_req.headers.content-type = application/json
 * auth.http.super_req.params = clientId=%c,username=%u
 *
 * ## 普通用户ACL认证
 * auth.http.acl_req = http://127.0.0.1:8991/mqtt/acl
 * auth.http.acl_req.method = post
 * auth.http.acl_req.headers.content-type = application/json
 * auth.http.acl_req.params = access=%A,username=%u,clientId=%c,ipaddr=%a,topic=%t,mountpoint=%m
 *
 * 你可以在认证请求中使用以下占位符，请求时 EMQX 将自动填充为客户端信息：
 *
 * %u：用户名
 * %c：Client ID
 * %a：客户端 IP 地址
 * %r：客户端接入协议
 * %P：明文密码
 * %p：客户端端口
 * %C：TLS 证书公用名（证书的域名或子域名），仅当 TLS 连接时有效
 * %d：TLS 证书 subject，仅当 TLS 连接时有效
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/22 13:54
 */
@Data
public class EmqAuthenticateRequest implements Serializable {

    private static final long serialVersionUID = 6593260244631866442L;

    // auth.http.auth_req.params = clientId=%c,username=%u,password=%P

    /**
     * Client ID
     */
    @NotBlank(message = "clientId不能为空")
    private String clientId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 明文密码
     */
    private String password;

}
