package com.elevenchu.controller;

import com.elevenchu.domain.CoinConfig;
import com.elevenchu.model.R;
import com.elevenchu.service.CoinConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "币种配置的控制器")
@RequestMapping("/coinConfigs")
public class CoinConfigController {
    @Autowired
    private CoinConfigService coinConfigService ;
    @GetMapping("/info/{coinId}")
    @ApiOperation(value = "查询币种的配置信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinId" ,value = "币种的id值")
    })
    public R<CoinConfig> getConfig(@PathVariable("coinId") Long coinId){

        CoinConfig coinConfig =  coinConfigService.findByCoinId(coinId) ;
        return R.ok(coinConfig) ;
    }


}
