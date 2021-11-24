package com.elevenchu.controller;

import com.elevenchu.model.LoginForm;
import com.elevenchu.model.LoginUser;
import com.elevenchu.model.R;
import com.elevenchu.service.LoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "登录控制器")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @RequestMapping("/login")
    @ApiOperation("会员的登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginForm",value = "登录的表单参数")
    })

    public R<LoginUser> login(@RequestBody @Validated LoginForm loginForm){
        LoginUser loginUser=loginService.login(loginForm);
        return R.ok(loginUser);
    }


}
