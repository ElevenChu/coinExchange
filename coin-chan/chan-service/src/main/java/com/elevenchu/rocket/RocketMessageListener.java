package com.elevenchu.rocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.starter.TioWebSocketServerBootstrap;

@Component
@Slf4j
public class RocketMessageListener {
    @Autowired
    private TioWebSocketServerBootstrap tioWebSocketServerBootstrap;


    @StreamListener("tio_group")
    public void handlerMessage(String message){
        log.info("接受到RocketMq的消息是=====>", JSON.toJSONString(message));
        //推送给前端用户
        Tio.sendToGroup(tioWebSocketServerBootstrap.getServerTioConfig(),"test",WsResponse.fromText("发送消息","utf-8") );
    }

}
