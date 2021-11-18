package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysUserService extends IService<SysUser>{


    Page<SysUser> findByPage(Page<SysUser> page, String mobile, String fullname);

    boolean addUser(SysUser sysUser);
}
