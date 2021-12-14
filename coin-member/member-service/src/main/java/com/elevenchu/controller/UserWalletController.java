package com.elevenchu.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.UserWallet;
import com.elevenchu.model.R;
import com.elevenchu.service.UserWalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@Api(tags = "用户的提币地址")
@RequestMapping("/userWallets")
public class UserWalletController {
    @Autowired
    private UserWalletService userWalletService;

    @GetMapping
    @ApiOperation(value = "分页查询用户的提币地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户的id"),
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数")
    })
    @PreAuthorize("hasAuthority('user_wallet_query')")
    public R<Page<UserWallet>> findByPage(@ApiIgnore Page<UserWallet> page, Long userId) {
        page.addOrder(OrderItem.desc("last_update_time"));
        Page<UserWallet> userWalletPage = userWalletService.findByPage(page, userId);
        return R.ok(userWalletPage);
    }
    @GetMapping("/getCoinAddress/{coinId}")
    @ApiOperation(value = "查询用户某种币的提现地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinId" ,value = "币种的Id")
    })
    public R<List<UserWallet>> getCoinAddress(@PathVariable("coinId") Long coinId) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        List<UserWallet> userWallets = userWalletService.findUserWallets(userId, coinId);
        return R.ok(userWallets);
    }


}
