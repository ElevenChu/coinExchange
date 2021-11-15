package com.elevenchu.service.impl;

import com.elevenchu.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.SysMenuMapper;
import com.elevenchu.domain.SysMenu;
import com.elevenchu.service.SysMenuService;
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService{
    /**
     * 通过用户ID查询用户的菜单数据
     * @param userId
     * @return
     */
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> getMenuByUserId(Long userId) {
        //1.当该用户是超级管理员时->拥有所有的菜单
        if(sysRoleService.isSuperAdmin(userId)){
            return list();
        }
        //2.当用户不是超级管理员-》查询角色-》查询菜单

        return sysMenuMapper.selectMenusByUserId(userId);
    }
}
