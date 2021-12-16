package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.MarketMapper;
import com.elevenchu.domain.Market;
import com.elevenchu.service.MarketService;
@Service
public class MarketServiceImpl extends ServiceImpl<MarketMapper, Market> implements MarketService{

    @Override
    public Page<Market> findByPage(Page<Market> page, Long tradeAreaId, Byte status) {
        return page(page,new LambdaQueryWrapper<Market>().eq(tradeAreaId!=null,Market::getTradeAreaId,tradeAreaId)
        .eq(status!=null,Market::getStatus,status));
    }
}
