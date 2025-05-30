package com.yunji.hygiene.entity.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "product")
public class ProductPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "sku", nullable = false)
    private String sku;

    @Size(max = 200)
    @Column(name = "product_name", length = 200)
    private String productName;

    @NotNull
    @Column(name = "product_height", nullable = false, precision = 10, scale = 4)
    private BigDecimal productHeight;

}