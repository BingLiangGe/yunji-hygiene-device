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

    @Transactional
    @Modifying
    @Query(value = "insert into `tl_device_event` (" +
            "`event_param`,`event_data_id`,`event_data_code`,`event_type`,`event_cmd`,`imei`,`belong_id`,`user_id`,`user_type`,`real_name`,`phone`," +
            "`agent_id`,`agent_name`,`site_id`,`site_name`,`location_id`,`location_name`,`container_id`,`container_type`,`container_name`," +
            "`before_cmd`,`after_cmd`,`finish_status`)" +
            "select '{}' event_param,-1 event_data_id,UUID() event_data_code,'UNEXPECTED_OPEN' event_type,'OPEN_SHIPPING' event_cmd,c.chip_imei imei," +
            "IFNULL(c.belong_id,-1) as belong_id,-1 user_id,'NONE' user_type,'NONE' real_name,'NONE' phone," +
            "IFNULL(agent.id,-1) as agent_id,IFNULL(agent.agent_name,'无') as agent_name," +
            "IFNULL(site.id,-1) as site_id,IFNULL(site.site_name,'无') as site_name,IFNULL(l.location_id,-1) as location_id,IFNULL(l.location_name,'无') as location_name," +
            "c.container_id,c.type_code container_type,c.container_name,'{}' before_cmd,:afterCmd after_cmd,1 finish_status " +
            "from t_container c left join t_site site on site.id=c.site_id left join s_agent_user agent on agent.id=c.agent_id " +
            " left join tm_site_location l on l.location_id=c.location_id " +
            "where c.del_flag=0 and c.chip_imei=:imei", nativeQuery = true)
    int addEvent(@Param("imei") String imei, @Param("afterCmd") String afterCmd);

}
