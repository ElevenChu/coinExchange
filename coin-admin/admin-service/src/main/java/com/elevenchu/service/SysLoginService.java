package com.elevenchu.service;

import com.elevenchu.model.LoginResult;

/**
 * 登录的接口
 */
public interface SysLoginService {
    LoginResult login(String username,String password);
}
