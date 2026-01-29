package com.sweet.user.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee {
    /** 用户 id */
    private Long id;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /**  姓名 */
    private String name;
    /**  手机号 */
    private String phone;
    /**  头像 */
    private String avatar;

    private Integer sex;
    /**  角色 */
    private Integer role;
    /**  使用状态 1正常 0禁用 */
    private Integer status;

    private String idNumber;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
