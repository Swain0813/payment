package com.payment.trade.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.*;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.HttpResponse;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.megaPay.MegaPayService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.*;
import com.payment.trade.dto.*;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.channels.ad3Offline.AD3Service;
import com.payment.trade.service.CancelOrderService;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.channels.alipay.AliPayService;
import com.payment.trade.channels.wechat.WechatService;
import com.payment.trade.vo.AD3LoginVO;
import com.payment.trade.vo.AD3RefundOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 撤销订单相关队列
 */
@Component
@Slf4j
public class CancelOrderMQReceive {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private TcsCtFlowMapper tcsCtFlowMapper;

    @Autowired
    private TcsStFlowMapper tcsStFlowMapper;

    @Autowired
    private CancelOrderService cancelOrderService;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private AD3Service ad3Service;

    @Autowired
    private OrderRefundMapper refundOrderMapper;

    @Autowired
    private MegaPayService megaPayService;

    @Autowired
    private MessageFeign messageFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    @Qualifier(value = "ad3ParamsConfig")
    private AD3ParamsConfig ad3ParamsConfig;

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private WechatService wechatService;


    /**
     * 撤销订单时支付中的订单的AD3的查询队列
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_AD3_ORDER_QUERY")
    public void processAd3OrderInfoQuery(String value) {
        log.info("****************撤销订单时支付中的订单的AD3的查询队列 MQ_AD3_ORDER_QUERY****************** : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            Orders orders = JSON.parseObject(rabbitMassage.getValue(), Orders.class);
            orders = ordersMapper.selectById(orders.getId());
            Channel channel = this.commonService.getChannelByChannelCode(orders.getChannelCode());
            //订单是交易成功的场合
            if (TradeConstant.ORDER_PAY_SUCCESS.equals(orders.getTradeStatus())) {
                //获取清算状态
                Integer ctState = tcsCtFlowMapper.getCTstatus(orders.getId());
                //获取结算状态
                Integer stState = tcsStFlowMapper.getSTstatus(orders.getId());
                if (TradeConstant.ORDER_CLEAR_SUCCESS.equals(ctState) && TradeConstant.ORDER_SETTLE_SUCCESS.equals(stState)) {//清算状态是已清算和已结算的场合
                    //调用退款操作
                    cancelOrderService.refund(orders);
                } else {
                    //调用撤销操作
                    cancelOrderService.cancelOrder(orders);
                }
            } else if (TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {//订单是付款中的场合
                if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//AD3线下通道查询接口
                    ad3Service.cancelRefunding(orders, rabbitMassage);
                } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
                    aliPayService.cancelRefunding(orders, rabbitMassage);
                } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
                    wechatService.cancelRefunding(orders, rabbitMassage);
                } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
                    megaPayService.cancelRefunding(orders, rabbitMassage);
                }
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销订单时支付中的订单的AD3的查询队列 MQ_AD3_ORDER_QUERY 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销订单时支付中的订单的AD3的查询队列 MQ_AD3_ORDER_QUERY 预警", "MQ_AD3_ORDER_QUERY 预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * AD3退款队列
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_AD3_REFUND")
    public void processAD3Refund(String value) {
        log.info("****************AD3退款队列 MQ_AD3_REFUND****************** : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            Orders orders = JSON.parseObject(rabbitMassage.getValue(), Orders.class);
            Channel channel = this.commonService.getChannelByChannelCode(orders.getChannelCode());
            if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {
                ad3Service.cancelRefund2(orders, rabbitMassage);//调用AD3通道的退款接口
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
                aliPayService.aliPayRefund2(orders, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
                wechatService.wechatRefund2(orders, rabbitMassage);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
                megaPayService.nextPosRefund2(orders, rabbitMassage);
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "AD3退款队列 MQ_AD3_REFUND 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "AD3退款队列 MQ_AD3_REFUND 预警", "MQ_AD3_REFUND 预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 撤销时更新订单表失败队列
     * 已对应
     *
     * @param value
     */
    @RabbitListener(queues = "TC_MQ_CANCEL_ORDER")
    public void processOrderWaitPay(String value) {
        log.info("****************撤销时更新订单表失败队列 TC_MQ_CANCEL_ORDER****************** : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            Orders orders = JSON.parseObject(rabbitMassage.getValue(), Orders.class);
            //根据订单id判断订单状态是否付款成功
            Orders order = ordersMapper.selectByInstitutionOrderId(orders.getInstitutionOrderId());
            if (TradeConstant.ORDER_PAY_SUCCESS.equals(order.getTradeStatus())) {
                //获取清算状态
                Integer ctState = tcsCtFlowMapper.getCTstatus(order.getId());
                //获取结算状态
                Integer stState = tcsStFlowMapper.getSTstatus(order.getId());
                if (TradeConstant.ORDER_CLEAR_SUCCESS.equals(ctState) && TradeConstant.ORDER_SETTLE_SUCCESS.equals(stState)) {//清算状态是已清算和已结算的场合
                    //调用退款操作
                    cancelOrderService.refund(order);
                } else {
                    //调用撤销操作
                    cancelOrderService.cancelOrder(order);
                }
            } else if (TradeConstant.ORDER_PAYING.equals(order.getTradeStatus())) {//付款中的队列继续放进查询队列
                //根据订单里的通道code判断去查那个通道接口
                Channel channel = this.commonService.getChannelByChannelCode(order.getChannelCode());
                if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//AD3线下通道查询接口
                    ad3Service.cancelRefunding(order, rabbitMassage);
                } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
                    aliPayService.cancelRefunding(order, rabbitMassage);
                } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
                    wechatService.cancelRefunding(order, rabbitMassage);
                } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                        channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
                    megaPayService.cancelRefunding(order, rabbitMassage);
                }
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销时更新订单表失败队列 TC_MQ_CANCEL_ORDER 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销时更新订单表失败队列 TC_MQ_CANCEL_ORDER  预警", "TC_MQ_CANCEL_ORDER 预警 ：{ " + value + " }");//邮件通知
        }

    }

    /**
     * 撤销订单时撤销时在调用清结算的资金变动时发生失败的队列
     * RV
     * 已对应
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL")
    public void processOrderFundChangeRvFail(String value) {
        log.info("****************撤销订单时撤销时在调用清结算的资金变动时发生失败的队列RV MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL**************** : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.RV, orderRefund);
            fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//上报上游清算系统的应结日期
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            log.info("************撤销订单时撤销时在调用清结算的资金变动时发生失败的队列 MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL************：{}", JSON.toJSONString(fundChangeDTO));
            if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                log.info("**************** MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL 撤销上报清结算失败-请求失败 **************** cFundChange : {}", JSON.toJSON(cFundChange));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL, JSON.toJSONString(rabbitMassage));
                return;
            } else {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                    log.info("**************** MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL 撤销上报清结算失败-业务失败 **************** cFundChange : {}", JSON.toJSON(cFundChange));
                    rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL, JSON.toJSONString(rabbitMassage));
                    return;
                }
            }
            //调取通道退款接口
            Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
            if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//AD3线下通道查询接口
                ad3Service.repeal(orderRefund, rabbitMassage);//调用AD3通道的退款接口
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
            messageFeign.sendSimple(developerMobile, "撤销订单时撤销时在调用清结算的资金变动时发生失败的队列RV MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAI 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销订单时撤销时在调用清结算的资金变动时发生失败的队列RV MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAI  预警", "MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAI 预警 ：{ " + value + " }");//邮件通知
        }

    }

    /**
     * 撤销订单时请求失败的队列
     * 调用通道退款请求失败
     * 已对应
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CANCEL_ORDER_REQUEST_FAIL")
    public void processOrderCancelRequestFail(String value) {
        log.info("****************撤销订单时请求失败的队列 MQ_CANCEL_ORDER_REQUEST_FAIL**************** : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            //获取ad3的终端号和token
            AD3LoginVO ad3LoginVO = this.ad3Service.getTerminalIdAndToken();
            if (ad3LoginVO == null) {
                log.info("************撤销订单时请求失败的队列 MQ_CANCEL_ORDER_REQUEST_FAIL 登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_REQUEST_FAIL, JSON.toJSONString(rabbitMassage));
                return;
            }
            AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
            AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
            ad3RefundDTO.setSignMsg(ad3Service.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
            ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
            HttpResponse httpResponse = ad3Service.RefundOrder(ad3RefundDTO, null);
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            log.info("************撤销订单时请求失败的队列 MQ_CANCEL_ORDER_REQUEST_FAIL************：{}", JSON.toJSONString(ad3RefundDTO));
            if (ad3RefundOrderVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//请求成功
                if (ad3RefundOrderVO.getStatus().equals(AD3Constant.REFUND_ORDER_SUCCESS)) {//退款成功
                    log.info("****************MQ_CANCEL_ORDER_REQUEST_FAIL 上游退款成功 **************** ad3RefundOrderVO : {}", JSON.toJSON(ad3RefundOrderVO));
                    //退款成功
                    refundOrderMapper.updateRefundOrder(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysOrderNo(), ad3RefundOrderVO.getRespMsg());
                    //撤销成功-更新订单的撤销状态
                    ordersMapper.updateOrderCancelStatus(orderRefund.getInstitutionOrderId(), null, TradeConstant.ORDER_CANNEL_SUCCESS);
                } else if (ad3RefundOrderVO.getStatus().equals(AD3Constant.REFUND_ORDER_FAILED)) {//退款失败
                    log.info("**************** MQ_CANCEL_ORDER_REQUEST_FAIL 上游退款失败 **************** ad3RefundOrderVO : {}", JSON.toJSON(ad3RefundOrderVO));
                    //退款失败
                    refundOrderMapper.updateRefundOrder(ad3RefundOrderVO.getOutRefundId(), TradeConstant.REFUND_FALID, ad3RefundOrderVO.getSysOrderNo(), ad3RefundOrderVO.getRespMsg());
                    //撤销失败
                    ordersMapper.updateOrderCancelStatus(orderRefund.getInstitutionOrderId(), null, TradeConstant.ORDER_CANNEL_FALID);
                }
            } else {//请求失败
                log.info("**************** MQ_CANCEL_ORDER_REQUEST_FAIL 上游请求失败 **************** rabbitMassage : {}", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_REQUEST_FAIL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销订单时请求失败的队列 MQ_CANCEL_ORDER_REQUEST_FAIL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销订单时请求失败的队列 MQ_CANCEL_ORDER_REQUEST_FAIL 预警", "MQ_CANCEL_ORDER_REQUEST_FAIL 预警 ：{ " + value + " }");//邮件通知
        }
    }

    /**
     * 撤销时退款时请求清结算资金变动失败RF
     * RF
     * 已对应
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL")
    public void processOrderCancelFundChangeRfFail(String value) {
        log.info("****************撤销时退款时请求清结算资金变动失败RF  MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL**************** : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.RF, orderRefund);
            fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//上报上游清算系统的应结日期
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                log.info("**************** 上报队列 MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL ----请求失败**************** rabbitMassage : {}", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL, JSON.toJSONString(rabbitMassage));
                return;
            } else {//请求成功
                FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                    log.info("**************** 上报队列 MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL ----业务失败**************** rabbitMassage : {}", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL, JSON.toJSONString(rabbitMassage));
                    return;
                }
            }
            //获取通道信息
            Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
            if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {//AD3线下通道查询接口
                //线下退款
                ad3Service.cancelRefund(orderRefund, fundChangeDTO);
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_BSC_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ALIPAY_CSB_OFFLINE)) {
                aliPayService.aliPayRefund(orderRefund, fundChangeDTO, new BaseResponse());
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_CSB_OFFLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.WECHAT_BSC_OFFLINE)) {
                wechatService.wechatRefund(orderRefund, fundChangeDTO, new BaseResponse());
            } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE) ||
                    channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_CSB_OFFLINE)) {
                //nextPos通道退款
                megaPayService.megaPayNextPosRefund(orderRefund, fundChangeDTO, new BaseResponse());
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销时退款时请求清结算资金变动失败RF MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销时退款时请求清结算资金变动失败RF MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL  预警", "MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL 预警 ：{ " + value + " }");//邮件通知
        }

    }

    /**
     * 撤销时退款时请求资金变动调账失败队列
     * 已对应
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CANCEL_ORDER_FUND_CHANGE_FAIL")
    public void processOrderCancelFundChangeFail(String value) {
        log.info("**************** 撤销时退款时请求资金变动调账失败队列 调账用 MQ_CANCEL_ORDER_FUND_CHANGE_FAIL**************** : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            FundChangeDTO fundChangeDTO = JSON.parseObject(orderRefund.getRemark3(), FundChangeDTO.class);
            fundChangeDTO.setSignMsg(null);
            BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
            if (cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                if (!cFundChange.getCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务失败
                    log.info("**************** MQ_CANCEL_ORDER_FUND_CHANGE_FAIL 上报清结算失败 **************** rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                } else {//业务成功
                    //退款失败
                    refundOrderMapper.updateRefundOrder(orderRefund.getId(), TradeConstant.REFUND_FALID, null, null);
                    //撤销失败
                    ordersMapper.updateOrderCancelStatus(orderRefund.getInstitutionOrderId(), null, TradeConstant.ORDER_CANNEL_FALID);
                }
            } else {//请求失败
                log.info("**************** MQ_CANCEL_ORDER_FUND_CHANGE_FAIL 请求失败 **************** rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销时退款时请求资金变动调账失败队列 调账用 MQ_CANCEL_ORDER_FUND_CHANGE_FAIL 预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销时退款时请求资金变动调账失败队列 调账用 MQ_CANCEL_ORDER_FUND_CHANGE_FAIL 预警", "MQ_CANCEL_ORDER_FUND_CHANGE_FAIL 预警 ：{ " + value + " }");//邮件通知
        }

    }

    /**
     * 撤销时退款请求上游通道请求失败队列
     * 已对应
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL")
    public void processOrderCancelChancelAcceptFail(String value) {
        log.info("************ 撤销时退款请求上游通道请求失败队列 MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL************  : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            //获取ad3的终端号和token
            AD3LoginVO ad3LoginVO = this.ad3Service.getTerminalIdAndToken();
            if (ad3LoginVO == null) {
                log.info("************撤销时退款请求上游通道请求失败队列 MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL 登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL, JSON.toJSONString(rabbitMassage));
                return;
            }
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
            AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
            ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
            ad3RefundDTO.setSignMsg(ad3Service.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
            HttpResponse httpResponse = ad3Service.RefundOrder(ad3RefundDTO, null);
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            if (ad3RefundOrderVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//请求成功
                if (ad3RefundOrderVO.getStatus().equals(AD3Constant.REFUND_ORDER_SUCCESS)) {//退款成功
                    log.info("************MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL 上游退款成功 ************ ad3RefundDTO : {} ", JSON.toJSON(ad3RefundOrderVO));
                    //退款成功
                    refundOrderMapper.updateRefundOrder(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysOrderNo(), ad3RefundOrderVO.getRespMsg());
                    //撤销成功-更新订单的撤销状态
                    ordersMapper.updateOrderCancelStatus(orderRefund.getInstitutionOrderId(), null, TradeConstant.ORDER_CANNEL_SUCCESS);
                } else if (ad3RefundOrderVO.getStatus().equals(AD3Constant.REFUND_ORDER_FAILED)) {//退款失败
                    log.info("************  MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL 上游退款失败 ************ ad3RefundDTO : {} ", JSON.toJSON(ad3RefundOrderVO));
                    FundChangeDTO fundChangeDTO = new FundChangeDTO(TradeConstant.AA, orderRefund);
                    fundChangeDTO.setShouldDealtime(orderRefund.getProductSettleCycle());//上报上游清算系统的应结日期
                    Reconciliation reconciliation = commonService.createReconciliation(orderRefund, TradeConstant.CANCEL_ORDER_REFUND_FAIL);//创建调账记录
                    reconciliationMapper.insert(reconciliation);
                    fundChangeDTO.setRefcnceFlow(reconciliation.getId());
                    fundChangeDTO.setSysorderid(orderRefund.getId());
                    fundChangeDTO.setTxnamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    fundChangeDTO.setSltamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    fundChangeDTO.setSignMsg(null);
                    BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
                    if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//退款失败调用调账请求失败的场合
                        orderRefund.setChannelNumber(ad3RefundOrderVO.getSysRefundId());
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage rabbitMSG = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMSG));
                    } else {//请求成功
                        FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                        if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务成功
                            //退款失败
                            refundOrderMapper.updateRefundOrder(orderRefund.getId(), TradeConstant.REFUND_FALID, ad3RefundOrderVO.getSysOrderNo(), ad3RefundOrderVO.getRespMsg());
                            //撤销失败
                            ordersMapper.updateOrderCancelStatus(orderRefund.getInstitutionOrderId(), null, TradeConstant.ORDER_CANNEL_FALID);
                        } else {//业务失败
                            orderRefund.setChannelNumber(ad3RefundOrderVO.getSysRefundId());
                            orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                            RabbitMassage rabbitMSG = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                            rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMSG));
                        }
                    }
                }
            } else {//请求失败
                log.info("************************ MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL 请求失败 ***************ad3RefundDTO : {} ", JSON.toJSON(ad3RefundDTO));
                rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL, JSON.toJSONString(orderRefund));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "撤销时退款请求上游通道请求失败队列 MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销时退款请求上游通道请求失败队列 MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL预警", "MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL预警 ：{ " + value + " }");//邮件通知
        }

    }

    /**
     * 撤销时撤销通道退款失败队列
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL")
    public void processOrderCancelChancelRefundFail(String value) {
        log.info("************ 撤销时撤销通道退款失败队列 MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL************  : {} ", value);
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减-1
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            Channel channel = this.commonService.getChannelByChannelCode(orderRefund.getChannelCode());
            if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_OFFLINE)) {
                ad3Service.repeal(orderRefund, rabbitMassage);//调用AD3通道的退款接口
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
            messageFeign.sendSimple(developerMobile, "撤销时撤销通道退款失败队列 MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL预警 ：{ " + value + " }");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "撤销时撤销通道退款失败队列 MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL预警", "MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL预警 ：{ " + value + " }");//邮件通知
        }
    }

}
