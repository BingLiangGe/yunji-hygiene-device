package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerTypePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * IContainerTypeRepository：柜子型号/类型表（ContainerTypePO）JPA 数据访问接口
 *
 * 主要用途：
 * - 根据柜子型号(typeCode)查询该型号对应的关键配置参数（例如 typeHeight）
 * - typeHeight 通常用于业务计算：如距离/高度换算库存、传感器阈值判断等
 */
public interface IContainerTypeRepository extends JpaRepository<ContainerTypePO, Long> {

    /**
     * 查询某个柜子型号(typeCode)的“型号高度配置”(typeHeight)
     *
     * 常见使用场景：
     * - 设备上报距离/高度数据时，结合型号高度进行数量/库存换算
     * - 计算格子容量、阈值、补货判断等
     *
     * @param typeCode 柜子型号编码（如 WIPE/HYGIENE 等）
     * @return 型号高度（BigDecimal），找不到可能返回 null
     */
    @Transactional
    @Query("select typeHeight from ContainerTypePO where typeCode = :typeCode")
    BigDecimal getTypeHeight(@Param("typeCode") String typeCode);
}
