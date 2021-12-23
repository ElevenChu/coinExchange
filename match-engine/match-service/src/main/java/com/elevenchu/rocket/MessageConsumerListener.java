package com.elevenchu.rocket;

import com.elevenchu.disruptor.DisruptorTemplate;
import com.elevenchu.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageConsumerListener {
    @Autowired
    private DisruptorTemplate disruptorTemplate;


    @StreamListener("order_in")
    public void handleMessage(Order order) {

        disruptorTemplate.onData(order);
    }
}