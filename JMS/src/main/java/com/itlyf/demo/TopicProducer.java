package com.itlyf.demo;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class TopicProducer {

    public static void main(String[] args) throws JMSException {
        //1.创建连接工厂
        ConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.153:61616");
        //2.创建连接
        Connection connection = activeMQConnectionFactory.createConnection();
        //3.启动连接
        connection.start();
        //4.获取Session(会话对象) 参数一：是否启动事务false：默认提交true：手动提交  参数二：消息的确认方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5.创建一个主题对象  参数：队列名称
        Topic topic = session.createTopic("lyf-test2-topic");
        //6.创建消息的生产者对象
        MessageProducer producer = session.createProducer(topic);
        //7.创建一个消息对象(文本消息)
        TextMessage textMessage = session.createTextMessage("欢迎欢迎topic");
        //8.发送消息
        producer.send(textMessage);
        //9.关闭资源
        producer.close();
        session.close();
        connection.close();
    }

}
