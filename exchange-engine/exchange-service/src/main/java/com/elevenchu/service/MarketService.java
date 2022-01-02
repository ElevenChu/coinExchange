package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.Market;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elevenchu.dto.MarketDto;

import java.util.List;

public interface MarketService extends IService<Market>{


    Page<Market> findByPage(Page<Market> page, Long tradeAreaId, Byte status);

    List<Market> getMarketsByTradeAreaId(Long id);

    Market getMarkerBySymbol(String symbol);


    MarketDto findByCoinId(Long buyCoinId, Long sellCoinId);

    List<MarketDto> queryAllMarkets();
}
