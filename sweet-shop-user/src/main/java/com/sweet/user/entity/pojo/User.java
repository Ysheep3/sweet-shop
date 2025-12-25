package com.sweet.user.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
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
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
