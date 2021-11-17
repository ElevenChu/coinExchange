package com.elevenchu.controller;

import com.elevenchu.domain.SysMenu;
import com.elevenchu.domain.SysRolePrivilege;
import com.elevenchu.model.R;
import com.elevenchu.service.SysRolePrivilegeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Api("角色权限的配置")
public class SysRolePrivilegeController {
    @Autowired
    private SysRolePrivilegeService sysRolePrivilegeService;


    public R<List<SysMenu>> findSysMenuAndPrivileges(Long roleId){
      List<SysMenu> sysMenus = sysRolePrivilegeService.findSysMenuAndPrivileges(roleId);
      return R.ok(sysMenus);


    }

}
