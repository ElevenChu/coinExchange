package com.elevenchu.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "现金交易的结果")
public class CashTradeVo {

    @ApiModelProperty(value = "收款方户名")
    private String name ;

    @ApiModelProperty(value = "收款方开户行")
    private String bankName ;

    @ApiModelProperty(value = "收款方账号")
    private String bankCard ;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal amount ;

    @ApiModelProperty(value = "参考号")
    private String remark ;

    @ApiModelProperty(value = "状态")
    private Byte status ;
}