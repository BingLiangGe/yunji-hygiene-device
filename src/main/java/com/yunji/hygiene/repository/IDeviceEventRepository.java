package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.DeviceEventPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * IDeviceEventRepository：设备事件表（DeviceEventPO）JPA 数据访问接口
 *
 * 主要用途：
 * - 查询事件更新时间（用于判断“最近是否有事件在执行/更新”）
 * - 标记事件完成（finishStatus=1）
 * - 更新事件执行后的回执数据（afterCmd + updateTime）
 * - 插入“异常开门”等系统事件（native insert-select 从柜子表补齐上下文信息）
 */
public interface IDeviceEventRepository extends JpaRepository<DeviceEventPO, Long> {

    /**
     * 查询事件的更新时间（updateTime）
     * - 用于判断事件是否在一定时间窗口内仍有效（例如 10 分钟内的事件才允许更新）
     */
    @Query("select updateTime from DeviceEventPO where id=:eventId")
    Date getUpdateTime(@Param("eventId") Long eventId);

    /**
     * 事件完成：finishStatus=1，并更新时间 updateTime
     *
     * @return 更新条数（通常 1）
     */
    @Transactional
    @Modifying
    @Query("update DeviceEventPO set finishStatus=1,updateTime=:finishTime where id=:eventId")
    int eventFinish(@Param("eventId") Long eventId, @Param("finishTime") Date finishTime);

    /**
     * 更新事件回执（after_cmd）与更新时间（update_time）
     * - 这里使用 nativeQuery，表字段是 event_id / after_cmd / update_time
     */
    @Transactional
    @Modifying
    @Query(value = "update tl_device_event set after_cmd=:afterCmd,update_time=:updateTime where event_id=:eventId", nativeQuery = true)
    int updateEvent(@Param("eventId") Long eventId, @Param("afterCmd") String afterCmd, @Param("updateTime") Date updateTime);

    /**
     * 插入一条“异常开门/异常事件”记录（native insert-select）
     * - 从柜子表 t_container 及相关站点/代理/点位表中补齐 belong/agent/site/location 等信息
     * - after_cmd 写入设备上报内容（用于追溯）
     * - finish_status=1（表示该异常事件记录插入即完成）
     *
     * @param imei     设备IMEI
     * @param afterCmd 设备上报/事件内容（通常是 JSON 或上报结构体）
     * @return 插入条数（通常 1）
     */
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
