package com.yunji.hygiene.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : peter-zhu
 * @date : 2025/2/17 15:36
 * @description : TODO
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UpgradeCommandDTO extends EnterCommandDTO {
    private Long fileId;
    private Long infoId;

    public UpgradeCommandDTO(){}

    public UpgradeCommandDTO(String cmd, int eventId, String imei, Long fileId, Long infoId) {
        super(cmd, eventId, imei);
        this.fileId = fileId;
        this.infoId = infoId;
    }
}
