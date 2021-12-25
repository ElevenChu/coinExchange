package com.elevenchu.match;

import com.elevenchu.disruptor.OrderEvent;
import com.elevenchu.disruptor.OrderEventHandler;
import com.elevenchu.model.OrderBooks;
import com.lmax.disruptor.EventHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(value = MatchEngineProperties.class)
public class MatchEngineAutoConfiguration {

    private MatchEngineProperties matchEngineProperties;


    public MatchEngineAutoConfiguration(MatchEngineProperties matchEngineProperties) {
        this.matchEngineProperties = matchEngineProperties; }

    /**
     * 交易对的加载
     * @return
     */

   @Bean("eventHandlers")
    public EventHandler<OrderEvent>[] eventHandlers(){
       Map<String, MatchEngineProperties.CoinScale> symbols = matchEngineProperties.getSymbols();
       Set<Map.Entry<String, MatchEngineProperties.CoinScale>> entries = symbols.entrySet();
       EventHandler<OrderEvent>[] eventHandlers = new EventHandler[symbols.size()];
       for (Map.Entry<String, MatchEngineProperties.CoinScale> entry : entries) {
           String symbol = entry.getKey();
           MatchEngineProperties.CoinScale value = entry.getValue();
           OrderBooks orderBooks=null;
           int i=0;
           if(value!=null){
               orderBooks=new OrderBooks(symbol,value.getCoinScale(),value.getBaseCoinScale());
           }else{
               orderBooks=new OrderBooks(symbol);
           }
           eventHandlers[i++]=new OrderEventHandler(orderBooks);

       }
       return eventHandlers;
   }




}