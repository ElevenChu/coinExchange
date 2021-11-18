package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.SysUserRole;
import com.elevenchu.service.SysUserRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.SysUser;
import com.elevenchu.mapper.SysUserMapper;
import com.elevenchu.service.SysUserService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService{
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Override
    public Page<SysUser> findByPage(Page<SysUser> page, String mobile, String fullname) {

        Page<SysUser> pageData=page(page,new LambdaQueryWrapper<SysUser>()
                        .like(!StringUtils.isEmpty(mobile),SysUser::getMobile,mobile)
                        .like(!StringUtils.isEmpty(fullname),SysUser::getFullname,fullname)
        );
        List<SysUser> records = pageData.getRecords();
        if(!CollectionUtils.isEmpty(records)){
            for (SysUser record : records) {
                List<SysUserRole> userRoles = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, record.getId()));
                if(!CollectionUtils.isEmpty(userRoles)){
                    record.setRole_strings(
                            userRoles.stream().
                                    map(sysUserRole -> sysUserRole.getRoleId().toString())
                                    .collect(Collectors.joining(",")));
                }
            }
        }
        return pageData;
    }
    @Override
    @Transactional
    public boolean addUser(SysUser sysUser) {
        //1.用户的密码-》加密
        String password = sysUser.getPassword();
        //用户的角色Ids
        String role_strings = sysUser.getRole_strings();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(password);//加密密码
        sysUser.setPassword(encode);
        boolean save = super.save(sysUser);
        if(save){
            //给用户新增角色数据
            if (StringUtils.isEmpty(role_strings)){
                String[] roleIds = role_strings.split(",");
                List<SysUserRole> sysUserRoleList=new ArrayList<>();
                for (String roleId:roleIds){
                    SysUserRole sysUserRole = new SysUserRole();
                    sysUserRole.setRoleId(Long.valueOf(roleId));
                    sysUserRole.setUserId(sysUser.getId());
                    sysUserRoleList.add(sysUserRole);
                }
                sysUserRoleService.saveBatch(sysUserRoleList);
            }
        }

        return save;
    }
}
