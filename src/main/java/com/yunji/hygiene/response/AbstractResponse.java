package com.yunji.hygiene.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2024/10/23 09:37
 * @description : TODO
 **/
public abstract class AbstractResponse<T> implements Response<T> {
    private int code;

    private String msg;

    private boolean success = true;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Object> dataIds = new ArrayList<>();

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }

    @Override
    public Response<T> withDataIds(List<Object> dataIds) {
        this.dataIds = dataIds;
        return this;
    }

    @Override
    public Response<T> withDataId(Object dataId) {
        this.dataIds.add(dataId);
        return this;
    }
}
