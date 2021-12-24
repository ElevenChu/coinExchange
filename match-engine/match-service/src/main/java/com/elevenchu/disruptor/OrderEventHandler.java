package com.elevenchu.disruptor;

import com.elevenchu.model.OrderBooks;
import com.lmax.disruptor.EventHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 该对象 有多个: 和Symbol的数据对应
 * 针对某一个OrderEventHandler ,只会同一时间有一个线程来执行它
 */
@Data
@Slf4j
public class OrderEventHandler implements EventHandler<OrderEvent> {
    private OrderBooks orderBooks;

    private String symbol ;

    public OrderEventHandler(OrderBooks orderBooks) {
        this.orderBooks = orderBooks;
        this.symbol =  this.orderBooks.getSymbol() ;
    }

    /**
     * 接收到了某个消息
     *
     * @param event
     * @param sequence
     * @param endOfBatch
     * @throws Exception
     */
    @Override
    public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws Exception {

    }
}
