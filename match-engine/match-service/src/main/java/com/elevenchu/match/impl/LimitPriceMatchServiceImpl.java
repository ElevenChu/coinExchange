package com.elevenchu.match.impl;

import com.elevenchu.match.MatchService;
import com.elevenchu.model.Order;
import com.elevenchu.model.OrderBooks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LimitPriceMatchServiceImpl implements MatchService {

    @Override
    public void match(OrderBooks orderBooks, Order order) {

    }
}
