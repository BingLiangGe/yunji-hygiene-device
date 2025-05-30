package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ProductPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : peter-zhu
 * @date : 2025/5/30 11:47
 * @description : TODO
 **/
public interface IProductRepository extends JpaRepository<ProductPO, Long> {

    @Transactional
    @Query("select id,sku,productHeight,productName from ProductPO where id = :productId")
    ProductPO getProduct(@Param("productId") Long productId);
}
