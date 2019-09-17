package com.payment.trade.service.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.OrderPayment;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.EResultEnum;
import com.payment.trade.dao.OrderPaymentMapper;
import com.payment.trade.dto.Help2PayOutCallbackDTO;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.PayOutCallBackService;
import com.payment.trade.service.PayOutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-08-05 11:53
 **/
@Service
@Slf4j
public class PayOutCallBackServiceImpl implements PayOutCallBackService {

    @Autowired
    private OrderPaymentMapper orderPaymentMapper;

    @Autowired
    private PayOutService payOutService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/5
     * @Descripate Help2Pay付款回调接口
     **/
    @Override
    public void help2PayCallBack(Help2PayOutCallbackDTO help2PayOutCallbackDTO) {
        OrderPayment orderPayment = orderPaymentMapper.selectByPrimaryKey(help2PayOutCallbackDTO.getTransactionID());
        if (orderPayment == null || orderPayment.getPayoutStatus() != TradeConstant.PAYMENT_WAIT) {
            log.info("-------------回调订单不存在------------help2PayOutCallbackDTO:{}", JSON.toJSON(help2PayOutCallbackDTO));
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        if (help2PayOutCallbackDTO.getStatus().equals("000")) {
            //分润
            if(!StringUtils.isEmpty(orderPayment.getExtend5())){
                rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orderPayment.getId());
            }
            //付款成功
            orderPayment.setId(help2PayOutCallbackDTO.getTransactionID());
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_SUCCESS);
            orderPayment.setChannelNumber(help2PayOutCallbackDTO.getID());
            orderPayment.setChannelCallbackTime(new Date());
            orderPayment.setUpdateTime(new Date());
            //更新汇款单表
            orderPaymentMapper.updateByPrimaryKeySelective(orderPayment);
        } else {
            //付款失败
            orderPayment.setChannelCallbackTime(new Date());
            orderPayment.setPayoutStatus(TradeConstant.PAYMENT_FAIL);
            payOutService.faliReconciliation(orderPayment,"Help2Pay付款回调接口,上游汇款失败调账记录");
        }
        if (!StringUtils.isEmpty(orderPayment.getServerUrl())) {
            try {
                log.info("-------------Help2Pay汇款服务器回调方法信息记录------------回调商户服务器开始----------");
                commonService.payOutCallBack(orderPayment);
            } catch (Exception e) {
                log.info("-------------Help2Pay汇款服务器回调方法信息记录---------回调商户服务器异常----------", e);
            }
        }

    }
}
