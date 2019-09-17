package com.payment.finance.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FinancialFreezeDTO;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.*;
import com.payment.common.response.BaseResponse;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.vo.FinancialFreezeVO;
import com.payment.common.vo.FundChangeVO;
import com.payment.finance.dao.*;
import com.payment.finance.feign.MessageFeign;
import com.payment.finance.service.ClearingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;


/**
 * @description:
 * @author: YangXu
 * @create: 2019-03-14 15:05
 **/
@Component
@Slf4j
public class ReconciliationMQReceive {

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderRefundMapper orderRefundMapper;

    @Autowired
    private CheckAccountMapper checkAccountMapper;

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    private SettleOrderMapper settleOrderMapper;

    /**
     * 调账失败
     *
     * @param value
     */
    @RabbitListener(queues = "TC_MQ_RECONCILIATION_DL")
    public void processTZSB(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        log.info("----------------- TC_MQ_RECONCILIATION_DL TC_MQ_RECONCILIATION_DL---------------- rabbitMassage : {} ", value);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            Reconciliation reconciliation = JSON.parseObject(rabbitMassage.getValue(), Reconciliation.class);
            FundChangeDTO fundChangeDTO = new FundChangeDTO(reconciliation);
            fundChangeDTO.setShouldDealtime(DateToolUtils.formatTimestamp.format(new Date()));
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务成功
                    reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.RECONCILIATION_SUCCESS, "sys", TradeConstant.RECONCILIATION_REFUND_FAIL);
                } else {//业务失败
                    log.info("----------------- TC_MQ_RECONCILIATION_DL 上报队列 TC_MQ_RECONCILIATION_DL 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.sendSleep(AD3MQConstant.TC_MQ_RECONCILIATION_DL, JSON.toJSONString(rabbitMassage));
                }
            } else {//请求失败
                log.info("----------------- TC_MQ_RECONCILIATION_DL 上报队列 TC_MQ_RECONCILIATION_DL 请求失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.TC_MQ_RECONCILIATION_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "审核调账失败 TC_MQ_RECONCILIATION_DL预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "审核调账失败 TC_MQ_RECONCILIATION_DL预警 ", "TC_MQ_RECONCILIATION_DL预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 冻结失败
     *
     * @param value
     */
    @RabbitListener(queues = "FREEZE_MQ_FAIL")
    public void processFREEZE(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        log.info("----------------- FREEZE_MQ_FAIL FREEZE_MQ_FAIL---------------- rabbitMassage : {} ", value);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            FinancialFreezeDTO ffd = JSON.parseObject(rabbitMassage.getValue(), FinancialFreezeDTO.class);
            ffd.setSignMsg(null);
            BaseResponse response = clearingService.freezingFunds(ffd, null);
            if (response.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FinancialFreezeVO financialFreezeVO = (FinancialFreezeVO) response.getData();
                if (!StringUtils.isEmpty(financialFreezeVO.getRespCode()) || financialFreezeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务成功
                    reconciliationMapper.updateStatusById(ffd.getReconciliationId(), TradeConstant.FREEZE_SUCCESS, "sys", TradeConstant.FREEZE_FAIL);
                } else {//业务失败
                    log.info("----------------- FREEZE_MQ_FAIL 上报队列 FREEZE_MQ_FAIL 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.sendSleep(AD3MQConstant.FREEZE_MQ_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {//请求失败
                log.info("----------------- FREEZE_MQ_FAIL 上报队列 FREEZE_MQ_FAIL 请求失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.FREEZE_MQ_FAIL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "冻结失败 FREEZE_MQ_FAIL ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "冻结失败 FREEZE_MQ_FAIL ", "FREEZE_MQ_FAIL ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 解冻失败
     *
     * @param value
     */
    @RabbitListener(queues = "UNFREEZE_MQ_FAIL")
    public void processUNFREEZE(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        log.info("----------------- UNFREEZE_MQ_FAIL UNFREEZE_MQ_FAIL---------------- rabbitMassage : {} ", value);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            FinancialFreezeDTO ffd = JSON.parseObject(rabbitMassage.getValue(), FinancialFreezeDTO.class);
            ffd.setSignMsg(null);
            BaseResponse response = clearingService.freezingFunds(ffd, null);
            if (response.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FinancialFreezeVO financialFreezeVO = (FinancialFreezeVO) response.getData();
                if (!StringUtils.isEmpty(financialFreezeVO.getRespCode()) || financialFreezeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务成功
                    reconciliationMapper.updateStatusById(ffd.getReconciliationId(), TradeConstant.UNFREEZE_SUCCESS, "sys", TradeConstant.UNFREEZE_FAIL);
                } else {//业务失败
                    log.info("----------------- UNFREEZE_MQ_FAIL 上报队列 UNFREEZE_MQ_FAIL 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.sendSleep(AD3MQConstant.UNFREEZE_MQ_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {//请求失败
                log.info("----------------- UNFREEZE_MQ_FAIL 上报队列 UNFREEZE_MQ_FAIL 请求失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.UNFREEZE_MQ_FAIL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "解冻失败 UNFREEZE_MQ_FAIL ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "解冻失败 UNFREEZE_MQ_FAIL ", "UNFREEZE_MQ_FAIL ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 退款系统补单队列
     *
     * @param value
     */
    @RabbitListener(queues = "TC_MQ_FINANCE_TKBUDAN_DL")
    @Transactional
    public void processTZBD(String value) {
        log.info("----------------- TC_MQ_FINANCE_TKBUDAN_DL 系统补单  -------------- 退款单号 ： {}", value);
        OrderRefund orderRefund = orderRefundMapper.selectByPrimaryKey(value);
        orderRefund.setRefundStatus(TradeConstant.REFUND_SUCCESS);
        orderRefund.setRefundChannelNumber(checkAccountMapper.selectByOrderId(value));
        orderRefund.setRemark("系统补单成功");
        orderRefund.setUpdateTime(new Date());
        if (TradeConstant.REFUND_WAIT.equals(orderRefund.getRefundStatus())) {
            orderRefundMapper.updateByPrimaryKeySelective(orderRefund);
            checkAccountMapper.upateErrorType(value, "系统补单");
        }
    }

    /**
     * 收单系统补单队列
     *
     * @param value
     */
    @RabbitListener(queues = "TC_MQ_FINANCE_SDBUDAN_DL")
    @Transactional
    public void processSDBD(String value) {
        log.info("----------------- TC_MQ_FINANCE_SDBUDAN_DL 系统补单  -------------- 退款单号 ： {}", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            Orders orders = ordersMapper.selectByPrimaryKey(rabbitMassage.getValue());
            orders.setChannelNumber(checkAccountMapper.selectByOrderId(rabbitMassage.getValue()));
            orders.setRemark("系统补单成功");
            orders.setUpdateTime(new Date());
            if (TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
                //上报清结算
                FundChangeDTO fundChangeDTO = new FundChangeDTO(orders, TradeConstant.NT, orders.getInstitutionCode());//收单
                //上报清结算资金变动接口
                BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                if (fundChangeResponse.getCode() != null && fundChangeResponse.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                    FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                    if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务处理失败
                        log.info("-----------------系统补单失败 上报队列 TC_MQ_FINANCE_SDBUDAN_DL -------------- rabbitMassage : {}", JSON.toJSONString(rabbitMassage));
                        rabbitMQSender.send(AD3MQConstant.TC_MQ_FINANCE_SDBUDAN_DL, JSON.toJSONString(rabbitMassage));
                    } else {
                        orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
                        ordersMapper.updateByPrimaryKeySelective(orders);
                        checkAccountMapper.upateErrorType(value, "系统补单");
                    }
                } else {//请求失败
                    log.info("-----------------系统补单失败 上报队列 TC_MQ_FINANCE_SDBUDAN_DL -------------- rabbitMassage : {}", JSON.toJSONString(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.TC_MQ_FINANCE_SDBUDAN_DL, JSON.toJSONString(rabbitMassage));
                }
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "退款系统补单队列失败 TC_MQ_FINANCE_SDBUDAN_DL预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "退款系统补单队列 TC_MQ_FINANCE_SDBUDAN_DL预警 ", "TC_MQ_FINANCE_SDBUDAN_DL预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 机构手动结算提款失败
     *
     * @param value
     */
    @RabbitListener(queues = "TC_MQ_ZD_DL")
    public void processWd(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        log.info("----------------- 机构手动结算提款失败 TC_MQ_ZD_DL---------------- rabbitMassage : {} ", value);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            SettleOrder settleOrder = JSON.parseObject(rabbitMassage.getValue(), SettleOrder.class);
            FundChangeDTO fundChangeDTO = new FundChangeDTO(settleOrder);
            fundChangeDTO.setShouldDealtime(DateToolUtils.formatTimestamp.format(new Date()));
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务成功
                    //插入数据到机构结算表
                    settleOrderMapper.insert(settleOrder);
                } else {//业务失败
                    log.info("----------------- 机构手动结算提款失败 TC_MQ_ZD_DL 上报清结算 业务失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.TC_MQ_ZD_DL, JSON.toJSONString(rabbitMassage));
                }
            } else {//请求失败
                log.info("----------------- 机构手动结算提款失败 上报队列 TC_MQ_ZD_DL 请求失败 -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                rabbitMQSender.send(AD3MQConstant.TC_MQ_ZD_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "机构手动结算提款失败 TC_MQ_ZD_DL预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "机构手动结算提款失败 TC_MQ_ZD_DL预警 ", "TC_MQ_ZD_DL预警 ：{ " + value + " }");//邮件通知
        }
    }
}
