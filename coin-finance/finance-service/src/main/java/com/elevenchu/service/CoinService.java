package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.Coin;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CoinService extends IService<Coin>{

    /**
     * 分页查询数字货币
     * @param name
     * @param type
     * @param status
     * @param title
     * @param walletType
     * @param page
     * @return
     */
    Page<Coin> findByPage(String name, String type, Byte status, String title, String walletType, Page<Coin> page);

    List<Coin> getCoinsByStatus(Byte status);
}
