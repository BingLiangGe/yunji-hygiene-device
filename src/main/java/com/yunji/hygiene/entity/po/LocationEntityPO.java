package com.yunji.hygiene.entity.po;

import com.yunji.hygiene.entity.domain.req.jt808.LocationMsg;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description:
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class LocationEntityPO extends AbstractPersistable<Long> {

    private String imei; // 终端手机号
    private Integer alarm;
    private Integer statusField;
    private Float latitude;
    private Float longitude;
    private Short elevation;
    private Short speed;
    private Short direction;
    private String time;

    public static LocationEntityPO parseFromLocationMsg(LocationMsg msg) {
        LocationEntityPO location = new LocationEntityPO();
        location.setImei(msg.getHeader().getImei());
        BeanUtils.copyProperties(msg, location);
        return location;
    }
}
