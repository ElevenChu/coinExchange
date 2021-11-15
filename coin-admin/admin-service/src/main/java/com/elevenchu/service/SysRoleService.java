package com.elevenchu.service;

import com.elevenchu.domain.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysRoleService extends IService<SysRole>{


    boolean isSuperAdmin(Long userId);
}
