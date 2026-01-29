package com.sweet.item.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;

    private Integer type;

    private String name;

    private String image;

    private BigDecimal price;
}
