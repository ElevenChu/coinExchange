package com.elevenchu.event;

import com.elevenchu.dto.CreateKLineDto;
import com.elevenchu.dto.MarketDto;
import com.elevenchu.feign.MarketServiceFeign;
import com.elevenchu.service.TradeKlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 行情数据的K线
 */
@Component
@Slf4j
public class TradeKLineEvent implements Event {


    @Autowired
    private MarketServiceFeign marketServiceFeign ;

    @Override
    public void handle() {
        List<MarketDto> marketDtos = marketServiceFeign.tradeMarkets();
        if (CollectionUtils.isEmpty(marketDtos)){
            return;
        }
        for (MarketDto marketDto : marketDtos) {
            CreateKLineDto createKLineDto = new CreateKLineDto();
            createKLineDto.setVolume(BigDecimal.ZERO);
            createKLineDto.setPrice(marketDto.getOpenPrice());
            createKLineDto.setSymbol(marketDto.getSymbol().toLowerCase());
            TradeKlineService.queue.offer(createKLineDto) ;
        }
    }
}
