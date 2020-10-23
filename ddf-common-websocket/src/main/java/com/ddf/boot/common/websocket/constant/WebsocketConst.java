package com.ddf.boot.common.websocket.constant;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/09/15 20:34
 */
public class WebsocketConst {

    /** 握手时从请求中获取到的本机IP */
    public static final String SERVER_IP = "serverIp";

    /** 握手时从请求头中获取到的真实IP */
    public static final String CLIENT_REAL_IP = "clientAddress";

    /** 认证用户在作用域存放时的key */
    public static final String PRINCIPAL_KEY = "principal";

    /** 认证的token参数名称，放在header中 */
    public static final String TOKEN_PARAMETER = "token";

    /** cmd命令转发的发布主题 */
    public static final String REDIRECT_CMD_TOPIC = "redirect_cmd";

    /** cmd命令转发后机器处理完成后返回的数据 */
    public static final String RETURN_MESSAGE_TOPIC = "return_message";

}
