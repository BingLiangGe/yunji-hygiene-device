package com.yunji.hygiene.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCellDetailDTO {
    private Integer ordinal;
    private Long productId;
    private Integer productNums;
    private String sku;
    private String productName;
}
