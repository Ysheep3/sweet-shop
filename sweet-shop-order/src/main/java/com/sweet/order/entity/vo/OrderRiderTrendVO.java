package com.sweet.order.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRiderTrendVO {
    private Integer todayFinished;

    private Integer weekFinished;

    private Integer totalFinished;

    private BigDecimal todayIncome;

    private BigDecimal weekIncome;

    private BigDecimal totalIncome;

    private List<String> dateList;

    private List<Integer> orderFinishCountList;

    private List<BigDecimal> incomeList;
}
