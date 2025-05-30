package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerCellPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2025/1/23 11:16
 * @description : TODO
 **/
public interface IContainerCellRepository extends JpaRepository<ContainerCellPO, Long> {

//    @Transactional
//    @Modifying
//    @Query("update ContainerCellPO set distance=:distance,deviceQuantity=:deviceQuantity " +
//            " where delFlag = 0 and ordinal = :ordinal and containerId = :containerId")
//    int updateCell(@Param("ordinal") Integer ordinal, @Param("containerId") Long containerId, @Param("distance") Integer distance, @Param("deviceQuantity") Integer deviceQuantity);

    @Transactional
    @Query("select c from ContainerCellPO c where c.containerId=:containerId and c.delFlag=0")
    List<ContainerCellPO> getCellList(@Param("containerId")Long containerId);

    @Transactional
    @Query("select c from ContainerCellPO c where c.containerId=:containerId and c.ordinal=1 and c.delFlag=0")
    ContainerCellPO getCell(@Param("containerId")Long containerId);
}
