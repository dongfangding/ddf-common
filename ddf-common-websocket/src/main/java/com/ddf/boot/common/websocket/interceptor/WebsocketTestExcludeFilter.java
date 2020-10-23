package com.ddf.boot.common.websocket.interceptor;

import com.ddf.boot.common.websocket.config.WebSocketConfig;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * <p>由于测试模块websocket相关配置会导致测试类无法启动，因此提供一个排除类，如果在引用了websocket模块类之后，可以在测试类
 * 扫描包的时候引入该类进行排除</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/12 17:40
 */
public class WebsocketTestExcludeFilter implements TypeFilter {
    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        String className = classMetadata.getClassName();
        if (Objects.equals(WebSocketConfig.class.getName(), className)) {
            return true;
        }
        return false;
    }
}
