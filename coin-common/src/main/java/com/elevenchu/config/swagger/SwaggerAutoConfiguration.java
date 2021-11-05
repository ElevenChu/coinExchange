package com.elevenchu.config.swagger;

import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration

public class SwaggerAutoConfiguration {
 public Docket docket(){
     Docket docket = new Docket(DocumentationType.SWAGGER_2)
             .apiInfo(apiInfo())
             .select()
             .apis(RequestHandlerSelectors.basePackage("com.elevenchu.controller"))
             .paths(PathSelectors.any())
             .build();
    return docket;
 }

    private ApiInfo apiInfo() {
 return    null;
 }
}
