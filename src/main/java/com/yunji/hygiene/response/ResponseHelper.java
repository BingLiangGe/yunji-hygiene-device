package com.yunji.hygiene.response;

import com.yunji.hygiene.constant.DeviceConstant;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * @author : peter-zhu
 * @date : 2024/10/23 9:52
 * @description : TODO
 */
public class ResponseHelper {

    private ResponseHelper() {
    }

    public static <T> Response<T> handle(boolean success, String errorMsg) {
        if (!success)
            return ResponseHelper.failure(errorMsg);
        return success();
    }


    public static <E> Response<List<E>> successWithPage(List<E> data, long total) {
        PageResponse<E> rspData = new PageResponse<>();
        rspData.setCode(HttpStatus.OK.value());
        rspData.setMsg("查询成功");
        rspData.setData(data);
        rspData.setTotal(total);
        return rspData;
    }

    public static <T> Response<T> success() {
        return success(null);
    }

    public static <T> Response<T> success(T data) {
        return success(data, DeviceConstant.SUCCESS);
    }

    public static <T> Response<T> success(T data, String message) {
        return build(data, Boolean.TRUE, HttpStatus.OK.value(), message);
    }

    public static <T> Response<T> failure() {
        return failure(DeviceConstant.FAILURE);
    }

    public static <T> Response<T> failure(String message) {
        return failure(HttpStatus.INTERNAL_SERVER_ERROR.value(),message);
    }

    public static <T> Response<T> failure(int code, String message) {
        return build(null, Boolean.FALSE, code, message);
    }

//	public static <T> Response<T> failure(T result, String msg) {
//		return failure(FAIL, result, msg);
//	}

    private static <T> Response<T> build(T data, boolean success, int code, String message) {
        Response<T> resp;
        // 这里处理国际化 国际化时需要把msg改成国际化CODE，然后转换相应的方言
        if (data instanceof List) {
            resp = (Response<T>) new ListResponse<>();
        } else if (data instanceof Map) {
            resp = (Response<T>) new MapResponse<>();
        } else {
            resp = new DataResponse<>();
            resp.setMsg("");
        }
        resp.setCode(code);
        resp.setSuccess(success);
        resp.setData(data);
        resp.setMsg(message);
        return resp;
    }
}
