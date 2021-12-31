package com.elevenchu.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.server.handler.IWsMsgHandler;

@Component
@Slf4j
public class WebSocketMessageHandler  implements IWsMsgHandler {

    /**
     * 处理握手
     *
     * @param httpRequest
     * @param httpResponse
     * @param channelContext
     * @return
     * @throws Exception
     */
    @Override
    public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        String clientIp = httpRequest.getClientIp();
        log.info("开始和{}客户端建立连接", clientIp);
        return httpResponse;
    }


    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
        log.info("和客户端握手成功");
    }
    /**
     * 当前websocket 前端发送一个Bytes 时,我们要做的处理
     *
     * @param wsRequest
     * @param bytes
     * @param channelContext
     * @return
     * @throws Exception
     */
    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        return null;
    }
    /**
     * 当前端发一个close方时,怎么处理
     *
     * @param wsRequest
     * @param bytes
     * @param channelContext
     * @return
     * @throws Exception
     */
    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
        Tio.remove(channelContext, "remove channelContext");
        return null;
    }
    /**
     * 当前端发送文本过来
     *
     * @param wsRequest
     * @param
     * @param channelContext
     * @return
     * @throws Exception
     */

    @Override
    public Object onText(WsRequest wsRequest, String s, ChannelContext channelContext) throws Exception {

        return null;

    }
}
