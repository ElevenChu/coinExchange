package com.elevenchu.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel("明细")
public class DepthItemVo {

    @ApiModelProperty(value = "价格")
    private BigDecimal price=BigDecimal.ZERO;

    @ApiModelProperty(value = "数量")
    private BigDecimal volume=BigDecimal.ZERO;
}
