package com.elevenchu.service.impl;

import com.elevenchu.feign.JwtToken;
import com.elevenchu.feign.OAuth2FeignClient;
import com.elevenchu.geetest.GeetestLib;
import com.elevenchu.model.LoginForm;
import com.elevenchu.model.LoginUser;
import com.elevenchu.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {


    @Autowired
    private OAuth2FeignClient oAuth2FeignClient;

    @Value("${basic.token:Basic Y29pbi1hcGk6Y29pbi1zZWNyZXQ=}")
    private String basicToken;

    @Autowired
    private StringRedisTemplate strRedisTemplate;

    @Autowired
    private GeetestLib geetestLib;
    @Override
    public LoginUser login(LoginForm loginForm) {
        log.info("用户{}开始登录",loginForm.getUsername());
        checkForData(loginForm);
        LoginUser loginUser=null;
        ResponseEntity<JwtToken> tokenResponseEntity = oAuth2FeignClient.getToken("password", loginForm.getUsername(), loginForm.getPassword(), "member_type", basicToken);
        if(tokenResponseEntity.getStatusCode()== HttpStatus.OK){
            JwtToken jwtToken = tokenResponseEntity.getBody();
            log.info("远程调用成功");
            loginUser=new LoginUser(loginForm.getUsername(),jwtToken.getExpiresIn(),jwtToken.getTokenType()+" "+jwtToken.getAccessToken(),jwtToken.getRefreshToken());
            //使用网关解决登出问题
            //token是直接存储的
            strRedisTemplate.opsForValue().set(jwtToken.getAccessToken(),"",jwtToken.getExpiresIn(), TimeUnit.SECONDS);
        }

        return loginUser;
    }


    /**
     * 校验表单数据
     * @param loginForm
     */

    private void checkForData(LoginForm loginForm) {

    }
}
