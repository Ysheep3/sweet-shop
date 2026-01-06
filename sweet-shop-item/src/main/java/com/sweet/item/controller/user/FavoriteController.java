package com.sweet.item.controller.user;

import com.sweet.item.entity.dto.FavoriteDTO;
import com.sweet.item.entity.vo.FavoriteVO;
import com.sweet.item.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sweet.shop.common.result.Result;

import java.util.List;

@RestController("favoriteController")
@RequestMapping("/items/user/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @GetMapping("/list")
    public Result<List<FavoriteVO>> listFavorites() {
        List<FavoriteVO> favorites = favoriteService.listFavorites();
        return Result.success(favorites);
    }

    @PostMapping("/check")
    public Result<Boolean> isFavorite(@RequestBody FavoriteDTO requestParam) {
        Boolean isFav = favoriteService.isFavorite(requestParam);
        return Result.success(isFav);
    }

    @PostMapping("/add")
    public Result<Void> addFavorite(@RequestBody FavoriteDTO requestParam) {
        favoriteService.addFavorite(requestParam);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteFavorite(@RequestBody FavoriteDTO requestParam) {
        favoriteService.deleteFavorite(requestParam);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        favoriteService.deleteFavoriteById(id);
        return Result.success();
    }
}
