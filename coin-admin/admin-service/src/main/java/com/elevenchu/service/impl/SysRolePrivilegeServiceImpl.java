package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elevenchu.domain.SysMenu;
import com.elevenchu.domain.SysPrivilege;
import com.elevenchu.model.RolePrivilegesParam;
import com.elevenchu.service.SysMenuService;
import com.elevenchu.service.SysPrivilegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.SysRolePrivilegeMapper;
import com.elevenchu.domain.SysRolePrivilege;
import com.elevenchu.service.SysRolePrivilegeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class SysRolePrivilegeServiceImpl extends ServiceImpl<SysRolePrivilegeMapper, SysRolePrivilege> implements SysRolePrivilegeService{
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysPrivilegeService sysPrivilegeService;
    @Autowired
    private SysRolePrivilegeService sysRolePrivilegeService;


    @Override
    public List<SysMenu> findSysMenuAndPrivileges(Long roleId) {
        List<SysMenu> list = sysMenuService.list();//查询所有菜单
        //在页面显示IDE是二级菜单，以及二级菜单所包含的权限
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        List<SysMenu> rootMenus = list.stream()
                .filter(sysMenu -> sysMenu.getParentId() == null)
                .collect(Collectors.toList());
        //查询所有二级权限
        List<SysMenu> subMenus=new ArrayList<>();
        for(SysMenu rootMenu:rootMenus){
            subMenus.addAll(getChildMenus(rootMenu.getId(),roleId,list));
        }


        return subMenus;
    }

    /**
     * 给角色授予权限
     * @param rolePrivilegesParam
     * @return
     */
    @Override
    @Transactional
    public boolean grantPrivileges(RolePrivilegesParam rolePrivilegesParam) {
        Long roleId = rolePrivilegesParam.getRoleId();
        //先删除之前该角色的权限
            sysRolePrivilegeService.remove(new LambdaQueryWrapper<SysRolePrivilege>().eq(SysRolePrivilege::getRoleId, roleId));
            //移除后
            List<Long> privilegesIds = rolePrivilegesParam.getPrivilegesIds();
            if(!CollectionUtils.isEmpty(privilegesIds)){
                List<SysRolePrivilege> sysRolePrivileges=new ArrayList<>();
                for (Long privilegeId:privilegesIds){
                    SysRolePrivilege sysRolePrivilege = new SysRolePrivilege();
                    sysRolePrivilege.setRoleId(rolePrivilegesParam.getRoleId());
                    sysRolePrivilege.setPrivilegeId(privilegeId);
                    sysRolePrivileges.add(sysRolePrivilege);

                }
                boolean b = sysRolePrivilegeService.saveBatch(sysRolePrivileges);
                return  b;
            }


        return true;
    }

    /**
     * 查询菜单的子菜单(递归
     * @param parentId
     * @param roleId
     * @param sources
     * @return
     */
    private List<SysMenu> getChildMenus(Long parentId, Long roleId, List<SysMenu> sources) {
     List<SysMenu> childs =new ArrayList<>();
    for(SysMenu source:sources){
        if(source.getParentId()==parentId){
            childs.add(source);
            source.setChilds(getChildMenus(source.getId(),roleId,sources));
            List<SysPrivilege> sysPrivileges=sysPrivilegeService.getAllSysPrivilege(source.getId(),roleId);
            source.setPrivileges(sysPrivileges);

        }
    }return  childs;
    }
}
