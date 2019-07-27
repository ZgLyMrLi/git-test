package com.itlyf.demo;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class QueueConsumer {

    public static void main(String[] args) throws Exception {
        //1.创建连接工厂
        ConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.153:61616");
        //2.创建连接
        Connection connection = activeMQConnectionFactory.createConnection();
        //3.启动连接
        connection.start();
        //4.获取Session(会话对象) 参数一：是否启动事务false：默认提交true：手动提交  参数二：消息的确认方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5.创建一个队列对象  参数：队列名称
        Queue queue = session.createQueue("lyf-test1-queue");
        //6.创建消息消费者对象
        MessageConsumer consumer = session.createConsumer(queue);
        //7.设置监听
        consumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("提取的消息:"+textMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
        //8.等待键盘输入
        System.in.read();

        //9.关闭资源
        consumer.close();
        session.close();
        connection.close();
    }

}
