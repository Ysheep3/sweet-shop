package com.sweet.item.service;


import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.pojo.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getByType(CategoryDTO categoryDTO);
}
