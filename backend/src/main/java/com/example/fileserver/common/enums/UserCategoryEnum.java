package com.example.fileserver.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserCategoryEnum {

    NORMAL("NORMAL", "普通用户"),
    ADVANCED("ADVANCED", "高级用户"),
    VIP("VIP", "VIP用户"),
    ENTERPRISE("ENTERPRISE", "企业用户"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String name;

    public static UserCategoryEnum fromCode(String code) {
        for (UserCategoryEnum category : values()) {
            if (category.getCode().equalsIgnoreCase(code)) {
                return category;
            }
        }
        return NORMAL;
    }
}
