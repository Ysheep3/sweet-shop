package com.sweet.item.service;


import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.entity.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> getByType(CategoryDTO categoryDTO);
}
