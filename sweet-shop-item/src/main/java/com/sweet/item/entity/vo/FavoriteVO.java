package com.sweet.item.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoriteVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String dishId;

    private String setmealId;

    private String name;

    private BigDecimal price;
}
