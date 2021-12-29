package com.elevenchu.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘口数据的详情
 */
@Data
public class TradePlateItem {

    /**
     * 交易的价格
     */
    private BigDecimal price;

    /**
     * 交易的数量
     */
    private BigDecimal amount;
}
