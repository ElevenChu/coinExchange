package com.elevenchu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.Market;
import com.elevenchu.dto.MarketDto;
import com.elevenchu.feign.MarketServiceFeign;
import com.elevenchu.model.R;
import com.elevenchu.service.MarketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/markets")
@Api(tags = "交易市场的控制器")
public class MarketController implements MarketServiceFeign {
    @Autowired
    private MarketService marketService;


    @GetMapping
    @ApiOperation(value = "交易市场的分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数")
    })
    @PreAuthorize("hasAuthority('trade_market_query')")
    public R<Page<Market>> findByPage(@ApiIgnore Page<Market> page, Long tradeAreaId, Byte status) {
        Page<Market> pageData = marketService.findByPage(page, tradeAreaId, status);
        return R.ok(pageData);
    }

    @PostMapping("/setStatus")
    @ApiOperation(value = "启用/禁用交易市场")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "market", value = "market的json数据")
    })
    @PreAuthorize("hasAuthority('trade_market_update')")
    public R setStatus(@RequestBody Market market){

        boolean updateById = marketService.updateById(market);
        if (updateById) {

            return R.ok();
        }
        return R.fail("状态设置失败");
    }

    @PostMapping
    @ApiOperation(value = "新增一个市场")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "market", value = "market json")
    })
    @PreAuthorize("hasAuthority('trade_market_create')")
    public R save(@RequestBody Market market) {
        boolean save = marketService.save(market);
        if (save) {
            return R.ok();
        }
        return R.fail("新增失败");
    }

    @PatchMapping
    @ApiOperation(value = "修改一个市场")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "market", value = "marketjson")
    })
    @PreAuthorize("hasAuthority('trade_market_update')")
    public R update(@RequestBody Market market) {
        boolean updateById = marketService.updateById(market);
        if (updateById) {
            return R.ok();
        }
        return R.fail("修改失败");
    }



    @GetMapping("/all")
    @ApiOperation(value = "查询所有的交易市场")
    public R<List<Market>> listMarks() {
        return R.ok(marketService.list());
    }


    @Override
    public MarketDto findByCoinId(Long buyCoinId, Long sellCoinId) {
    MarketDto marketDto= marketService.findByCoinId(buyCoinId,sellCoinId);

        return marketDto;
    }
}
