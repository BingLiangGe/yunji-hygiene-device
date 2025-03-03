package com.yunji.hygiene.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : peter-zhu
 * @date : 2025/2/17 16:50
 * @description : TODO
 **/
@Data
public class UpGradeFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String imei;
    private Long fileId;
    private Long infoId;
    private String versionPrefix;
    private String version;

    @Override
    public String toString() {
        return "UpGradeFileData{" +
                "fileId=" + fileId +
                ", imei='" + imei + '\'' +
                ", infoId=" + infoId +
                ", versionPrefix='" + versionPrefix + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
