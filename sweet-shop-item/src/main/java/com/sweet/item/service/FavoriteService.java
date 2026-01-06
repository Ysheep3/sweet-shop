package com.sweet.item.service;


import com.sweet.item.entity.dto.FavoriteDTO;
import com.sweet.item.entity.vo.FavoriteVO;

import java.util.List;

public interface FavoriteService {
    List<FavoriteVO> listFavorites();

    void addFavorite(FavoriteDTO requestParam);

    Boolean isFavorite(FavoriteDTO requestParam);

    void deleteFavorite(FavoriteDTO requestParam);

    void deleteFavoriteById(Long id);
}
