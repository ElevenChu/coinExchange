package com.elevenchu.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.SysUserLog;
import com.elevenchu.model.R;
import com.elevenchu.service.SysUserLogService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Api(tags = "用户的操作记录查询")
@RequestMapping("/sysUserLog")
public class SysUserLogController {
    @Autowired
    private SysUserLogService sysUserLogService;

    public R<Page<SysUserLog>> findByPage(@ApiIgnore Page<SysUserLog> page){
        page.addOrder(OrderItem.desc("created"));
        return R.ok(sysUserLogService.page(page));
    }
}
