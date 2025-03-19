package com.yunji.hygiene.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class HygieneInfoDTO extends DeviceInfoDTO {
    private static final long serialVersionUID = 1L;

    private List<HygieneDetailInfoDTO> infoList;

}
