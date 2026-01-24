package com.sweet.item.service;


import com.sweet.common.result.PageResult;
import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.dto.CategoryPageQueryDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.entity.vo.CategoryVO;

import java.util.List;

public interface CategoryService {
    List<CategoryVO> getByType(CategoryDTO categoryDTO);

    /**
     * 分类分页查询
     *
     * @param categoryPageDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageDTO);

    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 分类启用与禁用
     *
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改分类信息
     *
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 根据id删除分类
     *
     * @param id
     */
    void delete(Long id);

    /**
     * 查询分列表
     *
     * @param categoryDTO
     * @return
     */
    List<Category> list(CategoryDTO categoryDTO);

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    List<Category> getCategoryListByType(Integer type);
}
