package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "t_upgrade_file")
public class UpgradeFilePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_code")
    private String fileCode;

    @Column(name = "factory_brand")
    private String factoryBrand;

    @Column(name = "model_type")
    private String modelType;

    @Column(name = "version")
    private String version;

    @Column(name = "chip_type", length = 2)
    private String chipType;

    @Column(name = "pack")
    private byte[] pack;

    @Size(max = 32)
    @Column(name = "pack_size", length = 32)
    private String packSize;


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

    @Override
    public String toString() {
        return "UpgradeFilePO{" +
                "modelType='" + modelType + '\'' +
                ", id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileCode='" + fileCode + '\'' +
                ", factoryBrand='" + factoryBrand + '\'' +
                ", chipType='" + chipType + '\'' +
                '}';
    }
}