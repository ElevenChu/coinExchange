package com.elevenchu.task;

import com.elevenchu.event.TradeKLineEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TopicKLineTask {
    @Autowired
    private TradeKLineEvent tradeKLineEvent;

    /**
     * 币币交易生成一次K线
     */
    @Scheduled(fixedRate = 25000)
    public void generateKLine() {
        tradeKLineEvent.handle();
    }
}
