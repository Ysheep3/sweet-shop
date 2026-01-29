package com.sweet.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.item.entity.dto.FavoriteDTO;
import com.sweet.item.entity.pojo.Favorite;
import com.sweet.item.entity.vo.FavoriteVO;
import com.sweet.item.mapper.FavoriteMapper;
import com.sweet.item.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.FavoriteBusinessException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteMapper favoriteMapper;


    @Override
    public List<FavoriteVO> listFavorites() {
        LambdaQueryWrapper<Favorite> wrapper = Wrappers.lambdaQuery(Favorite.class)
                .eq(Favorite::getUserId, BaseContext.getCurrentId());
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);

        List<FavoriteVO> favoriteVOS = new ArrayList<>();
        if (CollUtil.isNotEmpty(favorites)) {
            for (Favorite favorite : favorites) {
                FavoriteVO favoriteVO = BeanUtil.toBean(favorite, FavoriteVO.class);
                favoriteVOS.add(favoriteVO);
            }
        }

        return favoriteVOS;
    }

    @Override
    public void addFavorite(FavoriteDTO requestParam) {
        if (requestParam == null) {
            throw new FavoriteBusinessException(MessageConstant.COLLECT_ERROR);
        }

        Favorite favorite;
        if (requestParam.getType() == 1) {
            // 菜品
            favorite = Favorite.builder()
                    .name(requestParam.getName())
                    .price(requestParam.getPrice())
                    .dishId(requestParam.getProductId())
                    .userId(BaseContext.getCurrentId())
                    .image(requestParam.getImage())
                    .build();
        } else {
            // 套餐
            favorite = Favorite.builder()
                    .name(requestParam.getName())
                    .price(requestParam.getPrice())
                    .setmealId(requestParam.getProductId())
                    .userId(BaseContext.getCurrentId())
                    .image(requestParam.getImage())
                    .build();

        }

        favoriteMapper.insert(favorite);
    }

    @Override
    public Boolean isFavorite(FavoriteDTO requestParam) {
        List<Favorite> favorites;
        if (requestParam.getType() == 1) {
            // 菜品
            LambdaQueryWrapper<Favorite> wrapper = Wrappers.lambdaQuery(Favorite.class)
                    .eq(Favorite::getUserId, BaseContext.getCurrentId())
                    .eq(Favorite::getDishId, requestParam.getProductId());

            favorites = favoriteMapper.selectList(wrapper);
        } else {
            // 套餐
            LambdaQueryWrapper<Favorite> wrapper = Wrappers.lambdaQuery(Favorite.class)
                    .eq(Favorite::getUserId, BaseContext.getCurrentId())
                    .eq(Favorite::getSetmealId, requestParam.getProductId());

            favorites = favoriteMapper.selectList(wrapper);
        }

        return CollUtil.isNotEmpty(favorites);
    }

    @Override
    public void deleteFavorite(FavoriteDTO requestParam) {
        if (requestParam == null) {
            throw new FavoriteBusinessException(MessageConstant.COLLECT_ERROR);
        }

        Favorite favorite;
        LambdaQueryWrapper<Favorite> wrapper = Wrappers.lambdaQuery(Favorite.class)
                .eq(Favorite::getUserId, BaseContext.getCurrentId());

        if (requestParam.getType() == 1) {
            // 菜品
            wrapper.eq(Favorite::getDishId, requestParam.getProductId());
        } else {
            // 套餐
            wrapper.eq(Favorite::getSetmealId, requestParam.getProductId());
        }
        favorite = favoriteMapper.selectOne(wrapper);
        if (favorite != null) {
            int row = favoriteMapper.deleteById(favorite.getId());
            if (row <= 0) {
                throw new FavoriteBusinessException(MessageConstant.DO_ERROR);
            }
        } else {
            throw new FavoriteBusinessException(MessageConstant.DO_ERROR);
        }
    }

    @Override
    public void deleteFavoriteById(Long id) {
        if (id == null) {
            throw new FavoriteBusinessException(MessageConstant.DO_ERROR);
        }

        int row = favoriteMapper.deleteById(id);

        if (row <= 0) {
            throw new FavoriteBusinessException(MessageConstant.DO_ERROR);
        }
    }
}
