package com.payment.trade.rabbitmq;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.service.ClearingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 线下下单队列
 */
@Component
@Slf4j
public class AD3PlaceOrdersMQReceive {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    /**
     * 支付成功后上报清结算失败的队列
     *
     * @param value json 数据
     */
    @RabbitListener(queues = "MQ_PLACE_ORDER_FUND_CHANGE_FAIL")
    public void processOrderFundChangeFail(String value) {
        log.info("----------------- MQ_PLACE_ORDER_FUND_CHANGE_FAIL rabbitMassage -------------- rabbitMassage : {}", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            Orders orders = JSON.parseObject(rabbitMassage.getValue(), Orders.class);
            //资金变动接口输入参数
            FundChangeDTO fundChangeDTO = new FundChangeDTO(orders, TradeConstant.NT, orders.getInstitutionCode());
            //上报清结算
            BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
            if (!fundChangeResponse.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                log.info("----------------- MQ_PLACE_ORDER_FUND_CHANGE_FAIL 上报队列 MQ_PLACE_ORDER_FUND_CHANGE_FAIL -------------- rabbitMassage : {}",
                        JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
            } else {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                    log.info("----------------- MQ_PLACE_ORDER_FUND_CHANGE_FAIL 上报队列 MQ_PLACE_ORDER_FUND_CHANGE_FAIL -------------- rabbitMassage : {}",
                            JSON.toJSON(rabbitMassage));
                    rabbitMQSender.sendSleep(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            }
        } else {
            messageFeign.sendSimple(developerMobile, "下单支付成功时上报清结算失败 MQ_PLACE_ORDER_FUND_CHANGE_FAIL 预警 :{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "下单支付成功时上报清结算失败 MQ_PLACE_ORDER_FUND_CHANGE_FAIL 预警", "MQ_PLACE_ORDER_FUND_CHANGE_FAIL 预警 ：{ " + value + " }");//邮件通知
        }
    }
}
