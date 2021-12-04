package com.elevenchu.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
@Slf4j
public class OAuth2FeignConfig implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate requestTemplate) {
        //可以从request上下文环境里面获取token
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes==null){
            log.info("没有请求的上下文，无法进行token的传递");
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);//获取请求上下文的头里面的AUTHORIZATION
        if(!StringUtils.isEmpty(header)){
            requestTemplate.header(HttpHeaders.AUTHORIZATION,header);
            log.info("本次的token传递成功，token的值为{}",header);
        }

    }
}
