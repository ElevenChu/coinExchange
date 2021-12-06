package com.elevenchu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.AdminAddress;
import com.elevenchu.model.R;
import com.elevenchu.service.AdminAddressService;
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
@RequestMapping("/adminAddress")
@Api(tags = "归集地址的控制器")
public class AdminAddressController {
    @Autowired
    private AdminAddressService adminAddressService;

    @GetMapping
    @ApiOperation(value = "查询归集地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coinId", value = "币种的id"),
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
    })
    public R<Page<AdminAddress>> findByPage(@ApiIgnore Page<AdminAddress> page, Long coinId) {
        Page<AdminAddress> adminAddressPage = adminAddressService.findByPage(page, coinId);
        return R.ok(adminAddressPage);
    }

}
