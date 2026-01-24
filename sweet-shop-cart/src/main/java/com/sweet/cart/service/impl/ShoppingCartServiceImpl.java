package com.sweet.cart.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.api.client.ItemClient;
import com.sweet.api.dto.DishVO;
import com.sweet.api.dto.SetmealVO;
import com.sweet.cart.entity.dto.ShoppingCartDTO;
import com.sweet.cart.entity.pojo.ShoppingCart;
import com.sweet.cart.entity.vo.ShoppingCartVO;
import com.sweet.cart.mapper.ShoppingCartMapper;
import com.sweet.cart.service.ShoppingCartService;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.ShoppingCartBusinessException;
import com.sweet.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartMapper shoppingCartMapper;
    private final ItemClient itemClient;

    @Override
    public void addItemToCart(ShoppingCartDTO requestParam) {
        if (requestParam.getDishId() == null && requestParam.getSetmealId() == null) {
            throw new ShoppingCartBusinessException(MessageConstant.INSERT_ERROR);
        }
        // 判断购物车中是否已经有相同的数据
        ShoppingCart shoppingCart = BeanUtil.copyProperties(requestParam, ShoppingCart.class);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
            ShoppingCart cart = shoppingCartList.get(0);
            cart.setNumber(cart.getNumber() + 1);

            shoppingCartMapper.updateById(cart);
        } else {
            // 判断是套餐还是菜品
            if (requestParam.getDishId() != null) {
                // 是菜品
                Result<DishVO> result = itemClient.getDishById(requestParam.getDishId());
                if (result.getCode() == 1) {
                    shoppingCart.setName(result.getData().getName());
                    shoppingCart.setDishId(shoppingCart.getDishId());
                    shoppingCart.setImage(result.getData().getImage());
                    shoppingCart.setAmount(result.getData().getPrice());
                }
            } else {

                Result<SetmealVO> result = itemClient.getSetmealById(requestParam.getSetmealId());
                if (result.getCode() == 1) {
                    shoppingCart.setAmount(result.getData().getPrice());
                    shoppingCart.setName(result.getData().getName());
                    shoppingCart.setImage(result.getData().getImage());
                    shoppingCart.setSetmealId(shoppingCart.getSetmealId());
                }
            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public Integer getCount() {
        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class)
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(wrapper);

        return shoppingCarts.stream().map(ShoppingCart::getNumber).reduce(0, Integer::sum);

    }

    @Override
    public List<ShoppingCartVO> list() {
        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class)
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(wrapper);

        if (CollUtil.isEmpty(shoppingCarts)) {
            return List.of();
        }

        List<ShoppingCartVO> shoppingCartVOS = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            ShoppingCartVO cartVO = BeanUtil.toBean(shoppingCart, ShoppingCartVO.class);
            shoppingCartVOS.add(cartVO);
        }

        return shoppingCartVOS;
    }

    @Override
    public void delete(ShoppingCartDTO requestParam) {
        ShoppingCart shoppingCart = BeanUtil.copyProperties(requestParam, ShoppingCart.class);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        ShoppingCart cart = shoppingCartList.get(0);

        int row;
        if (cart.getNumber() > 1) {
            cart.setNumber(cart.getNumber() - 1);
            row = shoppingCartMapper.updateById(cart);
        } else {
            row = shoppingCartMapper.deleteById(cart);
        }

        if (row < 0) {
            throw new ShoppingCartBusinessException(MessageConstant.DO_ERROR);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new ShoppingCartBusinessException(MessageConstant.DO_ERROR);
        }
        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class)
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId())
                .eq(ShoppingCart::getId, id);

        int row = shoppingCartMapper.delete(wrapper);

        if (row < 0) {
            throw new ShoppingCartBusinessException(MessageConstant.DO_ERROR);
        }

    }

    @Override
    public void clear() {
        LambdaQueryWrapper<ShoppingCart> wrapper = Wrappers.lambdaQuery(ShoppingCart.class)
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        int row = shoppingCartMapper.delete(wrapper);

        if (row < 0) {
            throw new ShoppingCartBusinessException(MessageConstant.DO_ERROR);
        }
    }

    @Override
    public List<ShoppingCartVO> listByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new ShoppingCartBusinessException(MessageConstant.DO_ERROR);
        }
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectBatchIds(ids);

        if (CollUtil.isEmpty(shoppingCarts)) {
            return List.of();
        }

        List<ShoppingCartVO> shoppingCartVOS = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            ShoppingCartVO cartVO = BeanUtil.toBean(shoppingCart, ShoppingCartVO.class);
            shoppingCartVOS.add(cartVO);
        }

        return shoppingCartVOS;
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new ShoppingCartBusinessException(MessageConstant.DO_ERROR);
        }

        int row = shoppingCartMapper.deleteBatchIds(ids);

        if (row < 1) {
            throw new ShoppingCartBusinessException(MessageConstant.DELETE_ERROR);
        }

    }

    @Override
    public void again(List<ShoppingCartVO> shoppingCartVOS) {
        if (CollUtil.isEmpty(shoppingCartVOS)) {
            throw new ShoppingCartBusinessException(MessageConstant.INSERT_ERROR);
        }

        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        for (ShoppingCartVO shoppingCartVO : shoppingCartVOS) {
            ShoppingCart cart = BeanUtil.toBean(shoppingCartVO, ShoppingCart.class);
            cart.setUserId(BaseContext.getCurrentId());
        }

        shoppingCartMapper.insertBatch(shoppingCarts);
    }
}
