package com.elevenchu.service;

import com.elevenchu.domain.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysMenuService extends IService<SysMenu>{

    //查询用户菜单数据
     List<SysMenu> getMenuByUserId(Long userId);

}
