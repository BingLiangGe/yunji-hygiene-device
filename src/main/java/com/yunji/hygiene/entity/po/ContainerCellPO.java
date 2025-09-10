package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
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

    @Column(name = "status")
    private Integer status;

    @Column(name = "status_update_time")
    private Instant statusUpdateTime;

    @Column(name = "product_quantity")
    private Integer productQuantity;

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

    @Column(name = "distance")
    private Integer distance;

    @Column(name = "infrared_status")
    private Integer infraredStatus;
}