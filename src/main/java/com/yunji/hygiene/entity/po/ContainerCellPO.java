package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "t_container_cell")
@DynamicUpdate
public class ContainerCellPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cell_id", nullable = false)
    private Integer id;

    @Column(name = "container_id")
    private Long containerId;

    @Column(name = "ordinal")
    private Integer ordinal;

    @Column(name = "product_id")
    private Long productId;

//    @Size(max = 32)
//    @Column(name = "product_name", length = 32)
//    private String productName;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "status_update_time", nullable = false)
    private Instant statusUpdateTime;

    @Column(name = "product_quantity")
    private Integer productQuantity;

    @Column(name = "lack_status")
    private Integer lackStatus;

    @Column(name = "lack_time")
    private Instant lackTime;

    @Column(name = "ceiling_quantity")
    private Integer ceilingQuantity;

    @Column(name = "device_quantity")
    private Integer deviceQuantity;

    @Column(name = "send_status")
    private Integer sendStatus;

    @Column(name = "del_flag")
    private Integer delFlag;

    @Column(name = "creator")
    private Long creator;

//    @Size(max = 255)
//    @Column(name = "create_name")
//    private String createName;
//
//    @Column(name = "create_time")
//    private Instant createTime;
//
//    @Column(name = "updater")
//    private Long updater;
//
//    @Size(max = 255)
//    @Column(name = "update_name")
//    private String updateName;
//
//    @Column(name = "update_time")
//    private Instant updateTime;

    @Column(name = "distance")
    private Integer distance;

    @Column(name = "infrared_status")
    private Integer infraredStatus;

}