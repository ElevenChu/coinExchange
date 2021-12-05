package com.elevenchu.feign;

import com.elevenchu.config.feign.OAuth2FeignConfig;
import com.elevenchu.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "member-service",configuration = OAuth2FeignConfig.class,path = "/users")
public interface UserServiceFeign {

    /**
     * 用于adminService 里面远程调用member-service
     */
    @GetMapping("/basic/users")
    List<UserDto> getBasicUsers(@RequestParam("ids") List<Long> ids);
}
