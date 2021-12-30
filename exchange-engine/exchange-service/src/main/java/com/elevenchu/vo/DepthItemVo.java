package com.elevenchu.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel("深度盘口数据")
public class DepthItemVo {

    /**
     * 委托买单
     */
    @ApiModelProperty(value = "委托买单")
    private List<com.elevenchu.domain.DepthItemVo> bids = Collections.emptyList() ;

    /**
     * 委托卖单
     */
    @ApiModelProperty(value = "委托卖单")
    private List<com.elevenchu.domain.DepthItemVo> asks = Collections.emptyList() ;

    /**
     * 当前成交价(GCN)
     */
    @ApiModelProperty(value = "当前成交价GCN")
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * 当前成交价对应CNY价格
     */
    @ApiModelProperty(value = "当前成交价CNY")
    private BigDecimal cnyPrice =  BigDecimal.ZERO;

}
