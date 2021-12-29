package com.elevenchu.match.impl;

import com.elevenchu.enums.OrderDirection;
import com.elevenchu.match.MatchService;
import com.elevenchu.match.MatchServiceFactory;
import com.elevenchu.match.MatchStrategy;
import com.elevenchu.model.ExchangeTrade;
import com.elevenchu.model.MergeOrder;
import com.elevenchu.model.Order;
import com.elevenchu.model.OrderBooks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LimitPriceMatchServiceImpl implements MatchService, InitializingBean {
    /**
     * 订单的撮合交易
     * @param orderBooks
     * @param order
     */

    @Override
    public void match(OrderBooks orderBooks, Order order)
    {
        //1.进行数据的校验
        if(order.getPrice().compareTo(BigDecimal.ZERO)<=0){
            return;
        }
        //2.获取一个挂单队列
        Iterator<Map.Entry<BigDecimal, MergeOrder>> markerQueueIterator=null;
        if (order.getOrderDirection() == OrderDirection.BUY) {
            markerQueueIterator = orderBooks.getCurrentLimitPriceIterator(OrderDirection.SELL);
        } else {
            markerQueueIterator = orderBooks.getCurrentLimitPriceIterator(OrderDirection.BUY);
        }

        //是否退出循环
        boolean exitLoop=false;

        //已经完成的订单
        List<Order> completedOrders=new ArrayList<>();

        //产生的交易记录
        List<ExchangeTrade> exchangeTrades = new ArrayList<>();

        // 3 循环我们的队列
        while(markerQueueIterator.hasNext()&&!exitLoop){
            Map.Entry<BigDecimal, MergeOrder> markerOrderEntry = markerQueueIterator.next();
            BigDecimal markerPrice = markerOrderEntry.getKey();
            MergeOrder markerMergeOrder = markerOrderEntry.getValue();
            // 我花10块钱买东西 ,别人的东西如果大于10块 ,我就买不了
            if(order.getOrderDirection()==OrderDirection.BUY&&order.getPrice().compareTo(markerPrice)<0){
                break;
            }
            //我出售一个东西10 ,结果有个人花5块钱
            if(order.getOrderDirection() == OrderDirection.SELL && order.getPrice().compareTo(markerPrice) > 0){
                break;
            }
            Iterator<Order> markerIterator = markerMergeOrder.iterator();
            while(markerIterator.hasNext()){
                Order marker = markerIterator.next();
                ExchangeTrade exchangeTrade = processMath(order, marker, orderBooks);
                exchangeTrades.add(exchangeTrade);


                if(order.isCompleted()){//经过一圈的吃单,吃饱了
                    completedOrders.add(order);
                    exitLoop = true; // 退出最外层的循环
                    break;  // 退出当前的MergeOrder的循环

                }
                if(marker.isCompleted()){//MergeOrder 的一个小的订单完成了
                    completedOrders.add(marker);
                    markerIterator.remove();

                }
            }

            if(markerMergeOrder.size()==0){
                // MergeOrder 已经吃完了
                markerQueueIterator.remove(); // 将该MergeOrder 从树上移除掉
            }
        }
            //4.若订单没有完成
        if(order.getAmount().compareTo(order.getTradedAmount())>0){
            orderBooks.addOrder(order);
        }

        if (exchangeTrades.size() > 0) {
            // 5 发送交易记录
            handlerExchangeTrades(exchangeTrades);

        }
        if (completedOrders.size() > 0) {

            // 6 发送已经成交的交易记录
            completedOrders(completedOrders);
        }



    }

    private ExchangeTrade processMath(Order order, Order marker, OrderBooks orderBooks) {
        return null;
    }

    private void completedOrders(List<Order> completedOrders) {
    }

    private void handlerExchangeTrades(List<ExchangeTrade> exchangeTrades) {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MatchServiceFactory.addMatchService(MatchStrategy.LIMIT_PRICE,this);
        
    }
}
