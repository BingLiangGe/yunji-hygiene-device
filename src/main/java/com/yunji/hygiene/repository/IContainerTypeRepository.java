package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerTypePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface IContainerTypeRepository extends JpaRepository<ContainerTypePO, Long> {

    @Transactional
    @Query("select typeHeight from ContainerTypePO where typeCode = :typeCode")
    BigDecimal getTypeHeight(@Param("typeCode") String typeCode);
}
