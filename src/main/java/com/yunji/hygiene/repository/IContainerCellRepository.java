package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerCyclePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : peter-zhu
 * @date : 2025/1/23 11:16
 * @description : TODO
 **/
public interface IContainerCellRepository extends JpaRepository<ContainerCyclePO, Long> {

    @Transactional
    @Modifying
    @Query("update ContainerCellPO set distance=:distance,inLimitStatus=:inLimitStatus,outLimitStatus=:outLimitStatus,lockStatus=:lockStatus" +
            " where delFlag = 0 and ordinal = :ordinal and containerId = :containerId")
    int cabinetCell(@Param("ordinal") Integer ordinal,@Param("containerId")  Long containerId, @Param("distance") Integer distance,
                    @Param("inLimitStatus") Integer inLimitStatus,@Param("outLimitStatus")  Integer outLimitStatus, @Param("lockStatus") Integer lockStatus);
}
