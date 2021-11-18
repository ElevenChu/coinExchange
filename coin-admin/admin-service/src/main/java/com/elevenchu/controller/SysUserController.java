package com.elevenchu.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.SysUser;
import com.elevenchu.model.R;
import com.elevenchu.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;

@RestController
@Api(tags = "用户的CRUD")
@RequestMapping("/users")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current" ,value = "当前页") ,
            @ApiImplicitParam(name = "size" ,value = "每页显示的条数") ,
            @ApiImplicitParam(name = "mobile" ,value = "员工的手机号码") ,
            @ApiImplicitParam(name = "fullname" ,value = "员工的全名称") ,
    })
    @PreAuthorize("hasAuthority('sys_user_query')")
    public R<Page<SysUser>> findByPage(@ApiIgnore Page<SysUser>page, String mobile, String fullname){
        page.addOrder(OrderItem.desc("last_update_time"));
        Page<SysUser> pageData =  sysUserService.findByPage(page,mobile ,fullname) ;
        return R.ok(pageData) ;
    }

    @PostMapping
    @ApiOperation("新增员工")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sysUser",value = "sysUser的JSON数据")
    })
    public R addUser(@RequestBody SysUser sysUser){
        Long userId =Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());

        sysUser.setCreateBy(userId);
        boolean isOK=  sysUserService.addUser(sysUser);
        if(isOK){
            return R.ok();
        }
        return R.fail("新增失败");
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除用户")
    public R deleteUser(@RequestBody Long[] ids){
        boolean b = sysUserService.removeByIds(Arrays.asList(ids));
        if(b){
            return R.ok();
        }
        return  R.fail("删除失败");

    }

    

}
