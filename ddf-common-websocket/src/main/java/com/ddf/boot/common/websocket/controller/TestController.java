package com.ddf.boot.common.websocket.controller;

import com.ddf.boot.common.websocket.dubbo.MessageWsDubboService;
import com.ddf.boot.common.websocket.helper.WebsocketSessionStorage;
import com.ddf.boot.common.websocket.model.ws.AuthPrincipal;
import com.ddf.boot.common.websocket.model.ws.MessageRequest;
import com.ddf.boot.common.websocket.model.ws.MessageResponse;
import com.ddf.boot.common.websocket.model.ws.WebSocketSessionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**


 */
@RestController
@Slf4j
@Profile("!prod")
@RequestMapping("/test")
public class TestController {
    @Autowired
    private MessageWsDubboService messageWsDubboService;

    @RequestMapping("getWebSocketSession")
    public Map<Principal, Map<String, Object>> getWebSocketSession(@RequestBody AuthPrincipal authPrincipal) {
        Map<Principal, Map<String, Object>> rtnMap = new HashMap<>();
        if (authPrincipal == null || StringUtils.isAnyBlank(authPrincipal.getIme(), authPrincipal.getRandomCode())) {
            ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> all = WebsocketSessionStorage.getAll();
            all.forEach((k, v) -> rtnMap.put(k, toMap(v)));
        } else {
            WebSocketSessionWrapper webSocketSessionWrapper = WebsocketSessionStorage.get(authPrincipal);
            if (webSocketSessionWrapper != null) {
                rtnMap.put(authPrincipal, toMap(webSocketSessionWrapper));
            }
        }
        return rtnMap;
    }

    @RequestMapping("getNotSync")
    public Map<Principal, Map<String, Object>> getNotSync() {
        ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> all = WebsocketSessionStorage.getAll();
        Map<Principal, Map<String, Object>> rtnMap = new HashMap<>();
        all.forEach((k, v) -> {
            if (!v.isSync()) {
                rtnMap.put(k, toMap(v));
            }
        });
        return rtnMap;
    }

    private Map<String, Object> toMap(WebSocketSessionWrapper webSocketSessionWrapper) {
        Map<String, Object> currMap = new HashMap<>();
        currMap.put("status", webSocketSessionWrapper.getStatus());
        currMap.put("sync", webSocketSessionWrapper.isSync());
        currMap.put("onlineChangeTime", webSocketSessionWrapper.getStatusChangeTime());
        currMap.put("serverAddress", webSocketSessionWrapper.getServerAddress());
        currMap.put("clientAddress", webSocketSessionWrapper.getClientAddress());
        currMap.put("attrs", webSocketSessionWrapper.getWebSocketSession().getAttributes());
        return currMap;
    }


    @RequestMapping("cmd")
    public MessageResponse cmd(@RequestBody MessageRequest messageRequest) {
        return messageWsDubboService.executeCmd(messageRequest);
    }

    @RequestMapping("count")
    public int count() {
        ConcurrentHashMap<AuthPrincipal, WebSocketSessionWrapper> all = WebsocketSessionStorage.getAll();
        AtomicInteger count = new AtomicInteger();
        all.forEach((k, v) -> {
            if (v.getStatus() == 1) {
                count.getAndIncrement();
            }
        });
        return count.get();
    }
}
