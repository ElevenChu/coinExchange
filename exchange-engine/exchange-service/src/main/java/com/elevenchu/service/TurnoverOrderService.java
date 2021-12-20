package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.TurnoverOrder;
import com.baomidou.mybatisplus.extension.service.IService;
public interface TurnoverOrderService extends IService<TurnoverOrder>{


    Page<TurnoverOrder> findByPage(Page<TurnoverOrder> page, Long userId, String symbol, Integer type);
}
