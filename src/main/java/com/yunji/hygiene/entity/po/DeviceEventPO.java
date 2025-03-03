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
@Table(name = "tl_device_event")
public class DeviceEventPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false)
    private Long id;

    @NotNull
    @Lob
    @Column(name = "event_param", nullable = false)
    private String eventParam;

    @NotNull
    @Column(name = "event_data_id", nullable = false)
    private Long eventDataId;

    @Size(max = 200)
    @NotNull
    @Column(name = "event_data_code", nullable = false, length = 200)
    private String eventDataCode;

    @Size(max = 50)
    @NotNull
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Size(max = 50)
    @NotNull
    @Column(name = "event_cmd", nullable = false, length = 50)
    private String eventCmd;

    @Size(max = 50)
    @NotNull
    @Column(name = "imei", nullable = false, length = 50)
    private String imei;

    @NotNull
    @Column(name = "belong_id", nullable = false)
    private Long belongId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Size(max = 10)
    @NotNull
    @Column(name = "user_type", nullable = false, length = 10)
    private String userType;

    @Size(max = 100)
    @NotNull
    @Column(name = "real_name", nullable = false, length = 100)
    private String realName;

    @Size(max = 255)
    @NotNull
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotNull
    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Size(max = 255)
    @NotNull
    @Column(name = "agent_name", nullable = false)
    private String agentName;

    @NotNull
    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Size(max = 255)
    @NotNull
    @Column(name = "site_name", nullable = false)
    private String siteName;

    @NotNull
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Size(max = 255)
    @NotNull
    @Column(name = "location_name", nullable = false)
    private String locationName;

    @NotNull
    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Size(max = 32)
    @NotNull
    @Column(name = "container_name", nullable = false, length = 32)
    private String containerName;

    @NotNull
    @Column(name = "ordinal", nullable = false)
    private Integer ordinal;

    @NotNull
    @Column(name = "lack_status", nullable = false)
    private Integer lackStatus;

    @Column(name = "product_id")
    private Long productId;

    @Size(max = 255)
    @Column(name = "product_name")
    private String productName;

    @Size(max = 50)
    @Column(name = "sku", length = 50)
    private String sku;

    @NotNull
    @Lob
    @Column(name = "before_cmd", nullable = false)
    private String beforeCmd;

    @NotNull
    @Lob
    @Column(name = "after_cmd", nullable = false)
    private String afterCmd;

    @NotNull
    @Column(name = "finish_status", nullable = false)
    private Integer finishStatus;

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

}