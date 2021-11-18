package com.elevenchu.service;

import com.elevenchu.domain.SysMenu;
import com.elevenchu.domain.SysRolePrivilege;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elevenchu.model.RolePrivilegesParam;

import java.util.List;

public interface SysRolePrivilegeService extends IService<SysRolePrivilege>{

    //查询角色权限
    List<SysMenu> findSysMenuAndPrivileges(Long roleId);
    //给角色授予权限
    boolean grantPrivileges(RolePrivilegesParam rolePrivilegesParam);
}
