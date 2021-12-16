package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.mappers.CoinMappersDto;
import dto.CoinDto;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.Coin;
import com.elevenchu.mapper.CoinMapper;
import com.elevenchu.service.CoinService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper, Coin> implements CoinService{

    @Override
    public Page<Coin> findByPage(String name, String type, Byte status, String title, String walletType, Page<Coin> page) {
        return page(page,new LambdaQueryWrapper<Coin>()
                .like(!StringUtils.isEmpty(name), Coin::getName, name) //名称的查询
                .like(!StringUtils.isEmpty(title), Coin::getTitle, title)  // 标题的查询
                .eq(status != null, Coin::getStatus, status)  // 状态的查询
                .eq(!StringUtils.isEmpty(type), Coin::getType, type) // 货币类型名称的查询
                .eq(!StringUtils.isEmpty(walletType), Coin::getWallet, walletType) // 货币钱包类型的查询

        );
    }

    @Override
    public List<Coin> getCoinsByStatus(Byte status) {
        return list(new LambdaQueryWrapper<Coin>().eq(Coin::getStatus,status));
    }

    @Override
    public Coin getCoinByCoinName(String coinName) {
        return getOne(new LambdaQueryWrapper<Coin>().eq(Coin::getName,coinName));
    }

    @Override
    public List<CoinDto> findList(List<Long> coinIds) {

        List<Coin> coins = super.listByIds(coinIds);
        if(CollectionUtils.isEmpty(coinIds)){
            return Collections.emptyList() ;
        }
        List<CoinDto> coinDtos = CoinMappersDto.INSTANCE.toConvertDto(coins);
        return coinDtos;
    }
}
