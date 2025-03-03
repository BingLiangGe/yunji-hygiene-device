package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "tt_upgrade_task")
public class UpgradeTaskPO {
    @Id
    @Size(max = 32)
    @Column(name = "task_code", nullable = false, length = 32)
    private String taskCode;

    @Size(max = 32)
    @NotNull
    @Column(name = "task_name", nullable = false, length = 32)
    private String taskName;

    @NotNull
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Size(max = 2)
    @NotNull
    @Column(name = "chip_type", nullable = false, length = 2)
    private String chipType;

    @Size(max = 32)
    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "task_status")
    private Integer taskStatus;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "close_time")
    private Instant closeTime;

    @Column(name = "facility_nums")
    private Integer facilityNums;

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