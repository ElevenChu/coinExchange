package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dto.CoinDto;
import feign.CoinServiceFeign;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.MarketMapper;
import com.elevenchu.domain.Market;
import com.elevenchu.service.MarketService;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class MarketServiceImpl extends ServiceImpl<MarketMapper, Market> implements MarketService{
    @Resource
    private CoinServiceFeign coinServiceFeign;



    @Override
    public Page<Market> findByPage(Page<Market> page, Long tradeAreaId, Byte status) {
        return page(page,new LambdaQueryWrapper<Market>().eq(tradeAreaId!=null,Market::getTradeAreaId,tradeAreaId)
        .eq(status!=null,Market::getStatus,status));
    }

    @Override
    public List<Market> getMarketsByTradeAreaId(Long id) {

        return list(new LambdaQueryWrapper<Market>().eq(Market::getId,id).eq(Market::getStatus,1).orderByAsc(Market::getSort));
    }

    @Override
    public boolean save(Market entity) {
        @NotBlank Long sellCoinId = entity.getSellCoinId();//报价货币
        @NotNull Long buyCoinId = entity.getBuyCoinId(); // 基础货币
        List<CoinDto> coins = coinServiceFeign.findCoins(Arrays.asList(sellCoinId, buyCoinId));
        if (CollectionUtils.isEmpty(coins) || coins.size() != 2) {
            throw new IllegalArgumentException("货币输入错误");
        }
        CoinDto coinDto = coins.get(0);
        CoinDto sellCoin = null;
        CoinDto buyCoin = null;
        if (coinDto.getId().equals(sellCoinId)) {
            sellCoin = coinDto;
            buyCoin = coins.get(1);
        } else {
            sellCoin = coins.get(1);
            buyCoin = coinDto;
        }

        entity.setName(sellCoin.getName() + "/" + buyCoin.getName()); // 交易市场的名称  报价货币/基础货币
        entity.setTitle(sellCoin.getTitle() + "/" + buyCoin.getTitle()); // 交易市场的标题 报价货币/基础货币
        entity.setSymbol(sellCoin.getName() + buyCoin.getName()); // 交易市场的标识 报价货币基础货币
        entity.setImg(sellCoin.getImg()); // 交易市场的图标

        return super.save(entity);
    }
}
