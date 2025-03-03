package com.yunji.hygiene.response;

import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2024/10/23 9:53
 * @description : TODO
 */
public interface Response<T> {

    boolean isSuccess();

    void setSuccess(boolean success);

    int getCode();

    void setCode(int code);

    void setMsg(String msg);

    String getMsg();

    long getTime();

    T getData();

    void setData(T data);

    Response<T> withDataIds(List<Object> dataIds);

    Response<T> withDataId(Object dataId);
}
