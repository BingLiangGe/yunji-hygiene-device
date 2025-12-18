package com.yunji.hygiene.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: peter-zhu
 * @Date: 2025/1/24 15:24
 * @Description: 设备单元详细信息DTO，用于表示设备的单元格详细信息，包含设备数量、产品信息等。
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCellDetailDTO {

    /**
     * 设备数量：表示该设备单元中设备的数量
     */
    private Integer deviceQuantity;

    /**
     * 序号：用于表示设备单元的排序或者编号
     */
    private Integer ordinal;

    /**
     * 产品ID：与设备单元相关联的产品ID
     */
    private Long productId;

    /**
     * SKU：产品的库存单位，表示产品的唯一标识符
     */
    private String sku;

    /**
     * 产品名称：与设备单元相关的产品名称
     */
    private String productName;
}
