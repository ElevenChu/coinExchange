package com.elevenchu.rocket;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

/**
 * 开启Stream的开发
 */
@Configuration
@EnableBinding(value = {Sink.class,Source.class})
public class RocketStreamConfig {


}
