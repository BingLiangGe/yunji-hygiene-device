package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tl_ct_container_cycle")
public class ContainerCyclePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Size(max = 255)
    @NotNull
    @Column(name = "chip_imei", nullable = false)
    private String chipImei;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "cycle_type", nullable = false)
    private Integer cycleType;

    @Column(name = "create_time")
    private Date createTime;
}