package com.elevenchu.controller;

import com.elevenchu.domain.Account;
import com.elevenchu.model.R;
import com.elevenchu.service.AccountService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@Api(tags = "资产服务的控制器")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @GetMapping("/{coinName}")
    public R<Account> getUserAccount(@PathVariable("coinName") String coinName){
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        Account account = accountService.findByUserAndCoin(userId, coinName);
            return R.ok(account);
    }


}
