package com.elevenchu.service.impl;

import com.elevenchu.domain.SysMenu;
import com.elevenchu.feign.OAuth2FeignClient;
import com.elevenchu.model.LoginResult;
import com.elevenchu.service.SysLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SysLoginServiceImpl implements SysLoginService {
    @Autowired
    private OAuth2FeignClient oAuth2FeignClient;
    @Override
    public LoginResult login(String username, String password) {
      log.info("用户{}开始登录",username);
       String token="";
       List<SysMenu> menus=null;
       List<SimpleGrantedAuthority> authorities=null;
       return new LoginResult(token,menus,authorities);







    }
}
