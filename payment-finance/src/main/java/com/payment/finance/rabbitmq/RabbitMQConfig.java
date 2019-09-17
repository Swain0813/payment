package com.payment.finance.rabbitmq;

import com.payment.common.constant.AD3MQConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Wu, Hua-Zheng
 * @version v1.0.0
 * @classDesc: 功能描述: RabbitMQ配置
 * @createTime 2018年8月9日 下午9:52:13
 */
@Configuration
public class RabbitMQConfig {

    //操作记录
    //—————————————————————————调账队列———————————————————————————————————————————————————
    public final static String TC_MQ_RECONCILIATION_DL = AD3MQConstant.TC_MQ_RECONCILIATION_DL;//调账队列
    public final static String TC_MQ_FINANCE_TKBUDAN_DL = AD3MQConstant.TC_MQ_FINANCE_TKBUDAN_DL;//退款补单队列
    public final static String TC_MQ_FINANCE_SDBUDAN_DL = AD3MQConstant.TC_MQ_FINANCE_SDBUDAN_DL;//收单补单队列
    public final static String FREEZE_MQ_FAIL = AD3MQConstant.FREEZE_MQ_FAIL;//冻结队列
    public final static String UNFREEZE_MQ_FAIL = AD3MQConstant.UNFREEZE_MQ_FAIL;//解冻队列

    //—————————————————————————机构手动提款队列———————————————————————————————————————————————————
    public final static String TC_MQ_ZD_DL = AD3MQConstant.TC_MQ_ZD_DL;//机构手动提款队列

    /**
     * 机构提款队列
     *
     * @return
     */
    @Bean
    public Queue wDl() {
        return new Queue(RabbitMQConfig.TC_MQ_ZD_DL);
    }

    /**
     * 调账队列
     *
     * @return
     */
    @Bean
    public Queue reconciliationDl() {
        return new Queue(RabbitMQConfig.TC_MQ_RECONCILIATION_DL);
    }

    @Bean
    public Queue reconciliationD2() {
        return new Queue(RabbitMQConfig.TC_MQ_FINANCE_TKBUDAN_DL);
    }

    @Bean
    public Queue reconciliationD3() {
        return new Queue(RabbitMQConfig.TC_MQ_FINANCE_SDBUDAN_DL);
    }

    @Bean
    public Queue reconciliationD4() {
        return new Queue(RabbitMQConfig.FREEZE_MQ_FAIL);
    }

    @Bean
    public Queue reconciliationD5() {
        return new Queue(RabbitMQConfig.UNFREEZE_MQ_FAIL);
    }


}
