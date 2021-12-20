package com.elevenchu.service;

import com.elevenchu.domain.UserFavoriteMarket;
import com.baomidou.mybatisplus.extension.service.IService;
public interface UserFavoriteMarketService extends IService<UserFavoriteMarket>{


    boolean deleteUserFavoriteMarket(Long id, Long userId);
}
