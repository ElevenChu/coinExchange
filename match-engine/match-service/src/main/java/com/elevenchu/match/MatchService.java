package com.elevenchu.match;

import com.elevenchu.model.Order;
import com.elevenchu.model.OrderBooks;

public interface MatchService {

    /**
     * 进行订单的撮合交易
     * @param order
     */
    void match(OrderBooks orderBooks, Order order) ;
}
