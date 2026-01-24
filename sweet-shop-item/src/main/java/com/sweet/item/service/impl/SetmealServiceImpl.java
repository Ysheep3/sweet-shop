package com.sweet.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.api.dto.SetmealOverViewVO;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.CategoryErrorException;
import com.sweet.common.exception.DeleteNotAllowException;
import com.sweet.common.exception.DishGetErrorException;
import com.sweet.common.exception.SetmealErrorException;
import com.sweet.common.result.PageResult;
import com.sweet.item.common.ItemStatusEnum;
import com.sweet.item.entity.dto.SetmealDTO;
import com.sweet.item.entity.dto.SetmealPageDTO;
import com.sweet.item.entity.pojo.Setmeal;
import com.sweet.item.entity.pojo.SetmealDish;
import com.sweet.item.entity.vo.SetmealVO;
import com.sweet.item.mapper.SetmealDishMapper;
import com.sweet.item.mapper.SetmealMapper;
import com.sweet.item.service.SetmealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;

    @Override
    public List<SetmealVO> getSetmealByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_ID_NOT_NULL);
        }

        LambdaQueryWrapper<Setmeal> wrapper = Wrappers.lambdaQuery(Setmeal.class)
                .eq(Setmeal::getStatus, ItemStatusEnum.ENABLED.getCode());
        List<Setmeal> setmealList = new ArrayList<>();
        List<SetmealVO> setmealVOList = new ArrayList<>();

        if (categoryId == 0) {
            setmealList = setmealMapper.selectList(wrapper);

            for (Setmeal setmeal : setmealList) {
                SetmealVO setmealVO = BeanUtil.toBean(setmeal, SetmealVO.class);
                setmealVOList.add(setmealVO);
            }
            return setmealVOList;
        }

        wrapper.eq(Setmeal::getCategoryId, categoryId)
                .eq(Setmeal::getStatus, ItemStatusEnum.ENABLED.getCode());
        setmealList = setmealMapper.selectList(wrapper);

        if (CollectionUtil.isEmpty(setmealList)) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_HAVE_NOT_SETMEAL);
        }

        for (Setmeal setmeal : setmealList) {
            SetmealVO setmealVO = BeanUtil.toBean(setmeal, SetmealVO.class);
            setmealVOList.add(setmealVO);
        }
        return setmealVOList;
    }

    @Override
    public SetmealVO getById(Long id) {
        if (id == null) {
            throw new DishGetErrorException(MessageConstant.DISH_OR_SETMEAL_ID_NOT_NULL);
        }

        Setmeal setmeal = setmealMapper.selectById(id);

        if (setmeal != null) {
            SetmealVO setmealVO = BeanUtil.toBean(setmeal, SetmealVO.class);
            LambdaQueryWrapper<SetmealDish> wrapper = Wrappers.lambdaQuery(SetmealDish.class)
                    .eq(SetmealDish::getSetmealId, id);

            List<SetmealDish> setmealDishes = setmealDishMapper.selectList(wrapper);
            if (CollectionUtil.isNotEmpty(setmealDishes)) {
                setmealVO.setSetmealDishes(setmealDishes);
                return setmealVO;
            }
        }

        throw new DishGetErrorException(MessageConstant.GET_ERROR);
    }


    /**
     * 管理端分页查询
     *
     * @param setmealPageDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageDTO setmealPageDTO) {
        Page<SetmealVO> page = new Page<>(
                setmealPageDTO.getPage(),
                setmealPageDTO.getPageSize()
        );

        IPage<SetmealVO> result = setmealMapper.pageQuery(page, setmealPageDTO);

        return new PageResult(
                result.getTotal(),
                result.getRecords()
        );
    }

    /**
     * 添加套餐
     *
     * @param setmealDTO
     */
    @Transactional
    public void insert(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        setmeal.setCreateUser(BaseContext.getCurrentId());
        setmeal.setUpdateUser(BaseContext.getCurrentId());

        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }

        setmealDishMapper.insertBatchs(setmealDishes);
    }

    /**
     * 删除套餐
     *
     * @param ids
     */
    @Transactional
    public void deleteBatchByIds(List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            // 判断是否起售中
            if (setmeal.getStatus().equals(ItemStatusEnum.ENABLED.getCode())) {
                throw new DeleteNotAllowException(setmeal.getName()+MessageConstant.DELETE_SETMEAL_ERROR_BY_START);
            }
        }

        setmealMapper.deleteBatchIds(ids);
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 修改套餐信息
     *
     * @param setmealDTO
     */
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());

        setmealMapper.updateById(setmeal);

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        setmealDishMapper.delete(wrapper);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealDishMapper.insertBatchs(setmealDishes);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            throw new SetmealErrorException(MessageConstant.SETMEAL_IS_NULL);
        }

        setmeal.setStatus(status);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());

        setmealMapper.updateById(setmeal);
    }

    @Override
    public SetmealOverViewVO overviewSetmeals() {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getStatus, ItemStatusEnum.DISABLED.getCode());
        long discontinued = setmealMapper.selectCount(wrapper);

        wrapper.clear();
        wrapper.eq(Setmeal::getStatus, ItemStatusEnum.ENABLED.getCode());
        long sold = setmealMapper.selectCount(wrapper);

        SetmealOverViewVO vo = new SetmealOverViewVO();
        vo.setSold((int) sold);
        vo.setDiscontinued((int) discontinued);

        return vo;
    }
}
