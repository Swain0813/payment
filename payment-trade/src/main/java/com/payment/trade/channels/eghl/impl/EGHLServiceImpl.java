package com.payment.trade.channels.eghl.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.eghl.EGHLRequestDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.ChannelsOrder;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.common.utils.Sha256Tools;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.eghl.EGHLService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.ChannelsOrderMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.EghlBrowserCallbackDTO;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * EGHL通道业务接口实现类
 */
@Service
@Slf4j
@Transactional
public class EGHLServiceImpl implements EGHLService {

    @Autowired
    private ChannelsFeign channelsFeign;

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;

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
     * EGHL收单方法
     *
     * @param orders       订单
     * @param channel      通道
     * @param baseResponse 响应实体
     * @return
     */
    @Override
    public BaseResponse eghlPay(Orders orders, Channel channel, BaseResponse baseResponse) {
        EGHLRequestDTO eghlRequestDTO = new EGHLRequestDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/eghlBrowserCallback"), ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/eghlServerCallback"));
        log.info("----------------- EGHL收单方法 ----------------- eghlRequestDTO: {}", JSON.toJSONString(eghlRequestDTO));
        BaseResponse response = channelsFeign.eGHLPay(eghlRequestDTO);
        log.info("----------------- EGHL收单方法 返回----------------- response: {}", JSON.toJSONString(response));
        baseResponse.setData(response.getData());
        return baseResponse;
    }

    /**
     * EGHL回调浏览器方法
     *
     * @param eghlBrowserCallbackDTO eghl回调输入实体
     * @param response               响应实体
     * @return
     */
    @Override
    public void eghlBrowserCallback(EghlBrowserCallbackDTO eghlBrowserCallbackDTO, HttpServletResponse response) {
        //校验回调参数
        if (!checkCallback(eghlBrowserCallbackDTO)) {
            return;
        }
        //查询通道md5key
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(eghlBrowserCallbackDTO.getPaymentID());
        if (StringUtils.isEmpty(eghlBrowserCallbackDTO.getAuthCode())) {
            eghlBrowserCallbackDTO.setAuthCode("");
        }
        //校验签名
        if (!checkSign(eghlBrowserCallbackDTO, channelsOrder)) {
            log.info("-------------EGHL回调浏览器方法信息记录------------签名不匹配");
            return;
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(eghlBrowserCallbackDTO.getPaymentID());
        if (orders == null) {
            log.info("-------------EGHL回调浏览器方法信息记录------------回调订单信息不存在");
            return;
        }
        if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
            //支付成功
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------EGHL回调浏览器方法信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付成功页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS);
                } catch (IOException e) {
                    log.error("--------------EGHL回调浏览器方法信息记录--------------调用AW支付成功页面失败", e);
                }
            }
        } else {
            //其他情况
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------EGHL回调浏览器方法信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付中页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
                } catch (IOException e) {
                    log.error("--------------EGHL回调浏览器方法信息记录--------------调用AW支付中页面失败", e);
                }
            }
        }
    }

    /**
     * EGHL服务器回调方法
     *
     * @param eghlBrowserCallbackDTO eghl回调输入实体
     * @return
     */
    @Override
    public void eghlServerCallback(EghlBrowserCallbackDTO eghlBrowserCallbackDTO, HttpServletResponse response) {
        //校验回调参数
        if (!checkCallback(eghlBrowserCallbackDTO)) {
            return;
        }
        Orders orders = ordersMapper.selectByPrimaryKey(eghlBrowserCallbackDTO.getPaymentID());
        //校验原有订单信息
        if (orders == null) {
            log.info("============【EGHL回调服务器方法信息记录】============【原始订单信息不存在】");
            return;
        }
        //查询通道md5key
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(eghlBrowserCallbackDTO.getPaymentID());
        if (StringUtils.isEmpty(eghlBrowserCallbackDTO.getAuthCode())) {
            eghlBrowserCallbackDTO.setAuthCode("");
        }
        //校验签名
        if (!checkSign(eghlBrowserCallbackDTO, channelsOrder)) {
            log.info("============【EGHL回调服务器方法信息记录】============【签名不匹配】");
            return;
        }
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【EGHL回调服务器方法信息记录】=================【订单状态不为支付中】");
            try {
                response.getWriter().write("OK");
            } catch (IOException e) {
                log.error("***************EGHL服务器回调方法发生异常*******************", e);
            }
        }
        BigDecimal eghlAmount = new BigDecimal(eghlBrowserCallbackDTO.getAmount());
        BigDecimal tradeAmount = orders.getTradeAmount();//交易金额
        if (eghlAmount.compareTo(tradeAmount) != 0 || !eghlBrowserCallbackDTO.getCurrencyCode().equals(orders.getTradeCurrency())) {
            log.info("=================【EGHL回调服务器方法信息记录】=================订单信息不匹配");
            return;
        }
        orders.setChannelNumber(eghlBrowserCallbackDTO.getTxnID());//通道流水号
        orders.setChannelCallbackTime(new Date());//通道回调时间
        orders.setUpdateTime(new Date());//修改时间
        String status = eghlBrowserCallbackDTO.getTxnStatus();//交易状态
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if ("0".equals(status)) {
            log.info("=================【EGHL回调服务器方法信息记录】=================【订单已支付成功】 orderId: {}", orders.getId());
            //支付成功
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(eghlBrowserCallbackDTO.getPaymentID(), eghlBrowserCallbackDTO.getTxnID(), TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("=================【EGHL回调服务器方法信息记录】=================【更新通道订单异常】", e);
            }
            //修改订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【EGHL回调服务器方法信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                        log.info("=================【EGHL回调服务器方法信息记录】=================【上报清结算前线下下单创建账户信息】");
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
                            log.info("=================【EGHL回调服务器方法信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【EGHL回调服务器方法信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【EGHL回调服务器方法信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【EGHL回调服务器方法信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("=================【EGHL回调服务器方法信息记录】=================【订单已支付失败】 orderId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //上游返回的错误code
            orders.setRemark4(status);
            try {
                //更改channelsOrders状态
                channelsOrderMapper.updateStatusById(eghlBrowserCallbackDTO.getPaymentID(), eghlBrowserCallbackDTO.getTxnID(), TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.error("=================【EGHL回调服务器方法信息记录】=================【更新通道订单异常】", e);
            }
            //计算支付失败时的通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【EGHL回调服务器方法信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【EGHL回调服务器方法信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        }
        try {
            //商户回调地址不为空,回调商户服务器
            if (!StringUtils.isEmpty(orders.getReturnUrl())) {
                commonService.replyReturnUrl(orders);
            }
            response.getWriter().write("OK");
        } catch (Exception e) {
            log.error("=================【EGHL回调服务器方法信息记录】=================【回调商户异常】", e);
        }
    }

    /**
     * 校验EGHL回调参数
     *
     * @param eghlBrowserCallbackDTO
     * @return
     */
    private boolean checkCallback(EghlBrowserCallbackDTO eghlBrowserCallbackDTO) {
        //商户上送的商户订单号
        if (StringUtils.isEmpty(eghlBrowserCallbackDTO.getPaymentID())) {
            log.info("-------------EGHL回调方法信息记录------------订单id为空");
            return false;
        }
        if (StringUtils.isEmpty(eghlBrowserCallbackDTO.getHashValue())) {
            log.info("-------------EGHL回调方法信息记录------------签名为空");
            return false;
        }
        if (StringUtils.isEmpty(eghlBrowserCallbackDTO.getTxnStatus())) {
            log.info("-------------EGHL回调方法信息记录------------订单状态为空");
            return false;
        }
        return true;
    }

    /**
     * 校验EGHL回调签名
     *
     * @param eghlBrowserCallbackDTO eghl回调参数
     * @return 布尔值
     */
    private boolean checkSign(EghlBrowserCallbackDTO eghlBrowserCallbackDTO, ChannelsOrder channelsOrder) {
        //拼接签名
        String flag = channelsOrder.getMd5KeyStr() + eghlBrowserCallbackDTO.getTxnID() + eghlBrowserCallbackDTO.getServiceID() + eghlBrowserCallbackDTO.getPaymentID()
                + eghlBrowserCallbackDTO.getTxnStatus() + eghlBrowserCallbackDTO.getAmount() + eghlBrowserCallbackDTO.getCurrencyCode() + eghlBrowserCallbackDTO.getAuthCode();
        log.info("------------EGHL回调方法信息记录------------签名前的明文:{}", flag);
        String sign = Sha256Tools.encrypt(flag);
        log.info("------------EGHL回调方法信息记录------------签名后的密文:{}", sign);
        String hashValue = eghlBrowserCallbackDTO.getHashValue();
        log.info("------------EGHL回调方法信息记录------------eghl回调密文:{}", hashValue);
        return sign.equals(hashValue);
    }
}
