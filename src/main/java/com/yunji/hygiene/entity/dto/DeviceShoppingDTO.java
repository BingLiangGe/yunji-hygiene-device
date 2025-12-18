package com.yunji.hygiene.entity.dto;

import lombok.Data;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 设备购物信息传输对象，用于表示设备相关的购物信息。
 * @Version: 1.0
 */
@Data
public class DeviceShoppingDTO {

    /**
     * 序号：用于表示设备在购物中的排序或编号
     */
    private Integer ordinal;

    /**
     * 数量：表示该设备的购物数量
     */
    private Integer nums;
}
