package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.Market;
import com.baomidou.mybatisplus.extension.service.IService;
public interface MarketService extends IService<Market>{


    Page<Market> findByPage(Page<Market> page, Long tradeAreaId, Byte status);
}
