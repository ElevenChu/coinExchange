package com.elevenchu.controller;

import com.elevenchu.domain.SysUser;
import com.elevenchu.model.R;
import com.elevenchu.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "后台管理系统的测试接口")
public class TestController {
    @Autowired
    private SysUserService sysUserService;

    @ApiOperation(value = "查询用户详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户的id")
    })
    @GetMapping("/user/info/{id}")
    public R<SysUser> getSysUserInfo(@PathVariable("id")Long id){
        SysUser sysUser = sysUserService.getById(id);
        return R.ok(sysUser);

    }


}
