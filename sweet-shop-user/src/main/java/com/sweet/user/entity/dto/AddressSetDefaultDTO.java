package com.sweet.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressSetDefaultDTO {
    /** 地址 id */
    private Long id;
    /** 用户 id */
    private Long userId;
    /** 是否默认地址 1是 0否 */
    private Integer isDefault;
}
