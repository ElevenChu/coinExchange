package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.config.rocket.Source;
import com.elevenchu.domain.ExchangeTrade;
import com.elevenchu.domain.Market;
import com.elevenchu.domain.TurnoverOrder;
import com.elevenchu.param.OrderParam;
import com.elevenchu.service.MarketService;
import com.elevenchu.service.TurnoverOrderService;
import com.elevenchu.vo.TradeEntrustOrderVo;
import feign.AccountServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.EntrustOrder;
import com.elevenchu.mapper.EntrustOrderMapper;
import com.elevenchu.service.EntrustOrderService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

@Service
public class EntrustOrderServiceImpl extends ServiceImpl<EntrustOrderMapper, EntrustOrder> implements EntrustOrderService {

    @Autowired
    private MarketService marketService;

    @Autowired
    private TurnoverOrderService turnoverOrderService;


    private AccountServiceFeign accountServiceFeign;

    @Autowired
    private Source source;


    @Override
    public Page<EntrustOrder> findByPage(Page<EntrustOrder> page, Long userId, String symbol, Integer type) {

        return page(page,
                new LambdaQueryWrapper<EntrustOrder>()
                        .eq(EntrustOrder::getUserId, userId)
                        .eq(!StringUtils.isEmpty(symbol), EntrustOrder::getSymbol, symbol)
                        .eq(type != null && type != 0, EntrustOrder::getType, type)
                        .orderByDesc(EntrustOrder::getCreated)

        );
    }

    @Override
    public Page<TradeEntrustOrderVo> getHistoryEntrustOrder(Page<EntrustOrder> page, String symbol, Long userId) {
        // ???????????????????????????????????????
        Page<EntrustOrder> entrustOrderPage = page(page, new LambdaQueryWrapper<EntrustOrder>()
                .eq(EntrustOrder::getUserId, userId)
                .eq(EntrustOrder::getSymbol, symbol)
        );
        Page<TradeEntrustOrderVo> tradeEntrustOrderVoPage = new Page<>(page.getCurrent(), page.getSize());
        List<EntrustOrder> entrustOrders = entrustOrderPage.getRecords();
        if (CollectionUtils.isEmpty(entrustOrders)) {
            tradeEntrustOrderVoPage.setRecords(Collections.emptyList());
        } else {
            List<TradeEntrustOrderVo> tradeEntrustOrderVos = entrustOrders2tradeEntrustOrderVos(entrustOrders);
            tradeEntrustOrderVoPage.setRecords(tradeEntrustOrderVos);
        }

        return tradeEntrustOrderVoPage;
    }

    /**
     * ?????????????????????
     * @param page
     * @param symbol
     * @param userId
     * @return
     */

    @Override
    public Page<TradeEntrustOrderVo> getEntrustOrder(Page<EntrustOrder> page, String symbol, Long userId) {
        // ???????????????????????????????????????
        Page<EntrustOrder> entrustOrderPage = page(page, new LambdaQueryWrapper<EntrustOrder>()
                .eq(EntrustOrder::getUserId, userId)
                .eq(EntrustOrder::getSymbol, symbol)
                .eq(EntrustOrder::getStatus, 0) // ???????????????
        );
        Page<TradeEntrustOrderVo> tradeEntrustOrderVoPage = new Page<>(page.getCurrent(), page.getSize());
        List<EntrustOrder> entrustOrders = entrustOrderPage.getRecords();
        if (CollectionUtils.isEmpty(entrustOrders)) {
            tradeEntrustOrderVoPage.setRecords(Collections.emptyList());
        } else {
            List<TradeEntrustOrderVo> tradeEntrustOrderVos = entrustOrders2tradeEntrustOrderVos(entrustOrders);
            tradeEntrustOrderVoPage.setRecords(tradeEntrustOrderVos);
        }
        return tradeEntrustOrderVoPage;
    }

    /**
     * ?????????????????????
     * @param userId
     * @param orderParam ??????????????????
     * @return
     */
    @Override
    public Boolean createEntrustOrder(Long userId, OrderParam orderParam) {


        // 1 ????????????
        String symbol = orderParam.getSymbol();
        Market markerBySymbol = marketService.getMarkerBySymbol(symbol);
        if (markerBySymbol == null) {
            throw new IllegalArgumentException("??????????????????????????????");
        }

        BigDecimal price = orderParam.getPrice().setScale(markerBySymbol.getPriceScale(), RoundingMode.HALF_UP);
        BigDecimal volume = orderParam.getVolume().setScale(markerBySymbol.getNumScale(), RoundingMode.HALF_UP);

        // ??????????????????
        BigDecimal mum = price.multiply(volume);

        // ?????????????????????
         BigDecimal numMax = markerBySymbol.getNumMax();
         BigDecimal numMin = markerBySymbol.getNumMin();
        if (volume.compareTo(numMax) > 0 || volume.compareTo(numMin) < 0) {
            throw new IllegalArgumentException("??????????????????????????????");
        }

        // ???????????????
        BigDecimal tradeMin = markerBySymbol.getTradeMin();
        BigDecimal tradeMax = markerBySymbol.getTradeMax();

        if (mum.compareTo(tradeMin) < 0 || mum.compareTo(tradeMax) > 0) {
            throw new IllegalArgumentException("??????????????????????????????");
        }
        // ???????????????
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal feeRate = BigDecimal.ZERO;
        Integer type = orderParam.getType();
        if (type == 1) { // ?????? buy
            feeRate = markerBySymbol.getFeeBuy();
            fee = mum.multiply(markerBySymbol.getFeeBuy());
        } else { // ?????? sell
            feeRate = markerBySymbol.getFeeSell();
            fee = mum.multiply(markerBySymbol.getFeeSell());
        }
        EntrustOrder entrustOrder = new EntrustOrder();
        entrustOrder.setUserId(userId);
        entrustOrder.setAmount(mum);
        entrustOrder.setType(orderParam.getType().byteValue());
        entrustOrder.setPrice(price);
        entrustOrder.setVolume(volume);
        entrustOrder.setFee(fee);
        entrustOrder.setCreated(new Date());
        entrustOrder.setStatus((byte) 0);
        entrustOrder.setMarketId(markerBySymbol.getId());
        entrustOrder.setMarketName(markerBySymbol.getName());
        entrustOrder.setMarketType(markerBySymbol.getType());
        entrustOrder.setSymbol(markerBySymbol.getSymbol());
        entrustOrder.setFeeRate(feeRate);
        entrustOrder.setDeal(BigDecimal.ZERO);
        entrustOrder.setFreeze(entrustOrder.getAmount().add(entrustOrder.getFee())); // ????????????

        boolean save = save(entrustOrder);
        if (save) {
            // ?????????????????????
             Long coinId = null;
            if (type == 1) { // ????????????
                coinId = markerBySymbol.getBuyCoinId();

            } else {
                coinId = markerBySymbol.getSellCoinId();
            }
            if (entrustOrder.getType() == (byte) 1) {
                accountServiceFeign.lockUserAmount(userId, coinId, entrustOrder.getFreeze(), "trade_create", entrustOrder.getId(), fee);
            }
            // ???????????????????????????
            MessageBuilder<EntrustOrder> entrustOrderMessageBuilder = MessageBuilder.withPayload(entrustOrder).setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);

            source.outputMessage().send(entrustOrderMessageBuilder.build());
        }
        return save;
    }

    /**
     * ???????????????????????????
     * @param exchangeTrade
     */
    @Override
    public void doMatch(ExchangeTrade exchangeTrade) {
        String sellOrderId = exchangeTrade.getSellOrderId();
        String buyOrderId = exchangeTrade.getBuyOrderId();
        EntrustOrder sellOrder = getById(sellOrderId);
        EntrustOrder buyOrder = getById(buyOrderId);
        Long marketId = sellOrder.getMarketId();
        Market market = marketService.getById(marketId);

        // 1 ??????????????????
        addTurnOverOrderRecord(sellOrder, buyOrder, market, exchangeTrade);
        // 2 ???????????????
        updateEntrustOrder(sellOrder, buyOrder, exchangeTrade);
        // 3 ???????????????
        rollBackAccount(sellOrder, buyOrder, exchangeTrade, market);

    }

    @Override
    public void cancleEntrustOrder(Long orderId) {
    //???????????????
     //1.??????????????????????????????????????????????????????????????????
        EntrustOrder entrustOrder = new EntrustOrder();
        entrustOrder.setStatus((byte)2);
        entrustOrder.setId(orderId);
        Message<EntrustOrder> message= MessageBuilder.withPayload(entrustOrder)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build();
        source.outputMessage().send(message);
    }

    //2?????????????????????????????????
    @Override
    public void cancleEntrustOrderToDb(String orderId) {
        if(StringUtils.hasText(orderId)){
            Long orderIdVal = Long.valueOf(orderId);
            EntrustOrder entrustOrder = getById(orderId);
            entrustOrder.setStatus((byte)2);
            updateById(entrustOrder);
        }
    }

    /**
     * ??????????????????
     * @param sellOrder
     * @param buyOrder
     * @param market
     * @param exchangeTrade
     */
    private void addTurnOverOrderRecord(EntrustOrder sellOrder, EntrustOrder buyOrder, Market market, ExchangeTrade exchangeTrade) {
        // ???????????????????????????
        TurnoverOrder sellTurnoverOrder = new TurnoverOrder();
        sellTurnoverOrder.setSellOrderId(sellOrder.getId());
        sellTurnoverOrder.setBuyCoinId(buyOrder.getId());
        sellTurnoverOrder.setBuyVolume(exchangeTrade.getAmount());
        sellTurnoverOrder.setAmount(exchangeTrade.getSellTurnover());

        sellTurnoverOrder.setBuyCoinId(market.getBuyCoinId());
        sellTurnoverOrder.setSellCoinId(market.getSellCoinId());
        sellTurnoverOrder.setCreated(new Date());
        sellTurnoverOrder.setBuyUserId(buyOrder.getUserId());
        sellTurnoverOrder.setSellUserId(sellOrder.getUserId());
        sellTurnoverOrder.setPrice(exchangeTrade.getPrice());
        sellTurnoverOrder.setBuyPrice(buyOrder.getPrice());
        sellTurnoverOrder.setTradeType(2);
        turnoverOrderService.save(sellTurnoverOrder);

        // ???????????????????????????
        TurnoverOrder buyTurnoverOrder = new TurnoverOrder();
        buyTurnoverOrder.setBuyOrderId(buyOrder.getId());
        buyTurnoverOrder.setSellOrderId(sellOrder.getId());
        buyTurnoverOrder.setAmount(exchangeTrade.getBuyTurnover());
        buyTurnoverOrder.setBuyVolume(exchangeTrade.getAmount());
        buyTurnoverOrder.setSellUserId(sellOrder.getUserId());
        buyTurnoverOrder.setBuyUserId(buyOrder.getUserId());
        buyTurnoverOrder.setSellCoinId(market.getSellCoinId());
        buyTurnoverOrder.setBuyCoinId(market.getBuyCoinId());
        buyTurnoverOrder.setCreated(new Date());
        sellTurnoverOrder.setTradeType(1);



        turnoverOrderService.save(sellTurnoverOrder);

    }
    /**
     * ?????????????????????
     *
     * @param
     */
    private void updateEntrustOrder(EntrustOrder sellOrder, EntrustOrder buyOrder, ExchangeTrade exchangeTrade) {
    /**
    * ?????????????????????
    */
        sellOrder.setDeal(exchangeTrade.getAmount());
        buyOrder.setDeal(exchangeTrade.getAmount());
        BigDecimal volume = sellOrder.getVolume(); // ????????????
        BigDecimal amount = exchangeTrade.getAmount(); // ?????????????????????

        if (amount.compareTo(volume) == 0) { // ????????????
            // ??????(????????????)
            sellOrder.setStatus((byte) 1);
        }
        BigDecimal buyOrderVolume = buyOrder.getVolume();
        if (buyOrderVolume.compareTo(volume) == 0) { // ????????????
            // ??????(????????????)
            buyOrder.setStatus((byte) 1);
        }

        // ???????????????
        updateById(sellOrder);
        updateById(buyOrder);

    }

    /**
     * ?????????????????????
     * @param sellOrder
     * @param buyOrder
     * @param exchangeTrade
     * @param market
     */
    private void rollBackAccount(EntrustOrder sellOrder, EntrustOrder buyOrder, ExchangeTrade exchangeTrade, Market market) {
        accountServiceFeign.transferBuyAmount(buyOrder.getUserId(),     // ????????????ID
                sellOrder.getUserId(),                          // ????????????ID
                market.getBuyCoinId(),                           // ??????????????????
                exchangeTrade.getBuyTurnover(),                      // ??????????????????
                "????????????",
                Long.valueOf(exchangeTrade.getBuyOrderId()));

        // ???????????????
        accountServiceFeign.transferSellAmount(sellOrder.getUserId(),    // ????????????ID
                sellOrder.getUserId(),                           // ????????????ID
                market.getSellCoinId(),                          // ??????????????????
                exchangeTrade.getSellTurnover(),                                      // ??????????????????
                "????????????",                        // ???????????????????????????????????????
                Long.valueOf(exchangeTrade.getSellOrderId()));                         // ????????????ID


    }


    private List<TradeEntrustOrderVo> entrustOrders2tradeEntrustOrderVos(List<EntrustOrder> entrustOrders) {
        List<TradeEntrustOrderVo> tradeEntrustOrderVos = new ArrayList<>(entrustOrders.size());
        for (EntrustOrder entrustOrder : entrustOrders) {
            tradeEntrustOrderVos.add(entrustOrder2TradeEntrustOrderVo(entrustOrder));
        }
        return tradeEntrustOrderVos;

    }

    private TradeEntrustOrderVo entrustOrder2TradeEntrustOrderVo(EntrustOrder entrustOrder) {

        TradeEntrustOrderVo tradeEntrustOrderVo = new TradeEntrustOrderVo();
        tradeEntrustOrderVo.setOrderId(entrustOrder.getId());
        tradeEntrustOrderVo.setCreated(entrustOrder.getCreated());
        tradeEntrustOrderVo.setStatus(entrustOrder.getStatus().intValue());
        tradeEntrustOrderVo.setAmount(entrustOrder.getAmount());
        tradeEntrustOrderVo.setDealVolume(entrustOrder.getDeal());
        tradeEntrustOrderVo.setPrice(entrustOrder.getPrice());
        tradeEntrustOrderVo.setVolume(entrustOrder.getVolume());

        tradeEntrustOrderVo.setType(entrustOrder.getType().intValue()); //1-?????????2-??????
        // ???????????????????????????
        BigDecimal dealAmount = BigDecimal.ZERO;
        BigDecimal dealVolume = BigDecimal.ZERO;
        if (tradeEntrustOrderVo.getType() == 1) {
            List<TurnoverOrder> buyTurnoverOrders = turnoverOrderService.getBuyTurnoverOrder(entrustOrder.getId(), entrustOrder.getUserId());
            if (!CollectionUtils.isEmpty(buyTurnoverOrders)) {
                for (TurnoverOrder buyTurnoverOrder : buyTurnoverOrders) {
                    BigDecimal amount = buyTurnoverOrder.getAmount();
                    dealAmount = dealAmount.add(amount);
                }
            }

        }
        if (tradeEntrustOrderVo.getType() == 2) {
            List<TurnoverOrder> sellTurnoverOrders = turnoverOrderService.getSellTurnoverOrder(entrustOrder.getId(), entrustOrder.getUserId());
            if (!CollectionUtils.isEmpty(sellTurnoverOrders)) {
                for (TurnoverOrder sellTurnoverOrder : sellTurnoverOrders) {
                    BigDecimal amount = sellTurnoverOrder.getAmount();
                    dealAmount = dealAmount.add(amount);
                }
            }
        }
        // ??????????????????
        tradeEntrustOrderVo.setDealAmount(dealAmount); // ?????????????????????(???)
        tradeEntrustOrderVo.setDealVolume(entrustOrder.getDeal()); // ???????????????
        BigDecimal dealAvgPrice = BigDecimal.ZERO;
        if (dealAmount.compareTo(BigDecimal.ZERO) > 0) {
            dealAvgPrice = dealAmount.divide(entrustOrder.getDeal(), 8, RoundingMode.HALF_UP);
        }
        tradeEntrustOrderVo.setDealAvgPrice(dealAvgPrice); // ?????????????????????
        return tradeEntrustOrderVo;
    }
}