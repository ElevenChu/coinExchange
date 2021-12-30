package com.elevenchu.config.rocket;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;

/**
 * 数据的接收
 */
public interface Sink {

    /**
     * 交易数据的输入
     * @return
     */
    @Input("exchange_trade_in")
    MessageChannel exchangeTradeIn() ;




}
