package com.payment.trade.rabbitmq;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.alipay.AliPayRefundDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.OrderRefund;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.entity.Reconciliation;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.dao.ChannelMapper;
import com.payment.trade.dao.OrderRefundMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dao.ReconciliationMapper;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-06-25 11:40
 **/
@Component
@Slf4j
public class AlipayMQReceive {

    @Autowired
    private CommonService commonService;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private OrderRefundMapper refundOrderMapper;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ChannelsFeign channelsFeign;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    /**
     * 退款请求失败对列 (线上)
     *
     * @param value
     */
    @RabbitListener(queues = "MQ_TK_APLIPAY_QQSB_DL")
    public void processTKXSQQSB(String value) {
        RabbitMassage rabbitMassage = JSON.parseObject(value, RabbitMassage.class);
        if (rabbitMassage.getCount() > 0) {
            rabbitMassage.setCount(rabbitMassage.getCount() - 1);//请求次数减一
            log.info("----------------- MQ_TK_APLIPAY_QQSB_DL 退款请求失败对列  ----------------  : {} ", value);
            OrderRefund orderRefund = JSON.parseObject(rabbitMassage.getValue(), OrderRefund.class);
            FundChangeDTO fundChangeDTO = JSON.parseObject(orderRefund.getRemark3(), FundChangeDTO.class);
            Channel channel = channelMapper.selectByChannelCode(orderRefund.getChannelCode());
            AliPayRefundDTO aliPayRefundDTO = new AliPayRefundDTO(orderRefund, channel);
            BaseResponse response = channelsFeign.alipayRefund(aliPayRefundDTO);
            if (response.getCode().equals("200")) {//请求成功
                Map<String, String> map = (Map<String, String>) response.getData();
                if (response.getMsg().equals("success")) {//退款成功
                    log.info("-----------------  AliPay线下退款方法 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
                    //退款成功
                    refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, map.get("alipay_trans_id"), null);
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
                    //退款失败
                    log.info("----------------- 退款操作 退款失败 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
                    //添加调账记录
                    Reconciliation reconciliation = commonService.createReconciliation(orderRefund, TradeConstant.REFUND_FAIL_RECONCILIATION);
                    reconciliationMapper.insert(reconciliation);
                    fundChangeDTO.setRefcnceFlow(reconciliation.getId());
                    fundChangeDTO.setSysorderid(orderRefund.getId());
                    fundChangeDTO.setTradetype(TradeConstant.AA);
                    fundChangeDTO.setTxnamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    fundChangeDTO.setSltamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                    fundChangeDTO.setBalancetype(TradeConstant.NORMAL_FUND);
                    fundChangeDTO.setSignMsg(null);
                    BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
                    if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                        orderRefund.setChannelNumber( map.get("alipay_trans_id"));
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage massage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("***************** 退款操作 上报队列 MQ_QJS_TZSB_DL ***************** rabbitMassage : {} ", JSON.toJSON(massage));
                        rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(massage));
                    } else {//请求成功
                        FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                        if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                            refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_FALID, map.get("alipay_trans_id"), fundChangeVO.getRespMsg());
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
                        } else {
                            orderRefund.setChannelNumber(map.get("alipay_trans_id"));
                            orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                            RabbitMassage massage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                            log.info("*****************退款操作 上报队列 MQ_QJS_TZSB_DL ***************** rabbitMassage : {} ", JSON.toJSON(massage));
                            rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(massage));
                        }
                    }
                }
            } else { //请求失败
                orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                log.info("*****************退款操作 请求失败上报队列 MQ_TK_APLIPAY_QQSB_DL ***************** orderRefund : {} ", JSON.toJSON(rabbitMassage));
                rabbitMQSender.send(AD3MQConstant.MQ_TK_APLIPAY_QQSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //预警机制
            messageFeign.sendSimple(developerMobile, "退款请求失败对列(APLIPAY) MQ_TK_APLIPAY_QQSB_DL 预警");//短信通知
            messageFeign.sendSimpleMail(developerEmail, "退款请求失败对列(APLIPAY) MQ_TK_APLIPAY_QQSB_DL 预警", "MQ_TK_APLIPAY_QQSB_DL 预警 ：{ " + value + " }");//邮件通知
        }
    }

}
