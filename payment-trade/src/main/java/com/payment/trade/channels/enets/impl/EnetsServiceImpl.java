package com.payment.trade.channels.enets.impl;

import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.dto.enets.EnetsBankRequestDTO;
import com.payment.common.dto.enets.EnetsOffLineRequestDTO;
import com.payment.common.dto.enets.EnetsSMRequestDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.ChannelsOrder;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.enets.EnetsService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.ChannelsOrderMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.EnetsCallbackDTO;
import com.payment.trade.dto.EnetsPosCallbackDTO;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.utils.AbstractHandlerAdapter;
import com.payment.trade.utils.ComTools;
import com.payment.trade.utils.HandlerType;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
@Transactional
@HandlerType(TradeConstant.ENETS_CSB_OFFLINE)
public class EnetsServiceImpl extends AbstractHandlerAdapter implements EnetsService {

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
     * enets网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse eNetsBankPay(Orders orders, Channel channel, BaseResponse baseResponse) {
        //最内层实体
        //EnetsBankCoreDTO enetsBankCoreDTO = new EnetsBankCoreDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/enetsBrowserCallback"), ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/enetsServerCallback"), amt);
        //内层实体
        //EnetsBankInSideDTO enetsBankInSideDTO = new EnetsBankInSideDTO(enetsBankCoreDTO);
        //请求实体
        //EnetsBankRequestDTO enetsBankRequestDTO = new EnetsBankRequestDTO(enetsBankInSideDTO, channel.getMd5KeyStr(), orders.getInstitutionOrderId(), sign);
        //需要把金额变成分,金额转换 通道要求要放大100倍上送
        double tempAmount = ComTools.mul(orders.getTradeAmount().doubleValue(), 100);
        BigDecimal b1 = new BigDecimal(tempAmount);
        tempAmount = b1.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
        DecimalFormat decimalFormat0 = new DecimalFormat("###0");
        String amt = decimalFormat0.format(tempAmount);
        JSONObject json = new JSONObject();
        //区分排序
        json.put("netsMid", channel.getChannelMerchantId());
        json.put("tid", "192.168.15.92");
        json.put("submissionMode", "B");
        json.put("txnAmount", amt);
        json.put("merchantTxnRef", orders.getId());
        json.put("merchantTxnDtm", DateToolUtils.getReqDateH(orders.getInstitutionOrderTime()));
        json.put("paymentType", "SALE");
        json.put("currencyCode", orders.getTradeCurrency());
        json.put("paymentMode", "DD");
        json.put("merchantTimeZone", "+8:00");
        json.put("b2sTxnEndURL", ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/eNetsBankBrowserCallback"));
        json.put("b2sTxnEndURLParam", "");
        json.put("s2sTxnEndURL", ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/eNetsBankServerCallback"));
        json.put("s2sTxnEndURLParam", "");
        json.put("clientType", "W");
        json.put("supMsg", "");
        json.put("netsMidIndicator", "U");
        json.put("ipAddress", "192.168.15.92");
        json.put("language", "en");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ss", "1");
        jsonObject.put("msg", json.toString());
        //交易请求参数
        String txnReq = jsonObject.toString();
        //生成签名
        String sign = createSign(txnReq, channel.getMd5KeyStr());
        //请求实体
        EnetsBankRequestDTO enetsBankRequestDTO = new EnetsBankRequestDTO(txnReq, channel.getMd5KeyStr(), orders.getInstitutionOrderId(), sign);
        log.info("----------------- enets网银收单方法 ----------------- enetsBankRequestDTO: {}", JSON.toJSONString(enetsBankRequestDTO));
        BaseResponse response = channelsFeign.eNetsBankPay(enetsBankRequestDTO);
        baseResponse.setData(response.getData());
        log.info("----------------- enets网银收单方法 返回----------------- baseResponse: {}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * enets线上扫码收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse eNetsOnlineQRCode(Orders orders, Channel channel, BaseResponse baseResponse) {
        //获取enets支付所需参数
        String netsMid = channel.getChannelMerchantId(); //enets通道商户号
        //标价金额,外币交易的支付金额精确到币种的最小单位，参数值不能带小数点。
        String amt = String.valueOf(orders.getTradeAmount());
        int temamt = 0;
        if (!StringUtils.isEmpty(amt)) {
            Double amt_d = new Double(amt);
            temamt = BigDecimal.valueOf(amt_d).multiply(new BigDecimal(100)).intValue();//通道要求要放大100倍上送
        }
        String txnAmount = Integer.toString(temamt); //交易金额
        String merchantTxnRef = orders.getId(); //商户交易订单流水号
        String b2sTxnEndURL = ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/eNetsQrCodeBrowserCallback"); //浏览器地址
        String s2sTxnEndURL = ad3ParamsConfig.getChannelCallbackUrl().concat("/onlinecallback/eNetsQrCodeServerCallback"); //服务器地址
        String merchantTxnDtm = DateToolUtils.getReqDateH(new Date()); //商户交易时间
        String submissionMode = "B"; //提交方式 B:浏览器 S:服务器
        String paymentType = "SALE"; //产品类型 :SALE
        String paymentMode = "QR"; //支付类型:QR
        String clientType = "W"; //W:计算机浏览器
        String currencyCode = orders.getTradeCurrency();//订单币种
        String merchantTimeZone = "+8:00";//当地时区
        String netsMidIndicator = "U";
        String tid = "";
        String b2sTxnEndURLParam = "";//前台通知地址参数
        String s2sTxnEndURLParam = "";//后台通知地址参数
        String supMsg = "";
        String ipAddress = "192.168.15.92";
        String language = "en";
        //String keyId = channel.getMd5KeyStr();
        String secretKey = "77bf7976-5969-49fa-9a71-87e3d5e7a75f";//MerOtherCert字段
        JSONObject json = new JSONObject();
        //区分排序
        json.put("netsMid", netsMid);
        json.put("tid", tid);
        json.put("submissionMode", submissionMode);
        json.put("txnAmount", txnAmount);
        json.put("merchantTxnRef", merchantTxnRef);
        json.put("merchantTxnDtm", merchantTxnDtm);
        json.put("paymentType", paymentType);
        json.put("currencyCode", currencyCode);
        json.put("paymentMode", paymentMode);
        json.put("merchantTimeZone", merchantTimeZone);
        json.put("b2sTxnEndURL", b2sTxnEndURL);
        json.put("b2sTxnEndURLParam", b2sTxnEndURLParam);
        json.put("s2sTxnEndURL", s2sTxnEndURL);
        json.put("s2sTxnEndURLParam", s2sTxnEndURLParam);
        json.put("clientType", clientType);
        json.put("supMsg", supMsg);
        json.put("netsMidIndicator", netsMidIndicator);
        json.put("ipAddress", ipAddress);
        json.put("language", language);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ss", "1");//默认
        jsonObject.put("msg", json.toString());
        //交易请求参数
        String txnReq = jsonObject.toString();
        //生成签名
        String sign = createSign(txnReq, secretKey);
        EnetsBankRequestDTO enetsBankRequestDTO = new EnetsBankRequestDTO(txnReq, channel.getMd5KeyStr(), orders.getInstitutionOrderId(), sign);
        enetsBankRequestDTO.setKeyId(channel.getMd5KeyStr());
        log.info("----------------- enets线上扫码收单方法 ----------------- enetsBankRequestDTO: {}", JSON.toJSONString(enetsBankRequestDTO));
        BaseResponse response = channelsFeign.eNetsBankPay(enetsBankRequestDTO);
        baseResponse.setData(response.getData());
        log.info("----------------- enets线上扫码收单方法 返回----------------- baseResponse: {}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * enets网银浏览器回调方法
     *
     * @param enetsCallbackDTO enets回调实体
     * @return
     */
    @Override
    public void eNetsBankBrowserCallback(EnetsCallbackDTO enetsCallbackDTO, String txnRes, HttpServletResponse response) {
        //校验回调参数
        if (!checkCallback(enetsCallbackDTO)) {
            return;
        }
        //查询通道md5key
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(enetsCallbackDTO.getMerchantTxnRef());
        //校验签名
        if (!Objects.equals(createSign(txnRes, channelsOrder.getMd5KeyStr()), enetsCallbackDTO.getHmac())) {
            log.info("-------------eNets网银浏览器回调接口信息记录------------签名不匹配");
            return;
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(enetsCallbackDTO.getMerchantTxnRef());
        if (orders == null) {
            log.info("-------------eNets网银浏览器回调接口信息记录------------回调订单信息不存在");
            return;
        }
        if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
            //支付成功
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------eNets网银浏览器回调接口信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付成功页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS);
                } catch (IOException e) {
                    log.error("--------------eNets网银浏览器回调接口信息记录--------------调用AW支付成功页面失败", e);
                }
            }
        } else {
            //其他情况
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------eNets网银浏览器回调接口信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付中页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
                } catch (IOException e) {
                    log.error("--------------eNets网银浏览器回调接口信息记录--------------调用AW支付中页面失败", e);
                }
            }
        }
    }

    /**
     * enets服务器回调方法
     *
     * @param enetsCallbackDTO enets回调实体
     * @return
     */
    @Override
    public ResponseEntity<Void> eNetsBankServerCallback(EnetsCallbackDTO enetsCallbackDTO, String txnRes, HttpServletResponse response) {
        //校验回调参数
        if (!checkCallback(enetsCallbackDTO)) {
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //查询通道md5key
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByPrimaryKey(enetsCallbackDTO.getMerchantTxnRef());
        //校验签名
        if (!Objects.equals(createSign(txnRes, channelsOrder.getMd5KeyStr()), enetsCallbackDTO.getHmac())) {
            log.info("=================【eNets网银服务器回调接口信息记录】=================【签名不匹配】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(enetsCallbackDTO.getMerchantTxnRef());
        if (orders == null) {
            log.info("=================【eNets网银服务器回调接口信息记录】=================【回调订单信息不存在】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【eNets网银服务器回调接口信息记录】=================【订单状态不为支付中】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //校验订单信息
        if (!enetsCallbackDTO.getCurrencyCode().equals(orders.getTradeCurrency())) {
            log.info("=================【eNets网银服务器回调接口信息记录】=================【订单信息不匹配】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        orders.setChannelNumber(enetsCallbackDTO.getNetsTxnRef());//通道流水号
        orders.setChannelCallbackTime(new Date());
        orders.setUpdateTime(new Date());
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", orders.getId());
        criteria.andEqualTo("tradeStatus", "2");
        if ("0".equals(enetsCallbackDTO.getNetsTxnStatus())) {
            log.info("=================【eNets网银服务器回调接口信息记录】=================【订单已支付成功】 orderId:{}", enetsCallbackDTO.getMerchantTxnRef());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), enetsCallbackDTO.getNetsTxnRef(), TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("=================【eNets网银服务器回调接口信息记录】=================【更新通道订单异常】", e);
            }
            //修改订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【eNets网银服务器回调接口信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                        log.info("=================【eNets网银服务器回调接口信息记录】=================【上报清结算前线下下单创建账户信息】");
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
                            log.info("=================【eNets网银服务器回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【eNets网银服务器回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【eNets网银服务器回调接口信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【eNets网银服务器回调接口信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("=================【eNets网银服务器回调接口信息记录】=================【订单已支付失败】 orderId:{}", enetsCallbackDTO.getMerchantTxnRef());
            //支付失败
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //上游返回的错误code
            orders.setRemark4(enetsCallbackDTO.getNetsTxnStatus());
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), enetsCallbackDTO.getNetsTxnRef(), TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.info("=================【eNets网银服务器回调接口信息记录】=================【更新通道订单异常】", e);
            }
            //计算支付失败时通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【eNets网银服务器回调接口信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【eNets网银服务器回调接口信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        }
        //商户服务器回调地址不为空,回调商户服务器
        try {
            if (!StringUtils.isEmpty(orders.getReturnUrl())) {
                commonService.replyReturnUrl(orders);
            }
        } catch (Exception e) {
            log.error("=================【eNets网银服务器回调接口信息记录】=================【回调商户服务器异常】", e);
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * enets线上扫码浏览器回调方法
     *
     * @param enetsCallbackDTO enets回调实体
     * @return
     */
    @Override
    public void eNetsQrCodeBrowserCallback(EnetsCallbackDTO enetsCallbackDTO, String txnRes, HttpServletResponse response) {
        //校验回调参数
        if (!checkCallback(enetsCallbackDTO)) {
            return;
        }
        String secretKey = "77bf7976-5969-49fa-9a71-87e3d5e7a75f";
        //校验签名
        if (!createSign(txnRes, secretKey).equalsIgnoreCase(enetsCallbackDTO.getHmac())) {
            log.info("-------------eNets线上扫码浏览器回调接口信息记录------------签名不匹配");
            return;
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(enetsCallbackDTO.getMerchantTxnRef());
        if (orders == null) {
            log.info("-------------eNets线上扫码浏览器回调接口信息记录------------回调订单信息不存在");
            return;
        }
        if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
            //支付成功
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------eNets线上扫码浏览器回调接口信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付成功页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS);
                } catch (IOException e) {
                    log.error("--------------eNets线上扫码浏览器回调接口信息记录--------------调用AW支付成功页面失败", e);
                }
            }
        } else {
            //其他情况
            if (!StringUtils.isEmpty(orders.getJumpUrl())) {
                log.info("-------------eNets线上扫码浏览器回调接口信息记录------------开始回调商户");
                commonService.replyJumpUrl(orders, response);
            } else {
                try {
                    //返回支付中页面
                    response.sendRedirect(ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
                } catch (IOException e) {
                    log.error("--------------eNets线上扫码浏览器回调接口信息记录--------------调用AW支付中页面失败", e);
                }
            }
        }
    }

    /**
     * enets线上扫码服务器回调方法
     *
     * @param enetsCallbackDTO enets回调实体
     * @return
     */
    @Override
    public ResponseEntity<Void> eNetsQrCodeServerCallback(EnetsCallbackDTO enetsCallbackDTO, String txnRes, HttpServletResponse response) {
        //校验回调参数
        if (!checkCallback(enetsCallbackDTO)) {
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        String secretKey = "77bf7976-5969-49fa-9a71-87e3d5e7a75f";
        //校验签名
        if (!createSign(txnRes, secretKey).equalsIgnoreCase(enetsCallbackDTO.getHmac())) {
            log.info("===============【eNets线上扫码服务器回调】===============【签名不匹配】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(enetsCallbackDTO.getMerchantTxnRef());
        if (orders == null) {
            log.info("===============【eNets线上扫码服务器回调】===============【回调订单信息不存在】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【eNets网银服务器回调接口信息记录】=================【订单状态不为支付中】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //校验订单信息
        if (!enetsCallbackDTO.getCurrencyCode().equals(orders.getTradeCurrency())) {
            log.info("===============【eNets线上扫码服务器回调】===============【订单信息不匹配】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        orders.setChannelNumber(enetsCallbackDTO.getNetsTxnRef());//通道流水号
        orders.setChannelCallbackTime(new Date());//通道回调时间
        orders.setUpdateTime(new Date());//修改时间
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if ("0".equals(enetsCallbackDTO.getNetsTxnStatus())) {//0为交易成功
            log.info("===============【eNets线上扫码服务器回调】===============【订单已支付成功】 orderId: {}", orders.getId());
            //支付成功
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), enetsCallbackDTO.getNetsTxnRef(), TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("=================【eNets线上扫码服务器回调】=================【更新通道订单异常】", e);
            }
            //修改原订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【eNets线上扫码服务器回调】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                        log.info("=================【eNets线上扫码服务器回调】=================【上报清结算前线下下单创建账户信息】");
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
                            log.info("=================【eNets线上扫码服务器回调】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【eNets线上扫码服务器回调】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【eNets线上扫码服务器回调】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【eNets线上扫码服务器回调】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("===============【eNets线上扫码服务器回调】===============【订单已支付失败】 orderId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), enetsCallbackDTO.getNetsTxnRef(), TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.error("=================【eNets线上扫码服务器回调】=================【更新通道订单异常】", e);
            }
            //计算支付失败时通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【eNets网银服务器回调接口信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【eNets网银服务器回调接口信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        }
        //商户服务器回调地址不为空,回调商户服务器
        if (!StringUtils.isEmpty(orders.getReturnUrl())) {
            try {
                log.info("===============【eNets线上扫码服务器回调】===============回调商户服务器开始");
                commonService.replyReturnUrl(orders);
            } catch (Exception e) {
                log.error("=================【eNets网银服务器回调接口信息记录】=================回调商户服务器异常", e);
            }
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * 校验enets回调参数
     *
     * @param enetsCallbackDTO enets通道回调实体
     * @return
     */
    private boolean checkCallback(EnetsCallbackDTO enetsCallbackDTO) {
        boolean flag = false;
        if (StringUtils.isEmpty(enetsCallbackDTO.getMerchantTxnRef())) {
            log.info("-------------Enets回调方法信息记录------------订单流水号为空");
            return flag;
        }
        if (StringUtils.isEmpty(enetsCallbackDTO.getHmac())) {
            log.info("-------------Enets回调方法信息记录------------签名为空");
            return flag;
        }
        if (StringUtils.isEmpty(enetsCallbackDTO.getNetsTxnStatus())) {
            log.info("-------------Enets回调方法信息记录------------订单状态为空");
            return flag;
        }
        flag = true;
        return flag;
    }

    /**
     * 生成enets签名
     *
     * @param txnReq    请求实体
     * @param md5KeyStr md5key
     * @return
     */
    private static String createSign(String txnReq, String md5KeyStr) {
        String concatPayloadAndSecretKey = txnReq + md5KeyStr;
        log.info("enets通道签名前的明文:{}", concatPayloadAndSecretKey);
        String sign;
        try {
            sign = encodeBase64(hashSHA256ToBytes(concatPayloadAndSecretKey.getBytes()));
            log.info("enets通道签名后的密文:{}", sign);
        } catch (Exception e) {
            log.error("*********************生成enets签名发生异常********************", e);
            return null;
        }
        return sign;
    }

    /**
     * 生成enets签名
     *
     * @return
     */
    private static String encodeBase64(byte[] data) throws Exception {
        return DatatypeConverter.printBase64Binary(data);
    }

    /**
     * 生成enets签名
     *
     * @return
     */
    private static byte[] hashSHA256ToBytes(byte[] input) throws Exception {
        byte[] byteData = null;
        if (input != null) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input);
            byteData = md.digest();
        } else {
            log.error("hashSHA256ToBytes#输入参数为空");
        }
        return byteData;
    }

    /**
     * enetsPOS CSB回调方法
     *
     * @param enetsPosCallbackDTO enets回调实体
     * @return
     */
    @Override
    public ResponseEntity<Void> eNetsPosCSBCallback(EnetsPosCallbackDTO enetsPosCallbackDTO, HttpServletResponse response) {
        //校验订单参数
        if (!checkPosCallback(enetsPosCallbackDTO)) {
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //根据回调数据查询通道订单
        ChannelsOrder channelsOrder = channelsOrderMapper.selectByRemarks(enetsPosCallbackDTO.getRetrieval_ref(), enetsPosCallbackDTO.getStan(), enetsPosCallbackDTO.getTxn_identifier());
        if (channelsOrder == null) {
            log.info("=============【eNetsPOS扫码服务器回调接口信息记录】=============根据回调数据查询通道订单失败");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //查询原订单信息
        Orders orders = ordersMapper.selectByPrimaryKey(channelsOrder.getId());
        if (orders == null) {
            log.info("=============【eNetsPOS扫码服务器回调接口信息记录】=============回调订单信息不存在");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        //校验订单状态
        if (!TradeConstant.ORDER_PAYING.equals(orders.getTradeStatus())) {
            log.info("=================【eNetsPOS扫码服务器回调接口信息记录】=================【订单状态不为支付中】");
            return new ResponseEntity<Void>(HttpStatus.OK);
        }
        orders.setChannelNumber(enetsPosCallbackDTO.getTxn_identifier());//通道流水号
        orders.setChannelCallbackTime(new Date());//通道回调时间
        orders.setUpdateTime(new Date());//修改时间
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tradeStatus", "2");
        criteria.andEqualTo("id", orders.getId());
        if ("00".equals(enetsPosCallbackDTO.getResponse_code())) {
            log.info("=============【eNetsPOS扫码服务器回调接口信息记录】=============【订单已支付成功】 orderId: {}", orders.getId());
            //支付成功
            orders.setTradeStatus(TradeConstant.ORDER_PAY_SUCCESS);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), null, TradeConstant.TRADE_SUCCESS);
                channelsOrderMapper.updateRemarkById(enetsPosCallbackDTO.getTxn_identifier(), TradeConstant.TRADE_SUCCESS);
            } catch (Exception e) {
                log.error("=================【eNetsPOS扫码服务器回调接口信息记录】=================【更新通道订单异常】", e);
            }
            //修改原订单状态
            int i = ordersMapper.updateByExampleSelective(orders, example);
            if (i > 0) {
                log.info("=================【eNetsPOS扫码服务器回调接口信息记录】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                        log.info("=================【eNetsPOS扫码服务器回调接口信息记录】=================【上报清结算前线下下单创建账户信息】");
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
                            log.info("=================【eNetsPOS扫码服务器回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } else {
                        log.info("=================【eNetsPOS扫码服务器回调接口信息记录】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } catch (Exception e) {
                    log.error("=================【eNetsPOS扫码服务器回调接口信息记录】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                    RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                    rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                }
            } else {
                log.info("=================【eNetsPOS扫码服务器回调接口信息记录】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
            }
        } else {
            log.info("=============【eNetsPOS扫码服务器回调接口信息记录】=============【订单已支付失败】 ordersId: {}", orders.getId());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //更改channelsOrders状态
            try {
                channelsOrderMapper.updateStatusById(orders.getId(), null, TradeConstant.TRADE_FALID);
                channelsOrderMapper.updateRemarkById(enetsPosCallbackDTO.getTxn_identifier(), TradeConstant.TRADE_FALID);
            } catch (Exception e) {
                log.error("=================【eNetsPOS扫码服务器回调接口信息记录】=================【更新通道订单异常】", e);
            }
            //计算支付失败时通道网关手续费
            commonService.calcCallBackGatewayFeeFailed(orders);
            if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                log.info("=================【eNetsPOS扫码服务器回调接口信息记录】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
            } else {
                log.info("=================【eNetsPOS扫码服务器回调接口信息记录】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
            }
        }
        //商户服务器回调地址不为空,回调商户服务器
        if (!StringUtils.isEmpty(orders.getReturnUrl())) {
            try {
                log.info("=============【eNetsPOS扫码服务器回调接口信息记录】=============回调商户服务器开始----------");
                commonService.replyReturnUrl(orders);
            } catch (Exception e) {
                log.error("=============【eNetsPOS扫码服务器回调接口信息记录】=============回调商户服务器异常----------", e);
            }
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    /**
     * 校验enetsPos回调参数
     *
     * @param enetsPosCallbackDTO enetsPos通道回调实体
     * @return
     */
    private boolean checkPosCallback(EnetsPosCallbackDTO enetsPosCallbackDTO) {
        boolean flag = false;
        if (StringUtils.isEmpty(enetsPosCallbackDTO.getRetrieval_ref())) {
            log.info("-------------Enets回调方法信息记录------------Retrieval为空");
            return flag;
        }
        if (StringUtils.isEmpty(enetsPosCallbackDTO.getResponse_code())) {
            log.info("-------------Enets回调方法信息记录------------订单状态为空");
            return flag;
        }
        flag = true;
        return flag;
    }


    /**
     * enets线下扫码收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse offlineCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        EnetsSMRequestDTO enetsSMRequestDTO = new EnetsSMRequestDTO(orders, channel, ad3ParamsConfig.getChannelCallbackUrl().concat("/offlineCallback/eNetsPosCSBCallback"));
        EnetsOffLineRequestDTO enetsOffLineRequestDTO = new EnetsOffLineRequestDTO(enetsSMRequestDTO, orders.getInstitutionOrderId(), orders.getReqIp(), orders.getId());
        log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【Enets-CSB接口】-请求实体  enetsOffLineRequestDTO: {}", JSON.toJSONString(enetsOffLineRequestDTO));
        BaseResponse response = channelsFeign.eNetsPosCSBPay(enetsOffLineRequestDTO);
        log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【Enets-CSB接口】-响应结果  response: {}", JSON.toJSONString(response));
        //请求失败
        if (!TradeConstant.HTTP_SUCCESS.equals(response.getCode())) {
            log.info("-----------------【线下CSB】下单信息记录--------------调用Channels服务【Enets-CSB接口】-状态码异常 code: {}", response.getCode());
            //订单创建失败
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return baseResponse;
        }
        baseResponse.setData(response.getData());
        return baseResponse;
    }
}
