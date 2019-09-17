package com.payment.trade.channels.xendit.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.xendit.XenditDTO;
import com.payment.common.dto.xendit.XenditPayRequestDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.xendit.XenditService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.ChannelsOrderMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.XenditServerCallbackDTO;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class XenditServiceImpl implements XenditService {

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;

    @Autowired
    private ChannelsFeign channelsFeign;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    /**
     * xendit网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse xenditPay(Orders orders, Channel channel, BaseResponse baseResponse) {
        XenditPayRequestDTO xenditPayRequestDTO = new XenditPayRequestDTO(orders, channel, ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS, ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
        XenditDTO xenditDTO = new XenditDTO(xenditPayRequestDTO, orders.getInstitutionOrderId(), channel.getMd5KeyStr(), orders.getReqIp(), orders.getTradeCurrency());
        log.info("----------------- xendit网银收单方法 ----------------- xenditDTO: {}", JSON.toJSONString(xenditDTO));
        BaseResponse response = channelsFeign.xenditPay(xenditDTO);
        if (response.getData() == null) {
            baseResponse.setCode(response.getCode());
            baseResponse.setMsg(response.getMsg());
            return baseResponse;
        }
        baseResponse.setData(response.getData());
        log.info("----------------- xendit网银收单方法 返回 ----------------- baseResponse: {}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }

//    /**
//     * xendit网银收单方法 付款
//     *
//     * @param orders  订单
//     * @param channel 通道
//     * @return
//     */
//    @Override
//    public BaseResponse xenditPay2(Orders orders, Channel channel, BaseResponse baseResponse) {
//        XenditRequestDTO xenditRequestDTO = new XenditRequestDTO(orders, channel);
//        XenditDTO xenditDTO = new XenditDTO(xenditRequestDTO, orders.getInstitutionOrderId(), channel.getMd5KeyStr(), orders.getReqIp(), orders.getTradeCurrency());
//        BaseResponse response = channelsFeign.xenditPay(xenditDTO);
//        if (response.getData() == null) {
//            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
//            return baseResponse;
//        }
//        baseResponse.setData(response.getData());
//        return baseResponse;
//    }

    /**
     * xendit服务器回调
     *
     * @param xenditServerCallbackDTO xendit服务器回调实体
     * @return
     */
    @Override
    public void xenditServerCallback(XenditServerCallbackDTO xenditServerCallbackDTO) {
        //校验回调参数
        if (!checkCallback(xenditServerCallbackDTO)) {
            return;
        }
        try {
            //查询通道md5key
            //ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(xenditServerCallbackDTO.getExternal_id());
            //校验签名
//            if (!createCallbackSign(xenditServerCallbackDTO, channelsOrder.getMd5KeyStr())) {
//                log.info("===============【xendit服务器回调接口信息记录】===============签名不匹配");
//                return;
//            }
            //查询原订单信息
            Orders orders = ordersMapper.selectByPrimaryKey(xenditServerCallbackDTO.getExternal_id());
            if (orders == null) {
                log.info("===============【xendit服务器回调接口信息记录】===============回调订单信息不存在");
                return;
            }
            //订单已支付
            if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
                log.info("===============【xendit服务器回调接口信息记录】===============订单状态为已支付");
                return;
            }
            //校验交易金额与交易币种
            BigDecimal channelAmount = new BigDecimal(xenditServerCallbackDTO.getAmount());
            BigDecimal tradeAmount = orders.getTradeAmount();//交易金额
            if (channelAmount.compareTo(tradeAmount) != 0) {
                log.info("===============【xendit服务器回调接口信息记录】===============订单信息不匹配");
                return;
            }
            String status = xenditServerCallbackDTO.getStatus();//交易状态
            orders.setChannelNumber(xenditServerCallbackDTO.getId());//通道流水号
            orders.setChannelCallbackTime(new Date());//通道回调时间
            orders.setUpdateTime(new Date());//修改时间
            Example example = new Example(Orders.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("id", orders.getId());
            criteria.andEqualTo("tradeStatus", "2");
            if ("PAID".equals(status)) {//COMPLETED为交易成功
                log.info("===============【xendit服务器回调接口信息记录】===============订单已支付成功");
                //支付成功
                orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
                //更改channelsOrders状态
                try {
                    channelsOrderMapper.updateStatusById(xenditServerCallbackDTO.getExternal_id(), xenditServerCallbackDTO.getId(), TradeConstant.TRADE_SUCCESS);
                } catch (Exception e) {
                    log.error("===============【xendit服务器回调接口信息记录】===============【更改通道订单异常】");
                }
                //修改订单状态
                int i = ordersMapper.updateByExampleSelective(orders, example);
                if (i > 0) {
                    log.info("=================【xendit服务器回调接口信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                            log.info("=================【xendit服务器回调接口信息记录】=================【上报清结算前线下下单创建账户信息】");
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
                                log.info("=================【xendit服务器回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                            }
                        } else {
                            log.info("=================【xendit服务器回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } catch (Exception e) {
                        log.info("=================【xendit服务器回调接口信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } else {
                    log.info("=================【xendit服务器回调接口信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
                }
            } else if ("EXPIRED".equals(status)) {//发票已过期 交易失败
                log.info("=================【xendit服务器回调接口信息记录】=================【订单已支付失败】 orderId: {}", orders.getId());
                //支付失败
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                //上游返回的错误code
                orders.setRemark4(status);
                ordersMapper.updateByExampleSelective(orders, example);//更新订单信息
                //更改channelsOrders状态
                try {
                    channelsOrderMapper.updateStatusById(xenditServerCallbackDTO.getExternal_id(), xenditServerCallbackDTO.getId(), TradeConstant.TRADE_FALID);
                } catch (Exception e) {
                    log.error("===============【xendit服务器回调接口信息记录】===============【更改通道订单异常】");
                }
                //计算支付失败时通道网关手续费
                commonService.calcCallBackGatewayFeeFailed(orders);
                if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                    log.info("=================【eNets网银服务器回调接口信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
                } else {
                    log.error("=================【eNets网银服务器回调接口信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
                }
            } else {
                log.info("===============【xendit服务器回调接口信息记录】===============订单是其他状态");
            }
            //商户回调地址不为空,回调商户服务器
            if (!StringUtils.isEmpty(orders.getReturnUrl())) {
                commonService.replyReturnUrl(orders);
            }
        } catch (Exception e) {
            log.info("----------【xendit服务器回调接口信息记录】----------回调接口异常", e);
        }
    }

    /**
     * 校验回调参数
     * @param xenditServerCallbackDTO
     * @return
     */
    private boolean checkCallback(XenditServerCallbackDTO xenditServerCallbackDTO) {
        //商户上送的商户订单号
        if (StringUtils.isEmpty(xenditServerCallbackDTO.getExternal_id())) {
            log.info("===============【xendit服务器回调接口信息记录】===============订单id为空");
            return false;
        }
        if (StringUtils.isEmpty(xenditServerCallbackDTO.getStatus())) {
            log.info("===============【xendit服务器回调接口信息记录】===============订单状态为空");
            return false;
        }
        if (StringUtils.isEmpty(xenditServerCallbackDTO.getAmount())) {
            log.info("===============【xendit服务器回调接口信息记录】===============订单金额");
            return false;
        }
        return true;
    }
}
