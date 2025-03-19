package com.yunji.hygiene.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : peter-zhu
 * @date : 2025/1/24 15:24
 * @description : TODO
 **/
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
}
