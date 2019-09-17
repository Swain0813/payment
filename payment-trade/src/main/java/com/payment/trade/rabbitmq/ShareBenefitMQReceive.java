package com.payment.trade.rabbitmq;
import com.payment.trade.service.AgentCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-08-23 10:51
 **/
@Component
@Slf4j
public class ShareBenefitMQReceive {

    @Autowired
    private AgentCalculateService agentCalculateService;

    @RabbitListener(queues = "MQ_FR_DL")
    public void processFR(String value) {
        try {
            agentCalculateService.insertShareBenefitLogs(value);
        } catch (Exception e) {
            log.error("================== MQ_FR_DL ================ Exception :{}",e);
            e.printStackTrace();
        }
    }
}
