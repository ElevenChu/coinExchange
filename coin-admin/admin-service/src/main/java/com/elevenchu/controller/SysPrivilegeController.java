package com.elevenchu.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.SysPrivilege;
import com.elevenchu.model.R;
import com.elevenchu.service.SysPrivilegeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/privileges")
@Api("权限的管理")
public class SysPrivilegeController {
    @Autowired
    private SysPrivilegeService sysPrivilegeService;

    /**
     * 权限数据的分页查询
     * @param page
     * @return
     */
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current",value = "当前页"),
            @ApiImplicitParam(name="size",value = "每页显示的大小"),
    })
    @PreAuthorize("hasAuthority('sys_privilege_query')")
    public R<Page<SysPrivilege>> findByPage(@ApiIgnore Page<SysPrivilege> page){
        //查询时我们将最近新增的，修改的数据优先展示-》排序-》lastUpdateTime
        page.addOrder(OrderItem.desc("last_update_time"));
        Page<SysPrivilege> sysPrivilegePage= sysPrivilegeService.page(page);
        return R.ok(sysPrivilegePage);

    }

}
