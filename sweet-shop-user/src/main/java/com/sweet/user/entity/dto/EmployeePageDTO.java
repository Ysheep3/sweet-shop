package com.sweet.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeePageDTO {
    private String name;
    private int page;  // 页码
    private int pageSize;  // 每页个数
}
