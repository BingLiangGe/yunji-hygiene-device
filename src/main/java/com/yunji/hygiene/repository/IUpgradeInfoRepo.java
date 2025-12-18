package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.UpgradeInfoPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * IUpgradeInfoRepo：升级信息表（tb_upgrade_info / UpgradeInfoPO）数据访问接口
 *
 * 主要用途：
 * - 更新某次升级(infoId)的状态：开始升级、结束/关闭升级
 * - 通常由升级流程的业务服务（如 DeviceService.finishTask/startUpgrade）调用
 */
public interface IUpgradeInfoRepo extends JpaRepository<UpgradeInfoPO, Long> {

    /**
     * 结束/关闭某次升级任务
     * - 将升级信息状态置为 3（完成/关闭）
     * - 记录 close_time=now()
     * - 仅对未删除数据(del_flag=0)生效
     *
     * @param infoId 升级信息ID
     */
    @Transactional
    @Modifying
    @Query(value = "update tb_upgrade_info set info_status=3 ,close_time = now() where del_flag=0 and info_id=:infoId",
            nativeQuery = true)
    void finishTask(@Param("infoId") Long infoId);

    /**
     * 标记某次升级任务开始执行
     * - 将升级信息状态置为 2（升级中/已开始）
     * - 记录 start_time=now()
     * - 仅对未删除数据(del_flag=0)生效
     *
     * @param infoId 升级信息ID
     */
    @Transactional
    @Modifying
    @Query(value = "update tb_upgrade_info set info_status=2 ,start_time = now() where del_flag=0 and info_id=:infoId",
            nativeQuery = true)
    void startUpgrade(@Param("infoId") Long infoId);
}
