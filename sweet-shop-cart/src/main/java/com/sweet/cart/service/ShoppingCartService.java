package com.sweet.cart.service;

import com.sweet.cart.entity.dto.ShoppingCartDTO;
import com.sweet.cart.entity.vo.ShoppingCartVO;

import java.util.List;

public interface ShoppingCartService {
    void addItemToCart(ShoppingCartDTO requestParam);

    Integer getCount();

    List<ShoppingCartVO> list();

    void delete(ShoppingCartDTO requestParam);

    void deleteById(Long id);

    void clear();

    List<ShoppingCartVO> listByIds(List<Long> ids);

    void deleteByIds(List<Long> ids);

    void again(List<ShoppingCartVO> shoppingCartVOS);
}
