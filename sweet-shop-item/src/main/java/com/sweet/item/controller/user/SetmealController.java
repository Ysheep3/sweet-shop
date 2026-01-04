package com.sweet.item.controller.user;

import com.sweet.item.entity.vo.SetmealVO;
import com.sweet.item.service.SetmealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sweet.shop.common.result.Result;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/items/user/setmeal")
@RequiredArgsConstructor
public class SetmealController {
    private final SetmealService setmealService;

    @GetMapping("/list")
    public Result<List<SetmealVO>> list(Long categoryId) {
        List<SetmealVO> setmealVOList = setmealService.getSetmealByCategoryId(categoryId);
        return Result.success(setmealVOList);
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }
}
