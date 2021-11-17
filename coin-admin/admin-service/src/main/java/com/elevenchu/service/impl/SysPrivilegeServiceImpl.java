package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.SysPrivilegeMapper;
import com.elevenchu.domain.SysPrivilege;
import com.elevenchu.service.SysPrivilegeService;
import org.springframework.util.CollectionUtils;

@Service
public class SysPrivilegeServiceImpl extends ServiceImpl<SysPrivilegeMapper, SysPrivilege> implements SysPrivilegeService{
    @Autowired
    private SysPrivilegeMapper sysPrivilegeMapper;
    @Override
    public List<SysPrivilege> getAllSysPrivilege(Long menuId, Long roleId) {


        //1 查询所有的该菜单下的权限
        List<SysPrivilege> sysPrivileges=list(new LambdaQueryWrapper<SysPrivilege>().eq(SysPrivilege::getMenuId,menuId));
        if(CollectionUtils.isEmpty(sysPrivileges)){
            return Collections.emptyList();
        }
        //2.当前传递的角色包含该权限信息也要放进去
        for (SysPrivilege sysPrivilege:sysPrivileges){
            Set<Long> currentRoleSysPrivilegeIds=sysPrivilegeMapper.getPrivilegesByRoleId(roleId);
        }


        return null;
    }
}
