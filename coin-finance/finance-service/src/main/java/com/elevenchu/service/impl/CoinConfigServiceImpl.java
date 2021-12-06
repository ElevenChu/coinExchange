package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elevenchu.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.CoinConfig;
import com.elevenchu.mapper.CoinConfigMapper;
import com.elevenchu.service.CoinConfigService;
@Service
public class CoinConfigServiceImpl extends ServiceImpl<CoinConfigMapper, CoinConfig> implements CoinConfigService{
    @Autowired
    private CoinService coinService;

    /**
     * 通过币种的id 查询币种的配置信息
     *
     * @param coinId 币种的id
     * @return 币种的配置信息
     */
    @Override
    public CoinConfig findByCoinId(Long coinId) {
        // coinConfig的id 和Coin的id 值是相同的
        return getOne(new LambdaQueryWrapper<CoinConfig>().eq(CoinConfig::getId, coinId));
    }

}
