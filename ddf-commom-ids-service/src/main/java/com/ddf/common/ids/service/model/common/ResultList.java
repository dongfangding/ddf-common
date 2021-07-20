package com.ddf.common.ids.service.model.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ResultList implements Serializable {
    private long code;
    private List<String> idList;
    private Status status;

    public ResultList(long code, Status status) {
        this.code = code;
        this.status = status;
    }

    public List<String> getIdList() {
        return idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public ResultList(List<String> idList, Status status) {
        this.idList = idList;
        this.status = status;
    }

    public ResultList() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Result{");
        sb.append("id=").append(Arrays.asList(idList).toString());
        sb.append(", code=").append(code);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
