package com.sweet.order.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    /** 地址 id */
    private Long id;
    /** 收货人 */
    private String consignee;
    /** 手机号 */
    private String phone;
    /** 省份 */
    private String province;
    /** 城市 */
    private String city;
    /** 区县 */
    private String district;
    /** 详细地址 */
    private String detailAddress;
    /** 标签 */
    private String label;
    /** 是否默认地址 1是 0否 */
    private Integer isDefault;
}
