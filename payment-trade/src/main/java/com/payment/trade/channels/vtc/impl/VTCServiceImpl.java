package com.payment.trade.channels.vtc.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.vtc.VTCRequestDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.ChannelsOrder;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.common.utils.ChannelsSignUtils;
import com.payment.common.utils.Sha256Tools;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.vtc.VTCService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.ChannelsOrderMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.VtcCallbackDTO;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class VTCServiceImpl implements VTCService {

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
     * vtc收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse vtcPay(Orders orders, Channel channel, BaseResponse baseResponse) {
        VTCRequestDTO vtcRequestDTO = new VTCRequestDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/vtcPayBrowserCallback"));
        log.info("----------------- vtc收单方法 ----------------- vtcRequestDTO: {}", JSON.toJSONString(vtcRequestDTO));
        BaseResponse response = channelsFeign.vtcPay(vtcRequestDTO);
        baseResponse.setData(response.getData());
        log.info("----------------- vtc收单方法 返回----------------- response: {}", JSON.toJSONString(response));
        return baseResponse;
    }

    /**
     * vtc服务器回调方法
     *
     * @param vtcCallbackDTO vtc回调输入参数
     * @return
     */
    @Override
    public void vtcPayServerCallback(VtcCallbackDTO vtcCallbackDTO, String data, HttpServletResponse response) {
        //校验订单参数
        if (!checkCallback(vtcCallbackDTO)) {
            return;
        }
        //查询md5keyStr
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(vtcCallbackDTO.getReference_number());
        //验签
        String reSignStr = data + "|" + channelsOrder.getMd5KeyStr();//签名前的明文
        log.info("-------------vtc服务器回调接口方法信息记录------------签名前的明文:{}", reSignStr);
        String sign = Sha256Tools.encrypt(reSignStr).toUpperCase();
        log.info("-------------vtc服务器回调接口方法信息记录------------签名后的密文:{}", sign);
        if (!sign.equals(vtcCallbackDTO.getSignature())) {
            log.info("-------------vtc服务器回调接口方法信息记录------------签名不匹配");
            return;
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(vtcCallbackDTO.getReference_number());
        if (orders == null) {
            log.info("-------------vtc服务器回调接口方法信息记录------------回调订单信息不存在");
            return;
        }
        //订单已支付
        if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
            log.info("-------------vtc服务器回调接口方法信息记录------------订单状态为已支付");
            return;
        }
        //校验交易金额
        if (new BigDecimal(vtcCallbackDTO.getAmount()).compareTo(orders.getTradeAmount()) != 0) {
            log.info("------------- vtc服务器回调接口方法信息记录 ------------- 通道订单交易金额信息,与系统订单交易金额信息不匹配");
            return;
        }
        orders.setChannelCallbackTime(new Date());//通道回调时间
        orders.setUpdateTime(new Date());//修改时间
        orders.setChannelNumber(vtcCallbackDTO.getTrans_ref_no());//通道流水号
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        Channel channel = commonService.getChannelByChannelCode(orders.getChannelCode());
        //1代表支付成功
        if (vtcCallbackDTO.getStatus().equals("1")) {
            log.info("-------------vtc服务器回调接口方法信息记录------------订单已支付成功");
            //成功
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //计算通道网关手续费
            if (!StringUtils.isEmpty(channel.getChannelGatewayRate()) && channel.getChannelGatewayCharge().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_YES)
                    && channel.getChannelGatewayStatus().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_SUCCESS_STATUS)) {
                log.info("----------vtc服务器回调接口方法信息记录----------------计算支付成功时的通道网关手续费----------");
                commonService.calcGatewayFee(channel, orders, null);
            }
            //更改channelsOrders状态
            channelsOrderMapper.updateStatusById(orders.getId(), vtcCallbackDTO.getTrans_ref_no(), TradeConstant.TRADE_SUCCESS);
            //修改原订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("---------vtc回调时上报清结算前线上下单创建账户信息----------");
                //账户信息不存在的场合创建对应的账户信息
                if (commonService.getAccount(orders.getInstitutionCode(), orders.getOrderCurrency()) == null) {
                    commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                }
                log.info("---------vtc服务器回调接口方法信息记录----------上报清结算");
                //分润
                if(!StringUtils.isEmpty(orders.getAgencyCode())){
                    rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orders.getId());
                }
                //上报清结算
                FundChangeDTO fundChangeDTO = new FundChangeDTO(orders, TradeConstant.NT, orders.getInstitutionCode());//收单
                //上报清结算资金变动接口
                BaseResponse fundChangeResponse = clearingService.fundChange(fundChangeDTO, null);
                if (fundChangeResponse.getCode() != null && fundChangeResponse.getCode().equals(String.valueOf(AsianWalletConstant.HTTP_SUCCESS_STATUS))) {//请求成功
                    FundChangeVO fundChangeVO = (FundChangeVO) fundChangeResponse.getData();
                    if (!fundChangeVO.getRespCode().equals(TradeConstant.CLEARING_SUCCESS)) {//业务处理失败
                        log.info("----------vtc服务器回调接口方法信息记录-------回调订单状态信息记录 上报清结算失败 上报队列 MQ_PLACE_ORDER_FUND_CHANGE_FAIL -------------- orders : {}", JSON.toJSONString(orders));
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));

                    }
                } else {//请求失败
                    log.info("-----------vtc服务器回调接口方法信息记录------回调订单状态信息记录 上报清结算失败 上报队列 MQ_PLACE_ORDER_FUND_CHANGE_FAIL -------------- orders : {}", JSON.toJSON(orders));
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));//撤销时调用清结算资金变动RV时发生失败时的队列
                }
            }
            //新增交易成功的订单物流信息
            this.commonService.insertOrderLogistics(orders);
            //支付成功后向用户发送邮件
            this.commonService.sendEmail(orders.getDraweeEmail(), orders.getLanguage(), Status._1, orders);//支付成功给付款人发送邮件
        } else {
            //支付失败
            log.info("-------------vtc服务器回调接口方法信息记录------------订单已支付失败");
            //计算通道网关手续费
            if (!StringUtils.isEmpty(channel.getChannelGatewayRate()) && channel.getChannelGatewayCharge().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_YES)
                    && channel.getChannelGatewayStatus().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_FAILURE_STATUS)) {
                log.info("----------vtc服务器回调接口方法信息记录----------------计算支付失败时的通道网关手续费----------");
                commonService.calcGatewayFee(channel, orders, null);
            }
            //更改channelsOrders状态
            channelsOrderMapper.updateStatusById(orders.getId(), vtcCallbackDTO.getTrans_ref_no(), TradeConstant.TRADE_FALID);
            //支付失败
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //上游返回的错误code
            orders.setRemark4(vtcCallbackDTO.getStatus());
            ordersMapper.updateByExampleSelective(orders, example);//更新订单
        }
        //回调商户服务器
        if (!StringUtils.isEmpty(orders.getReturnUrl())) {
            try {
                log.info("-------------vtc服务器回调接口方法信息记录------------回调商户开始----------");
                commonService.replyReturnUrl(orders);
            } catch (Exception e) {
                log.error("-------------vtc服务器回调接口方法信息记录---------回调商户异常----------", e);
            }
        }
        try {
            response.getWriter().write("success");
        } catch (IOException e) {
            log.error("************************vtc服务器回调方法发生异常******************",e);
        }
    }

    /**
     * vtc回调方法
     *
     * @param vtcCallbackDTO vtc回调输入参数
     * @return
     */
    @Override
    public void vtcPayBrowserCallback(VtcCallbackDTO vtcCallbackDTO, HttpServletResponse response) {
        //校验回调参数
        if (!checkCallback(vtcCallbackDTO)) {
            return;
        }
        //查询md5keyStr
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(vtcCallbackDTO.getReference_number());
        //验签
        String channelsSign = ChannelsSignUtils.getVtcSign(vtcCallbackDTO, channelsOrder.getMd5KeyStr());
        if (!channelsSign.equals(vtcCallbackDTO.getSignature())) {
            log.info("-------------vtc浏览器接口回调方法信息记录------------签名不匹配");
            return;
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(vtcCallbackDTO.getReference_number());
        if (orders == null) {
            log.info("-------------vtc浏览器接口回调方法信息记录------------回调订单信息不存在");
            return;
        }
        if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
            //支付成功
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------vtc浏览器接口回调方法信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付成功页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS);
                } catch (IOException e) {
                    log.error("--------------vtc浏览器接口回调方法信息记录--------------调用AW支付成功页面失败", e);
                }
            }
        } else {
            //其他情况
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------vtc浏览器接口回调方法信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付中页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
                } catch (IOException e) {
                    log.error("--------------vtc浏览器接口回调方法信息记录--------------调用AW支付中页面失败", e);
                }
            }
        }
        //回调浏览器
        if (!StringUtils.isEmpty(orders.getJumpUrl())) {
            log.info("-------------vtc浏览器接口回调方法信息记录------------回调商户浏览器开始");
            commonService.replyJumpUrl(orders, response);
        } else {
            try {
                //返回支付成功页面
                response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS);
            } catch (IOException e) {
                log.info("--------------vtc浏览器接口回调方法信息记录--------------回调商户浏览器异常-------", e);
            }
        }
    }

    /**
     * 校验vtc回调参数
     *
     * @param vtcCallbackDTO megaPay回调参数
     * @return
     */
    private boolean checkCallback(VtcCallbackDTO vtcCallbackDTO) {
        //商户上送的商户订单号
        if (StringUtils.isEmpty(vtcCallbackDTO.getReference_number())) {
            log.info("-------------vtc回调方法信息记录------------订单id为空");
            return false;
        }
        if (StringUtils.isEmpty(vtcCallbackDTO.getAmount())) {
            log.info("-------------vtc回调方法信息记录------------金额为空");
            return false;
        }
        if (StringUtils.isEmpty(vtcCallbackDTO.getStatus())) {
            log.info("-------------vtc回调方法信息记录------------交易状态为空");
            return false;
        }
        if (StringUtils.isEmpty(vtcCallbackDTO.getSignature())) {
            log.info("-------------vtc回调方法信息记录------------签名为空");
            return false;
        }
        return true;
    }
}
