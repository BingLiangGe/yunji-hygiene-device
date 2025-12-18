package com.yunji.hygiene.constant;

import java.math.BigDecimal;
/**
 * DeviceConstant：设备/业务通用常量
 *
 * - SUCCESS / FAILURE：设备通信或业务处理的统一返回标识（字符串）
 * - PROJECT_CODE：项目/平台标识码（用于区分不同项目、渠道、协议域等）
 * - PRODUCT_DIFFER_VALUE：商品差异阈值（BigDecimal，避免浮点精度问题）
 */
public class DeviceConstant {

    /** 通用成功标识：通常用于协议返回、业务处理结果、设备ACK等 */
    public static final String SUCCESS = "OK";

    /** 通用失败标识：通常用于协议返回、业务处理结果、设备ACK等 */
    public static final String FAILURE = "FAIL";

    /** 项目编码：用于标识当前项目（如日志、协议域、Redis Key 前缀、租户隔离等） */
    public static final String PROJECT_CODE = "SZJ";

    /**
     * 商品差异阈值：3（用 BigDecimal 表示，避免 double 精度问题）
     * 使用建议：用 compareTo 比较，而不是 equals
     * 例如：if (diff.compareTo(PRODUCT_DIFFER_VALUE) >= 0) { ... }
     */
    public static final BigDecimal PRODUCT_DIFFER_VALUE = new BigDecimal("3");
}
