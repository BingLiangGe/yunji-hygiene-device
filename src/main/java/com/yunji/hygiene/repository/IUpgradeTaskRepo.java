package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.UpgradeTaskPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : peter-zhu
 * @date : 2025/2/15 17:28
 * @description : TODO
 **/
public interface IUpgradeTaskRepo extends JpaRepository<UpgradeTaskPO, Long> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE tt_upgrade_task ts SET ts.task_status = 3,ts.close_time = NOW() " +
            " WHERE ts.task_code = ( SELECT task_code FROM tb_upgrade_info WHERE info_id =:infoId) " +
            " AND NOT EXISTS ( SELECT  inf.info_id FROM tb_upgrade_info inf WHERE ts.task_code = inf.task_code" +
                " AND inf.info_status IN (0,1,2) )", nativeQuery = true)
    void finishTask(@Param("infoId") Long infoId);
}
