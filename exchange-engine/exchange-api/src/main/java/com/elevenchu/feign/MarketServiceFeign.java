package com.elevenchu.feign;

import com.elevenchu.config.feign.OAuth2FeignConfig;
import com.elevenchu.dto.MarketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "exchange-service", contextId = "marketServiceFeign", configuration = OAuth2FeignConfig.class, path = "/markets")
public interface MarketServiceFeign {

    /**
     * 使用报价货币 以及 出售的货币的iD
     *
     * @param buyCoinId
     * @return
     */
    @GetMapping("/getMarket")
    MarketDto findByCoinId(@RequestParam("buyCoinId") Long buyCoinId, @RequestParam("sellCoinId") Long sellCoinId);

    /**
     * 查询所有的交易市场
     * @return
     */
    @GetMapping("/tradeMarkets")
    List<MarketDto> tradeMarkets();


    /**
     * 查询该交易对下的盘口数据
     * @param symbol
     * @param value
     * @return
     */
    @GetMapping("/depthData/{symbol}/{type}")
    String depthData(@PathVariable("symbol") String symbol,@PathVariable("type") int value);
}
