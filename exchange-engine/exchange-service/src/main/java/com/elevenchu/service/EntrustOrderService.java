package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.EntrustOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elevenchu.domain.ExchangeTrade;
import com.elevenchu.param.OrderParam;
import com.elevenchu.vo.TradeEntrustOrderVo;

public interface EntrustOrderService extends IService<EntrustOrder>{


    Page<EntrustOrder> findByPage(Page<EntrustOrder> page, Long userId, String symbol, Integer type);

    Page<TradeEntrustOrderVo> getHistoryEntrustOrder(Page<EntrustOrder> page, String symbol, Long userId);

    Page<TradeEntrustOrderVo> getEntrustOrder(Page<EntrustOrder> page, String symbol, Long userId);

    Boolean createEntrustOrder(Long userId, OrderParam orderParam);

    void doMatch(ExchangeTrade exchangeTrade);

    void cancleEntrustOrder(Long orderId);

    void cancleEntrustOrderToDb(String orderId);
}
