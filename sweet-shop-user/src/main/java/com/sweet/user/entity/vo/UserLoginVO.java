package com.sweet.user.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVO {
    /** 用户 id */
    private Long id;
    /** openId */
    private String openId;
    /**  昵称 */
    private String nickname;
    /**  手机号 */
    private String phone;
    /**  头像 */
    private String avatar;
    /**  使用状态 1正常 0禁用 */
    private Integer status;
}
