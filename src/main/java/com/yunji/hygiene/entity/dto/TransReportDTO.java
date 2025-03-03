package com.yunji.hygiene.entity.dto;

import com.yunji.hygiene.entity.domain.DataPacket;
import lombok.Data;

/**
 * @author : peter-zhu
 * @date : 2025/2/13 14:00
 * @description : TODO
 **/
@Data
public class TransReportDTO {
    private boolean success;
    private boolean trans;// 如果是false 就是服务器通用回应(jt808) true就是的TransMsg
    private DataPacket dataPacket;

    public TransReportDTO(boolean success, boolean trans, DataPacket dataPacket) {
        this.success = success;
        this.trans = trans;
        this.dataPacket = dataPacket;
    }
}
