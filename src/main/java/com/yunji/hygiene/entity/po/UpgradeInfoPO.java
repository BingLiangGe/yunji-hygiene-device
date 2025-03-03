package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tb_upgrade_info")
public class UpgradeInfoPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "info_id", nullable = false)
    private Long id;

    @Size(max = 32)
    @NotNull
    @Column(name = "task_code", nullable = false, length = 32)
    private String taskCode;

    @NotNull
    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Size(max = 32)
    @NotNull
    @Column(name = "container_name", nullable = false, length = 32)
    private String containerName;

    @Size(max = 255)
    @NotNull
    @Column(name = "chip_imei", nullable = false)
    private String chipImei;

    @NotNull
    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    @Size(max = 32)
    @Column(name = "type_name", length = 32)
    private String typeName;

    @NotNull
    @Column(name = "online_status", nullable = false)
    private Integer onlineStatus;

    @NotNull
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Size(max = 2)
    @Column(name = "chip_type", length = 2)
    private String chipType;

    @Size(max = 32)
    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "info_status")
    private Integer infoStatus;

    @Column(name = "whether_retry")
    private Integer whetherRetry;

    @NotNull
    @Column(name = "retry_times", nullable = false)
    private Integer retryTimes;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "close_time")
    private Instant closeTime;

    @Column(name = "use_time")
    private Instant useTime;

    @Column(name = "del_flag")
    private Integer delFlag;

    @Column(name = "creator")
    private Long creator;

    @Size(max = 100)
    @Column(name = "create_name", length = 100)
    private String createName;

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "updater")
    private Long updater;

    @Size(max = 100)
    @Column(name = "update_name", length = 100)
    private String updateName;

    @Column(name = "update_time")
    private Instant updateTime;

}