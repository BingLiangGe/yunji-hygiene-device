package com.yunji.hygiene.entity.enums;


import lombok.Getter;

@Getter
public enum ContainerTypeEnum {
    HYGIENE("hygiene-060"),
    WIPE("wet-wipe-030-b");

    private final String typeCode;

    ContainerTypeEnum(String typeCode) {
        this.typeCode = typeCode;
    }


}