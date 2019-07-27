package com.itlyf.test;

import com.itlyf.demo.QueueProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-jms-producer.xml")
public class TestQueue {

    @Autowired
    QueueProducer queueProducer;

    @Test
    public void testSend(){
        queueProducer.sendTextMsg("spring JMS 点对点 李云飞2019/7/4/8.52");
    }

}
