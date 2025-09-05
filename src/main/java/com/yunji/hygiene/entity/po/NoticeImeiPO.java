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

    @NotNull
    @Column(name = "notice_type", nullable = false)
    private Integer noticeType;

    @Size(max = 32)
    @NotNull
    @Column(name = "imei", nullable = false, length = 32)
    private String imei;

    public NoticeImeiPO(String imei, Integer noticeType) {
        this.imei = imei;
        this.noticeType = noticeType;
    }

    public NoticeImeiPO() {
    }
}