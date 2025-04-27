package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.ContainerPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author : peter-zhu
 * @date : 2025/1/22 10:41
 * @description : TODO
 */
public interface IContainerRepository extends JpaRepository<ContainerPO, Long> {

//    @Query("select id from ContainerPO  where chipImei=:chipImei and delFlag=:delFlag")
//    Long findIdByChipImeiAndDelFlag(@Param("chipImei") String chipImei, @Param("delFlag") Integer delFlag);

    //@Query("select id,chipImei, onlineStatus,onlineTime,offlineTime from ContainerPO where chipImei = :chipImei and delFlag = 0")
    ContainerPO findByChipImeiAndDelFlag(@Param("chipImei") String chipImei, @Param("delFlag") Integer delFlag);

    @Modifying
    @Transactional
    @Query("update ContainerPO set onlineTime = :time,onlineStatus=1 where chipImei = :chip and delFlag = 0")
    int cabinetOnline(@Param("chip") String chip, @Param("time") Date time);

    @Modifying
    @Transactional
    @Query("update ContainerPO set offlineTime = :time,onlineStatus=0 where chipImei = :chip and delFlag = 0")
    int cabinetOffline(@Param("chip") String chip, @Param("time") Date time);

    @Modifying
    @Query("update ContainerPO set version = :version where chipImei = :imei and delFlag = 0")
    @Transactional
    void updateVersion(@Param("imei") String imei, @Param("version") String version);

    @Query("select version  from  ContainerPO where chipImei = :chipImei and delFlag = 0")
    String selectVersion(@Param("chipImei") String chipImei);

    @Modifying
    @Transactional
    @Query("update ContainerPO set sleepStatus=:sleepStatus,battleLevel=:battleLevel,rssi=:rssi,inLimitStatus=:inLimitStatus,outLimitStatus=:outLimitStatus,lockStatus=:lockStatus" +
            " where id = :containerId and delFlag = 0 ")
    int updateCabinet(@Param("containerId") Long containerId, @Param("battleLevel") Integer battleLevel, @Param("sleepStatus") Integer sleepStatus
            , @Param("rssi") Integer rssi,@Param("inLimitStatus") Integer inLimitStatus,@Param("outLimitStatus")  Integer outLimitStatus, @Param("lockStatus") Integer lockStatus);

    @Modifying
    @Transactional
    @Query("update ContainerPO set battleStatus=:battleStatus,updateBattleTime=:updateBattleTime where id=:id and delFlag=0")
    int updateBattle(@Param("id") Long id, @Param("battleStatus") int battleStatus, @Param("updateBattleTime") Date updateBattleTime);

    @Modifying
    @Transactional
    @Query("update ContainerPO  set rssi=:rssi,lockStatus=:lockStatus where id=:containerId and delFlag=0")
    void updateHygieneCabinet(@Param("containerId") Long containerId,@Param("rssi") Integer rssi,@Param("lockStatus") Integer lockStatus);

    @Modifying
    @Transactional
    @Query("update ContainerPO  set runtimeStatus=:runtimeStatus,runtimeError=:runtimeError where id=:containerId and delFlag=0")
    void updateCabinetRuntime(@Param("containerId") Long id,@Param("runtimeStatus") Integer runtimeStatus,@Param("runtimeError") String runtimeError);
}
