package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerCyclePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author : peter-zhu
 * @date : 2025/1/23 11:20
 * @description : TODO
 **/
public interface IContainerCycleRepository extends JpaRepository<ContainerCyclePO, Long> {
    //    @Modifying
//    @Query(" INSERT INTO  `tl_ct_container_cycle` (`container_id`, `chip_imei`, `start_time`, `end_time`, `cycle_type`) " +
//            " VALUES ( #{containerId}, #{chipImei}, #{startTime}, #{endTime}, #{cycleType})")
//    int insertCycle(ContainerCyclePO containerCycle);

    @Query("SELECT c FROM ContainerCyclePO c WHERE c.id = (SELECT MAX(c2.id) FROM ContainerCyclePO c2 WHERE c2.chipImei = :chip)")
    ContainerCyclePO getNewestCycle(@Param("chip") String chip);

    @Transactional
    @Query("UPDATE ContainerCyclePO SET endTime = :time WHERE id = :id")
    @Modifying
    int modifyNewestCycle(@Param("time") Date time, @Param("id") Long id);
}
