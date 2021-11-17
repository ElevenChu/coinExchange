package com.elevenchu.service;

import com.elevenchu.domain.SysMenu;
import com.elevenchu.domain.SysRolePrivilege;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysRolePrivilegeService extends IService<SysRolePrivilege>{


    List<SysMenu> findSysMenuAndPrivileges(Long roleId);
}
