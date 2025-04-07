package com.yunji.hygiene.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : peter-zhu
 * @date : 2025/1/10 11:01
 * @description : TODO
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class EnterHandleDTO extends EnterCommandDTO {
    private String serviceHandle;
}
