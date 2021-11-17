package com.elevenchu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elevenchu.domain.SysPrivilege;

import java.util.Set;

public interface SysPrivilegeMapper extends BaseMapper<SysPrivilege> {
    Set<Long> getPrivilegesByRoleId(Long roleId);
}