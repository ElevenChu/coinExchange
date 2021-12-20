package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.Market;
import com.elevenchu.domain.UserFavoriteMarket;
import com.elevenchu.service.MarketService;
import com.elevenchu.service.UserFavoriteMarketService;
import com.elevenchu.vo.MergeDeptVo;
import com.elevenchu.vo.TradeAreaMarketVo;
import com.elevenchu.vo.TradeMarketVo;
import dto.CoinDto;
import feign.CoinServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.TradeAreaMapper;
import com.elevenchu.domain.TradeArea;
import com.elevenchu.service.TradeAreaService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class TradeAreaServiceImpl extends ServiceImpl<TradeAreaMapper, TradeArea> implements TradeAreaService{
    @Autowired
    private MarketService marketService;

    @Resource
    private CoinServiceFeign coinServiceFeign;

    @Autowired
    private UserFavoriteMarketService userFavoriteMarketService;




    @Override
    public Page<TradeArea> findByPage(Page<TradeArea> page, String name, Byte status) {
        return page(page,new LambdaQueryWrapper<TradeArea>().eq(status!=null,TradeArea::getStatus,status)
                                                            .like(!StringUtils.isEmpty(name),TradeArea::getName,name)             );
    }

    @Override
    public List<TradeArea> findAll(Byte status) {
        return list(new LambdaQueryWrapper<TradeArea>().eq(status!=null,TradeArea::getStatus,status));
    }

    @Override
    public List<TradeAreaMarketVo> findTradeAreaMarket() {
        //1.查询所有的交易区域
        List<TradeArea> tradeAreas = list(new LambdaQueryWrapper<TradeArea>().eq(TradeArea::getStatus, 1).orderByAsc(TradeArea::getSort));
        if(CollectionUtils.isEmpty(tradeAreas)){
            return Collections.emptyList();
        }
        ArrayList<TradeAreaMarketVo> tradeAreaMarketVos = new ArrayList<>();
        for(TradeArea tradeArea:tradeAreas){
            //2.查询交易区域里面的市场
          List<Market> markets= marketService.getMarketsByTradeAreaId(tradeArea.getId());
          if(!CollectionUtils.isEmpty(markets)){
              TradeAreaMarketVo tradeAreaMarketVo=new TradeAreaMarketVo();
              tradeAreaMarketVo.setAreaName(tradeArea.getName());
              tradeAreaMarketVo.setMarkets(market2marketVos(markets));
              tradeAreaMarketVos.add(tradeAreaMarketVo);
          }
        }


        return tradeAreaMarketVos;
    }

    @Override
    public List<TradeAreaMarketVo> getUserFavoriteMarkets(Long userId) {
        List<UserFavoriteMarket> userFavoriteMarkets = userFavoriteMarketService.list(new LambdaQueryWrapper<UserFavoriteMarket>().eq(UserFavoriteMarket::getUserId, userId));
        if(CollectionUtils.isEmpty(userFavoriteMarkets)){
            return Collections.emptyList() ;
        }
        List<Long> marketIds = userFavoriteMarkets.stream().map(UserFavoriteMarket::getMarketId).collect(Collectors.toList());
        //创建一个TradeAreaMarketVo
        TradeAreaMarketVo tradeAreaMarketVo=new TradeAreaMarketVo();
        tradeAreaMarketVo.setAreaName("自选");
        List<Market> markets = marketService.listByIds(marketIds);
        List<TradeMarketVo> tradeMarketVos = market2marketVos(markets);
        tradeAreaMarketVo.setMarkets(tradeMarketVos);
        return Arrays.asList(tradeAreaMarketVo);



    }

    private List<TradeMarketVo> market2marketVos(List<Market> markets) {
        return markets.stream().map(market -> toConvertVo(market)).collect(Collectors.toList());


    }
    /**
     * 将market 转化为TradeMarketVo
     *
     * @param market
     * @return
     */
    private TradeMarketVo toConvertVo(Market market) {
        TradeMarketVo tradeMarketVo = new TradeMarketVo();
        tradeMarketVo.setImage(market.getImg()); // 报价货币的图片
        tradeMarketVo.setName(market.getName());
        tradeMarketVo.setSymbol(market.getSymbol());


        //TODO未开发完全
        // 价格的设置
        tradeMarketVo.setHigh(market.getOpenPrice()); // OpenPrice 开盘价格
        tradeMarketVo.setLow(market.getOpenPrice()); // 获取K线数据
        tradeMarketVo.setPrice(market.getOpenPrice()); // 获取K线数据
        tradeMarketVo.setCnyPrice(market.getOpenPrice()); // 计算获得
        tradeMarketVo.setCnyPrice(market.getOpenPrice()); // 计算活动
        tradeMarketVo.setPriceScale(market.getPriceScale()); // 价格保存的小数点

        // 获取报价货币
       Long buyCoinId = market.getBuyCoinId();
        List<CoinDto> coins = coinServiceFeign.findCoins(Arrays.asList(buyCoinId));

        if (CollectionUtils.isEmpty(coins) || coins.size() > 1) {
            throw new IllegalArgumentException("报价货币错误");
        }
        CoinDto coinDto = coins.get(0);
        tradeMarketVo.setPriceUnit(coinDto.getName()); // 报价货币的名称

        // 交易的额度
        tradeMarketVo.setTradeMin(market.getTradeMin());
        tradeMarketVo.setTradeMax(market.getTradeMax());

        // 下单的数量限制
        tradeMarketVo.setNumMin(market.getNumMin());
        tradeMarketVo.setNumMax(market.getNumMax());

        // 手续费的设置
        tradeMarketVo.setSellFeeRate(market.getFeeSell());
        tradeMarketVo.setBuyFeeRate(market.getFeeBuy());

        // 价格的小数的位数
        tradeMarketVo.setNumScale(market.getNumScale());


        //  排序
        tradeMarketVo.setSort(market.getSort());

        // 设置交易量
        tradeMarketVo.setVolume(BigDecimal.ZERO); // // 日的总交易量
        tradeMarketVo.setAmount(BigDecimal.ZERO); // 日的总的交易额


        // 设置涨幅
        tradeMarketVo.setChange(0.00);

        // 设置合并的深度
        tradeMarketVo.setMergeDepth(getMergeDepths(market.getMergeDepth()));

        return tradeMarketVo;

    }

    private List<MergeDeptVo> getMergeDepths(String mergeDepth) {
        String[] split = mergeDepth.split(",");
        if (split.length != 3) {
            throw new IllegalArgumentException("合并深度不合法");

        }
        //  6(1/100000),5(100000),4 (10000)
        // 最小深度
        MergeDeptVo minMergeDeptVo = new MergeDeptVo();
        minMergeDeptVo.setMergeType("MIN"); //
        minMergeDeptVo.setValue(getDeptValue(Integer.valueOf(split[0])));


        MergeDeptVo defaultMergeDeptVo = new MergeDeptVo();
        defaultMergeDeptVo.setMergeType("DEFAULT"); //
        defaultMergeDeptVo.setValue(getDeptValue(Integer.valueOf(split[1])));

        MergeDeptVo maxMergeDeptVo = new MergeDeptVo();
        maxMergeDeptVo.setMergeType("MAX"); //
        maxMergeDeptVo.setValue(getDeptValue(Integer.valueOf(split[2])));

        List<MergeDeptVo> mergeDeptVos = new ArrayList<>();
        mergeDeptVos.add(minMergeDeptVo);
        mergeDeptVos.add(defaultMergeDeptVo);
        mergeDeptVos.add(maxMergeDeptVo);

        return mergeDeptVos;

    }

    private BigDecimal getDeptValue(Integer scale) {
        BigDecimal bigDecimal = new BigDecimal(Math.pow(10, scale)); // Math.pow(10, scale) 指数操作
        return BigDecimal.ONE.divide(bigDecimal).setScale(scale, RoundingMode.HALF_UP) ; // 1/10^n
    }


}
