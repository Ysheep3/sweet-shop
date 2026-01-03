package com.sweet.item.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class DishPageDTO {
    private String name;

    private int page;

    private int pageSize;

    private Long categoryId;

    private Integer status;
}
