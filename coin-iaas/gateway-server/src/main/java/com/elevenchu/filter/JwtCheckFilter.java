package com.elevenchu.filter;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class JwtCheckFilter implements GlobalFilter, Ordered {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Value("${no.require.urls:/admin/login,/user/gt/register,/user/login}")
    private Set<String> noRequireTokenUris;




    /**
     * 拦截器拦截到用户的请求时该做的
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.判断该接口是否需要Token
        if(!isRequireToken(exchange)){
            return chain.filter(exchange); //不需要则放行
        }

        //2.取出用户的Token
        String token=getUserToken(exchange);

        //3.判断用户Token是否有效
        if(StringUtils.isEmpty(token)){
            return buildeNoAuthorizationResult(exchange);
        }

        Boolean hasKey = redisTemplate.hasKey(token);
        if(hasKey!=null&&hasKey){
            return chain.filter(exchange); //token有效则放行
        }

        return buildeNoAuthorizationResult(exchange);
    }


    //给用户响应一个没有Token的错误
    private Mono<Void> buildeNoAuthorizationResult(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set("Content-Type","application/json");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error","NoAuthorization");
        jsonObject.put("errorMsg","Token is null or error");
        DataBuffer wrap = response.bufferFactory().wrap(jsonObject.toJSONString().getBytes());
        return response.writeWith(Flux.just(wrap));


    }

    /**
     * 从请求头中获取用户Token
     * @param exchange
     * @return
     */
    private String getUserToken(ServerWebExchange exchange) {
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return token==null?null:token.replace("bearer ","");
    }

    /**
     * 判断该接口是否需要token
     * @param exchange
     * @return
     */
    private boolean isRequireToken(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        if(noRequireTokenUris.contains(path)){
            return false;       //不需要Token，放行
        }
return true;

    }

    /**
     * 拦截器的顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
