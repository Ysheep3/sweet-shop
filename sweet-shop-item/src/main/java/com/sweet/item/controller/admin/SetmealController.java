package com.sweet.item.controller.admin;

import com.sweet.api.dto.SetmealOverViewVO;
import com.sweet.common.result.PageResult;
import com.sweet.common.result.Result;
import com.sweet.item.entity.dto.SetmealDTO;
import com.sweet.item.entity.dto.SetmealPageDTO;
import com.sweet.item.entity.vo.SetmealVO;
import com.sweet.item.service.SetmealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 套餐 前端控制器
 * </p>
 *
 * @author Ysheep
 * @since 2024-11-19
 */
@RestController("adminSetmealController")
@RequestMapping("/items/admin/setmeal")
@RequiredArgsConstructor
public class SetmealController {
    private final SetmealService setmealService;

    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageDTO setmealPageDTO) {
        PageResult pageResult = setmealService.pageQuery(setmealPageDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Void> save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.insert(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    //@CacheEvict(value = "setmealCache", allEntries = true)
    public Result<Void> startOrStop(@PathVariable Integer status, Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping
   // @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<Void> delete(@RequestParam List<Long> ids) {
        setmealService.deleteBatchByIds(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    //@CacheEvict(value = "setmealCache", allEntries = true)
    public Result<Void> update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.updateWithDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        SetmealOverViewVO vo = setmealService.overviewSetmeals();
        return Result.success(vo);
    }
}
