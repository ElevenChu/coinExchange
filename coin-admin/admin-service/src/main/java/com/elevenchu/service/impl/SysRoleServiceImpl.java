package com.elevenchu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.SysRoleMapper;
import com.elevenchu.domain.SysRole;
import com.elevenchu.service.SysRoleService;
import org.springframework.util.StringUtils;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService{
    @Autowired
    private SysRoleMapper sysRoleMapper;
    /**
     * 判断一个用户是否为超级管理员
     * @param userId
     * @return
     */
    @Override
    public boolean isSuperAdmin(Long userId) {
        //当用户的code为ROLE_ADMIN时 该用户为超级的管理员
        //用户ID->用户的角色-》该角色的Code是否为Role_ADMIN
        String roleCode=sysRoleMapper.getUserRoleCode(userId);
        if(!StringUtils.isEmpty(roleCode)&&roleCode.equals("ROLE_ADMIN")){
            return  true;
        }


        return false;
    }
}
