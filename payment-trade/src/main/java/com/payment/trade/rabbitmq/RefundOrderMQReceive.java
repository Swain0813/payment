package com.payment.trade.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.entity.Reconciliation;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.HttpResponse;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.megaPay.MegaPayService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.OrderRefundMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dao.ReconciliationMapper;
import com.payment.trade.dto.AD3RefundDTO;
import com.payment.trade.dto.AD3RefundWorkDTO;
import com.payment.trade.dto.SendAdRefundDTO;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.channels.ad3Online.AD3OnlineAcquireService;
import com.payment.trade.channels.ad3Offline.AD3Service;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.channels.alipay.AliPayService;
import com.payment.trade.channels.wechat.WechatService;
import com.payment.trade.vo.AD3LoginVO;
import com.payment.trade.vo.AD3RefundOrderVO;
import com.payment.trade.vo.RefundAdResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


/**
 * @description:
 * @author: YangXu
 * @create: 2019-03-14 15:05
 **/
@Component
@Slf4j
public class RefundOrderMQReceive {

    @Autowired
    private CommonService commonService;

    @Autowired
    private AD3Service ad3Service;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private AD3OnlineAcquireService ad3OnlineAcquireService;

    @Autowired
    private OrderRefundMapper refundOrderMapper;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private MegaPayService megaPayService;

    /**
     * 上报清结算调账失败
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_QJS_TZSB_DL")
    public void processTZSB(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        log.info("----------------- MQ_QJS_TZSB_DL 调账失败 ---------------- rabbitMassage : {} ", value);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            FundChangeDTO fundChangeDTO = JSON.parseObject(orderRefund.getRemark3(), FundChangeDTO.class);
            fundChangeDTO.setSignMsg(null);
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                if (!cFundChange.getCode().equals(TradeConstant.CLEARING_SUCCESS)) {//退款失败
                    log.info("-----------------  MQ_QJS_TZSB_DL 上报队列 MQ_QJS_TZSB_DL ---------------- rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                    rabbitMQSender.sendSleep(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                } else {//退款成功
                    refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_FALID, orderRefund.getChannelNumber(), null);
                    //改原订单状态
                    if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                    } else {
                        if (orderRefund.getRemark2().equals("全额")) {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                        } else {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_PART_SUCCESS);
                        }
                    }
                }
            } else {//请求失败
                log.info("-----------------  MQ_QJS_TZSB_DL 上报队列 MQ_QJS_TZSB_DL ---------------- rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "上报清结算调账失败队列 MQ_QJS_TZSB_DL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "上报清结算调账失败队列 MQ_QJS_TZSB_DL 预警", "MQ_QJS_TZSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 退款请求失败对列 (线上)
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_TK_XS_QQSB_DL")
    public void processTKXSQQSB(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            log.info("----------------- MQ_TK_XS_QQSB_DL 退款请求失败对列  ----------------  : {} ", value);
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            FundChangeDTO fundChangeDTO = JSON.parseObject(orderRefund.getRemark3(), FundChangeDTO.class);
            SendAdRefundDTO sendAdRefundDTO = new SendAdRefundDTO(ad3ParamsConfig.getMerchantCode(), orderRefund);
            sendAdRefundDTO.setMerchantSignType(ad3ParamsConfig.getMerchantSignType());
            sendAdRefundDTO.setSignMsg(ad3OnlineAcquireService.signMsg(sendAdRefundDTO));
            HttpResponse httpResponse = ad3OnlineAcquireService.RefundOrder(sendAdRefundDTO, null);
            if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {  //请求成功
                RefundAdResponseVO refundAdResponseVO = JSONObject.parseObject(httpResponse.getJsonObject().toJSONString(), RefundAdResponseVO.class);
                if (ad3OnlineAcquireService.judgeRefundAdResponseVO(refundAdResponseVO)) {
                    log.info("----------------- MQ_TK_XS_QQSB_DL 退款成功 -------------- refundAdResponseVO : {} ", JSON.toJSON(refundAdResponseVO));
                    //退款成功
                    refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, refundAdResponseVO.getTxnId(), null);
                    //改原订单状态
                    if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                    } else {
                        if (orderRefund.getRemark2().equals("全额")) {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                        } else {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_PART_SUCCESS);
                        }
                    }
                } else { //退款失败
                    log.info("----------------- MQ_TK_XS_QQSB_DL 退款失败 -------------- refundAdResponseVO : {} ", JSON.toJSON(refundAdResponseVO));
                    Reconciliation reconciliation = commonService.createReconciliation(orderRefund, TradeConstant.REFUND_FAIL_RECONCILIATION);
                    reconciliationMapper.insert(reconciliation);
                    fundChangeDTO.setRefcnceFlow(reconciliation.getId());
                    fundChangeDTO.setSysorderid(orderRefund.getId());
                    fundChangeDTO.setSignMsg(null);
                    fundChangeDTO.setTxnamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    fundChangeDTO.setSltamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    fundChangeDTO.setTradetype(TradeConstant.AA);
                    fundChangeDTO.setBalancetype(TradeConstant.NORMAL_FUND);
                    BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
                    if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                        orderRefund.setChannelNumber(refundAdResponseVO.getTxnId());
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage rabbitmsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("----------------- MQ_TK_XS_QQSB_DL 上报队列 MQ_QJS_TZSB_DL -------------- rabbitmsg : {} ", JSON.toJSON(rabbitmsg));
                        rabbitMQSender.sendSleep(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitmsg));
                    } else {//请求成功
                        FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                        if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务成功
                            refundOrderMapper.updateStatuts(refundAdResponseVO.getMerOrderNo(), TradeConstant.REFUND_FALID, refundAdResponseVO.getTxnId(), null);
                            reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.RECONCILIATION_SUCCESS);
                            //改原订单状态
                            if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                                ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_FAIL);
                            } else {
                                BigDecimal oldRefundAmount = refundOrderMapper.getTotalAmountByOrderId(orderRefund.getOrderId()); //已退款金额
                                oldRefundAmount = oldRefundAmount == null ? BigDecimal.ZERO : oldRefundAmount;
                                if (oldRefundAmount.compareTo(BigDecimal.ZERO) == 0) {
                                    ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_FAIL);
                                }
                            }
                        } else {//业务失败
                            orderRefund.setChannelNumber(refundAdResponseVO.getTxnId());
                            orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                            RabbitMassage rabbitmsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                            log.info("----------------- MQ_TK_XS_QQSB_DL 上报队列 MQ_QJS_TZSB_DL -------------- rabbitmsg : {} ", JSON.toJSON(rabbitmsg));
                            rabbitMQSender.sendSleep(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitmsg));
                        }
                    }
                }
            } else { //请求失败
                log.info("----------------- MQ_TK_XS_QQSB_DL 请求失败上报队列 MQ_TK_XS_QQSB_DL -------------- value : {} ", JSON.toJSONString(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_TK_XS_QQSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "退款请求失败对列(线上) MQ_TK_XS_QQSB_DL 预警");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "退款请求失败对列(线上) MQ_TK_XS_QQSB_DL 预警", "MQ_TK_XS_QQSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }


    /**
     * 退款请求失败对列(线下)
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_TK_XX_QQSB_DL")
    public void processTKQQSB(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1); //请求次数减一
            log.info("----------------- MQ_TK_XX_QQSB_DL 退款请求失败对列 ----------------  : {} ", value);
            //获取ad3的终端号和token
            AD3LoginVO ad3LoginVO = this.ad3Service.getTerminalIdAndToken();
            if (ad3LoginVO == null) {
                log.info("************ MQ_TK_XX_QQSB_DL 退款请求失败对列  登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
                return;
            }
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
            AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
            ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
            ad3RefundDTO.setSignMsg(ad3Service.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
            HttpResponse httpResponse = ad3Service.RefundOrder(ad3RefundDTO, null);
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {//请求成功
                if (ad3RefundOrderVO.getRespCode().equals("10000")) {//退款成功
                    log.info("----------------- MQ_TK_XX_QQSB_DL 上游退款成功 -------------- ad3RefundOrderVO : {} ", JSON.toJSONString(ad3RefundOrderVO));
                    refundOrderMapper.updateStatuts(ad3RefundOrderVO.getOutRefundId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysRefundId(), null);
                    //改原订单状态
                    if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                    } else {
                        if (orderRefund.getRemark2().equals("全额")) {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                        } else {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_PART_SUCCESS);
                        }
                    }
                } else {//退款失败
                    log.info("----------------- MQ_TK_XX_QQSB_DL 上游退款失败 -------------- ad3RefundOrderVO : {} ", JSON.toJSONString(ad3RefundOrderVO));
                    FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.AA, orderRefund);
                    fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//应结日期
                    Reconciliation reconciliation = commonService.createReconciliation(orderRefund, TradeConstant.REFUND_FAIL_RECONCILIATION);
                    reconciliationMapper.insert(reconciliation);
                    fundChangeDTO.setRefcnceFlow(reconciliation.getId());
                    fundChangeDTO.setSysorderid(orderRefund.getId());
                    fundChangeDTO.setTxnamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    fundChangeDTO.setSltamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    fundChangeDTO.setSignMsg(null);
                    BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
                    if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                        orderRefund.setChannelNumber(ad3RefundOrderVO.getSysRefundId());
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage rabbitMSG = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("----------------- MQ_TK_XX_QQSB_DL 上报队列 MQ_QJS_TZSB_DL-------------- rabbitMSG : {} ", JSON.toJSONString(rabbitMSG));
                        rabbitMQSender.sendSleep(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMSG));
                    } else {//请求成功
                        FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                        if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务成功
                            refundOrderMapper.updateStatuts(ad3RefundOrderVO.getOutRefundId(), TradeConstant.REFUND_FALID, ad3RefundOrderVO.getSysRefundId(), null);
                            reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.RECONCILIATION_SUCCESS);
                            //改原订单状态
                            if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                                ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_FAIL);
                            } else {
                                BigDecimal oldRefundAmount = refundOrderMapper.getTotalAmountByOrderId(orderRefund.getOrderId()); //已退款金额
                                oldRefundAmount = oldRefundAmount == null ? BigDecimal.ZERO : oldRefundAmount;
                                if (oldRefundAmount.compareTo(BigDecimal.ZERO) == 0) {
                                    ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_FAIL);
                                }
                            }
                        } else {//业务失败
                            orderRefund.setChannelNumber(ad3RefundOrderVO.getSysRefundId());
                            orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                            RabbitMassage rabbitMSG = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                            log.info("----------------- MQ_TK_XX_QQSB_DL 上报队列 MQ_QJS_TZSB_DL-------------- rabbitMSG : {} ", JSON.toJSONString(rabbitMSG));
                            rabbitMQSender.sendSleep(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMSG));
                        }
                    }
                }
            } else {//请求失败
                log.info("----------------- MQ_TK_XX_QQSB_DL 请求失败 -------------- rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_TK_XX_QQSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "退款请求失败对列(线下) MQ_TK_XX_QQSB_DL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "退款请求失败对列(线下) MQ_TK_XX_QQSB_DL 预警", "MQ_TK_XX_QQSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }


    /**
     * 撤销请求失败对列(线下)
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CX_XX_QQSB_DL")
    public void processCXQQSB(String value) {
        log.info("----------------- MQ_CX_XX_QQSB_DL 撤销请求失败对列 ----------------  : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            //获取ad3的终端号和token
            AD3LoginVO ad3LoginVO = this.ad3Service.getTerminalIdAndToken();
            if (ad3LoginVO == null) {
                log.info("************ MQ_CX_XX_QQSB_DL 撤销请求失败对列  登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
                return;
            }
            AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
            AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
            ad3RefundDTO.setSignMsg(ad3Service.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
            ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
            HttpResponse httpResponse = ad3Service.RefundOrder(ad3RefundDTO, null);
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {//请求成功
                if (ad3RefundOrderVO.getRespCode().equals("10000")) {//退款成功
                    log.info("----------------- MQ_CX_XX_QQSB_DL 上游退款成功 -------------- ad3RefundOrderVO : {}", JSON.toJSON(ad3RefundOrderVO));
                    //退款成功
                    refundOrderMapper.updateStatuts(ad3RefundOrderVO.getOutRefundId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysRefundId(), null);
                    //改原订单状态
                    if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                    } else {
                        if (orderRefund.getRemark2().equals("全额")) {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                        } else {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_PART_SUCCESS);
                        }
                    }
                } else {//退款失败
                    log.info("----------------- MQ_CX_XX_QQSB_DL 上游退款失败 -------------- ad3RefundOrderVO : {}", JSON.toJSON(ad3RefundOrderVO));
                    RabbitMassage rabbitMasage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                    rabbitMQSender.send(AD3MQConstant.MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMasage));
                }
            } else {//请求失败
                log.info("----------------- MQ_CX_XX_QQSB_DL 上游请求失败 -------------- rabbitMassage : {}", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_CX_XX_QQSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销请求失败对列(线下) MQ_CX_XX_QQSB_DL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销请求失败对列(线下) MQ_CX_XX_QQSB_DL 预警", "MQ_CX_XX_QQSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 撤销请求失败对列(线上)
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CX_XS_QQSB_DL")
    public void processCXXSQQSB(String value) {
        log.info("----------------- 撤销请求失败对列 MQ_CX_XS_QQSB_DL----------------  : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            SendAdRefundDTO sendAdRefundDTO = new SendAdRefundDTO(ad3ParamsConfig.getMerchantCode(), orderRefund);
            sendAdRefundDTO.setMerchantSignType(ad3ParamsConfig.getMerchantSignType());
            sendAdRefundDTO.setSignMsg(ad3OnlineAcquireService.signMsg(sendAdRefundDTO));
            HttpResponse httpResponse = ad3OnlineAcquireService.RefundOrder(sendAdRefundDTO, null);
            if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) { //请求成功
                RefundAdResponseVO refundAdResponseVO = JSONObject.parseObject(httpResponse.getJsonObject().toJSONString(), RefundAdResponseVO.class);
                if (ad3OnlineAcquireService.judgeRefundAdResponseVO(refundAdResponseVO)) {
                    log.info("----------------- MQ_CX_XS_QQSB_DL 上游退款成功 -------------- refundAdResponseVO : {}", JSON.toJSON(refundAdResponseVO));
                    //退款成功
                    refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, refundAdResponseVO.getTxnId(), null);
                    //改原订单状态
                    if (TradeConstant.REFUND_TYPE_TOTAL.equals(orderRefund.getRefundType())) {
                        ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                    } else {
                        if (orderRefund.getRemark2().equals("全额")) {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_SUCCESS);
                        } else {
                            ordersMapper.updateOrderRefundStatus(orderRefund.getInstitutionOrderId(), TradeConstant.ORDER_REFUND_PART_SUCCESS);
                        }
                    }
                } else {
                    log.info("----------------- MQ_CX_XS_QQSB_DL 上游退款失败 -------------- refundAdResponseVO : {}", JSON.toJSON(refundAdResponseVO));
                    RabbitMassage rabbitMasage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                    rabbitMQSender.send(AD3MQConstant.MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMasage));
                }
            } else { //请求失败
                log.info("----------------- MQ_CX_XS_QQSB_DL 请求失败 上报线上撤销请求失败 -------------- orderRefund : {}", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_CX_XS_QQSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销请求失败对列(线上) MQ_CX_XS_QQSB_DL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销请求失败对列(线上) MQ_CX_XS_QQSB_DL 预警", "MQ_CX_XS_QQSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }


    /**
     * 撤销上报清结算失败
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CX_SBQJSSB_DL")
    public void processSBQJSSB(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.RV, orderRefund);
            fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//应结日期
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                log.info("----------------- MQ_CX_SBQJSSB_DL 撤销上报清结算失败 -------------- cFundChange : {}", JSON.toJSON(cFundChange));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_CX_SBQJSSB_DL, JSON.toJSONString(rabbitMassage));
                return;
            } else {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                    log.info("----------------- MQ_CX_SBQJSSB_DL 撤销上报清结算失败 -------------- cFundChange : {}", JSON.toJSON(cFundChange));
                    rabbitMQSender.sendSleep(AD3MQConstant.MQ_CX_SBQJSSB_DL, JSON.toJSONString(rabbitMassage));
                    return;
                }
            }
            //上报上游退款通道
            Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
            BaseResponse baseResponse = new BaseResponse();
            if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_ONLINE)) {//线上退款
                ad3OnlineAcquireService.doUsRefundInRef(baseResponse, fundChangeDTO, orderRefund);
            } else if (channel.getChannelEnName().equalsIgnoreCase((AD3Constant.AD3_OFFLINE))) {//线下退款
                ad3Service.doUsRefundInRef(baseResponse, orderRefund, fundChangeDTO);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) || //Alipay
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
                aliPayService.aliPayCancel(orderRefund, baseResponse, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
                wechatService.wechatCancel(orderRefund, baseResponse, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
                //nextPos通道撤销
                megaPayService.megaPayNextPosCancel(orderRefund, baseResponse, rabbitMassage);
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销上报清结算失败 MQ_CX_SBQJSSB_DL 预警  ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销上报清结算失败 MQ_CX_SBQJSSB_DL 预警", "MQ_CX_SBQJSSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 退款上报清结算失败
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_TK_SBQJSSB_DL")
    public void processTKSBQJSSB(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        log.info("----------------- MQ_TK_SBQJSSB_DL rabbitMassage -------------- rabbitMassage : {}", value);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.RF, orderRefund);
            fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//应结日期
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                log.info("----------------- MQ_TK_SBQJSSB_DL 上报队列 MQ_TK_SBQJSSB_DL -------------- rabbitMassage : {}", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendSleep(AD3MQConstant.MQ_TK_SBQJSSB_DL, JSON.toJSONString(rabbitMassage));
                return;
            } else {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                    log.info("----------------- MQ_TK_SBQJSSB_DL 上报队列 MQ_TK_SBQJSSB_DL -------------- rabbitMassage : {}", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.sendSleep(AD3MQConstant.MQ_TK_SBQJSSB_DL, JSON.toJSONString(rabbitMassage));
                    return;
                }
            }
            Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
            BaseResponse baseResponse = new BaseResponse();
            if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_ONLINE)) {//线上退款
                ad3OnlineAcquireService.doUsRefundInRef(baseResponse, fundChangeDTO, orderRefund);
            } else if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//线下退款
                ad3Service.doUsRefundInRef(baseResponse, orderRefund, fundChangeDTO);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
                aliPayService.aliPayRefund(orderRefund, fundChangeDTO, baseResponse);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
                wechatService.wechatRefund(orderRefund, fundChangeDTO, baseResponse);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
                //nextPos通道退款
                megaPayService.megaPayNextPosRefund(orderRefund, fundChangeDTO, baseResponse);
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "退款上报清结算失败 MQ_TK_SBQJSSB_DL ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "退款上报清结算失败 MQ_TK_SBQJSSB_DL 预警", "MQ_TK_SBQJSSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }


    /**
     * 撤销通道退款失败
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CX_TDTKSB_DL")
    public void processTDTKSB(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        log.info("----------------- MQ_CX_TDTKSB_DL 撤销 通道退款失败 ---------------- rabbitMassage : {} ", value);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
            if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_ONLINE)) {
                //线上撤销操作
                ad3OnlineAcquireService.doUsCancelInRef(new BaseResponse(), orderRefund, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {
                //线下撤销操作
                ad3Service.doUsCancelInRef(new BaseResponse(), orderRefund, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
                aliPayService.aliPayCancel(orderRefund, new BaseResponse(), rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
                wechatService.wechatCancel(orderRefund, new BaseResponse(), rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
                //nextPos通道撤销
                megaPayService.megaPayNextPosCancel(orderRefund, new BaseResponse(), rabbitMassage);
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销通道退款失败 MQ_CX_TDTKSB_DL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销通道退款失败 MQ_CX_TDTKSB_DL 预警", "撤销通道退款失败 MQ_CX_TDTKSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }
}
