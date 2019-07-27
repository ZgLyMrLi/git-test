package com.pinyougou.page.service.impl;

import com.pinyougou.page.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.io.Serializable;

@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodId = (Long[]) objectMessage.getObject();
            boolean b = itemPageService.deleteItemHtml(goodId);
            System.out.println(b+"删除成功");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
