package com.elevenchu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.Coin;
import com.elevenchu.model.R;
import com.elevenchu.service.CoinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/coins")
@Api("数字货币的数据接口")
public class CoinController {
    @Autowired
    private CoinService coinService ;

    /**
     * http://localhost:9527/finance/coins?name=xxx&type=usdt&status=1&title=xxx&wallet_type=rgb¤t=1&size=15
     * @return
     */
    @GetMapping
    @ApiOperation(value = "分页条件查询数字货币")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name" ,value = "数字货币的名称") ,
            @ApiImplicitParam(name = "type" ,value = "数字货币类型的名称") ,
            @ApiImplicitParam(name = "status" ,value = "数字货币类型的状态") ,
            @ApiImplicitParam(name = "status" ,value = "数字货币类型的标题") ,
            @ApiImplicitParam(name = "wallet_type" ,value = "数字货币钱包类型") ,
            @ApiImplicitParam(name = "current" ,value = "当前页") ,
            @ApiImplicitParam(name = "size" ,value = "每页显示的条数") ,
    })
    public R<Page<Coin>> findByPage(
            String name , String type , Byte status ,
            String title ,@RequestParam(name = "wallet_type",required = false) String walletType ,
            @ApiIgnore Page<Coin> page

    ){
        Page<Coin> coinPage =  coinService.findByPage(name,type,status,title,walletType,page) ;
        return R.ok(coinPage) ;
    }
    /**
     * 禁用或启用
     */
    @PostMapping("/setStatus")
    @ApiOperation(value = "禁用或启用币种")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coin" ,value = "coin的json数据")
    })
    public R setStatus(@RequestBody Coin coin){
        boolean updateById = coinService.updateById(coin);
        if(updateById){
            return R.ok() ;
        }
        return R.fail("设置状态失败") ;

    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "查询币种的详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "币种的id")
    })
    public R<Coin> info(@PathVariable("id")Long id){
        Coin coin = coinService.getById(id);
        return R.ok(coin);

    }

    @GetMapping("/all")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status",value = "币种当前的状态")
    })
    @ApiOperation(value = "通过状态查询所有的币种信息")
    public R<List<Coin>> getCoinAll(Byte status){
        List<Coin> coins =  coinService.getCoinsByStatus(status) ;
        return R.ok(coins) ;
    }

}
