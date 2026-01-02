package com.sweet.item.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类id */
    private Long id;

    /**类型: 1菜品分类 2套餐分类 */
    private Integer type;

    /** 分类名称 */
    private String name;

    /** 分类排序 */
    private Integer sort;

    /** 状态：1启用 0禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;

    /** "创建人" */
    private Long createUser;

    /** "修改人" */
    private Long updateUser;
}
