package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.TradeArea;
import com.baomidou.mybatisplus.extension.service.IService;
public interface TradeAreaService extends IService<TradeArea>{


    Page<TradeArea> findByPage(Page<TradeArea> page, String name, Byte status);
}
