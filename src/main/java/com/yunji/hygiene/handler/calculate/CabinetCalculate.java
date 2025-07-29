package com.yunji.hygiene.handler.calculate;

import com.yunji.hygiene.constant.DeviceConstant;
import com.yunji.hygiene.entity.dto.DeviceCellDetailDTO;
import com.yunji.hygiene.entity.po.ContainerCellPO;
import com.yunji.hygiene.entity.po.ProductPO;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CabinetCalculate {
    //    public BigDecimal getProductHeight(Long productId) {
//        ProductCacheDTO product = ProductCache.getProduct(productId);
//        return product.getProductHeight();
//    }
//    public static BigDecimal getQuantityByHeightDecimal(Integer productId, BigDecimal distance, BigDecimal productHeight) {
//        return distance.add(DeviceConstant.PRODUCT_DIFFER_VALUE).divide(productHeight, 0, RoundingMode.DOWN);
//    }
    public static DeviceCellDetailDTO getEventQuantity(ContainerCellPO cell, Integer distance, BigDecimal typeHeight, ProductPO p) {
        BigDecimal quantity = typeHeight.subtract(new BigDecimal(distance)).add(DeviceConstant.PRODUCT_DIFFER_VALUE)
                .divide(p.getProductHeight(), 0, RoundingMode.DOWN);
        return new DeviceCellDetailDTO(quantity.intValue(), cell.getOrdinal(), cell.getProductId(), p.getSku(), p.getProductName());
    }
}
