package com.elevenchu.model;

import com.elevenchu.enums.OrderDirection;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedList;

@Data
public class TradePlate {

    /**
     * 交易的盘口数据,以后前端可以查询该数据
     */



        /**
         * 判断数据的详情
         */
        private LinkedList<TradePlateItem> items;
        /**
         * 最大支持的深度
         */
        private int maxDepth = 100;

        /**
         * 订单的方向
         */
        private OrderDirection direction;

        /**
         * 交易对
         */
        private String symbol;

    public TradePlate(String symbol, OrderDirection direction) {
        this.symbol = symbol;
        this.direction = direction;
    }

        public void remove(Order marker, BigDecimal tradedAmount) {
        }
    }



