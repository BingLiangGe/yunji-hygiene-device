package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "t_container")
public class ContainerPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "container_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "container_name", nullable = false)
    private String containerName;

    @Size(max = 64)
    @NotNull
    @Column(name = "chip_sn", nullable = false, length = 64)
    private String chipSn;

    @Size(max = 255)
    @NotNull
    @Column(name = "chip_imei", nullable = false)
    private String chipImei;

    @Size(max = 255)
    @NotNull
    @Column(name = "qrcode", nullable = false)
    private String qrcode;

    @Size(max = 32)
    @Column(name = "version", length = 32)
    private String version;

    @NotNull
    @Column(name = "online_status", nullable = false)
    private Integer onlineStatus;

    @Column(name = "online_time")
    private Date onlineTime;

    @Column(name = "offline_time")
    private Date offlineTime;

    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "shape_id")
    private Long shapeId;

    @Column(name = "bind_time")
    private Date bindTime;

    @Column(name = "del_flag")
    private Integer delFlag;

    @Column(name = "creator")
    private Long creator;

    @Size(max = 255)
    @Column(name = "create_name")
    private String createName;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "updater")
    private Long updater;

    @Size(max = 255)
    @Column(name = "update_name")
    private String updateName;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "battle_level")
    private Integer battleLevel;

    @Column(name = "battle_status")
    private Integer battleStatus;

    @Column(name = "update_battle_time")
    private Date updateBattleTime;

    @Column(name = "sleep_status")
    private Integer sleepStatus;
}