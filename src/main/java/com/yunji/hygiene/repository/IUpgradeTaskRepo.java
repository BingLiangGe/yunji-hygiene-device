package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.UpgradeTaskPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * IUpgradeTaskRepo：升级任务表（tt_upgrade_task / UpgradeTaskPO）数据访问接口
 *
 * 主要用途：
 * - startUpgrade：把某个 task_code 的任务状态置为“升级中”(2)，写入 start_time
 * - finishTask：把某个 task_code 的任务状态置为“已结束/关闭”(3)，写入 close_time
 *
 * 注意：
 * - finishTask 有“兜底收口”逻辑：只有当同 task_code 下不存在 info_status 仍处于(0,1,2)的升级信息时，才允许把 task 结束
 *   => 避免“还有升级单没完成，就把整体任务提前关掉”
 */
public interface IUpgradeTaskRepo extends JpaRepository<UpgradeTaskPO, Long> {

    /**
     * 结束升级任务（task_status=3，close_time=NOW）
     * 条件：
     * - 通过 infoId 先反查 task_code
     * - 且不存在同 task_code 下 info_status 仍处于(0,1,2)的升级信息记录
     *
     * @param infoId 升级信息ID（tb_upgrade_info.info_id）
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE tt_upgrade_task ts SET ts.task_status = 3,ts.close_time = NOW() " +
            " WHERE ts.task_code = ( SELECT task_code FROM tb_upgrade_info WHERE info_id =:infoId) " +
            " AND NOT EXISTS ( SELECT  inf.info_id FROM tb_upgrade_info inf WHERE ts.task_code = inf.task_code" +
            " AND inf.info_status IN (0,1,2) )", nativeQuery = true)
    void finishTask(@Param("infoId") Long infoId);

    /**
     * 开始升级任务（task_status=2，start_time=NOW）
     * 条件：
     * - 通过 infoId 先反查 task_code
     * - 且 task 当前状态必须在 (1)（通常表示“待开始/已创建”）
     *
     * @param infoId 升级信息ID
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE tt_upgrade_task ts SET ts.task_status = 2,ts.start_time = NOW() " +
            " WHERE ts.task_code = ( SELECT task_code FROM tb_upgrade_info WHERE info_id =:infoId) " +
            " AND ts.task_status IN (1) ", nativeQuery = true)
    void startUpgrade(@Param("infoId") Long infoId);
}
