package com.payment.trade.channels.ad3Online.impl;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.HttpResponse;
import com.payment.common.utils.BeanToMapUtil;
import com.payment.common.utils.HttpClientUtils;
import com.payment.common.utils.RSAUtils;
import com.payment.common.utils.SignTools;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.ad3Online.AD3OnlineAcquireService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.OrderRefundMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dao.ReconciliationMapper;
import com.payment.trade.dto.AD3OnlineAcquireDTO;
import com.payment.trade.dto.AD3OnlineOrderQueryDTO;
import com.payment.trade.dto.SendAdRefundDTO;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.vo.RefundAdResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * AD3线上相关业务
 */
@Service
@Slf4j
@Transactional
public class AD3OnlineAcquireServiceImpl implements AD3OnlineAcquireService {


    @Autowired
    private CommonService commonService;

    @Autowired
    private OrderRefundMapper refundOrderMapper;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;


    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate ad3线上退款接口
     **/
    @Override
    public HttpResponse RefundOrder(SendAdRefundDTO sendAdRefundDTO, Map<String, Object> headerMap) {
        HttpResponse httpResponse = HttpClientUtils.reqPost(ad3ParamsConfig.getAd3Url() + "/v1/beforSendToItsRefund.json", sendAdRefundDTO, null);
        log.info("-----------------ad3线上退款接口---------------- RefundOrder - httpResponse : {} ", JSON.toJSON(httpResponse));
        return httpResponse;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate 判断退款是否成功
     **/
    @Override
    public boolean judgeRefundAdResponseVO(RefundAdResponseVO refundAdResponseVO) {
        if (refundAdResponseVO.getStatus() == null) {
            return false;
        } else if (refundAdResponseVO.getStatus().equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/15
     * @Descripate 退款时 ---  通道退款操作
     **/
    @Override
    @Async
    public void doUsRefundInRef(BaseResponse baseResponse, FundChangeDTO fundChangeDTO, OrderRefund orderRefund) {
        SendAdRefundDTO sendAdRefundDTO = new SendAdRefundDTO(ad3ParamsConfig.getMerchantCode(), orderRefund);
        sendAdRefundDTO.setMerchantSignType(ad3ParamsConfig.getMerchantSignType());
        sendAdRefundDTO.setSignMsg(this.signMsg(sendAdRefundDTO));
        HttpResponse httpResponse = this.RefundOrder(sendAdRefundDTO, null);
        if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {  //请求成功
            RefundAdResponseVO refundAdResponseVO = JSONObject.parseObject(httpResponse.getJsonObject().toJSONString(), RefundAdResponseVO.class);
            if (this.judgeRefundAdResponseVO(refundAdResponseVO)) {
                log.info("----------------- 退款操作 退款成功 -------------- refundAdResponseVO : {} ", JSON.toJSON(refundAdResponseVO));
                //退款成功
                refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, refundAdResponseVO.getTxnId(), refundAdResponseVO.getRespMsg());
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
                //退款失败
                log.info("----------------- 退款操作 退款失败 -------------- refundAdResponseVO : {} ", JSON.toJSON(refundAdResponseVO));
                baseResponse.setMsg(EResultEnum.REFUND_FAIL.getCode());
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
                    orderRefund.setChannelNumber(refundAdResponseVO.getTxnId());
                    orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                    log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                } else {//请求成功
                    FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                    if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                        refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_FALID, refundAdResponseVO.getTxnId(), fundChangeVO.getRespMsg());
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
                        orderRefund.setChannelNumber(refundAdResponseVO.getTxnId());
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                        rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                    }
                }
            }
        } else { //请求失败
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            log.info("----------------- 退款操作 请求失败上报队列 MQ_TK_XS_QQSB_DL -------------- orderRefund : {} ", JSON.toJSON(rabbitMassage));
            rabbitMQSender.send(AD3MQConstant.MQ_TK_XS_QQSB_DL, JSON.toJSONString(rabbitMassage));
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/4
     * @Descripate 创建网银退款实体
     **/
    @Override
    public String repeatRefund(String name, OrderRefund orderRefund) {
        SendAdRefundDTO sendAdRefundDTO = new SendAdRefundDTO(ad3ParamsConfig.getMerchantCode(), orderRefund);
        sendAdRefundDTO.setSignMsg(this.signMsg(sendAdRefundDTO));
        HttpResponse httpResponse = this.RefundOrder(sendAdRefundDTO, null);
        if (httpResponse.getHttpStatus() != null && httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) { //请求成功
            RefundAdResponseVO refundAdResponseVO = JSONObject.parseObject(httpResponse.getJsonObject().toJSONString(), RefundAdResponseVO.class);
            if (this.judgeRefundAdResponseVO(refundAdResponseVO)) {
                log.info("----------------- 撤销操作 上游退款成功 -------------- refundAdResponseVO : {}", JSON.toJSON(refundAdResponseVO));
                //退款成功
                refundOrderMapper.updateStatutsByName(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, refundAdResponseVO.getTxnId(), null, name);
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
                return refundAdResponseVO.getRespMsg();
            }
        }
        return "请求失败";
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/15
     * @Descripate 退款时 ---  通道撤销操作
     **/
    @Override
    @Async
    public void doUsCancelInRef(BaseResponse baseResponse, OrderRefund orderRefund,RabbitMassage rabbitMassage) {
        SendAdRefundDTO sendAdRefundDTO = new SendAdRefundDTO(ad3ParamsConfig.getMerchantCode(), orderRefund);
        sendAdRefundDTO.setMerchantSignType(ad3ParamsConfig.getMerchantSignType());
        sendAdRefundDTO.setSignMsg(this.signMsg(sendAdRefundDTO));
        HttpResponse httpResponse = this.RefundOrder(sendAdRefundDTO, null);
        if (httpResponse.getHttpStatus() != null && httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) { //请求成功
            RefundAdResponseVO refundAdResponseVO = JSONObject.parseObject(httpResponse.getJsonObject().toJSONString(), RefundAdResponseVO.class);
            if (this.judgeRefundAdResponseVO(refundAdResponseVO)) {
                log.info("----------------- 撤销操作 上游退款成功 -------------- refundAdResponseVO : {}", JSON.toJSON(refundAdResponseVO));
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
                log.info("----------------- 撤销操作 上游退款失败 -------------- refundAdResponseVO : {}", JSON.toJSON(refundAdResponseVO));
                //退款失败
                baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
                refundOrderMapper.updateRemark(refundAdResponseVO.getMerOrderNo(), refundAdResponseVO.getRespMsg());
                rabbitMQSender.send(AD3MQConstant.E_MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else { //请求失败
            log.info("----------------- 撤销操作 请求失败 上报线上撤销请求失败 -------------- rabbitMassage : {}", JSON.toJSONString(rabbitMassage));
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            rabbitMQSender.send(AD3MQConstant.MQ_CX_XS_QQSB_DL, JSON.toJSONString(rabbitMassage));
        }
    }

    /**
     * 对向ad3的请求进行签名
     *
     * @param object
     * @return
     */
    @Override
    public String signMsg(Object object) {
        //去空
        String privateKey = ad3ParamsConfig.getPlatformProvidesPrivateKey().replaceAll("\\s*", "");
        HashMap<String, Object> dtoMap = BeanToMapUtil.beanToMap(object);
        HashMap<String, String> map = new HashMap<>();
        Set<String> keySet = dtoMap.keySet();
        for (String dtoKey : keySet) {
            map.put(dtoKey, String.valueOf(dtoMap.get(dtoKey)));
        }
        byte[] msg = SignTools.getSignStr(map).getBytes();
        String signMsg = null;
        try {
            //签名
            signMsg = RSAUtils.sign(msg, privateKey);
        } catch (Exception e) {
            log.info("----------------- 线上签名错误信息记录 ----------------签名原始明文:{},签名:{}", msg, signMsg);
            throw new BusinessException(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        return signMsg;
    }

    /**
     * ad3 线上网关收单接口
     *
     * @param
     * @param response
     * @return 线上网关收单接口响应实体
     */
    @Override
    public BaseResponse onlineOrder(Orders orders, Channel channel, BaseResponse response) {
        //截取币种默认值
        if (!commonService.interceptDigit(orders, response)) {
            log.info("-----------------AD3线上收单参数--------------【币种默认值】未取到 tradeCurrency:{}", orders.getTradeCurrency());
            return response;
        }
        //封装参数
        AD3OnlineAcquireDTO ad3OnlineAcquireDTO = getAd3OnlineAcquireDTOAttr(orders, channel);
        log.info("-------AD3线上收单参数-------AD3OnlineAcquireDTO:{}", JSON.toJSON(ad3OnlineAcquireDTO));
        //返回收款消息
        log.info("-----------------URL---------------- type:{}**issuerId:{}**url:{}", channel.getChannelEnName(), channel.getIssuerId(), ad3ParamsConfig.getAd3Url() + "/v1/beforSendToItsPay");
        long start = System.currentTimeMillis();
        cn.hutool.http.HttpResponse execute = HttpRequest.post(ad3ParamsConfig.getAd3Url() + "/v1/beforSendToItsPay")
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(BeanToMapUtil.beanToMap(ad3OnlineAcquireDTO))
                .timeout(10000)
                .execute();
        long end = System.currentTimeMillis();
        log.info("-------通道消耗时间-------Time:{} MS", (end - start));
        int status = execute.getStatus();
        //判断HTTP状态码
        if (status != AsianWalletConstant.HTTP_SUCCESS_STATUS) {
            log.info("----------------------向上游接口发送订单失败日志记录----------------------http状态码:{},ad3OnlineAcquireDTO:{}", status, JSON.toJSON(ad3OnlineAcquireDTO));
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            //上游返回的错误code
            orders.setRemark4(String.valueOf(status));
            ordersMapper.updateByPrimaryKeySelective(orders);
            return response;
        }
        String body = execute.body();
        if (StringUtils.isEmpty(body)) {
            log.info("----------------------向上游接口发送订单失败日志记录----------------------ad3OnlineAcquireDTO:{}", JSON.toJSON(ad3OnlineAcquireDTO));
            response.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return response;
        }
        response.setData(body);
        return response;
    }


    /**
     * 封装AD3收单参数
     *
     * @param orders
     * @param channel
     * @return
     */
    @Override
    public AD3OnlineAcquireDTO getAd3OnlineAcquireDTOAttr(Orders orders, Channel channel) {
        AD3OnlineAcquireDTO ad3OnlineAcquireDTO = new AD3OnlineAcquireDTO(orders, ad3ParamsConfig.getMerchantCode(), channel.getPayCode(), channel.getIssuerId());
        //回调地址
        ad3OnlineAcquireDTO.setPickupUrl(ad3ParamsConfig.getChannelCallbackUrl() + "/onlinecallback/paysuccess");
        ad3OnlineAcquireDTO.setReceiveUrl(ad3ParamsConfig.getChannelCallbackUrl() + "/onlinecallback/callback");
        ad3OnlineAcquireDTO.setSignMsg(signMsg(ad3OnlineAcquireDTO));
        return ad3OnlineAcquireDTO;
    }

    /**
     * ad3 线上订单查询接口
     *
     * @param
     * @return
     */
    @Override
    public HttpResponse ad3OnlineOrderQuery(AD3OnlineOrderQueryDTO ad3OnlineOrderQueryDTO, Map<String, Object> headerMap) {
        ad3OnlineOrderQueryDTO.setSignMsg(signMsg(ad3OnlineOrderQueryDTO));
        //log.info("-----------------调用AD3线上订单查询接口 参数---------------- ad3OnlineOrderQueryDTO : {} ", JSON.toJSON(ad3OnlineOrderQueryDTO));
        HttpResponse httpResponse = HttpClientUtils.reqPost(ad3ParamsConfig.getAd3Url() + "/v1/merQueryOneOrder.json", ad3OnlineOrderQueryDTO, headerMap);
        //log.info("-----------------调用AD3线上订单查询接口 返回---------------- AD3OnlineOrderQueryVO - httpResponse : {} ", JSON.toJSON(ad3OnlineOrderQueryDTO));
        return httpResponse;
    }


}
