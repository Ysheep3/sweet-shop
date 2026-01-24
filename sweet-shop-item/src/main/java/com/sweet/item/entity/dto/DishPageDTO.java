package com.sweet.item.entity.dto;

import lombok.Data;

@Data
public class DishPageDTO {
    private String name;

    private int page;

    private int pageSize;

    private Long categoryId;

    private Integer status;
}
