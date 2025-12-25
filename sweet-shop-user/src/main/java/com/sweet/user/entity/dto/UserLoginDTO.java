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
public class UserLoginDTO {
    /** 用户 id */
    private Long id;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /**  昵称 */
    private String nickname;
    /**  手机号 */
    private String phone;
    /**  头像 */
    private String avatar;
    /**  使用状态 1正常 0禁用 */
    private Integer status;
}
