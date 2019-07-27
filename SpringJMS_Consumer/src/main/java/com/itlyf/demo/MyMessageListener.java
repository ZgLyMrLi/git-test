package com.itlyf.demo;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class MyMessageListener implements MessageListener {
    public void onMessage(Message message) {

        TextMessage testMessage = (TextMessage) message;
        try {
            System.out.println("接收到的消息:"+testMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
