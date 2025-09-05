package com.yunji.hygiene.repository;

import com.yunji.hygiene.entity.po.NoticeImeiPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : peter-zhu
 * @date : 2025/9/5 15:37
 * @description : TODO
 **/
public interface INoticeImeiRepo extends JpaRepository<NoticeImeiPO, Long> {

    @Transactional
    @Query(" from NoticeImeiPO where imei=:chipImei and noticeType=:type and isConsume=0")
    NoticeImeiPO getNoticeImei(String chipImei, int type);
}
