package com.elevenchu.RocketMqConfig;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding(value = Source.class)
public class RocketMqConfig {
    
}