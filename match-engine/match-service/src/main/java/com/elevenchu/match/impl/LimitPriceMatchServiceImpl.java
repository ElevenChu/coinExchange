package com.elevenchu.match.impl;

import com.elevenchu.match.MatchService;
import com.elevenchu.match.MatchServiceFactory;
import com.elevenchu.match.MatchStrategy;
import com.elevenchu.model.Order;
import com.elevenchu.model.OrderBooks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LimitPriceMatchServiceImpl implements MatchService, InitializingBean {


    @Override
    public void match(OrderBooks orderBooks, Order order) {
    orderBooks.addOrder(order);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MatchServiceFactory.addMatchService(MatchStrategy.LIMIT_PRICE,this);
        
    }
}
