package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.po.UpgradeInfoPO;
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
public interface IUpgradeInfoRepo extends JpaRepository<UpgradeInfoPO, Long> {

    @Transactional
    @Modifying
    @Query(value = "update tb_upgrade_info set info_status=3 ,close_time = now() where del_flag=0 and info_id=:infoId", nativeQuery = true)
    void finishTask(@Param("infoId")Long infoId);

    @Transactional
    @Modifying
    @Query(value = "update tb_upgrade_info set info_status=2 ,start_time = now() where del_flag=0 and info_id=:infoId", nativeQuery = true)
    void startUpgrade(@Param("infoId")Long infoId);
}
