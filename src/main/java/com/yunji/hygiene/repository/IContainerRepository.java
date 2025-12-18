package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * IContainerRepository：货柜/设备主表（ContainerPO）Repository
 *
 * 一、它管的是什么数据？
 * - ContainerPO 通常是“柜子主表/设备主表”，记录：
 *   1）设备标识：chipImei（设备IMEI）
 *   2）在线状态：onlineStatus、onlineTime、offlineTime
 *   3）设备实时字段：sleepStatus(睡眠)、battleLevel(电量)、rssi(信号)、inLimitStatus/outLimitStatus(门限位)、lockStatus(锁)
 *   4）版本信息：version（用于 OTA/版本管理）
 *   5）告警/运行状态：battleStatus（低电告警）、updateBattleTime（告警更新时间）、runtimeStatus/runtimeError（运行异常）
 *
 * 二、为什么大量使用 @Query(update ...)？
 * - 直接 UPDATE 可以避免：
 *   - 先 select 再 save 的额外 SQL
 *   - 在高频上报（设备每分钟/每几秒）时降低数据库压力
 *
 * 三、幂等性/并发控制设计点（非常关键）
 * 1）cabinetOnline：
 *   - 条件 onlineStatus=0 才更新
 *   - 目的：防止重复“上线”调用不停覆盖 onlineTime（上线时间应是首次上线的时间点）
 *   - 返回值：更新行数（0 表示本次没有发生状态切换）
 *
 * 2）cabinetOffline：
 *   - 条件 onlineStatus=1 才更新
 *   - 目的：防止重复“离线”调用不停覆盖 offlineTime
 *
 * 3）updateStatus：
 *   - 无条件更新在线状态（你们可能用于强制设置、补偿任务、后台管理操作）
 *
 * 四、关于 @Modifying / @Transactional
 * - JPQL UPDATE 属于“批量更新”，必须：
 *   - @Modifying：告诉 Spring Data 这是写操作
 *   - @Transactional：保证更新在事务中执行
 *
 * 五、接口说明
 * - findByChipImeiAndDelFlag：查询柜子主信息
 * - cabinetOnline/cabinetOffline：在线状态切换（带状态条件 => 更安全）
 * - updateCabinet：批量写入实时字段（常用于设备上报）
 * - updateVersion/selectVersion：版本读写
 * - updateBattle：电量告警状态变更（低电/恢复）
 */
public interface IContainerRepository extends JpaRepository<ContainerPO, Long> {

    /**
     * 查询柜子主表记录（按 imei + delFlag）
     *
     * 使用场景：
     * - 收到设备上报：先查是否存在该柜子
     * - 下发指令前：查柜子信息、校验绑定关系
     *
     * @param chipImei 设备IMEI（唯一标识）
     * @param delFlag  逻辑删除标记（0=正常）
     * @return ContainerPO，找不到返回 null
     */
    ContainerPO findByChipImeiAndDelFlag(@Param("chipImei") String chipImei,
                                         @Param("delFlag") Integer delFlag);

    /**
     * 设备上线（幂等更新）
     *
     * SQL语义：
     * - 仅当 delFlag=0 且 onlineStatus=0 时：
     *   设置 onlineStatus=1，并写 onlineTime=当前时间
     *
     * 设计目的：
     * - 防止“重复上线”覆盖 onlineTime（在线时间应该代表首次进入在线态的时刻）
     *
     * @param chip 设备IMEI
     * @param time 上线时间
     * @return 更新条数（1=成功切换到在线态，0=未切换/已在线/无此记录）
     */
    @Modifying
    @Transactional
    @Query("update ContainerPO set onlineTime = :time,onlineStatus=1 where chipImei = :chip and delFlag = 0 and onlineStatus=0")
    int cabinetOnline(@Param("chip") String chip, @Param("time") Date time);

    /**
     * 强制更新设备在线状态（不限制原状态）
     *
     * 使用场景：
     * - 补偿逻辑：修正在线状态
     * - 管理后台：强制标记在线/离线
     *
     * @param chip   设备IMEI
     * @param status 在线状态（你们定义：1=在线，0=离线）
     * @return 更新条数
     */
    @Modifying
    @Transactional
    @Query("update ContainerPO set onlineStatus=:status where chipImei=:chip and delFlag = 0")
    int updateStatus(@Param("chip") String chip, @Param("status") Integer status);

    /**
     * 设备离线（幂等更新）
     *
     * SQL语义：
     * - 仅当 delFlag=0 且 onlineStatus=1 时：
     *   设置 onlineStatus=0，并写 offlineTime=当前时间
     *
     * 设计目的：
     * - 防止重复离线反复覆盖离线时间
     *
     * @param chip 设备IMEI
     * @param time 离线时间
     * @return 更新条数（1=成功切换到离线态，0=未切换/已离线/无此记录）
     */
    @Modifying
    @Transactional
    @Query("update ContainerPO set offlineTime = :time,onlineStatus=0 where chipImei = :chip and delFlag = 0 and onlineStatus=1")
    int cabinetOffline(@Param("chip") String chip, @Param("time") Date time);

    /**
     * 更新设备版本号（OTA/上报版本同步）
     *
     * @param imei    设备IMEI
     * @param version 版本号
     */
    @Modifying
    @Transactional
    @Query("update ContainerPO set version = :version where chipImei = :imei and delFlag = 0")
    void updateVersion(@Param("imei") String imei, @Param("version") String version);

    /**
     * 查询设备版本号
     *
     * @param chipImei 设备IMEI
     * @return version
     */
    @Query("select version  from  ContainerPO where chipImei = :chipImei and delFlag = 0")
    String selectVersion(@Param("chipImei") String chipImei);

    /**
     * 更新柜子实时字段（通常来自设备上报）
     *
     * 字段含义（按你们业务推测）：
     * - sleepStatus：设备睡眠/唤醒状态
     * - battleLevel：电量百分比
     * - rssi：信号强度
     * - inLimitStatus/outLimitStatus：门限位（关门到位/开门到位等）
     * - lockStatus：锁状态（0/1）
     *
     * @param containerId   柜子ID
     * @param battleLevel   电量
     * @param sleepStatus   睡眠状态
     * @param rssi          信号强度
     * @param inLimitStatus 入门限位
     * @param outLimitStatus 出门限位
     * @param lockStatus    锁状态
     * @return 更新条数
     */
    @Modifying
    @Transactional
    @Query("update ContainerPO set sleepStatus=:sleepStatus,battleLevel=:battleLevel,rssi=:rssi,inLimitStatus=:inLimitStatus,outLimitStatus=:outLimitStatus,lockStatus=:lockStatus" +
            " where id = :containerId and delFlag = 0 ")
    int updateCabinet(@Param("containerId") Long containerId,
                      @Param("battleLevel") Integer battleLevel,
                      @Param("sleepStatus") Integer sleepStatus,
                      @Param("rssi") Integer rssi,
                      @Param("inLimitStatus") Integer inLimitStatus,
                      @Param("outLimitStatus") Integer outLimitStatus,
                      @Param("lockStatus") Integer lockStatus);

    /**
     * 更新电量告警状态（battleStatus）与更新时间
     *
     * 使用场景：
     * - 电量跨阈值（比如 <=30 触发低电告警；>30 恢复）
     *
     * @param id 柜子ID
     * @param battleStatus 电量告警状态（你们定义：0/1）
     * @param updateBattleTime 告警状态更新时间
     * @return 更新条数
     */
    @Modifying
    @Transactional
    @Query("update ContainerPO set battleStatus=:battleStatus,updateBattleTime=:updateBattleTime where id=:id and delFlag=0")
    int updateBattle(@Param("id") Long id,
                     @Param("battleStatus") int battleStatus,
                     @Param("updateBattleTime") Date updateBattleTime);
}
