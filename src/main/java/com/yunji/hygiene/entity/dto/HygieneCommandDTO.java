package com.yunji.hygiene.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2024/12/30 09:30
 * @description : TODO
 **/
@Data
public class HygieneCommandDTO {
    @NotNull(message = "指令不能为空")
    private String cmd;
    @NotBlank(message = "imei不能为空")
    private String imei;
    private int eventId;
    private List<ShoppingDTO> purchase;

    public HygieneCommandDTO(){}

    public HygieneCommandDTO(String cmd, int eventId, String imei) {
        this.cmd = cmd;
        this.eventId = eventId;
        this.imei = imei;
    }

    @Override
    public String toString() {
        return "HygieneCommandDTO{" +
                "cmd='" + cmd + '\'' +
                ", imei='" + imei + '\'' +
                ", eventId=" + eventId +
                '}';
    }
}
