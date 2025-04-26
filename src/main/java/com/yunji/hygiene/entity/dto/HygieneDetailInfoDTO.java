package com.yunji.hygiene.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class HygieneDetailInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer ordinal;  // 格子号
    private Integer distance;  // 距离
    private Integer tissueStatus; // 是否有货
    private Integer motorStatus; // 马达状态
}
