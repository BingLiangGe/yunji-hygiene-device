package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "t_container_type")
public class ContainerTypePO {
    @Id
    @Size(max = 50)
    @Column(name = "type_code", nullable = false, length = 50)
    private String typeCode;

    @Size(max = 255)
    @NotNull
    @Column(name = "type_name", nullable = false)
    private String typeName;

    @NotNull
    @Column(name = "type_height", nullable = false, precision = 10, scale = 4)
    private BigDecimal typeHeight;

    @NotNull
    @Column(name = "nums", nullable = false)
    private Integer nums;

}