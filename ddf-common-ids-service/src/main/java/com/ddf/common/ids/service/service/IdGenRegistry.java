package com.ddf.common.ids.service.service;

import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ID 生成策略注册表，接入方注册自定义 IDGen 实现 Bean 即可扩展。
 */
public class IdGenRegistry {

    private final List<IDGen> idGens;

    public IdGenRegistry(List<IDGen> idGens) {
        this.idGens = idGens.stream().filter(IDGen::supportsKey).collect(Collectors.toList());
    }

    public Result get(String key) {
        for (IDGen idGen : idGens) {
            Result result = idGen.get(key);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException("没有可用的 IDGen 实现");
    }

    public ResultList list(String key, int number) {
        for (IDGen idGen : idGens) {
            ResultList result = idGen.list(key, number);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException("没有可用的 IDGen 实现");
    }

    public <T extends IDGen> T find(Class<T> type) {
        for (IDGen idGen : idGens) {
            if (type.isInstance(idGen)) {
                return type.cast(idGen);
            }
        }
        return null;
    }
}
