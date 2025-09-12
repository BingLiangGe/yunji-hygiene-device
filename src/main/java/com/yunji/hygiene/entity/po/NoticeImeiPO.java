package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "tl_notice_imei")
public class NoticeImeiPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "notice_type", nullable = false)
    private Integer noticeType;

    @Size(max = 32)
    @Column(name = "imei", nullable = false, length = 32)
    private String imei;

    @Column(name = "is_consume")
    private Integer isConsume;

    public NoticeImeiPO(String imei, Integer noticeType,Integer isConsume) {
        this.imei = imei;
        this.noticeType = noticeType;
        this.isConsume = isConsume;
    }

    public NoticeImeiPO() {
    }
}