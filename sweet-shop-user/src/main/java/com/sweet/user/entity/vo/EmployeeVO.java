package com.sweet.user.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeVO {
    /** 用户 id */
    private String  id;
    /** 用户名 */
    private String username;
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
}
