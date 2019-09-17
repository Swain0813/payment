package com.payment.task.rabbitmq;
import com.payment.common.constant.AD3MQConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 */
@Configuration
public class RabbitMQConfig {

    //操作记录
    //—————————————————————————机构提款队列———————————————————————————————————————————————————
    public final static String TC_MQ_WD_DL = AD3MQConstant.TC_MQ_WD_DL;//机构提款队列

    /**
     * 机构提款队列
     *
     * @return
     */
    @Bean
    public Queue wDl() {
        return new Queue(RabbitMQConfig.TC_MQ_WD_DL);
    }

}
