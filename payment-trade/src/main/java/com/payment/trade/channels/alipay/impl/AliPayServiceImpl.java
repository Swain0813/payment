package com.payment.trade.channels.alipay.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.alipay.AliPayCSBDTO;
import com.payment.common.dto.alipay.AliPayOfflineBSCDTO;
import com.payment.common.dto.alipay.AliPayQueryDTO;
import com.payment.common.dto.alipay.AliPayRefundDTO;
import com.payment.common.entity.*;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.AlipayCore;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.alipay.AliPayService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.*;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.utils.AbstractHandlerAdapter;
import com.payment.trade.utils.HandlerType;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@Slf4j
@HandlerType({TradeConstant.ALIPAY_CSB_OFFLINE, TradeConstant.ALIPAY_BSC_OFFLINE})
public class AliPayServiceImpl extends AbstractHandlerAdapter implements AliPayService {

    @Autowired
    private ChannelsFeign channelsFeign;

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    @Autowired
    private ChannelMapper channelMapper;

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


    /**
     * AliPayCSB收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse aliPayCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        AliPayCSBDTO aliPayCSBDTO = new AliPayCSBDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/aliPayCB_TPMQRCReturn"));
        BaseResponse response = channelsFeign.aliPayCSB(aliPayCSBDTO);
        log.info("==================AliPayCSB收单方法==================aliPayCSBDTO:{}", JSON.toJSONString(aliPayCSBDTO));
        if (response.getData() == null) {
            baseResponse.setCode(response.getCode());
            baseResponse.setMsg(response.getMsg());
            return baseResponse;
        }
        baseResponse.setData(response.getData());
        log.info("----------------- AliPayCSB收单方法 返回 ----------------- baseResponse: {}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * AliPay退款方法
     *
     * @param orderRefund   订单
     * @param fundChangeDTO
     * @param baseResponse
     * @return
     */
    @Override
    public BaseResponse aliPayRefund(OrderRefund orderRefund, FundChangeDTO fundChangeDTO, BaseResponse baseResponse) {
        Channel channel = channelMapper.selectByChannelCode(orderRefund.getChannelCode());
        AliPayRefundDTO aliPayRefundDTO = new AliPayRefundDTO(orderRefund, channel);
        BaseResponse response = channelsFeign.alipayRefund(aliPayRefundDTO);
        if (response.getCode().equals("200")) {//请求成功
            Map<String, String> map = (Map<String, String>) response.getData();
            if (response.getMsg().equals("success")) {//退款成功
                log.info("-----------------  AliPay退款方法 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
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
            } else {//退款失败
                //退款失败
                log.info("----------------- 退款操作 退款失败 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
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
                    orderRefund.setChannelNumber(map.get("alipay_trans_id"));
                    orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                    log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                    rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
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
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
                        log.info("----------------- 退款操作 上报队列 MQ_QJS_TZSB_DL -------------- rabbitMassage : {} ", JSON.toJSON(rabbitMassage));
                        rabbitMQSender.send(AD3MQConstant.MQ_QJS_TZSB_DL, JSON.toJSONString(rabbitMassage));
                    }
                }
            }
        } else {//请求失败
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            orderRefund.setRemark3(JSON.toJSONString(fundChangeDTO));
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            log.info("----------------- 退款操作 请求失败上报队列 MQ_TK_APLIPAY_QQSB_DL -------------- orderRefund : {} ", JSON.toJSON(rabbitMassage));
            rabbitMQSender.send(AD3MQConstant.MQ_TK_APLIPAY_QQSB_DL, JSON.toJSONString(rabbitMassage));

        }
        return response;
    }


    /**
     * AliPay退款方法 不上报清结算
     *
     * @param orders 订单
     * @param
     * @return
     */
    @Override
    public void aliPayRefund2(Orders orders, RabbitMassage rabbitMassage) {
        Channel channel = channelMapper.selectByChannelCode(orders.getChannelCode());
        AliPayRefundDTO aliPayRefundDTO = new AliPayRefundDTO(orders, channel);
        BaseResponse response = channelsFeign.alipayRefund(aliPayRefundDTO);
        if (response.getCode().equals("200")) {//请求成功
            if (response.getMsg().equals("success")) {//退款成功
                log.info("-----------------  AliPay退款方法 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
                //退款成功
                log.info("-----------------  wechat退款方法 -------------- refundAdResponseVO : {} ", JSON.toJSON(response));
                ordersMapper.updateOrderCancelStatus(orders.getInstitutionOrderId(), orders.getDeviceOperator(), TradeConstant.ORDER_CANNEL_SUCCESS);
            } else {
                //退款失败
                ordersMapper.updateOrderCancelStatus(orders.getInstitutionOrderId(), orders.getDeviceOperator(), TradeConstant.ORDER_CANNEL_FALID);
            }
        } else {//请求失败
            log.info("----------------- 退款操作 请求失败上报队列 MQ_TK_WECHAT_QQSB_DL -------------- orderRefund : {} ", JSON.toJSON(rabbitMassage));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.MQ_AD3_REFUND, JSON.toJSONString(rabbitMassage));

        }
    }

    /**
     * AliPay撤销方法
     *
     * @param orderRefund 订单
     * @param
     * @return
     */
    @Override
    public BaseResponse aliPayCancel(OrderRefund orderRefund, BaseResponse baseResponse, RabbitMassage rabbitMassage) {
        Channel channel = channelMapper.selectByChannelCode(orderRefund.getChannelCode());
        AliPayRefundDTO aliPayRefundDTO = new AliPayRefundDTO(orderRefund, channel);
        BaseResponse response = channelsFeign.alipayRefund(aliPayRefundDTO);
        if (response.getCode().equals("200")) {//请求成功
            Map<String, String> map = (Map<String, String>) response.getData();
            if (response.getMsg().equals("success")) {//退款成功
                log.info("-----------------  AliPay撤销方法 -------------- response : {} ", JSON.toJSON(response));
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
            } else {//退款失败
                log.info("----------------- 撤销操作 上游退款失败 -------------- response : {}", JSON.toJSON(response));
                //退款失败
                baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
                rabbitMQSender.send(AD3MQConstant.E_MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMassage));
            }
        } else {//请求失败
            log.info("----------------- 撤销操作 请求失败 -------------- response : {}", JSON.toJSON(response));
            baseResponse.setMsg(EResultEnum.REFUNDING.getCode());
            rabbitMQSender.send(AD3MQConstant.E_MQ_CX_TDTKSB_DL, JSON.toJSONString(rabbitMassage));
        }
        return baseResponse;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/24
     * @Descripate aliPay支付CSB扫码服务器回调
     **/
    @Override
    public BaseResponse aliPayCB_TPMQRCReturn(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> param = new HashMap<>();
        //商户订单号
        String out_trade_no = null;
        //支付宝交易号
        String trade_no = null;
        //获取支付宝POST过来反馈信息
        Map requestParams = request.getParameterMap();
        try {
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                log.info("-------------------------- aliPay支付CSB扫码服务器回调--------------------------#时间：{},name:{},valueStr:{}", new Date(), name, valueStr);
                param.put(name, valueStr);
            }
            out_trade_no = param.get("out_trade_no");
            //
            trade_no = param.get("trade_no");
            //结算币种
            //交易状态
            String trade_status = param.get("trade_status");
            //签名方式
            String sign_type = param.get("sign_type");//N
            //签名
            String sign = param.get("sign");//N
            if (out_trade_no != null && !out_trade_no.equals("") && trade_no != null && !trade_no.equals("") && trade_status != null && !trade_status.equals("")
                    && sign_type != null && !sign_type.equals("") && sign != null && !sign.equals("")) {
                //查询原订单信息
                Orders orders = ordersMapper.selectByPrimaryKey(out_trade_no);
                //查询通道md5key
                ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(out_trade_no);
                if (null != orders) {
                    orders.setChannelCallbackTime(new Date());//通道回调时间
                    orders.setChannelNumber(trade_no);//通道流水号
                    orders.setUpdateTime(new Date());//修改时间
                    Example example = new Example(Orders.class);
                    Example.Criteria criteria = example.createCriteria();
                    criteria.andEqualTo("tradeStatus", "2");
                    criteria.andEqualTo("id", orders.getId());
                    if (verifCSBSign(param, channelsOrder.getMd5KeyStr())) {
                        //状态为交易完成
                        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
                            log.error("---------aliPay支付CSB扫码回调----------订单已支付成功 ordersId: {}", orders.getId());
                            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
                            try {
                                //更改channelsOrders状态
                                channelsOrderMapper.updateStatusById(orders.getId(), out_trade_no, TradeConstant.TRADE_SUCCESS);
                            } catch (Exception e) {
                                log.error("---------aliPay支付CSB扫码回调----------更新通道订单异常");
                            }
                            int i = ordersMapper.updateByExampleSelective(orders, example);
                            if (i > 0) {
                                log.info("=================【aliPay支付CSB扫码回调】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                                        log.info("=================【aliPay支付CSB扫码回调】=================【上报清结算前线下下单创建账户信息】");
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
                                            log.info("=================【aliPay支付CSB扫码回调】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                                        }
                                    } else {
                                        log.info("=================【aliPay支付CSB扫码回调】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                                    }
                                } catch (Exception e) {
                                    log.error("=================【aliPay支付CSB扫码回调】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                                }
                            } else {
                                log.info("=================【aliPay支付CSB扫码回调】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
                            }
                        } else {
                            log.info("==================【aliPay支付CSB扫码服务器回调】==================【订单已支付失败】");
                            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                            //上游返回的错误code
                            orders.setRemark4(trade_status);
                            //更改channelsOrders状态
                            try {
                                channelsOrderMapper.updateStatusById(orders.getId(), trade_no, TradeConstant.TRADE_FALID);
                            } catch (Exception e) {
                                log.error("---------aliPay支付CSB扫码回调----------更新通道订单异常");
                            }
                            //计算支付失败时通道网关手续费
                            commonService.calcCallBackGatewayFeeFailed(orders);
                            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                                log.info("=================【aliPay支付CSB扫码回调】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
                            } else {
                                log.info("=================【aliPay支付CSB扫码回调】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
                            }
                        }
                        //处理完业务逻辑需要给alipay返回success
                        response.getWriter().write("success");
                    } else {
                        log.info("-------------------------- aliPay支付CSB扫码服务器回调--------------------------返回验签失败");
                    }
                } else {
                    log.info("-------------------------- aliPay支付CSB扫码服务器回调--------------------------返回找不到交易=========交易号:" + out_trade_no);
                }
            } else {
                //支付宝返回的订单号为空
                log.info("-------------------------- aliPay支付CSB扫码服务器回调-------------------------- 支付宝返回的订单号为空,时间：" + new Date());
            }
        } catch (Exception e) {
            log.error("-------------------------- aliPay支付CSB扫码服务器回调--------------------------异常", e);
        }
        return null;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/6/24
     * @Descripate 支付宝CSB验签
     **/
    public boolean verifCSBSign(Map<String, String> params, String md5Key) {
        //判断responsetTxt是否为true，isSign是否为true
        //responsetTxt的结果不是true，与服务器设置问题、合作身份者ID、notify_id一分钟失效有关
        //isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
        //String responseTxt = "true";
        String sign = "";
        if (params.get("sign") != null) {
            sign = params.get("sign");
        }
        //boolean isSign = getSignVeryfy(params, sign, pc);
        Map<String, String> sParaNew = AlipayCore.paraFilter(params);
        //获取待签名字符串
        String preSignStr = AlipayCore.createLinkString(sParaNew);
        //获得签名验证结果
        boolean isSign = false;
        //isSign = MD5.verify(preSignStr, sign, md5Key, "utf-8");
        preSignStr = preSignStr + md5Key;
        String mysign = null;
        try {
            mysign = DigestUtils.md5Hex(preSignStr.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + "utf-8");
        }
        if (mysign.equals(sign)) {
            return true;
        } else {
            return false;
        }
        //if (isSign && responseTxt.equals("true")) {
        //    return true;
        //} else {
        //    return false;
        //}
    }

    /**
     * AliPay退款方法  订单处于退款中时
     *
     * @param orders
     * @param rabbitMassage
     */
    @Override
    public void cancelRefunding(Orders orders, RabbitMassage rabbitMassage) {
        Channel channel = channelMapper.selectByChannelCode(orders.getChannelCode());
        AliPayQueryDTO aliPayQueryDTO = new AliPayQueryDTO(orders.getId(), channel);
        BaseResponse baseResponse = channelsFeign.alipayQuery(aliPayQueryDTO);
        if (baseResponse.getCode().equals(TradeConstant.HTTP_SUCCESS)) {//请求成功
            Map<String, String> map = (Map<String, String>) baseResponse.getData();
            if (map.get("is_success").equals("T") && map.get("result_code").equals("SUCCESS")) {
                //查询成功，查看交易状态
                //map.put("queryStatus", "SUCCESS"); //查询成功
                //map.put("alipay_trans_status", reParams.get("alipay_trans_status")); //查询成功后的订单状态
                //map.put("alipay_trans_id", reParams.get("alipay_trans_id")); //alipay订单号
                //map.put("partner_trans_id", reParams.get("partner_trans_id")); //商户订单号
                if (map.get("alipay_trans_status").equals("TRADE_SUCCESS")) {

                    //交易成功
                    orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);//付款成功
                    //更新订单状态
                    if (ordersMapper.updateOrderByAd3Query(orders.getId(), TradeConstant.ORDER_PAY_SUCCESS,
                            orders.getChannelNumber(), orders.getChannelCallbackTime()) == 1) {//更新成功
                        RabbitMassage rabbitRefundMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        this.aliPayRefund2(orders, rabbitRefundMsg);
                    } else {//更新失败后去查询订单信息
                        RabbitMassage rabbitOrderMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.TC_MQ_CANCEL_ORDER, JSON.toJSONString(rabbitOrderMsg));
                    }

                } else {
                    //交易失败
                    log.info("****************  支付失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                    orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);//付款失败
                    ordersMapper.updateOrderByAd3Query(orders.getId(), TradeConstant.ORDER_PAY_FAILD, orders.getChannelNumber(), orders.getChannelCallbackTime());
                }
            } else if ((map.get("is_success").equals("F") && !map.get("error").equals("SYSTEM_ERROR"))
                    || (map.get("is_success").equals("T") && map.get("result_code").equals("FAIL") && !map.get("detail_error_code").equals("SYSTEM_ERROR"))) {
                //明确查询失败
                log.info("**************** 订单处于退款中时 AliPay查询订单失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
            } else {
                //未知情况，继续调用查询接口 3秒调用一次，最多10次
                log.info("**************** 订单处于退款中时 AliPay查询订单失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
                rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
            }
        } else {
            //请求失败
            log.info("**************** 订单处于退款中时 AliPay查询订单失败 ***************** rabbitMassage : {} ", JSON.toJSONString(rabbitMassage));
            rabbitMQSender.sendAd3Sleep(AD3MQConstant.E_MQ_AD3_ORDER_QUERY, JSON.toJSONString(rabbitMassage));
        }


    }

    /**
     * AliPayCSB收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse offlineCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        AliPayCSBDTO aliPayCSBDTO = new AliPayCSBDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/aliPayCB_TPMQRCReturn"));
        log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【AliPay-CSB接口】-请求实体  aliPayCSBDTO: {}", JSON.toJSONString(aliPayCSBDTO));
        BaseResponse response = channelsFeign.aliPayCSB(aliPayCSBDTO);
        log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【AliPay-CSB接口】-请求实体  response: {}", JSON.toJSONString(response));
        if (!TradeConstant.HTTP_SUCCESS.equals(response.getCode())) {
            log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【AliPay-CSB接口】-状态码异常 code: {}", response.getCode());
            baseResponse.setCode(response.getCode());
            baseResponse.setMsg(response.getMsg());
            return baseResponse;
        }
        baseResponse.setData(response.getData());
        return baseResponse;
    }

    /**
     * AliPayBSC收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse offlineBSC(Orders orders, Channel channel, BaseResponse baseResponse, String authCode) {
        AliPayOfflineBSCDTO aliPayOfflineBSCDTO = new AliPayOfflineBSCDTO(orders, channel, authCode);
        log.info("==================【线下BSC】下单信息记录==================调用Channels服务【AliPay-BSC接口】-请求实体 aliPayOfflineBSCDTO: {}", JSON.toJSONString(aliPayOfflineBSCDTO));
        BaseResponse response = channelsFeign.aliPayOfflineBSC(aliPayOfflineBSCDTO);
        log.info("==================【线下BSC】下单信息记录==================调用Channels服务【AliPay-BSC接口】-响应结果 response:{}", JSON.toJSONString(response));
        //支付失败时
        if (!TradeConstant.HTTP_SUCCESS.equals(response.getCode())) {
            log.info("==================【线下BSC】下单信息记录==================调用Channels服务【AliPay-BSC接口】-状态码异常 code: {}", response.getCode());
            //创建订单失败
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return baseResponse;
        }
        JSONObject json = JSONObject.fromObject(response.getData());
        orders.setUpdateTime(new Date());
        orders.setChannelNumber(json.getString("alipay_trans_id"));
        orders.setChannelCallbackTime(new Date());
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", orders.getId());
        criteria.andEqualTo("tradeStatus", "2");
        if (TradeConstant.HTTP_SUCCESS_MSG.equals(response.getMsg())) {
            log.info("==================【线下BSC】下单信息记录==================【订单已支付成功】 ordersId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            try {
                //更改channelsOrders状态
                channelsOrderMapper.updateStatusById(orders.getId(), json.getString("alipay_trans_id"), TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("==================【线下BSC】下单信息记录==================【更新通道订单异常】");
            }
            //修改订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【线下BSC】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                        log.info("=================【线下BSC】=================【上报清结算前线下下单创建账户信息】");
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
                            log.info("=================【线下BSC】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【线下BSC】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【线下BSC】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【线下BSC】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else if (TradeConstant.HTTP_FAIL_MSG.equals(response.getMsg())) {
            log.info("==================【线下BSC】下单信息记录==================【订单已支付失败】 ordersId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), json.getString("alipay_trans_id"), TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.error("==================【线下BSC】下单信息记录==================【更新通道订单异常】");
            }
            //计算支付失败时通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            //上游返回的错误code
            orders.setRemark4(response.getMsg());
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【线下BSC】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【线下BSC】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
            //返回msg信息支付异常
            baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
        } else {
            log.info("==================【线下BSC】下单信息记录==================【其他情况 正常不会发生】");
            baseResponse.setCode(EResultEnum.PAYMENT_ABNORMAL.getCode());
        }
        return baseResponse;
    }
}
