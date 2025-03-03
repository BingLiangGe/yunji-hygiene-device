package com.yunji.hygiene.entity.domain;

import com.yunji.hygiene.entity.enums.DeviceErrorEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

/**
 * @author : peter-zhu
 * @date : 2025/1/11 15:49
 * @description : TODO
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class DeviceException extends RuntimeException {
    private static final long serialVersionUID = 8186340498580439590L;

    public DeviceException(DeviceErrorEnum errorEnum, Object... values) {
        this.code = errorEnum.getCode();
        if (values != null && values.length > 0)
            this.message = MessageFormat.format(errorEnum.getMessage(), values);
        else
            this.message = errorEnum.getMessage();
        log.error(this.message);
    }

    public DeviceException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private Integer code;

    private String message;

    @Override
    public String toString() {
        return "DeviceException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
