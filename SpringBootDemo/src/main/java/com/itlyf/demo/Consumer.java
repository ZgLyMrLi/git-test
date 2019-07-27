package com.itlyf.demo;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消费者
 */
@Component
public class Consumer {

    @JmsListener(destination = "itlyf")
    public void readMessage(String text){
        System.out.println("接收到消息："+text);
    }

    @JmsListener(destination = "itlyf_map")
    public void readMapMessage(Map map){
        System.out.println("接收到消息："+map);
    }

}
