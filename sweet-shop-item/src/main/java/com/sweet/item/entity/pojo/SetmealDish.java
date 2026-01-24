package com.sweet.item.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 套餐菜品关系
 * </p>
 *
 * @author Ysheep
 * @since 2024-11-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("setmeal_dish")
public class SetmealDish implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long setmealId;

    private Long dishId;

    /** 菜品名 */
    private String name;

    /** 菜品单价 */
    private BigDecimal price;

    /** "菜品份数" */
    private Integer copies;
}
