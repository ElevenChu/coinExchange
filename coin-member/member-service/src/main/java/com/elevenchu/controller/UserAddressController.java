package com.elevenchu.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.UserAddress;
import com.elevenchu.model.R;
import com.elevenchu.service.UserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Api(tags = "用户钱包地址")
@RequestMapping("/userAddress")
public class UserAddressController {
    @Autowired
    private UserAddressService userAddressService ;


    @GetMapping
    @ApiOperation(value = "查阅用户的钱包地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId" ,value = "用户的Id"),
            @ApiImplicitParam(name = "current" ,value = "当前页") ,
            @ApiImplicitParam(name = "size" ,value = "每页显示的条数")
    })
    public R<Page<UserAddress>> findByPage(@ApiIgnore Page<UserAddress> page , Long userId){
        page.addOrder(OrderItem.desc("last_update_time")) ;
        Page<UserAddress> userAddressPage = userAddressService.findByPage(page,userId) ;
        return R.ok(userAddressPage) ;
    }

}
