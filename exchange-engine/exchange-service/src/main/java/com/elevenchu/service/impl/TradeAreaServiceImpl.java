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
        //1.???????????????????????????
        List<TradeArea> tradeAreas = list(new LambdaQueryWrapper<TradeArea>().eq(TradeArea::getStatus, 1).orderByAsc(TradeArea::getSort));
        if(CollectionUtils.isEmpty(tradeAreas)){
            return Collections.emptyList();
        }
        ArrayList<TradeAreaMarketVo> tradeAreaMarketVos = new ArrayList<>();
        for(TradeArea tradeArea:tradeAreas){
            //2.?????????????????????????????????
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
        //????????????TradeAreaMarketVo
        TradeAreaMarketVo tradeAreaMarketVo=new TradeAreaMarketVo();
        tradeAreaMarketVo.setAreaName("??????");
        List<Market> markets = marketService.listByIds(marketIds);
        List<TradeMarketVo> tradeMarketVos = market2marketVos(markets);
        tradeAreaMarketVo.setMarkets(tradeMarketVos);
        return Arrays.asList(tradeAreaMarketVo);



    }

    private List<TradeMarketVo> market2marketVos(List<Market> markets) {
        return markets.stream().map(market -> toConvertVo(market)).collect(Collectors.toList());


    }
    /**
     * ???market ?????????TradeMarketVo
     *
     * @param market
     * @return
     */
    private TradeMarketVo toConvertVo(Market market) {
        TradeMarketVo tradeMarketVo = new TradeMarketVo();
        tradeMarketVo.setImage(market.getImg()); // ?????????????????????
        tradeMarketVo.setName(market.getName());
        tradeMarketVo.setSymbol(market.getSymbol());


        //TODO???????????????
        // ???????????????
        tradeMarketVo.setHigh(market.getOpenPrice()); // OpenPrice ????????????
        tradeMarketVo.setLow(market.getOpenPrice()); // ??????K?????????
        tradeMarketVo.setPrice(market.getOpenPrice()); // ??????K?????????
        tradeMarketVo.setCnyPrice(market.getOpenPrice()); // ????????????
        tradeMarketVo.setCnyPrice(market.getOpenPrice()); // ????????????
        tradeMarketVo.setPriceScale(market.getPriceScale()); // ????????????????????????

        // ??????????????????
       Long buyCoinId = market.getBuyCoinId();
        List<CoinDto> coins = coinServiceFeign.findCoins(Arrays.asList(buyCoinId));

        if (CollectionUtils.isEmpty(coins) || coins.size() > 1) {
            throw new IllegalArgumentException("??????????????????");
        }
        CoinDto coinDto = coins.get(0);
        tradeMarketVo.setPriceUnit(coinDto.getName()); // ?????????????????????

        // ???????????????
        tradeMarketVo.setTradeMin(market.getTradeMin());
        tradeMarketVo.setTradeMax(market.getTradeMax());

        // ?????????????????????
        tradeMarketVo.setNumMin(market.getNumMin());
        tradeMarketVo.setNumMax(market.getNumMax());

        // ??????????????????
        tradeMarketVo.setSellFeeRate(market.getFeeSell());
        tradeMarketVo.setBuyFeeRate(market.getFeeBuy());

        // ????????????????????????
        tradeMarketVo.setNumScale(market.getNumScale());


        //  ??????
        tradeMarketVo.setSort(market.getSort());

        // ???????????????
        tradeMarketVo.setVolume(BigDecimal.ZERO); // // ??????????????????
        tradeMarketVo.setAmount(BigDecimal.ZERO); // ?????????????????????


        // ????????????
        tradeMarketVo.setChange(0.00);

        // ?????????????????????
        tradeMarketVo.setMergeDepth(getMergeDepths(market.getMergeDepth()));

        return tradeMarketVo;

    }

    private List<MergeDeptVo> getMergeDepths(String mergeDepth) {
        String[] split = mergeDepth.split(",");
        if (split.length != 3) {
            throw new IllegalArgumentException("?????????????????????");

        }
        //  6(1/100000),5(100000),4 (10000)
        // ????????????
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
        BigDecimal bigDecimal = new BigDecimal(Math.pow(10, scale)); // Math.pow(10, scale) ????????????
        return BigDecimal.ONE.divide(bigDecimal).setScale(scale, RoundingMode.HALF_UP) ; // 1/10^n
    }


}
