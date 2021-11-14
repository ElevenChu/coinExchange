package com.elevenchu.controller;

import com.elevenchu.model.LoginResult;
import com.elevenchu.service.SysLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "登录的控制器")
public class SysLoginController {
    @Autowired
    private SysLoginService sysLoginService;
    @ApiOperation(value = "后台管理人员登录")
    @PostMapping("/login")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名称"),
            @ApiImplicitParam(name = "password", value = "用户密码")
    })
    public LoginResult login(
            @RequestParam(required = true) String username,
            @RequestParam(required = true) String password){
        return  sysLoginService.login(username,password);

    }
}
