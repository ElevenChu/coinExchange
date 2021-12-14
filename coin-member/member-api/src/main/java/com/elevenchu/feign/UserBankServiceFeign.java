package com.elevenchu.feign;

import com.elevenchu.config.feign.OAuth2FeignConfig;
import com.elevenchu.dto.UserBankDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service",contextId = "userBankServiceFeign",configuration = OAuth2FeignConfig.class,path = "/userBanks")
public interface UserBankServiceFeign {

     @GetMapping("/{userId}/info")
    UserBankDto getUserBankInfo(@PathVariable Long userId);
}
