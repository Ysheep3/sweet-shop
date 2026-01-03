package com.sweet.item.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO implements Serializable {
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
}
