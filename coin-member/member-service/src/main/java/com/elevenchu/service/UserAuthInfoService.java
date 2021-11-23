package com.elevenchu.service;

import com.elevenchu.domain.UserAuthInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserAuthInfoService extends IService<UserAuthInfo>{

    /**
     * 通过认证的code 来查询用户的认证详情列表
     * @param authCode
     * @return
     */
    List<UserAuthInfo> getUserAuthInfoByCode(Long authCode);

    /**
     * 用户未被认证，我们需要通过用户的ID 查询用户的认证列表
     * @param id
     * @return
     */
    List<UserAuthInfo> getUserAuthInfoByUserId(Long id);
}
