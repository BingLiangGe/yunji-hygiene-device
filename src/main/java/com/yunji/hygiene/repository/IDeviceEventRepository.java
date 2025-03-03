package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.DeviceEventPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author : peter-zhu
 * @date : 2025/1/25 21:50
 * @description : TODO
 **/
public interface IDeviceEventRepository extends JpaRepository<DeviceEventPO, Long> {

    @Query("select updateTime from DeviceEventPO  where  id=:eventId")
    Date getUpdateTime(@Param("eventId") Long eventId);

    @Transactional
    @Modifying
    @Query("update DeviceEventPO set finishStatus=1,updateTime=:finishTime where id=:eventId")
    int eventFinish(@Param("eventId") Long eventId, @Param("finishTime") Date finishTime);

    @Transactional
    @Modifying
    @Query(value = "update tl_device_event set after_cmd=:afterCmd,update_time=:updateTime where event_id=:eventId", nativeQuery = true)
    int updateEvent(@Param("eventId") Long eventId, @Param("afterCmd") String afterCmd, @Param("updateTime") Date updateTime);
}
