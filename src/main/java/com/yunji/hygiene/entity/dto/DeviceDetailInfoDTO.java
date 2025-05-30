package com.yunji.hygiene.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDetailInfoDTO  implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer ordinal;  // 格子号
    private Integer distance;  // 距离
    private Integer quantityAvailable; // 是否有货
    private Integer motorStatus;  // 马达状态
    private Long productId;
    private Integer productNums;
    private String sku;
    private String productName;
}
