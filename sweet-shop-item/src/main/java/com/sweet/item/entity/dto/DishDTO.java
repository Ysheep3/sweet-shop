package com.sweet.item.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DishDTO {
    private Long id;

    private String name;

    private Long categoryId;

    private Integer type;

    private BigDecimal price;

    private String image;

    private String description;

    private Integer status;
}
