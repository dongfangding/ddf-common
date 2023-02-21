package com.ddf.common.ids.service.model;


import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.Status;
import com.ddf.common.ids.service.service.IDGen;

public class ZeroIDGen implements IDGen {
    @Override
    public Result get(String key) {
        return new Result("0", Status.SUCCESS);
    }

    @Override
    public boolean init() {
        return true;
    }
}
