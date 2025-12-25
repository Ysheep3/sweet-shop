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
public class EmployeeDTO {
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
    /**  角色 */
    private String role;
    /**  使用状态 1正常 0禁用 */
    private Integer status;
}
