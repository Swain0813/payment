package com.payment.trade.channels.ad3Offline.impl;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.*;
import com.payment.common.enums.Status;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.HttpResponse;
import com.payment.common.utils.HttpClientUtils;
import com.payment.common.utils.MD5Util;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.common.utils.SignTools;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.ad3Offline.AD3Service;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.OrderRefundMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dao.ReconciliationMapper;
import com.payment.trade.dto.*;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.utils.AbstractHandlerAdapter;
import com.payment.trade.utils.HandlerType;
import com.payment.trade.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AD3线下相关业务
 */
@Service
@Slf4j
@Transactional
@HandlerType(TradeConstant.AD3_OFFLINE)
public class AD3ServiceImpl extends AbstractHandlerAdapter implements AD3Service {

    @Autowired
    @Qualifier(value = "ad3ParamsConfig")
    private AD3ParamsConfig ad3ParamsConfig;

    @Autowired
    private CommonService commonService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private OrderRefundMapper refundOrderMapper;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 终端登陆接口
     *
     * @return
     */
    @Override
    public HttpResponse ad3Login(AD3LoginDTO ad3LoginDTO, Map<String, Object> headerMap) {
        HttpResponse httpResponse = HttpClientUtils.reqPost(ad3ParamsConfig.getAd3Url() + "/terminalLogin.json", ad3LoginDTO, headerMap);
        log.info("----------------- AD3登陆接口信息 返回----------------");
        return httpResponse;
    }

    /**
     * 终端CSB扫码支付接口
     *
     * @param ad3CSBScanPayDTO 终端CSB扫码支付输入实体
     * @param headerMap        请求头map
     * @return CSB扫码支付返回实体
     */
    @Override
    public HttpResponse ad3CSBScanPay(AD3CSBScanPayDTO ad3CSBScanPayDTO, Map<String, Object> headerMap) {
        log.info("=========调用【AD3-CSB接口】开始时间=========");
        HttpResponse httpResponse = HttpClientUtils.reqPost(ad3ParamsConfig.getAd3Url() + "/posPay.json", ad3CSBScanPayDTO, headerMap);
        log.info("=========调用【AD3-CSB接口】结束时间=========");
        return httpResponse;
    }

    /**
     * 终端BSC扫码支付接口
     *
     * @param ad3BSCScanPayDTO 终端BSC扫码支付输入实体
     * @param headerMap        请求头map
     * @return BSC扫码支付返回实体
     */
    @Override
    public HttpResponse ad3BSCScanPay(AD3BSCScanPayDTO ad3BSCScanPayDTO, Map<String, Object> headerMap) {
        log.info("=========调用【AD3-BSC接口】开始时间=========");
        HttpResponse httpResponse = HttpClientUtils.reqPost(ad3ParamsConfig.getAd3Url() + "/posPay.json", ad3BSCScanPayDTO, headerMap);
        log.info("=========调用【AD3-BSC接口】结束时间=========");
        return httpResponse;
    }

    /**
     * 终端单笔订单查询接口
     *
     * @return
     */
    @Override
    public HttpResponse ad3QueryOneOrder(AD3QuerySingleOrderDTO ad3QuerySingleOrderDTO, Map<String, Object> headerMap) {
        log.info("=========调用【AD3-QueryOrder接口】开始时间=========");
        HttpResponse httpResponse = HttpClientUtils.reqPost(ad3ParamsConfig.getAd3Url() + "/terminalQueryOrder.json", ad3QuerySingleOrderDTO, headerMap);
        log.info("=========调用【AD3-QueryOrder接口】结束时间=========");
        return httpResponse;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/14
     * @Descripate ad3线下退款接口
     **/
    @Override
    public HttpResponse RefundOrder(AD3RefundDTO ad3RefundDTO, Map<String, Object> headerMap) {
        log.info("=========调用【AD3-Refund接口】开始时间=========");
        HttpResponse httpResponse = HttpClientUtils.reqPost(ad3ParamsConfig.getAd3Url() + "/posRefund.json", ad3RefundDTO, headerMap);
        log.info("=========调用【AD3-Refund接口】结束时间=========");
        return httpResponse;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/4/4
     * @Descripate 重复请求退款
     **/
    @Override
    public String repeatRefund(String name, OrderRefund orderRefund) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("************重复请求退款 --- 退款操作AD3登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
            return "AD3登录失败";
        }
        AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
        AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
        ad3RefundDTO.setSignMsg(this.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
        ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
        HttpResponse httpResponse = this.RefundOrder(ad3RefundDTO, null);
        if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {//请求成功
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            if (ad3RefundOrderVO.getRespCode() != null && ad3RefundOrderVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//退款成功
                log.info("----------------- 重复请求退款 上游退款成功 -------------- ad3RefundOrderVO : {} ", JSON.toJSON(ad3RefundOrderVO));
                refundOrderMapper.updateStatutsByName(ad3RefundOrderVO.getOutRefundId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysRefundId(), ad3RefundOrderVO.getRespMsg(), name);
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
                return ad3RefundOrderVO.getRespMsg();
            }
        }
        return "请求失败";
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/15
     * @Descripate 线下退款时 --- 退款操作
     **/
    @Override
    @Async
    public void doUsRefundInRef(BaseResponse baseResponse, OrderRefund orderRefund, FundChangeDTO fundChangeDTO) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("************退款时 --- 退款操作AD3登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
            return;
        }
        AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
        AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
        ad3RefundDTO.setSignMsg(this.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
        ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
        HttpResponse httpResponse = this.RefundOrder(ad3RefundDTO, null);
        if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {//请求成功
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            if (ad3RefundOrderVO.getRespCode() != null && ad3RefundOrderVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//退款成功
                log.info("----------------- 退款操作 上游退款成功 -------------- ad3RefundOrderVO : {} ", JSON.toJSON(ad3RefundOrderVO));
                refundOrderMapper.updateStatuts(ad3RefundOrderVO.getOutRefundId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysRefundId(), ad3RefundOrderVO.getRespMsg());
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
                log.info("----------------- 退款操作 上游退款失败 -------------- ad3RefundOrderVO : {} ", JSON.toJSON(ad3RefundOrderVO));
                baseResponse.setMsg(EResultEnum.REFUND_FAIL.getCode());
                //创建调账记录
                Reconciliation reconciliation = commonService.createReconciliation(orderRefund, TradeConstant.REFUND_FAIL_RECONCILIATION);
                reconciliationMapper.insert(reconciliation);
                fundChangeDTO.setRefcnceFlow(reconciliation.getId());
                fundChangeDTO.setSysorderid(orderRefund.getId());
                fundChangeDTO.setSignMsg(null);
                fundChangeDTO.setTradetype(TradeConstant.AA);
                fundChangeDTO.setBalancetype(TradeConstant.NORMAL_FUND);
                fundChangeDTO.setTxnamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                fundChangeDTO.setSltamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));
                BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
                if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {
                    orderRefund.setChannelNumber(ad3RefundOrderVO.getSysRefundId());
                    orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                    log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                } else {
                    FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                    if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                        refundOrderMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_FALID, ad3RefundOrderVO.getSysRefundId(), fundChangeVO.getRespMsg());
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
                        orderRefund.setChannelNumber(ad3RefundOrderVO.getSysRefundId());
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                        rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                    }
                }
            }
        } else {//请求失败
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            log.info("----------------- 退款操作 请求失败 上报队列 MQ_TK_XX_QQSB_DL -------------- orderRefund : {} ", JSON.toJSON(rabbitMassage));
            rabbitMQSender.send(AD3MQConstant.MQ_TK_XX_QQSB_DL, JSON.toJSONString(rabbitMassage));
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/3/15
     * @Descripate 退款时 --- 撤销操作
     **/
    @Override
    @Async
    public void doUsCancelInRef(BaseResponse baseResponse, OrderRefund orderRefund, RabbitMassage rabbitMassage) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("************退款时 --- 撤销操作AD3登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
            return;
        }
        AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
        AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
        ad3RefundDTO.setSignMsg(this.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
        ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
        HttpResponse httpResponse = this.RefundOrder(ad3RefundDTO, null);
        if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {//请求成功
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            if (ad3RefundOrderVO.getRespCode() != null && ad3RefundOrderVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//退款成功
                log.info("----------------- 撤销操作 上游退款成功 -------------- ad3RefundOrderVO : {}", JSON.toJSON(ad3RefundOrderVO));
                //退款成功
                refundOrderMapper.updateStatuts(ad3RefundOrderVO.getOutRefundId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysRefundId(), ad3RefundOrderVO.getRespMsg());
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
            } else if (ad3RefundOrderVO.getStatus() != null && ad3RefundOrderVO.getStatus().equals(AD3Constant.REFUND_ORDER_FAILED)) {//退款失败
                log.info("----------------- 撤销操作 上游退款失败 -------------- ad3RefundOrderVO : {}", JSON.toJSON(ad3RefundOrderVO));
                baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
                //退款失败
                refundOrderMapper.updateRemark(orderRefund.getId(), ad3RefundOrderVO.getRespMsg());
                rabbitMQSender.send(AD3MQConstant.E_MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {//请求失败
            log.info("----------------- 撤销操作 上游请求失败 -------------- rabbitMassage : {}", JSON.toJSON(rabbitMassage));
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            rabbitMQSender.send(AD3MQConstant.MQ_CX_XX_QQSB_DL, JSON.toJSONString(rabbitMassage));
        }
    }

    /**
     * 撤销时-撤销操作
     *
     * @Author yangshanlong
     */
    @Override
    @Async
    public void repeal(OrderRefund orderRefund, RabbitMassage rabbitMassage) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("************撤销时-调用线下的通道退款AD3登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
            return;
        }
        AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
        AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
        ad3RefundDTO.setSignMsg(this.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
        ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
        HttpResponse httpResponse = this.RefundOrder(ad3RefundDTO, null);
        log.info("**************AD3 撤销时-撤销操作调用AD3线下退款接口的输入参数：{}", JSON.toJSONString(ad3RefundDTO));
        if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {//请求成功
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            log.info("撤销时撤销操作请求AD3通道退款接口返回结果：{}", JSON.toJSON(ad3RefundOrderVO));
            if (ad3RefundOrderVO.getRespCode() != null && ad3RefundOrderVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//退款成功
                log.info("撤销时撤销操作请求AD3通道退款接口退款成功：", orderRefund.getId());
                //更新退款表
                refundOrderMapper.updateRefundOrder(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysOrderNo(), ad3RefundOrderVO.getRespMsg());
                //撤销成功-更新订单的撤销状态
                ordersMapper.updateOrderCancelStatus(orderRefund.getInstitutionOrderId(), null, TradeConstant.ORDER_CANNEL_SUCCESS);
            } else if (ad3RefundOrderVO.getStatus() != null && ad3RefundOrderVO.getStatus().equals(AD3Constant.REFUND_ORDER_FAILED)) {//退款失败
                log.info("撤销时撤销操作请求AD3通道退款接口退款失败：", orderRefund.getId());
                //退款失败更新退款表的备注
                refundOrderMapper.updateRefundOrderFail(orderRefund.getId(), ad3RefundOrderVO.getRespMsg());
                //上游通道退款失败更新订单表的备注
                ordersMapper.updateCancelOrderRemark(orderRefund.getInstitutionOrderId(), ad3RefundOrderVO.getRespMsg());
                rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL, JSON.toJSONString(rabbitMassage));//撤销时撤销通道退款失败
            }
        } else {//请求失败
            log.info("撤销时撤销操作请求AD3通道退款接口请求失败：", orderRefund.getId());
            rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_REQUEST_FAIL, JSON.toJSONString(rabbitMassage));//撤销时撤销请求失败队列
        }
    }

    /**
     * 撤销时---退款操作 上报清结算
     *
     * @param orderRefund
     * @param fundChangeDTO
     * @Author yangshanlong
     */
    @Override
    @Async
    public void cancelRefund(OrderRefund orderRefund, FundChangeDTO fundChangeDTO) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("************撤销时退款--调用线下通道退款-AD3登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
            return;
        }
        AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
        AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orderRefund);
        ad3RefundDTO.setSignMsg(this.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
        ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
        HttpResponse httpResponse = this.RefundOrder(ad3RefundDTO, null);
        log.info("***************************AD3 撤销时---退款操作时调用AD3线下退款接口的输入参数：{}", JSON.toJSONString(ad3RefundDTO));
        if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {//请求成功
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            if (ad3RefundOrderVO.getRespCode() != null && ad3RefundOrderVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//退款成功
                log.info("撤销操作时退款--调用线下通道退款成功：", orderRefund.getId());
                //更新退款表
                refundOrderMapper.updateRefundOrder(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, ad3RefundOrderVO.getSysOrderNo(), ad3RefundOrderVO.getRespMsg());
                //撤销成功-更新订单的撤销状态
                ordersMapper.updateOrderCancelStatus(orderRefund.getInstitutionOrderId(), null, TradeConstant.ORDER_CANNEL_SUCCESS);
            } else {//退款失败 交易类型是AA-调账
                log.info("撤销操作时退款--调用线下通道退款失败：", orderRefund.getId());
                //创建调账记录--撤销退款
                Reconciliation reconciliation = commonService.createReconciliation(orderRefund, TradeConstant.CANCEL_ORDER_REFUND_FAIL);
                reconciliationMapper.insert(reconciliation);//创建调账记录
                fundChangeDTO.setRefcnceFlow(reconciliation.getId());
                fundChangeDTO.setSysorderid(orderRefund.getId());
                fundChangeDTO.setSignMsg(null);
                fundChangeDTO.setTradetype(TradeConstant.AA);
                fundChangeDTO.setBalancetype(TradeConstant.NORMAL_FUND);//调账传正常资金
                fundChangeDTO.setTxnamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));//交易金额
                fundChangeDTO.setSltamount(String.valueOf(orderRefund.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP)));//结算金额
                BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO, null);
                if (!cFundChange.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求失败
                    orderRefund.setChannelNumber(ad3RefundOrderVO.getSysRefundId());
                    orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                    log.info("********** 撤销操作时调用退款操作 资金变动失败调账请求失败的场合 ********** rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));//撤销时退款调用清结算资金变动失败队列
                } else {
                    FundChangeVO fundChangeVO = (FundChangeVO) cFundChange.getData();
                    if (fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//调账成功的场合
                        //退款失败
                        refundOrderMapper.updateRefundOrder(orderRefund.getId(), TradeConstant.REFUND_FALID, ad3RefundOrderVO.getSysOrderNo(), fundChangeVO.getRespMsg());
                        //撤销失败
                        ordersMapper.updateOrderCancelStatus(orderRefund.getInstitutionOrderId(), null, TradeConstant.ORDER_CANNEL_FALID);
                    } else {//调账失败的场合
                        orderRefund.setChannelNumber(ad3RefundOrderVO.getSysRefundId());
                        orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("********** 撤销操作时调用退款操作 资金变动调账失败 ********** rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                        rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));//撤销时退款调用清结算资金变动失败队列
                    }
                }
            }
        } else {//请求失败
            log.info("**********撤销时退款操作 请求失败 ********** orderRefund : {} ", JSON.toJSON(orderRefund));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            rabbitMQSender.send(AD3MQConstant.MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL, JSON.toJSONString(rabbitMassage));//撤销时退款请求上游通道失败队列
        }
    }

    /**
     * 撤销时退款--调用线下通道退款 不上报清结算
     *
     * @param orders
     * @param rabbitMassage
     */
    @Override
    public void cancelRefund2(Orders orders, RabbitMassage rabbitMassage) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("************撤销订单时支付中的订单的AD3的查询队列 MQ_AD3_ORDER_QUERY 登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_AD3_REFUND, JSON.toJSONString(rabbitMassage));
            return;
        }
        //调用AD3退款接口直接退款
        AD3RefundDTO ad3RefundDTO = new AD3RefundDTO(ad3ParamsConfig.getMerchantCode());
        AD3RefundWorkDTO ad3RefundWorkDTO = new AD3RefundWorkDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getTradePassword(), orders);
        ad3RefundDTO.setSignMsg(this.createAD3Signature(ad3RefundDTO, ad3RefundWorkDTO, ad3LoginVO.getToken()));
        ad3RefundDTO.setBizContent(ad3RefundWorkDTO);
        HttpResponse httpResponse = this.RefundOrder(ad3RefundDTO, null);
        log.info("************调用AD3退款接口直接退款的参数 MQ_AD3_REFUND************：{}", JSON.toJSONString(ad3RefundDTO));
        if (httpResponse.getHttpStatus() == AsianWalletConstant.HTTP_SUCCESS_STATUS) {//请求成功
            AD3RefundOrderVO ad3RefundOrderVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3RefundOrderVO.class);
            log.info("撤销时直接调用AD3退款的接口返回结果：{}", JSON.toJSON(ad3RefundOrderVO));
            if (ad3RefundOrderVO.getRespCode() != null && ad3RefundOrderVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//退款成功
                log.info("撤销时直接调用AD3退款的接口返回退款成功：", orders.getId());
                //撤销成功
                ordersMapper.updateOrderCancelStatus(orders.getInstitutionOrderId(), orders.getDeviceOperator(), TradeConstant.ORDER_CANNEL_SUCCESS);
            } else if (ad3RefundOrderVO.getStatus() != null && ad3RefundOrderVO.getStatus().equals(AD3Constant.REFUND_ORDER_FAILED)) {//退款失败
                log.info("撤销时直接调用AD3退款的接口返回退款失败：", orders.getId());
                //撤销失败
                ordersMapper.updateOrderCancelStatus(orders.getInstitutionOrderId(), orders.getDeviceOperator(), TradeConstant.ORDER_CANNEL_FALID);
            }
        } else {//请求失败
            log.info("撤销时直接调用AD3退款的接口返回请求失败：", orders.getId());
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_AD3_REFUND, JSON.toJSONString(rabbitMassage));
        }

    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/28
     * @Descripate 处于退款中时
     **/
    @Override
    public void cancelRefunding(Orders orders, RabbitMassage rabbitMassage) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("************撤销订单时支付中的订单的AD3的查询队列 MQ_AD3_ORDER_QUERY 登录时未获取到终端号和token*****************ad3LoginVO：{}", JSON.toJSON(ad3LoginVO));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
            return;
        }
        //AD3通道订单信息-查询订单接口公共参数实体
        AD3QuerySingleOrderDTO ad3QuerySingleOrderDTO = new AD3QuerySingleOrderDTO(ad3ParamsConfig.getMerchantCode());//商户号
        //查询订单接业务共参数实体
        QueryOneOrderBizContentDTO queryBizContent = new QueryOneOrderBizContentDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), AD3Constant.TRADE_ORDER, orders.getId(), "");
        //生成查询签名
        String querySign = this.createAD3Signature(ad3QuerySingleOrderDTO, queryBizContent, ad3LoginVO.getToken());
        ad3QuerySingleOrderDTO.setBizContent(queryBizContent);
        ad3QuerySingleOrderDTO.setSignMsg(querySign);
        HttpResponse httpResponse = this.ad3QueryOneOrder(ad3QuerySingleOrderDTO, null);
        log.info("************撤销订单时支付中的订单的AD3的查询的参数 MQ_AD3_ORDER_QUERY************：{}", JSON.toJSONString(ad3QuerySingleOrderDTO));
        if (httpResponse == null || !httpResponse.getHttpStatus().equals(AsianWalletConstant.HTTP_SUCCESS_STATUS)) {//请求失败
            log.info("****************  请求失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
            return;
        }
        //反序列化Json数据
        AD3OrdersVO ad3OrdersVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3OrdersVO.class);
        if (ad3OrdersVO == null || !ad3OrdersVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {
            log.info("**************撤销订单时支付中的订单的AD3的查询队列 MQ_AD3_ORDER_QUERY AD3通道订单信息查询异常*****************");
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
            return;
        }
        //校验交易金额与交易币种
        BigDecimal ad3Amount = new BigDecimal(ad3OrdersVO.getMerorderAmount());
        BigDecimal tradeAmount = orders.getTradeAmount();
        if (ad3Amount.compareTo(tradeAmount) != 0 || !ad3OrdersVO.getMerorderCurrency().equals(orders.getTradeCurrency())) {
            log.info("**************  AD3查询接口返回的订单金额和币种和订单信息不一致 ************** 订单信息不匹配");
            return;
        }
        if (ad3OrdersVO == null || !ad3OrdersVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {//业务失败
            log.info("****************  业务失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
            return;
        } else {//业务成功
            //ad3查询订单信息有返回的场合
            orders.setChannelCallbackTime(DateUtil.parse(ad3OrdersVO.getTxnDate(), "yyyyMMddHHmmss"));//通道回调时间
            orders.setChannelNumber(ad3OrdersVO.getTxnId());//通道流水号
            if (ad3OrdersVO.getState().equals(AD3Constant.ORDER_SUCCESS)) {//交易成功
                log.info("****************  交易成功 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);//付款成功
                //更新订单状态
                if (ordersMapper.updateOrderByAd3Query(orders.getId(), TradeConstant.ORDER_PAY_SUCCESS,
                        orders.getChannelNumber(), orders.getChannelCallbackTime()) == 1) {//更新成功
                    RabbitMassage rabbitRefundMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    this.cancelRefund2(orders, rabbitRefundMsg);
                    return;
                } else {//更新失败后去查询订单信息
                    RabbitMassage rabbitOrderMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.TC_MQ_CANCEL_ORDER, JSON.toJSONString(rabbitOrderMsg));
                    return;
                }
            } else if (ad3OrdersVO.getState().equals(AD3Constant.ORDER_IN_TRADING)) {//交易中
                log.info("****************  交易中 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                orders.setTradeStatus(TradeConstant.ORDER_PAYING);//付款中
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
                return;
            } else {//支付失败
                log.info("****************  支付失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);//付款失败
                ordersMapper.updateOrderByAd3Query(orders.getId(), TradeConstant.ORDER_PAY_FAILD, orders.getChannelNumber(), orders.getChannelCallbackTime());
            }
        }
    }

    /**
     * 生成AD3认证签名
     *
     * @param commonObj   AD3公共参数输入实体
     * @param businessObj AD3业务参数输入实体
     * @param token       token
     * @return ad3签名
     */
    @Override
    public String createAD3Signature(Object commonObj, Object businessObj, String token) {
        Map<String, Object> commonMap = ReflexClazzUtils.getFieldNames(commonObj);
        Map<String, Object> businessMap = ReflexClazzUtils.getFieldNames(businessObj);
        commonMap.putAll(businessMap);
        HashMap<String, String> paramMap = new HashMap<>();
        for (String str : commonMap.keySet()) {
            paramMap.put(str, String.valueOf(commonMap.get(str)));
        }
        String signature = SignTools.getSignStr(paramMap);//密文字符串拼装处理
        String ad3Signature = MD5Util.getMD5String(signature + "&" + token).toUpperCase();//与token进行拼接MD5加密
        log.info("ad3签名:{}", ad3Signature);
        return ad3Signature;
    }


    /**
     * 获取终端编号和token
     *
     * @return
     */
    @Override
    public AD3LoginVO getTerminalIdAndToken() {
        AD3LoginVO ad3LoginVO = null;
        String token = redisService.get(AD3Constant.AD3_LOGIN_TOKEN);
        String terminalId = redisService.get(AD3Constant.AD3_LOGIN_TERMINAL);
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(terminalId)) {
            //AD3登陆业务参数实体
            LoginBizContentDTO bizContent = new LoginBizContentDTO(AD3Constant.LOGIN_OUT, ad3ParamsConfig.getOperatorId(), MD5Util.getMD5String(ad3ParamsConfig.getPassword()), ad3ParamsConfig.getImei());
            //AD3登陆公共参数实体
            AD3LoginDTO ad3LoginDTO = new AD3LoginDTO(ad3ParamsConfig.getMerchantCode(), bizContent);
            //先登出
            ad3Login(ad3LoginDTO, null);
            //再登陆
            bizContent.setType(AD3Constant.LOGIN_IN);
            HttpResponse httpResponse = this.ad3Login(ad3LoginDTO, null);
            //状态码为200
            if (httpResponse != null && httpResponse.getHttpStatus().equals(AsianWalletConstant.HTTP_SUCCESS_STATUS)) {
                ad3LoginVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3LoginVO.class);
                //业务返回码为成功
                if (ad3LoginVO != null && ad3LoginVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {
                    redisService.set(AD3Constant.AD3_LOGIN_TOKEN, ad3LoginVO.getToken());
                    redisService.set(AD3Constant.AD3_LOGIN_TERMINAL, ad3LoginVO.getTerminalId());
                }
            }
        } else {//存在的场合
            ad3LoginVO = new AD3LoginVO();//创建对象
            ad3LoginVO.setTerminalId(terminalId);//终端编号
            ad3LoginVO.setToken(token);//token
        }
        return ad3LoginVO;
    }


    /**
     * AD3通道终端查询订单状态
     *
     * @param terminalOrderVO 查询订单输出实体
     * @param orders          订单实体
     * @return baseResponse
     */
    @Override
    public TerminalOrderVO ad3TerminalQueryOrder(TerminalOrderVO terminalOrderVO, Orders orders) {
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();//ad3登陆接口输出实体
        if (ad3LoginVO == null) {
            log.info("=================【终端查询订单状态信息记录】=================【调用ad3登陆接口异常】");
            throw new BusinessException(EResultEnum.QUERY_ORDER_ERROR.getCode());
        }
        //查询订单接口公共参数实体
        AD3QuerySingleOrderDTO ad3QuerySingleOrderDTO = new AD3QuerySingleOrderDTO(ad3ParamsConfig.getMerchantCode());
        //查询订单接业务共参数实体
        QueryOneOrderBizContentDTO queryBizContent = new QueryOneOrderBizContentDTO(ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), AD3Constant.TRADE_ORDER, orders.getId(), "");
        //生成查询签名
        String querySign = this.createAD3Signature(ad3QuerySingleOrderDTO, queryBizContent, ad3LoginVO.getToken());
        //查询业务实体
        ad3QuerySingleOrderDTO.setBizContent(queryBizContent);
        //签名
        ad3QuerySingleOrderDTO.setSignMsg(querySign);
        HttpResponse httpResponse = this.ad3QueryOneOrder(ad3QuerySingleOrderDTO, null);
        if (httpResponse == null || !httpResponse.getHttpStatus().equals(AsianWalletConstant.HTTP_SUCCESS_STATUS)) {
            log.info("=================【终端查询订单状态信息记录】=================调用ad3查询接口状态码异常");
            throw new BusinessException(EResultEnum.QUERY_ORDER_ERROR.getCode());
        }
        //反序列化Json数据
        AD3OrdersVO ad3OrdersVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3OrdersVO.class);
        if (ad3OrdersVO == null || !ad3OrdersVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {
            log.info("=================【终端查询订单状态信息记录】=================调用ad3查询接口业务返回码异常");
            throw new BusinessException(EResultEnum.QUERY_ORDER_ERROR.getCode());
        }
        //校验交易金额与交易币种
        BigDecimal ad3Amount = new BigDecimal(ad3OrdersVO.getMerorderAmount());
        BigDecimal tradeAmount = orders.getTradeAmount();
        if (ad3Amount.compareTo(tradeAmount) != 0 || !ad3OrdersVO.getMerorderCurrency().equals(orders.getTradeCurrency())) {
            log.info("=================【终端查询订单状态信息记录】=================【订单信息不匹配】");
            throw new BusinessException(EResultEnum.QUERY_ORDER_ERROR.getCode());
        }
        orders.setUpdateTime(new Date());//修改时间
        orders.setChannelCallbackTime(DateUtil.parse(ad3OrdersVO.getTxnDate(), "yyyyMMddHHmmss"));//通道回调时间
        orders.setChannelNumber(ad3OrdersVO.getTxnId());//通道流水号
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if (AD3Constant.ORDER_SUCCESS.equals(ad3OrdersVO.getState())) {
            log.info("=================【终端查询订单状态信息记录】=================【订单已支付成功】 orderId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更新订单信息
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【终端查询订单状态信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
                //计算支付成功时的通道网关手续费
                commonService.calcCallBackGatewayFeeSuccess(orders);
                //添加日交易限额与日交易笔数
                commonService.quota(orders.getInstitutionCode(), orders.getProductCode(), orders.getTradeAmount());
                //新增交易成功的订单物流信息
                commonService.insertOrderLogistics(orders);
                //支付成功后向用户发送邮件
                commonService.sendEmail(orders.getDraweeEmail(), orders.getLanguage(), Status._1, orders);
                try {
                    //账户信息不存在的场合创建对应的账户信息
                    if (commonService.getAccount(orders.getInstitutionCode(), orders.getOrderCurrency()) == null) {
                        log.info("=================【终端查询订单状态信息记录】=================【上报清结算前线下下单创建账户信息】");
                        commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                    }
                    //分润
                    if(!StringUtils.isEmpty(orders.getAgencyCode())){
                        rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orders.getId());
                    }
                    //更新成功,上报清结算
                    FundChangeDTO fundChangeDTO = new FundChangeDTO(orders, TradeConstant.NT, orders.getInstitutionCode());
                    //上报清结算资金变动接口
                    BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                    if (fundChangeResponse.getCode() != null && TradeConstant.HTTP_SUCCESS.equals(fundChangeResponse.getCode())) {
                        //请求成功
                        FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                        if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                            //业务处理失败
                            log.info("=================【终端查询订单状态信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【终端查询订单状态信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【终端查询订单状态信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【终端查询订单状态信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else if (AD3Constant.ORDER_FAILED.equals(ad3OrdersVO.getState())) {
            log.info("=================【终端查询订单状态信息记录】=================【订单已支付失败】 orderId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //计算支付失败时的通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【终端查询订单状态信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【终端查询订单状态信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("=================【终端查询订单状态信息记录】=================【订单为其它状态】 orderId: {}", orders.getId());
        }
        terminalOrderVO.setTxnstatus(orders.getTradeStatus());
        return terminalOrderVO;
    }

    /**
     * AD3通道线下CSB下单
     *
     * @return
     */
    @Override
    public BaseResponse offlineCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("-----------------【线下CSB】下单信息记录---------------调用【AD3登陆接口】异常 ad3LoginVO: {}", JSON.toJSONString(ad3LoginVO));
            orders.setUpdateTime(new Date());//修改时间
            orders.setRemark("调用AD3登陆接口异常");
            ordersMapper.updateByPrimaryKeySelective(orders);
            baseResponse.setCode(EResultEnum.TOKEN_IS_INVALID.getCode());//上报通道失败报token不合法
            return baseResponse;
        }
        //CSB请求二维码接口公共参数实体
        AD3CSBScanPayDTO ad3CSBScanPayDTO = new AD3CSBScanPayDTO(ad3ParamsConfig.getMerchantCode());
        //CSB请求二维码接口业务参数实体
        CSBScanBizContentDTO csbScanBizContent = new CSBScanBizContentDTO(orders, ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), ad3ParamsConfig.getChannelCallbackUrl() + "/offlineCallback/ad3Callback", channel);
        //生成ad3签名
        ad3CSBScanPayDTO.setSignMsg(this.createAD3Signature(ad3CSBScanPayDTO, csbScanBizContent, ad3LoginVO.getToken()));
        ad3CSBScanPayDTO.setBizContent(csbScanBizContent);//业务实体
        //调用CSB请求二维码接口,获取二维码url
        HttpResponse httpResponse = this.ad3CSBScanPay(ad3CSBScanPayDTO, null);
        if (httpResponse == null || !httpResponse.getHttpStatus().equals(AsianWalletConstant.HTTP_SUCCESS_STATUS)) {
            log.info("-----------------【线下CSB】下单信息记录---------------调用【AD3-CSB接口】状态码异常");
            //上报通道失败
            orders.setRemark("调用AD3-CSB接口状态码异常");
            ordersMapper.updateByPrimaryKeySelective(orders);
            baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
            return baseResponse;
        }
        //反序列化Json数据
        AD3CSBScanVO csbScanVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3CSBScanVO.class);
        if (csbScanVO == null) {
            log.info("-----------------【线下CSB】下单信息记录---------------调用【AD3-CSB接口】返回结果为空");
            orders.setUpdateTime(new Date());//修改时间
            orders.setRemark("调用AD3CSB接口返回结果为空");
            ordersMapper.updateByPrimaryKeySelective(orders);
            baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
            return baseResponse;
        }
        //调用支付接口,业务码异常时
        if (!csbScanVO.getRespCode().equals(AD3Constant.AD3_OFFLINE_SUCCESS)) {
            orders.setUpdateTime(new Date());//修改时间
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            log.info("-----------------【线下CSB】下单信息记录---------------调用【AD3-CSB接口】业务返回码异常 csbScanVO: {}", JSON.toJSONString(csbScanVO));
            //金额不合法时
            if (csbScanVO.getRespCode().equals(AD3Constant.AMOUNT_IS_ILLEGAL)) {
                log.info("-----------------【线下CSB】下单信息记录---------------金额不合法");
                orders.setRemark("AD3通道金额不合法");
                baseResponse.setCode(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
            } else {
                orders.setRemark("调用AD3-CSB接口业务码异常");
                //支付异常
                baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
            }
            ordersMapper.updateByPrimaryKeySelective(orders);//更新订单信息
            return baseResponse;
        }
        //调用支付接口,业务码为成功,返回二维码URL
        baseResponse.setData(csbScanVO.getCode_url());
        return baseResponse;
    }

    /**
     * AD3通道线下BSC下单
     *
     * @param authCode 付款码
     * @param orders   订单实体
     * @return baseResponse
     */
    @Override
    public BaseResponse offlineBSC(Orders orders, Channel channel, BaseResponse baseResponse, String authCode) {
        //获取ad3的终端号和token
        AD3LoginVO ad3LoginVO = this.getTerminalIdAndToken();
        if (ad3LoginVO == null) {
            log.info("==================【线下BSC】下单信息记录==================调用【AD3登陆接口】异常 ad3LoginVO: {}", JSON.toJSONString(ad3LoginVO));
            orders.setUpdateTime(new Date());//修改时间
            orders.setRemark("调用AD3登陆接口异常");
            ordersMapper.updateByPrimaryKeySelective(orders);
            baseResponse.setCode(EResultEnum.TOKEN_IS_INVALID.getCode());//上报通道失败报token不合法
            return baseResponse;
        }
        //BSC支付接口公共参数实体
        AD3BSCScanPayDTO ad3BSCScanPayDTO = new AD3BSCScanPayDTO(ad3ParamsConfig.getMerchantCode());
        //BSC支付接口业务参数实体
        BSCScanBizContentDTO bscScanBizContentDTO = new BSCScanBizContentDTO(orders, ad3LoginVO.getTerminalId(), ad3ParamsConfig.getOperatorId(), authCode, channel);
        //生成ad3签名
        ad3BSCScanPayDTO.setSignMsg(this.createAD3Signature(ad3BSCScanPayDTO, bscScanBizContentDTO, ad3LoginVO.getToken()));
        ad3BSCScanPayDTO.setBizContent(bscScanBizContentDTO);//业务实体
        //调用BSC支付接口
        HttpResponse httpResponse = this.ad3BSCScanPay(ad3BSCScanPayDTO, null);
        if (httpResponse == null || !httpResponse.getHttpStatus().equals(AsianWalletConstant.HTTP_SUCCESS_STATUS)) {
            log.info("==================【线下BSC】下单信息记录==================调用【AD3-BSC接口】状态码异常");
            orders.setUpdateTime(new Date());//修改时间
            orders.setRemark("调用AD3BSC接口状态码异常");
            //支付异常
            ordersMapper.updateByPrimaryKeySelective(orders);
            baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
            return baseResponse;
        }
        //反序列化Json数据
        AD3BSCScanVO ad3BSCScanVO = JSON.parseObject(String.valueOf(httpResponse.getJsonObject()), AD3BSCScanVO.class);
        if (ad3BSCScanVO == null) {
            log.info("==================【线下BSC】下单信息记录==================调用【AD3-BSC接口】返回结果为空");
            orders.setUpdateTime(new Date());
            orders.setRemark("调用AD3-BSC接口返回结果为空");
            //支付异常
            ordersMapper.updateByPrimaryKeySelective(orders);
            baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
            return baseResponse;
        }
        orders.setUpdateTime(new Date());
        //支付失败时
        if (!AD3Constant.AD3_OFFLINE_SUCCESS.equals(ad3BSCScanVO.getRespCode())) {
            log.info("==================【线下BSC】下单信息记录==================调用【AD3-BSC接口】接口业务码异常 ad3BSCScanVO:{}", JSON.toJSONString(ad3BSCScanVO));
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //计算支付失败时通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            //金额不合法
            if (AD3Constant.AMOUNT_IS_ILLEGAL.equals(ad3BSCScanVO.getRespCode())) {
                log.info("==================【线下BSC】下单信息记录==================【金额不合法】");
                orders.setRemark("AD3通道金额不合法");
                baseResponse.setCode(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
            } else {
                //支付异常
                baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
                orders.setRemark("调用AD3BSC接口业务码异常");
            }
            //上游返回的错误code
            orders.setRemark4(ad3BSCScanVO.getRespCode());
            ordersMapper.updateByPrimaryKeySelective(orders);
            return baseResponse;
        }
        //支付成功时
        BigDecimal ad3Amount = new BigDecimal(ad3BSCScanVO.getMerorderAmount());
        BigDecimal tradeAmount = orders.getTradeAmount();
        if (ad3Amount.compareTo(tradeAmount) != 0 || !ad3BSCScanVO.getMerorderCurrency().equals(orders.getTradeCurrency())) {
            log.info("==================【线下BSC】下单信息记录==================【订单信息不匹配】");
            baseResponse.setCode(EResultEnum.ORDER_INFO_NO_MATCHING.getCode());
            return baseResponse;
        }
        orders.setChannelNumber(ad3BSCScanVO.getTxnId());
        orders.setChannelCallbackTime(DateUtil.parse(ad3BSCScanVO.getPayFinishTime(), "yyyyMMddHHmmss"));//通道回调时间
        orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
        //更新订单信息
        if (ordersMapper.updateByPrimaryKeySelective(orders) == 1) {
            log.info("=================【线下BSC】下单信息记录=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
            //计算支付成功时的通道网关手续费
            commonService.calcCallBackGatewayFeeSuccess(orders);
            //添加日交易限额与日交易笔数
            commonService.quota(orders.getInstitutionCode(), orders.getProductCode(), orders.getTradeAmount());
            //新增交易成功的订单物流信息
            commonService.insertOrderLogistics(orders);
            //支付成功后向用户发送邮件
            commonService.sendEmail(orders.getDraweeEmail(), orders.getLanguage(), Status._1, orders);
            try {
                //账户信息不存在的场合创建对应的账户信息
                if (commonService.getAccount(orders.getInstitutionCode(), orders.getOrderCurrency()) == null) {
                    log.info("=================【线下BSC】下单信息记录=================【上报清结算前线下下单创建账户信息】");
                    commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                }
                //分润
                if(!StringUtils.isEmpty(orders.getAgencyCode())){
                    rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orders.getId());
                }
                //更新成功,上报清结算
                FundChangeDTO fundChangeDTO = new FundChangeDTO(orders, TradeConstant.NT, orders.getInstitutionCode());
                //上报清结算资金变动接口
                BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                if (fundChangeResponse.getCode() != null && TradeConstant.HTTP_SUCCESS.equals(fundChangeResponse.getCode())) {
                    //请求成功
                    FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                    if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                        //业务处理失败
                        log.info("***********************【线下BSC】下单信息记录***********************【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } else {
                    log.info("***********************【线下BSC】下单信息记录***********************【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } catch (Exception e) {
                log.error("***********************【线下BSC】下单信息记录***********************【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
            }
        } else {
            log.info("***********************【线下BSC】下单信息记录***********************【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
        }
        return baseResponse;
    }
}
