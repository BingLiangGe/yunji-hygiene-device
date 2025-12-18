package com.yunji.hygiene.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractResponse：统一响应对象的抽象基类（通用返回结构）
 *
 * 设计目的：
 * - 把所有接口返回的公共字段抽取到基类：code/msg/success/time/dataIds
 * - 子类只需要关注自己的 data（泛型 T）或额外扩展字段
 * - 通过链式方法 withDataId/withDataIds 方便把“关联数据ID”塞进响应，用于日志、排查、前端定位
 *
 * 常见用法示例：
 * - return ResponseHelper.success(data).withDataId(orderId);
 * - return ResponseHelper.fail("xxx").withDataIds(Lists.newArrayList(id1,id2));
 *
 * @param <T> 响应主体数据的泛型类型（例如：T=UserVO、T=String、T=List<OrderVO>）
 */
public abstract class AbstractResponse<T> implements Response<T> {

    /** 业务响应码：通常 0/200 表示成功，非 0 表示各种业务错误（由你们系统定义） */
    private int code;

    /** 响应提示信息：成功提示/错误原因等 */
    private String msg;

    /** 是否成功：默认 true，失败时需要 setSuccess(false) */
    private boolean success = true;

    /**
     * 关联数据ID列表（可选字段）
     *
     * 用途：
     * - 把关键业务ID带回前端或日志系统（如 orderId、eventId、imei、scanCode 等）
     * - 在复杂链路中，用于快速定位“这次响应关联的是哪条业务数据”
     *
     * Jackson 注解：
     * - NON_NULL：字段为 null 时不输出
     *   注意：当前代码默认 new ArrayList<>()，它永远不为 null，因此通常都会输出（即使空数组）
     *   若你希望“空集合不输出”，可以改为：JsonInclude.Include.NON_EMPTY
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Object> dataIds = new ArrayList<>();

    /** 获取业务响应码 */
    @Override
    public int getCode() {
        return code;
    }

    /** 设置业务响应码 */
    @Override
    public void setCode(int code) {
        this.code = code;
    }

    /** 获取提示信息 */
    @Override
    public String getMsg() {
        return msg;
    }

    /** 设置提示信息 */
    @Override
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /** 是否成功 */
    @Override
    public boolean isSuccess() {
        return success;
    }

    /** 设置成功/失败标志 */
    @Override
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取响应时间戳（毫秒）
     *
     * 说明：
     * - 每次调用都会返回“当前时间”，不是固定的响应创建时间
     * - 如果你希望 time 固定不变，通常会定义一个字段：private final long time = System.currentTimeMillis();
     */
    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }

    /**
     * 批量设置 dataIds（链式调用）
     *
     * @param dataIds 关联数据ID列表（如订单ID列表、事件ID列表等）
     * @return 返回 Response 自身，支持链式调用
     */
    @Override
    public Response<T> withDataIds(List<Object> dataIds) {
        this.dataIds = dataIds;
        return this;
    }

    /**
     * 追加单个 dataId（链式调用）
     *
     * @param dataId 单个关联数据ID（如 orderId / eventId / imei / scanCode）
     * @return 返回 Response 自身，支持链式调用
     */
    @Override
    public Response<T> withDataId(Object dataId) {
        this.dataIds.add(dataId);
        return this;
    }
}
