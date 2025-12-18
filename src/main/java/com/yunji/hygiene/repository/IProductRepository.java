package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ProductPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * IProductRepository：商品表（ProductPO）JPA 数据访问接口
 *
 * 主要用途：
 * - 按商品ID查询商品信息（名称、规格、价格、容量参数等）
 * - 常用于设备上报/库存计算时，通过 productId 补齐商品属性参与计算
 */
public interface IProductRepository extends JpaRepository<ProductPO, Long> {

    /**
     * 根据商品ID查询商品记录
     *
     * @param productId 商品ID
     * @return 商品信息；不存在返回 null
     */
    @Transactional
    @Query("from ProductPO where id = :productId")
    ProductPO getProduct(@Param("productId") Long productId);
}
