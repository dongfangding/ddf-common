package com.ddf.common.websocket.dubbo;

import com.ddf.common.websocket.model.ws.MessageRequest;
import com.ddf.common.websocket.model.ws.MessageResponse;

import java.util.List;

/**
 * 通过message ws服务对app发送指令接口
 *
 * @author: shuaishuai.xiao
 * @create: 2019/8/19
 */
public interface MessageWsDubboService {
    /**
     * 执行指令
     *
     * @param request 请求参数
     * @return
     */
    MessageResponse executeCmd(MessageRequest request);

    /**
     * 针对每个设备的数据不一样的批量发送接口指令
     * @param requests
     * @return
     */
    MessageResponse executeCmd(List<MessageRequest> requests);
}
