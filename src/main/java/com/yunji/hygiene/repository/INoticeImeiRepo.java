package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.NoticeImeiPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * INoticeImeiRepo：设备通知记录表（NoticeImeiPO）JPA 数据访问接口
 *
 * 主要用途：
 * - 根据设备 IMEI + 通知类型 查询“未消费/未处理”的通知记录（isConsume=0）
 * - 常用于：避免重复插入同类型通知、判断是否已存在待处理告警/通知
 */
public interface INoticeImeiRepo extends JpaRepository<NoticeImeiPO, Long> {

    /**
     * 查询某设备某类型的“未消费通知”（isConsume=0）
     *
     * @param imei       设备IMEI（唯一标识）
     * @param noticeType 通知类型（例如：低电、异常、缺货等）
     * @return 未消费通知记录；若不存在返回 null
     */
    @Transactional
    @Query("from NoticeImeiPO where imei=:imei and noticeType=:noticeType and isConsume=0")
    NoticeImeiPO getNoticeImei(@Param("imei") String imei, @Param("noticeType") int noticeType);
}
