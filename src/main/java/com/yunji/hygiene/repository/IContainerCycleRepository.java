package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerCyclePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * IContainerCycleRepository：设备在线/离线周期表（ContainerCyclePO）JPA 数据访问接口
 *
 * 主要用途：
 * - 记录设备(imei) 的在线/离线周期（startTime ~ endTime）
 * - 查询某设备最新一条周期记录
 * - 在设备状态切换时，更新上一条周期的 endTime（闭合周期）
 */
public interface IContainerCycleRepository extends JpaRepository<ContainerCyclePO, Long> {

    /**
     * 查询某设备(imei) 最新一条周期记录
     * - 通过 MAX(id) 获取最新记录（默认 id 递增代表时间顺序）
     *
     * @param chip 设备IMEI
     * @return 最新周期记录（可能为 null）
     */
    @Query("SELECT c FROM ContainerCyclePO c WHERE c.id = (SELECT MAX(c2.id) FROM ContainerCyclePO c2 WHERE c2.chipImei = :chip)")
    ContainerCyclePO getNewestCycle(@Param("chip") String chip);

    /**
     * 闭合最新周期：更新指定周期记录的 endTime
     * - 常用于：设备从在线->离线 或 离线->在线 时，把上一段周期结束时间补齐
     *
     * @param time 结束时间
     * @param id   周期记录ID
     * @return 更新条数（通常为 1）
     */
    @Transactional
    @Modifying
    @Query("UPDATE ContainerCyclePO SET endTime = :time WHERE id = :id")
    int modifyNewestCycle(@Param("time") Date time, @Param("id") Long id);
}
