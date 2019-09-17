package com.payment.finance.rabbitmq;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Wu, Hua-Zheng
 * @version v1.0.0
 * @classDesc: 功能描述: RabbitMQ消息发送
 * @createTime 2018年8月9日 下午9:50:46
 */
@Component
@Slf4j
public class RabbitMQSender {

    @Autowired
    private AmqpTemplate amqpTemplate;


    public void send(String queue, String msg) {
        log.info("------------------ RabbitMQSender send -----------------：queue:{},msg :{}", queue, JSON.toJSONString(msg));
        this.amqpTemplate.convertAndSend(queue, msg);
    }

    @Async
    public void sendSleep(String queue, String msg) {
        log.info("------------------ RabbitMQSender sendSleep -----------------：queue:{},msg :{}", queue, JSON.toJSONString(msg));
        //try {
        //    Thread.sleep(1000*60*3);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
        this.amqpTemplate.convertAndSend(queue, msg);
    }

}
