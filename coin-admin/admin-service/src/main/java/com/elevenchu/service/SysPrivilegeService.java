package com.elevenchu.service;

import com.elevenchu.domain.SysPrivilege;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysPrivilegeService extends IService<SysPrivilege>{

    /**
     *获取该菜单下所有的权限
     * @param menuId 菜单ID
     * @param roleId 代表当前查询角色的ID
     * @return
     */
    List<SysPrivilege> getAllSysPrivilege(Long menuId, Long roleId);
}
