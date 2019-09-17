package com.payment.trade.rabbitmq;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.vo.PayOutNoticeVO;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.vo.OnlineCallbackURLVO;
import com.payment.trade.vo.OnlineCallbackVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author shenxinran
 * @Date: 2019/4/17 14:41
 * @Description: 回调商户队列
 */
@Slf4j
@Component
public class CallbackOrderMQReceive {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private OrdersMapper ordersMapper;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;


    /**
     * 回调商户队列
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_AW_CALLBACK_URL_FAIL")
    public void processAWCU(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        //解析订单信息
        OnlineCallbackURLVO onlineCallbackURLVO = JSON.parseObject(rabbitMassage.getValue(), OnlineCallbackURLVO.class);
        OnlineCallbackVO onlineCallbackVO = onlineCallbackURLVO.getOnlineCallbackVO();
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);
            log.info("==================【回调商户队列信息记录】==================【队列参数记录】 rabbitMassage: {} ", JSON.toJSONString(rabbitMassage));
            //30s超时
            cn.hutool.http.HttpResponse execute = null;
            try {
                log.info("==================【回调商户队列信息记录】==================【商户回调接口URL记录】  serverUrl: {}", onlineCallbackURLVO.getReturnUrl());
                execute = HttpRequest.post(onlineCallbackURLVO.getReturnUrl())
                        .header(Header.CONTENT_TYPE, "application/json")
                        .body(JSON.toJSONString(onlineCallbackVO))
                        .timeout(30000)
                        .execute();
            } catch (Exception e) {
                log.info("==================【回调商户队列信息记录】==================【httpException异常,继续上报商户回调队列】 MQ_AW_CALLBACK_URL_FAIL");
                e.printStackTrace();
                rabbitMQSender.send(AD3MQConstant.E_MQ_AW_CALLBACK_URL_FAIL, JSON.toJSONString(rabbitMassage));
                return;
            }
            String body = execute.body();
            log.info("==================【回调商户队列信息记录】==================【响应结果记录】 body: {}", body);
            if (StringUtils.isEmpty(body) || !body.equalsIgnoreCase(AsianWalletConstant.CALLBACK_SUCCESS)) {
                log.info("==================【回调商户队列信息记录】==================【商户响应失败,继续上报商户回调队列】 MQ_AW_CALLBACK_URL_FAIL");
                rabbitMQSender.send(AD3MQConstant.E_MQ_AW_CALLBACK_URL_FAIL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            log.info("==================【回调商户队列信息记录】==================【三次回调完成,回调商户失败】");
            //回调下游商户失败原因
            ordersMapper.updateOrderRemark(onlineCallbackVO.getReferenceNo(), "商户回调返回消息不为SUCCESS");
            messageFeign.sendSimple(developerMobile, "回调商户失败 MQ_AW_CALLBACK_URL_FAIL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "回调商户失败 MQ_AW_CALLBACK_URL_FAIL 预警", "MQ_AW_CALLBACK_URL_FAIL 预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 汇款回调商户队列
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_PAYMENT_CALLBACK_URL_FAIL")
    public void processPAYMENTAWCU(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);
            log.info("==================【汇款回调商户队列】==================【队列参数记录】 rabbitMassage: {} ", JSON.toJSONString(rabbitMassage));
            //解析订单信息
            PayOutNoticeVO payOutNoticeVO = JSON.parseObject(rabbitMassage.getValue(), PayOutNoticeVO.class);
            log.info("==================【汇款回调商户队列】==================【商户回调接口URL记录】  serverUrl: {}", payOutNoticeVO.getServerUrl());
            log.info("==================【汇款回调商户队列】==================【回调参数记录】  payOutNoticeVO: {}", JSON.toJSON(payOutNoticeVO));
            cn.hutool.http.HttpResponse execute = HttpRequest.post(payOutNoticeVO.getServerUrl())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(JSON.toJSONString(payOutNoticeVO))
                    .timeout(6000)
                    .execute();
            String body = execute.body();
            log.info("==================【汇款回调商户队列】==================【响应结果记录】 body: {}", body);
            if (StringUtils.isEmpty(body) || !body.equalsIgnoreCase(AsianWalletConstant.CALLBACK_SUCCESS)) {
                log.info("==================【汇款回调商户队列】==================【商户响应失败,继续上报商户回调队列】 MQ_PAYMENT_CALLBACK_URL_FAIL");
                rabbitMQSender.send(AD3MQConstant.E_MQ_PAYMENT_CALLBACK_URL_FAIL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            log.info("==================【汇款回调商户队列】==================【三次回调完成,回调商户失败】");
            messageFeign.sendSimple(developerMobile, "回调商户失败 MQ_PAYMENT_CALLBACK_URL_FAIL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "回调商户失败 MQ_PAYMENT_CALLBACK_URL_FAIL 预警", "MQ_PAYMENT_CALLBACK_URL_FAIL 预警 ：{ " + value + " }");//邮件通知
        }
    }

}
