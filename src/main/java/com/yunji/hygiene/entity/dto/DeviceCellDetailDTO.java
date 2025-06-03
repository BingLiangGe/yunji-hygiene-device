package com.yunji.hygiene.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCellDetailDTO {
    private Integer deviceQuantity;
    private Integer ordinal;
    private Long productId;
    private String sku;
    private String productName;
}
