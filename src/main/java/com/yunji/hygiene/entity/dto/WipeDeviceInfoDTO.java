package com.yunji.hygiene.entity.dto;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2025/1/24 15:24
 * @description : TODO
 **/
@EqualsAndHashCode(callSuper = true)
@Data
//@JsonTypeInfo(
//        use = JsonTypeInfo.Id.NAME,   // 让 `DeviceInfoDTO` 只使用 `@type`
//        include = JsonTypeInfo.As.PROPERTY,
//        property = "@type"
//)
public class WipeDeviceInfoDTO extends DeviceInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer inLimitStatus; //  是否完全进仓
    private Integer outLimitStatus; //  是否完全出仓
    private Integer distance;  // 距离
    private Integer tissueStatus; // 是否有货
    private Long productId;
    private Integer productNums;
    private String sku;
    private String productName;

    @Override
    public List<DeviceDetailInfoDTO> getInfoList() {
        return Lists.newArrayList(new DeviceDetailInfoDTO(1, getDistance(),
                getTissueStatus(), 1, getProductId(), getProductNums(), getSku(), getProductName()));
    }
}
