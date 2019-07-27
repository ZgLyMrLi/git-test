package com.itlyf.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息的生产者
 */
@RestController
public class QueueController {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @RequestMapping("/send")
    public void send(String text){
        jmsMessagingTemplate.convertAndSend("itlyf",text);
    }

    @RequestMapping("/sendmap")
    public void sendMap(){
        Map map = new HashMap();
        map.put("mobile","15237921791");
        map.put("sign_name","云信端");
        map.put("template_code","SMS_169895733");
        map.put("param","{\"code\":\"0218\"}");
        jmsMessagingTemplate.convertAndSend("sms",map);
    }

}
