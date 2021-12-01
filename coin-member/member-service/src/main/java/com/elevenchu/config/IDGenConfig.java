package com.elevenchu.config;

import cn.hutool.core.lang.Snowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IDGenConfig {
    @Value("${id.machine:0}")
    private int machineCode;
    @Value("${id.app:0}")
    private int appCode;
    @Bean
    public Snowflake snowflake(){
        Snowflake snowflake=new Snowflake(appCode,machineCode);
        return snowflake;
    }
}
