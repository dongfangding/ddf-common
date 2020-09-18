package com.ddf.boot.common.websocket.listeners;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import com.ddf.boot.common.websocket.enumu.CacheKeyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 强制删除无效下线key$
 *
 * @author dongfang.ding
 * @date 2020/9/17 0017 23:33
 */
@Component
public class RemoveOfflineKeyListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private Environment environment;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String host = NetUtil.getLocalhostStr();
        String port = environment.getProperty("server.port");
        String key = CacheKeyEnum.AUTH_PRINCIPAL_SERVER_MONITOR.getTemplate();
        ScanOptions scanOptions = ScanOptions.scanOptions().match(host + ":" + port + "*").build();
        Cursor<Map.Entry<Object, Object>> scan = stringRedisTemplate.opsForHash().scan(key, scanOptions);
        List<Object> list = new ArrayList<>();
        scan.forEachRemaining((v) -> {
            list.add(v.getKey());
        });
        if (CollUtil.isNotEmpty(list)) {
            stringRedisTemplate.opsForHash().delete(key, list.toArray());
        }
    }
}
