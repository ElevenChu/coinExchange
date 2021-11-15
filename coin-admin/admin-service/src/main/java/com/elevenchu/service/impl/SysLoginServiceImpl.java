package com.elevenchu.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.enums.ApiErrorCode;
import com.baomidou.mybatisplus.extension.exceptions.ApiException;
import com.elevenchu.domain.SysMenu;
import com.elevenchu.feign.JwtToken;
import com.elevenchu.feign.OAuth2FeignClient;
import com.elevenchu.model.LoginResult;
import com.elevenchu.service.SysLoginService;
import com.elevenchu.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysLoginServiceImpl implements SysLoginService {
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private OAuth2FeignClient oAuth2FeignClient;
    @Value("${basic.token:Basic Y29pbi1hcGk6Y29pbi1zZWNyZXQ=}")
    private String basicToken ;
    @Override
    public LoginResult login(String username, String password) {
      log.info("用户{}开始登录",username);
      //1.获取Token需要远程调用Authorization-server服务
        ResponseEntity<JwtToken> tokenResponseEntity = oAuth2FeignClient.getToken("password", username, password, "admin_type", basicToken);
        if(tokenResponseEntity.getStatusCode()!= HttpStatus.OK){
            throw new ApiException(ApiErrorCode.FAILED);
        }
        JwtToken jwtToken = tokenResponseEntity.getBody();
        log.info("远程调用授权服务器成功，获取的token为{}", JSON.toJSONString(jwtToken,true));
        String token=jwtToken.getAccessToken();
        //2.查询菜单数据
        Jwt jwt = JwtHelper.decode(token);
        String jwtJsonStr = jwt.getClaims();
        JSONObject jwtJson = JSON.parseObject(jwtJsonStr);
        Long userId = Long.valueOf(jwtJson.getString("user_name"));
        List<SysMenu> menus= sysMenuService.getMenuByUserId(userId);
        //3.权限数据-jwt里面包含权限数据
        JSONArray authoritiesJsonArray = jwtJson.getJSONArray("authorities");
        List<SimpleGrantedAuthority> authorities = authoritiesJsonArray.stream()//组装我们的权限数据
                .map(authorityJson -> new SimpleGrantedAuthority(authorityJson.toString()))
                .collect(Collectors.toList());

       return new LoginResult(token,menus,authorities);







    }
}
