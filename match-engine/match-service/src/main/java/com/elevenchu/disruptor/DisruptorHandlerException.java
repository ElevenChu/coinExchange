package com.elevenchu.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * DisruptorHandlerException 的异常处理
 */
@Slf4j
public class DisruptorHandlerException implements ExceptionHandler {
    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        log.info("handleEventException Exception===>{} , sequence==> {} ,event===>{}",ex.getMessage(),sequence,event);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.info("OnStartHandler Exception===>{} ",ex.getMessage());
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.info("OnShutdownHandler Exception===>{} ",ex.getMessage());
    }
}
