package com.payment.trade.service.impl;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.FundChangeDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.enums.Status;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.HttpResponse;
import com.payment.common.utils.DateToolUtils;
import com.payment.common.utils.IDS;
import com.payment.common.vo.FundChangeVO;
import com.payment.trade.channels.ad3Online.AD3OnlineAcquireService;
import com.payment.trade.channels.alipay.AliPayService;
import com.payment.trade.channels.eghl.EGHLService;
import com.payment.trade.channels.enets.EnetsService;
import com.payment.trade.channels.help2pay.Help2PayService;
import com.payment.trade.channels.megaPay.MegaPayService;
import com.payment.trade.channels.nganluong.NganLuongService;
import com.payment.trade.channels.vtc.VTCService;
import com.payment.trade.channels.wechat.WechatService;
import com.payment.trade.channels.xendit.XenditService;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.InstitutionMapper;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.*;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.ClearingService;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.OnlineGatewayService;
import com.payment.trade.vo.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author shenxinran
 * @Date: 2019/3/14 18:23
 * @Description: 亚洲钱包业务
 */
@Slf4j
@Service
@Transactional
public class OnlineGatewayServiceImpl implements OnlineGatewayService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private AD3OnlineAcquireService ad3OnlineAcquireService;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private AuditorProvider auditorProvider;

    @Autowired
    @Qualifier(value = "ad3ParamsConfig")
    private AD3ParamsConfig ad3ParamsConfig;

    @Value("${custom.cashierDeskUrl}")
    private String cashierDeskUrl;//线上收银台url

    @Autowired
    private EGHLService eghlService;

    @Autowired
    private MegaPayService megaPayService;

    @Autowired
    private VTCService vtcService;

    @Autowired
    private EnetsService enetsService;

    @Autowired
    private Help2PayService help2PayService;

    @Autowired
    private NganLuongService nganLuongService;

    @Autowired
    private XenditService xenditService;

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private WechatService wechatService;

    /**
     * 对商户的线上网关收单
     *
     * @param placeOrdersDTO
     * @return BaseResponse
     */
    @Override
    public BaseResponse gateway(PlaceOrdersDTO placeOrdersDTO) {
        //判断
        if (!StringUtils.isEmpty(placeOrdersDTO.getIssuerId())) {
            //直连
            return directConnection(placeOrdersDTO);
        }
        //间连
        return indirectConnection(placeOrdersDTO);
    }

    /**
     * 线上间连下单
     *
     * @param placeOrdersDTO
     * @return
     */
    private BaseResponse indirectConnection(PlaceOrdersDTO placeOrdersDTO) {
        log.info("----------【线上间连】下单信息记录---------【请求参数】 placeOrdersDTO:{}", JSON.toJSON(placeOrdersDTO));
        if (!commonService.checkOrderCurrency(placeOrdersDTO)) {
            log.info("----------------订单金额不符合的当前币种默认值----------------");
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        //非空check
        if (StringUtils.isEmpty(placeOrdersDTO.getProductName())) {
            //商品名称
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getServerUrl())) {
            //服务器回调地址
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getSignType())) {
            //签名方式
            throw new BusinessException(EResultEnum.SIGN_TYPE_IS_NULL.getCode());
        }
        //重复请求check
        if (!commonService.repeatedRequests(placeOrdersDTO.getInstitutionId(), placeOrdersDTO.getOrderNo())) {
            log.info("-----------------【线上直连】下单信息记录--------------【重复请求】");
            throw new BusinessException(EResultEnum.REPEAT_ORDER_REQUEST.getCode());
        }
        //验签
        if (placeOrdersDTO.getSignType().equals(TradeConstant.RSA)) {
            //RSA验签
            if (!commonService.checkOnlineSignMsg(placeOrdersDTO)) {
                log.info("-----------------【线上间连】下单信息记录--------------【RSA签名不匹配】");
                throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
            }
        } else if (placeOrdersDTO.getSignType().equals(TradeConstant.MD5)) {
            //MD5验签
            if (!commonService.checkOnlineSignMsgUseMD5(placeOrdersDTO)) {
                log.info("-----------------【线上间连】下单信息记录--------------【MD5签名不匹配】");
                throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
            }
        }
        //查询机构对应的产品通道信息
        InstitutionVO institutionVO = institutionMapper.selectRelevantInfo(placeOrdersDTO.getInstitutionId(), null, placeOrdersDTO.getOrderCurrency(), TradeConstant.TRADE_ONLINE, null);
        //校验订单
        commonService.checkICOnlineOrder(placeOrdersDTO, institutionVO);
        //返回结果
        BaseResponse baseResponse = new BaseResponse();
        //收银台URL
        CheckoutCounterURLVO checkoutCounterURLVO = new CheckoutCounterURLVO();
        //根据机构订单号查询订单信息
        Orders dbOrders = ordersMapper.selectOrderByInstitutionOrderId(placeOrdersDTO.getOrderNo());
        if (dbOrders == null) {
            //数据库不存在
            log.info("-----------------【线上间连】下单信息记录-----------------【间连首单】");
            Orders orders = setICAttr(placeOrdersDTO, institutionVO);
            orders.setReportNumber("Q" + IDS.uniqueID().toString());
            ordersMapper.insert(orders);//落地
            //响应实体
            checkoutCounterURLVO.setCheckoutCounterURL(cashierDeskUrl + "?id=" + orders.getId());
            baseResponse.setData(checkoutCounterURLVO);
        } else {
            if (!dbOrders.getTradeStatus().equals(TradeConstant.ORDER_WAIT_PAY)) {
                log.info("-----------------【线上间连】下单信息记录-----------------【间连相同订单】");
                Orders orders = setICAttr(placeOrdersDTO, institutionVO);
                orders.setReportNumber(dbOrders.getReportNumber());
                ordersMapper.insert(orders);//落地
                //响应实体
                checkoutCounterURLVO.setCheckoutCounterURL(cashierDeskUrl + "?id=" + orders.getId());
                baseResponse.setData(checkoutCounterURLVO);
            } else {
                checkoutCounterURLVO.setCheckoutCounterURL(cashierDeskUrl + "?id=" + dbOrders.getId());
                baseResponse.setData(checkoutCounterURLVO);
            }
        }
        return baseResponse;
    }

    /**
     * 间连收银台的设值
     *
     * @param placeOrdersDTO
     * @param institutionVO
     * @return
     */
    private Orders setICAttr(PlaceOrdersDTO placeOrdersDTO, InstitutionVO institutionVO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(placeOrdersDTO, orders);
        //机构code
        orders.setInstitutionCode(placeOrdersDTO.getInstitutionId());
        //设置付款人及邮箱
        orders.setDraweeName(ad3ParamsConfig.getDraweeName());
        if (StringUtils.isEmpty(placeOrdersDTO.getPayerEmail())) {
            orders.setDraweeEmail(ad3ParamsConfig.getDraweeEmail());
        } else {
            orders.setDraweeEmail(placeOrdersDTO.getPayerEmail());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getPayerName())) {
            orders.setDraweeName(ad3ParamsConfig.getDraweeName());
        } else {
            orders.setDraweeName(placeOrdersDTO.getPayerName());
        }
        orders.setId("O" + IDS.uniqueID().toString());//id
        orders.setSecondInstitutionCode(placeOrdersDTO.getSubInstitutionCode());
        orders.setSecondInstitutionName(placeOrdersDTO.getSubInstitutionName());
        commonService.getUrl(placeOrdersDTO, orders);//请求ip
        orders.setGoodsDescription(placeOrdersDTO.getProductDescription());
        orders.setDraweePhone(placeOrdersDTO.getPayerPhone());
        orders.setReturnUrl(placeOrdersDTO.getServerUrl());
        orders.setInstitutionOrderTime(DateToolUtils.getDateByStr(placeOrdersDTO.getOrderTime()));//机构订单时间
        orders.setLanguage(StringUtils.isEmpty(placeOrdersDTO.getLanguage()) ? auditorProvider.getLanguage() : placeOrdersDTO.getLanguage());//语言
        orders.setTradeType(TradeConstant.GATHER_TYPE);//交易类型
        orders.setTradeStatus(TradeConstant.ORDER_WAIT_PAY);//订单状态-待支付
        //订单类型-间连
        orders.setClearStatus(TradeConstant.INDIRECTCONNECTION);
        orders.setAmount(placeOrdersDTO.getOrderAmount());//订单金额
        orders.setInstitutionName(institutionVO.getCnName());//机构名称
        orders.setAgencyCode(institutionVO.getAgencyCode());//代理机构编号
        orders.setInstitutionOrderId(placeOrdersDTO.getOrderNo());
        orders.setCreator(institutionVO.getCnName());//创建人
        orders.setInstitutionName(institutionVO.getCnName());//机构名称
        orders.setCreateTime(new Date());//创建时间
        orders.setTradeDirection(TradeConstant.TRADE_ONLINE);//线上
        orders.setJumpUrl(placeOrdersDTO.getBrowserUrl());//回调浏览器url
        orders.setSign(null);
        return orders;
    }

    /**
     * 线上直连下单
     *
     * @param placeOrdersDTO 下单实体
     * @return baseResponse
     */
    private BaseResponse directConnection(PlaceOrdersDTO placeOrdersDTO) {
        log.info("----------【线上直连】下单信息记录---------【请求参数】 placeOrdersDTO:{}", JSON.toJSON(placeOrdersDTO));
        if (!commonService.checkOrderCurrency(placeOrdersDTO)) {
            log.info("----------------订单金额不符合的当前币种默认值----------------");
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        //非空check

        if (StringUtils.isEmpty(placeOrdersDTO.getProductName())) {
            //商品名称
            throw new BusinessException(EResultEnum.PRODUCT_NAME_DOES_NOT_EXIST.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getServerUrl())) {
            //服务器回调地址
            throw new BusinessException(EResultEnum.SERVER_URL_CANNOT_BE_EMPTY.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getSignType())) {
            //签名方式
            throw new BusinessException(EResultEnum.SIGN_TYPE_IS_NULL.getCode());
        }
        //重复请求check
        if (!commonService.repeatedRequests(placeOrdersDTO.getInstitutionId(), placeOrdersDTO.getOrderNo())) {
            log.info("-----------------【线上直连】下单信息记录--------------【重复请求】");
            throw new BusinessException(EResultEnum.REPEAT_ORDER_REQUEST.getCode());
        }
        //验签
        if (placeOrdersDTO.getSignType().equals(TradeConstant.RSA)) {
            //RSA验签
            if (!commonService.checkOnlineSignMsg(placeOrdersDTO)) {
                log.info("-----------------【线上直连】下单信息记录--------------【RSA签名不匹配】");
                throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
            }
        } else if (placeOrdersDTO.getSignType().equals(TradeConstant.MD5)) {
            //MD5验签
            if (!commonService.checkOnlineSignMsgUseMD5(placeOrdersDTO)) {
                log.info("-----------------【线上直连】下单信息记录--------------【MD5签名不匹配】");
                throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
            }
        }
        //根据机构订单号查询订单
        Orders order = ordersMapper.selectOrderByInstitutionOrderId(placeOrdersDTO.getOrderNo());
        log.info("-----------------【线上直连】下单信息记录-----------------根据【机构订单号】查询数据库订单信息 order:{}", JSON.toJSONString(order));
        //判断订单状态,不为支付中直接返回
        if (order != null && !TradeConstant.ORDER_PAYING.equals(order.getTradeStatus())) {
            log.info("-----------------【线上直连】下单信息记录-----------------订单状态不是【交易中】");
            throw new BusinessException(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        if (order != null && TradeConstant.ORDER_PAY_FAILD.equals(order.getTradeStatus())) {
            log.info("----------【线上收银台】下单信息记录----------【收银台订单支付失败】");
            throw new BusinessException(EResultEnum.ORDER_PAYMENT_FAILED.getCode());
        }
        //查询机构配置信息
        BasicsInfoVO basicsInfo = commonService.getBasicsInfoByIssuerId(placeOrdersDTO.getIssuerId(), placeOrdersDTO, TradeConstant.TRADE_ONLINE);
        log.info("-----------------【线上直连】下单信息记录-----------------【机构配置信息记录】 basicsInfo:{}", JSON.toJSONString(basicsInfo));
        //校验订单
        commonService.checkOnlineOrder(placeOrdersDTO, basicsInfo);
        //设置订单属性
        Orders orders = commonService.setAttributes(placeOrdersDTO, basicsInfo);
        //上报通道的流水号,不为空时使用上一次订单的reportNumber
        if (order != null && !StringUtils.isEmpty(order.getReportNumber())) {
            //上报通道的流水号
            orders.setReportNumber(order.getReportNumber());
        }
        //交易方向--线上
        orders.setTradeDirection(TradeConstant.TRADE_ONLINE);
        //订单类型-直连
        orders.setClearStatus(TradeConstant.DIRECTCONNECTION);
        //响应实体
        BaseResponse baseResponse = new BaseResponse();
        //换汇与手续费计算,下单业务校验
        if (calculationDCMethod(orders, baseResponse, basicsInfo)) {
            return baseResponse;
        }
        //交易状态--支付中
        orders.setTradeStatus(TradeConstant.ORDER_PAYING);
        //上报通道时间
        orders.setReportChannelTime(new Date());
        //订单落地
        ordersMapper.insert(orders);
        //判断通道及上传通道
        if (channelMethod(orders, basicsInfo.getChannel(), baseResponse)) {
            //间联
            if (!StringUtils.isEmpty(baseResponse.getData()) && TradeConstant.INDIRECTCONNECTION.equals(orders.getClearStatus())) {
                AD3OnlineVO ad3OnlineVO = (AD3OnlineVO) baseResponse.getData();
                if (!StringUtils.isEmpty(ad3OnlineVO.getRespCode())) {
                    AD3OnlineScanVO ad3OnlineScanVO = new AD3OnlineScanVO();
                    BeanUtils.copyProperties(ad3OnlineVO, ad3OnlineScanVO);
                    ad3OnlineScanVO.setTradeAmount(orders.getTradeAmount());
                    ad3OnlineScanVO.setTradeCurrency(orders.getTradeCurrency());
                    baseResponse.setData(ad3OnlineScanVO);
                }
            }
            return baseResponse;
        }
        //判断data是否有数据
        if (baseResponse.getData() == null) {
            log.info("----------【线上直连】下单信息记录----------【通道响应结果】为空");
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        return baseResponse;
    }

    /**
     * 收银台收单
     *
     * @param cashierDTO
     * @return
     */
    @Override
    public BaseResponse cashierGateway(CashierDTO cashierDTO) {
        log.info("----------【线上收银台】下单信息记录----------【请求参数】 cashierDTO:{}", JSON.toJSONString(cashierDTO));
        //解密线上参数
        CashierDTO dto = null;
        try {
            dto = commonService.decryptCashierSignMsg(cashierDTO);
        } catch (Exception e) {
            log.info("----------【线上收银台】下单信息记录----------收银台【参数解密】错误");
            throw new BusinessException(EResultEnum.CASH_COUNTER_PARAMETER_DECRYPTION_ERROR.getCode());
        }
        if (StringUtils.isEmpty(dto.getExchangeTime()) || "false".equals(dto.getExchangeTime())) {
            log.info("----------【线上收银台】下单信息记录----------收银台【换汇时间】不存在");
            throw new BusinessException(EResultEnum.CASHIER_EXCHANGE_TIME_DOES_NOT_EXIST.getCode());
        }
        log.info("--------------【收银台解密后参数】--------------dto:{}", JSON.toJSONString(dto));
        //重复请求
        if (!commonService.repeatedRequests(dto.getInstitutionCode(), cashierDTO.getInstitutionOrderId())) {
            log.info("----------【线上收银台】下单信息记录----------【重复请求】");
            throw new BusinessException(EResultEnum.REPEAT_ORDER_REQUEST.getCode());
        }
        //判断原始订单存不存在
        Orders orders = ordersMapper.selectOrderByInstitutionOrderId(dto.getInstitutionOrderId());
        if (orders == null) {
            log.info("----------【线上收银台】下单信息记录----------收银台原始订单不存在 institutionOrderId:{}", cashierDTO.getInstitutionOrderId());
            throw new BusinessException(EResultEnum.ORIGINAL_ORDER_DOES_NOT_EXIST.getCode());
        }
        if (TradeConstant.ORDER_PAY_SUCCESS.equals(orders.getTradeStatus())) {
            log.info("----------【线上收银台】下单信息记录----------【收银台订单已完成支付】");
            throw new BusinessException(EResultEnum.ORDER_PAID_ERROR.getCode());
        }
        if (TradeConstant.ORDER_PAY_FAILD.equals(orders.getTradeStatus())) {
            log.info("----------【线上收银台】下单信息记录----------【收银台订单支付失败】");
            throw new BusinessException(EResultEnum.ORDER_PAYMENT_FAILED.getCode());
        }
        //查询机构配置信息
        BasicsInfoVO basicsInfo = commonService.getBasicsInfoByIssuerId(dto.getIssuerId(), new PlaceOrdersDTO(dto), TradeConstant.TRADE_ONLINE);
        log.info("----------【线上收银台】下单信息记录----------【机构配置信息】 basicsInfo:{}", JSON.toJSONString(basicsInfo));
        //收银台校验订单
        commonService.checkCashierOrder(dto, basicsInfo, orders);
        //通道信息
        Channel channel = basicsInfo.getChannel();
        //校验通道限额
        if (!commonService.verifyChannelLimits(new BigDecimal(dto.getTradeAmount()), basicsInfo.getChannel())) {
            log.info("----------【线上收银台】下单信息记录----------【通道限额】不合法");
            throw new BusinessException(EResultEnum.LIMIT_AMOUNT_ERROR.getCode());
        }
        BaseResponse baseResponse = new BaseResponse();
        //设置订单属性
        orders = commonService.setCashierAttributes(dto, basicsInfo, orders);
        //截取金额
        if (!commonService.interceptDigit(orders, baseResponse)) {
            log.info("-----------------【线上收银台】下单信息记录--------------【币种默认值】未取到 tradeCurrency:{}", orders.getTradeCurrency());
            return baseResponse;
        }
        //换汇和手续费计算
        if (calcPoundageMethod(orders, basicsInfo, baseResponse)) {
            return baseResponse;
        }
        //订单信息的设置
        orders.setInstitutionOrderTime(orders.getInstitutionOrderTime());
        orders.setReportNumber(orders.getReportNumber());
        orders.setReportChannelTime(new Date());//上报通道时间
        orders.setIssuerId(channel.getIssuerId());//间连的场合设置issuerId的值
        orders.setBankName(basicsInfo.getBankName());//银行名称
        log.info("----------【线上收银台】下单信息记录----------【收银台收单】");
        //交易状态--支付中
        orders.setTradeStatus(TradeConstant.ORDER_PAYING);
        //订单id
        orders.setId("O" + IDS.uniqueID().toString());
        ordersMapper.insert(orders);
        //判断通道
        if (channelMethod(orders, channel, baseResponse)) {
            //间联
            if (!StringUtils.isEmpty(baseResponse.getData()) && TradeConstant.INDIRECTCONNECTION.equals(orders.getClearStatus())) {
                AD3OnlineVO ad3OnlineVO = (AD3OnlineVO) baseResponse.getData();
                if (!StringUtils.isEmpty(ad3OnlineVO.getRespCode())) {
                    AD3OnlineScanVO ad3OnlineScanVO = new AD3OnlineScanVO();
                    BeanUtils.copyProperties(ad3OnlineVO, ad3OnlineScanVO);
                    ad3OnlineScanVO.setTradeAmount(orders.getTradeAmount());
                    ad3OnlineScanVO.setTradeCurrency(orders.getTradeCurrency());
                    baseResponse.setData(ad3OnlineScanVO);
                }
            }
            return baseResponse;
        }
        //若无任何响应
        if (baseResponse.getData() == null) {
            log.info("----------【线上收银台】下单信息记录----------【通道响应结果】为空");
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        return baseResponse;
    }


    /**
     * 判断通道及上传通道
     *
     * @param orders
     * @param baseResponse
     * @return
     */
    private boolean channelMethod(Orders orders, Channel channel, BaseResponse baseResponse) {
        //获取通道信息
        log.info("----------------- 判断通道及上传通道 ----------------- channel: {}", JSON.toJSONString(channel));
        AD3OnlineVO ad3OnlineVO = new AD3OnlineVO();
        //根据en_name判断通道
        if (channel.getChannelEnName().equalsIgnoreCase(AD3Constant.AD3_ONLINE)) {
            //AD3收单返回参数
            baseResponse = ad3OnlineAcquireService.onlineOrder(orders, channel, baseResponse);
            if (baseResponse.getCode() != null) {
                return true;
            }
            String aD3ReturnParameter = (String) baseResponse.getData();
            if (aD3ReturnParameter.replaceAll("\\s*", "").matches(".*html.*")) {
                //网银
                ad3OnlineVO.setRespCode("T000");
                ad3OnlineVO.setCode_url(aD3ReturnParameter);
                ad3OnlineVO.setType(TradeConstant.ONLINE_BANKING.toUpperCase());
                baseResponse.setData(ad3OnlineVO);
                return true;
            } else {
                //扫码
                ad3OnlineVO = JSON.parseObject(aD3ReturnParameter, AD3OnlineVO.class);
                if (!ad3OnlineVO.getRespCode().equals(AD3Constant.AD3_ONLINE_SUCCESS)) {
                    baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
                    return true;
                }
                ad3OnlineVO.setType(channel.getIssuerId().toUpperCase());
                log.info("------------通道响应参数------------ad3OnlineVO:{}", JSON.toJSON(ad3OnlineVO));
                baseResponse.setData(ad3OnlineVO);
                return true;
            }
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.EGHL_ONLINE)) {
            //eghl通道
            baseResponse = eghlService.eghlPay(orders, channel, baseResponse);
            if (StringUtils.isEmpty(baseResponse.getData())) {
                return false;
            }
            String aD3ReturnParameter = (String) baseResponse.getData();
            if (aD3ReturnParameter.replaceAll("\\s*", "").matches(".*html.*")) {
                //网银
                ad3OnlineVO.setRespCode("T000");
                ad3OnlineVO.setCode_url(aD3ReturnParameter);
                ad3OnlineVO.setType(TradeConstant.ONLINE_BANKING);
                baseResponse.setData(ad3OnlineVO);
                return true;
            }
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.MEGAPAY_ONLINE)) {
            //megapay通道
            baseResponse = megaPayService.megaPay(orders, channel, baseResponse);
            if (StringUtils.isEmpty(baseResponse.getData())) {
                return false;
            }
            String aD3ReturnParameter = (String) baseResponse.getData();
            if (aD3ReturnParameter.replaceAll("\\s*", "").matches(".*html.*")) {
                //网银
                ad3OnlineVO.setRespCode("T000");
                if (aD3ReturnParameter.contains("href=\"")) {
                    ad3OnlineVO.setCode_url(TradeConstant.START + aD3ReturnParameter.substring(aD3ReturnParameter.indexOf("href=\"") + 6, aD3ReturnParameter.indexOf("\">here</a>")) + TradeConstant.END);
                } else {
                    ad3OnlineVO.setCode_url(aD3ReturnParameter);
                }
                ad3OnlineVO.setType(TradeConstant.ONLINE_BANKING);
                baseResponse.setData(ad3OnlineVO);
                return true;
            }
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.VTC_ONLINE)) {
            //vtc通道
            baseResponse = vtcService.vtcPay(orders, channel, baseResponse);
            if (StringUtils.isEmpty(baseResponse.getData())) {
                return false;
            }
            String aD3ReturnParameter = (String) baseResponse.getData();
            if (aD3ReturnParameter.replaceAll("\\s*", "").matches(".*html.*")) {
                //网银
                ad3OnlineVO.setRespCode("T000");
                ad3OnlineVO.setCode_url(aD3ReturnParameter);
                ad3OnlineVO.setType(TradeConstant.ONLINE_BANKING);
                baseResponse.setData(ad3OnlineVO);
                return true;
            }
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ENETS_ONLINE_BANK)) {
            //ENETS个人网银
            baseResponse = enetsService.eNetsBankPay(orders, channel, baseResponse);
            if (StringUtils.isEmpty(baseResponse.getData())) {
                return false;
            }
            //网银
            ad3OnlineVO.setRespCode("T000");
            ad3OnlineVO.setType(TradeConstant.ONLINE_BANKING);
            ad3OnlineVO.setCode_url(String.valueOf(baseResponse.getData()));//网银请求实体
            baseResponse.setData(ad3OnlineVO);
            return true;
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.ENETS_ONLINE_QRCODE)) {
            //ENETS线上扫码
            baseResponse = enetsService.eNetsOnlineQRCode(orders, channel, baseResponse);
            if (StringUtils.isEmpty(baseResponse.getData())) {
                return false;
            }
            //扫码
            ad3OnlineVO.setRespCode("T000");
            ad3OnlineVO.setType(TradeConstant.ONLINE_BANKING);
            ad3OnlineVO.setCode_url(String.valueOf(baseResponse.getData()));//网银请求实体
            baseResponse.setData(ad3OnlineVO);
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.HELP2PAY_ONLINE)) {
            //help2pay通道
            baseResponse = help2PayService.help2Pay(orders, channel, baseResponse);
            if (StringUtils.isEmpty(baseResponse.getData())) {
                return false;
            }
            String aD3ReturnParameter = (String) baseResponse.getData();
            if (aD3ReturnParameter.replaceAll("\\s*", "").matches(".*html.*")) {
                //网银
                ad3OnlineVO.setRespCode("T000");
                ad3OnlineVO.setCode_url(aD3ReturnParameter);
                ad3OnlineVO.setType(TradeConstant.ONLINE_BANKING);
                baseResponse.setData(ad3OnlineVO);
                return true;
            }
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NEXTPOS_ONLINE)) {
            //nextPos线上扫码
            baseResponse = megaPayService.megaPayNextPos(orders, channel, baseResponse);
            //CODE不为空,返回错误信息
            if (!StringUtils.isEmpty(baseResponse.getCode())) {
                return true;
            }
            //扫码
            ad3OnlineVO.setRespCode("T000");
            ad3OnlineVO.setType(TradeConstant.NEXTPOS);
            ad3OnlineVO.setCode_url(String.valueOf(baseResponse.getData()));//二维码
            baseResponse.setData(ad3OnlineVO);
            return true;
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.NGANLUONG_ONLINE)) {
            //nganLuong网银收单
            baseResponse = nganLuongService.nganLuongPay(orders, channel, baseResponse);
            //CODE不为空,返回错误信息
            if (!StringUtils.isEmpty(baseResponse.getCode())) {
                return true;
            }
            Map<String, String> map = (Map<String, String>) baseResponse.getData();
            //网银
            ad3OnlineVO.setRespCode("T000");
            ad3OnlineVO.setType(TradeConstant.ONLINE_BANKING);
            ad3OnlineVO.setCode_url(TradeConstant.START + map.get("checkout_url") + TradeConstant.END);//跳转URL
            baseResponse.setData(ad3OnlineVO);
            return true;
        } else if (channel.getChannelEnName().equalsIgnoreCase(TradeConstant.XENDIT_ONLINE)) {
            //xendit网银收单
            baseResponse = xenditService.xenditPay(orders, channel, baseResponse);
            //CODE不为空,返回错误信息
            if (!StringUtils.isEmpty(baseResponse.getCode())) {
                return true;
            }
            JSONObject jsonObject = JSONObject.fromObject(baseResponse.getData());
            //判断发票url
            if (StringUtils.isEmpty(jsonObject.getString("invoice_url"))) {
                baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
                return true;
            }
            ad3OnlineVO.setRespCode("T000");
            ad3OnlineVO.setCode_url(jsonObject.getString("invoice_url"));
            baseResponse.setData(ad3OnlineVO);
            return true;
        } else if (channel.getChannelEnName().equals(TradeConstant.ALIPAY_CSB_ONLINE)) {
            //支付宝线下CSB支付接口
            baseResponse = aliPayService.aliPayCSB(orders, channel, baseResponse);
            //CODE不为空,返回错误信息
            if (!StringUtils.isEmpty(baseResponse.getCode())) {
                return true;
            }
            //扫码
            ad3OnlineVO.setRespCode("T000");
            ad3OnlineVO.setType(TradeConstant.ALIPAY);
            ad3OnlineVO.setCode_url(String.valueOf(baseResponse.getData()));//二维码
            baseResponse.setData(ad3OnlineVO);
            return true;
        } else if (channel.getChannelEnName().equals(TradeConstant.WECHAT_CSB_ONLINE)) {
            //微信线上CSB支付接口
            baseResponse = wechatService.wechatCSB(orders, channel, baseResponse);
            //CODE不为空,返回错误信息
            if (!StringUtils.isEmpty(baseResponse.getCode())) {
                return true;
            }
            //扫码
            ad3OnlineVO.setRespCode("T000");
            ad3OnlineVO.setType(TradeConstant.WECHAT);
            ad3OnlineVO.setCode_url(String.valueOf(baseResponse.getData()));//二维码
            baseResponse.setData(ad3OnlineVO);
            return true;
        } else if (channel.getChannelEnName().equals(TradeConstant.CLOUD_PAY_ONLINE)) {
            //云闪付通道,扫码
            ad3OnlineVO.setRespCode("T000");
            ad3OnlineVO.setType(TradeConstant.CLOUD);
            ad3OnlineVO.setCode_url(IDS.uniqueID().toString());//二维码
            baseResponse.setData(ad3OnlineVO);
            return true;
        } else {
            log.info("-----------------【线上】下单信息记录--------------【通道服务名称】不匹配 channelEnName: {}", channel.getChannelEnName());
            baseResponse.setCode(EResultEnum.CHANNEL_SERVICE_NAME_NO_MATCH.getCode());
            return true;
        }
        return false;
    }


    /**
     * 线上通道订单状态查询
     *
     * @param onlineOrderQueryDTO
     * @return
     */
    @Override
    public BaseResponse onlineOrderQuery(OnlineOrderQueryDTO onlineOrderQueryDTO) {
        log.info("-----------线上通道订单状态查询开始-----------onlineOrderQueryDTO:{}", JSON.toJSON(onlineOrderQueryDTO));
        Orders orders = ordersMapper.selectOrderByInstitutionOrderId(onlineOrderQueryDTO.getOrderNo());
        BaseResponse response = new BaseResponse();
        //订单不存在
        if (orders == null) {
            log.info("-------------订单不存在------------ad3OnlineOrderQueryVO:{}", JSON.toJSON(onlineOrderQueryDTO));
            response.setCode(EResultEnum.ORDER_NOT_EXIST.getCode());
            return response;
        }
        //返回结果
        OnlineQueryOrderVO onlineQueryOrderVO = new OnlineQueryOrderVO();
        //查询通道
        Channel channel = commonService.getChannelByChannelCode(orders.getChannelCode());
        if (!orders.getTradeStatus().equals(TradeConstant.ORDER_PAYING)) {
            onlineQueryOrderVO.setOrderNo(orders.getInstitutionOrderId());
            onlineQueryOrderVO.setTxnstatus(orders.getTradeStatus());
            response.setData(onlineQueryOrderVO);
            return response;
        }
        //判断通道
        if (channel.getChannelEnName().equals(TradeConstant.AD3_ONLINE)) {
            AD3OnlineOrderQueryDTO ad3OnlineOrderQueryDTO = new AD3OnlineOrderQueryDTO(orders, ad3ParamsConfig.getMerchantCode());
            ad3OnlineOrderQueryDTO.setMerorderDatetime(DateUtil.format(orders.getReportChannelTime(), "yyyyMMddHHmmss"));
            HttpResponse httpResponse = ad3OnlineAcquireService.ad3OnlineOrderQuery(ad3OnlineOrderQueryDTO, null);
            if (!httpResponse.getHttpStatus().equals(AsianWalletConstant.HTTP_SUCCESS_STATUS)) {
                log.info("------------------向上游查询订单状态异常------------------OnlineOrderQueryDTO:{},ad3OnlineOrderQueryDTO:{}", JSON.toJSON(onlineOrderQueryDTO), JSON.toJSON(ad3OnlineOrderQueryDTO));
                response.setCode(EResultEnum.QUERY_ORDER_ERROR.getCode());
                return response;
            }
            AD3OnlineOrderQueryVO ad3OnlineOrderQueryVO = JSON.parseObject(httpResponse.getJsonObject().toJSONString(), AD3OnlineOrderQueryVO.class);
            if (ad3OnlineOrderQueryVO == null || StringUtils.isEmpty(ad3OnlineOrderQueryVO.getState())) {
                log.info("---------------上游返回的查询信息为空---------------");
                response.setCode(EResultEnum.QUERY_ORDER_ERROR.getCode());
                return response;
            }
            orders.setChannelNumber(ad3OnlineOrderQueryVO.getTxnId());//通道流水号
            //更新时间
            orders.setUpdateTime(new Date());
            //通道回调时间
            orders.setChannelCallbackTime(DateUtil.parse(ad3OnlineOrderQueryVO.getTxnDate(), "yyyyMMddHHmmss"));
            String status = ad3OnlineOrderQueryVO.getState();
            Example example = new Example(Orders.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("tradeStatus", "2");
            criteria.andEqualTo("id", orders.getId());
            if (AD3Constant.ORDER_SUCCESS.equals(status)) {
                orders.setTradeStatus((TradeConstant.ORDER_PAY_SUCCESS));
                int i = ordersMapper.updateByExampleSelective(orders, example);
                if (i > 0) {
                    log.info("=================【线上通道订单状态查询】=================【订单支付成功后更新数据库成功】 orderId: {}", orders.getId());
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
                            log.info("=================【线上通道订单状态查询】=================【上报清结算前线下下单创建账户信息】");
                            commonService.createAccount(orders.getInstitutionCode(), orders.getOrderCurrency());
                        }
                        //分润
                        if (!StringUtils.isEmpty(orders.getAgencyCode())) {
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
                                log.info("=================【线上通道订单状态查询】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                                RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                                rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                            }
                        } else {
                            log.info("=================【线上通道订单状态查询】=================【上报清结算失败,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】");
                            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                            rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                        }
                    } catch (Exception e) {
                        log.error("=================【线上通道订单状态查询】=================【上报清结算异常,上报队列】 【MQ_PLACE_ORDER_FUND_CHANGE_FAIL】", e);
                        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orders));
                        rabbitMQSender.send(AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL, JSON.toJSONString(rabbitMassage));
                    }
                } else {
                    log.info("=================【线上通道订单状态查询】=================【订单支付成功后更新数据库失败】 orderId: {}", orders.getId());
                }
            } else if (AD3Constant.ORDER_FAILED.equals(status)) {
                log.info("=================【线上通道订单状态查询】=================【订单已支付失败】 orderId: {}", orders.getId());
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                orders.setRemark4(status);
                //计算支付失败时通道网关手续费
                commonService.calcCallBackGatewayFeeFailed(orders);
                if (ordersMapper.updateByExampleSelective(orders, example) == 1) {
                    log.info("=================【线上通道订单状态查询】=================【订单支付失败后更新数据库成功】 orderId: {}", orders.getId());
                } else {
                    log.info("=================【线上通道订单状态查询】=================【订单支付失败后更新数据库失败】 orderId: {}", orders.getId());
                }
            } else {
                log.info("=================【线上通道订单状态查询】=================【订单为支付中】");
            }
        }
        onlineQueryOrderVO.setOrderNo(orders.getInstitutionOrderId());
        onlineQueryOrderVO.setTxnstatus(orders.getTradeStatus());
        log.info("--------------返回给收银台参数--------------onlineQueryOrderVO:{}", JSON.toJSONString(onlineQueryOrderVO));
        response.setData(onlineQueryOrderVO);
        return response;
    }

    /**
     * 收银台所需的基础信息
     *
     * @param orderId
     * @param language
     * @return
     */
    @Override
    public BaseResponse cashier(String orderId, String language) {
        log.info("--------------收银台业务开始-------------- 订单号:{}", orderId);
        if (StringUtils.isEmpty(orderId)) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        Orders orders = ordersMapper.selectByPrimaryKey(orderId);
        if (orders == null) {
            log.info("--------------收银台订单不存在-------------- 订单号:{}", orderId);
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        //查询机构对应的产品通道信息
        InstitutionVO institutionVO = institutionMapper.selectRelevantInfo(orders.getInstitutionCode(), null, orders.getOrderCurrency(), TradeConstant.TRADE_ONLINE, language);
        if (institutionVO == null) {
            log.info("-----------收银台机构CODE对应的产品通道信息不存在-----------订单id:{},orders:{},机构code:{}", orderId, JSON.toJSON(orders), orders.getInstitutionCode());
            throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_CHANNEL_NOT_EXISTS.getCode());//机构产品通道信息不存在
        }
        institutionVO.setOrderId(orders.getId());
        institutionVO.setOrders(orders);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(institutionVO);
        return baseResponse;
    }


    /**
     * 计算手续费 通道手续费 通道网关手续费
     *
     * @param orders
     * @param basicsInfoVO
     * @param baseResponse
     * @return
     */
    private boolean calcPoundageMethod(Orders orders, BasicsInfoVO basicsInfoVO, BaseResponse baseResponse) {
        //计算手续费
        CalcFeeVO calcFeeVO = commonService.calcPoundage(orders, basicsInfoVO);
        orders.setChargeTime(calcFeeVO.getChargeTime());//计费时间
        orders.setChargeStatus(calcFeeVO.getChargeStatus());//计费状态
        //判断计费状态
        if (calcFeeVO.getChargeStatus().equals(TradeConstant.CHARGE_STATUS_OTHER)) {
            //备注
            orders.setRemark("计算手续费时,产品币种转换订单币种的汇率不存在,本位币种:" + basicsInfoVO.getProduct().getCurrency() + " 目标币种:" + orders.getOrderCurrency());
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            ordersMapper.insert(orders);
            baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
            return true;
        }
        if (calcFeeVO.getChargeStatus().equals(TradeConstant.CHARGE_STATUS_FALID)) {
            orders.setRemark("计算手续费失败");//备注
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            ordersMapper.insert(orders);
            baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
            return true;
        }
        orders.setFee(calcFeeVO.getFee());//手续费
        //计算通道手续费用
        if (!StringUtils.isEmpty(basicsInfoVO.getChannel().getChannelRate())) {
            CalcFeeVO channelPoundage = commonService.calcChannelPoundage(orders.getAmount(), basicsInfoVO.getChannel());
            if (channelPoundage.getChargeStatus().equals(TradeConstant.CHARGE_STATUS_FALID)) {
                orders.setRemark("通道手续费计费失败");//备注
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                ordersMapper.insert(orders);
                baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
                return true;
            }
            //通道手续费
            orders.setChannelFee(channelPoundage.getFee());
        }
        //计算通道网关手续费
        if (!StringUtils.isEmpty(basicsInfoVO.getChannel().getChannelGatewayRate())
                && basicsInfoVO.getChannel().getChannelGatewayCharge().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_YES)
                && basicsInfoVO.getChannel().getChannelGatewayStatus().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_ALL_STATUS)
        ) {
            log.info("--------------通道网关手续费收取：全收--------------");
            return commonService.calcGatewayFee(basicsInfoVO.getChannel(), orders, baseResponse);
        }
        return false;
    }

    /**
     * 计算直连 汇率 手续费 通道手续费 通道网关手续费
     *
     * @param orders
     * @param baseResponse
     * @param basicsInfoVO
     * @return
     */
    private boolean calculationDCMethod(Orders orders, BaseResponse baseResponse, BasicsInfoVO basicsInfoVO) {
        //判断订单币种,在对应通道中是否支持
        if (!orders.getOrderCurrency().equals(basicsInfoVO.getChannel().getCurrency())) {
            //换汇
            CalcRateVO calcRateVO = commonService.calcExchangeRate(orders.getOrderCurrency(), basicsInfoVO.getChannel().getCurrency(), basicsInfoVO.getInstitutionProduct().getFloatRate(), orders.getAmount());
            orders.setExchangeTime(calcRateVO.getExchangeTime());//换汇时间
            //判断换汇状态
            if (calcRateVO.getExchangeStatus().equals(TradeConstant.SWAP_FALID)) {
                orders.setExchangeStatus(calcRateVO.getExchangeStatus());//换汇状态
                orders.setRemark("未查询到币种汇率,本位币种：" + orders.getOrderCurrency() + " 目标币种：" + basicsInfoVO.getChannel().getCurrency());//备注
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                //交易币种
                orders.setTradeCurrency(basicsInfoVO.getChannel().getCurrency());
                ordersMapper.insert(orders);
                //换汇失败
                baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
                log.info("-------------换汇失败------------- 结束下单");
                return true;
            }
            orders.setExchangeRate(calcRateVO.getExchangeRate());//换汇汇率
            orders.setTradeCurrency(basicsInfoVO.getChannel().getCurrency());//交易币种
            orders.setTradeAmount(calcRateVO.getTradeAmount());//交易金额
            orders.setExchangeStatus(calcRateVO.getExchangeStatus());//换汇状态
            orders.setCommodityName(String.valueOf(calcRateVO.getOriginalRate()));//原始汇率
        } else {
            //未换汇
            orders.setExchangeRate(new BigDecimal(1));//换汇汇率
            orders.setExchangeTime(new Date());//换汇时间
            orders.setTradeCurrency(orders.getOrderCurrency());//交易币种
            orders.setTradeAmount(orders.getAmount());//交易金额
            orders.setCommodityName("1");//汇率
        }
        //下单业务信息校验
        if (!StringUtils.isEmpty(commonService.checkPlaceOrder(orders, basicsInfoVO, baseResponse).getCode())) {
            //错误信息不为空则返回
            return true;
        }
        //手续费 通道手续费 通道网关手续费
        return calcPoundageMethod(orders, basicsInfoVO, baseResponse);
    }

    /**
     * 查询线上订单信息
     *
     * @param onlineqOrderInfoDTO
     * @returnon
     */
    @Override
    public List<OnlineOrdersInfoVO> pageOnlineqOrderInfo(OnlineqOrderInfoDTO onlineqOrderInfoDTO) {
        if (StringUtils.isEmpty(onlineqOrderInfoDTO.getSignType())) {
            onlineqOrderInfoDTO.setSignType(TradeConstant.RSA);
        }
        String language = onlineqOrderInfoDTO.getLanguage();
        onlineqOrderInfoDTO.setLanguage(null);
        //验签
        if (!commonService.checkSignMsgWithRSAMD5(onlineqOrderInfoDTO)) {
            log.info("----------------- 查询线上订单信息签名错误--------------");
            throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
        }
        //默认为页码1,条数30
        if (StringUtils.isEmpty(onlineqOrderInfoDTO.getPageNum())) {
            onlineqOrderInfoDTO.setPageNum(1);
        }
        if (StringUtils.isEmpty(onlineqOrderInfoDTO.getPageSize())) {
            onlineqOrderInfoDTO.setPageSize(30);
        }
        onlineqOrderInfoDTO.setLanguage(language);
        return ordersMapper.pageOnlineOrderInfo(onlineqOrderInfoDTO);
    }

    /**
     * 线上通道订单状态查询 RSA
     *
     * @param onlineOrderQueryRSADTO
     * @return
     */
    @Override
    public BaseResponse onlineqOrderQueryingUseRSA(OnlineOrderQueryRSADTO onlineOrderQueryRSADTO) {
        //校验签名
        if (!commonService.checkOnlineSignMsg(onlineOrderQueryRSADTO)) {
            log.info("----------------- 线上通道订单状态查询--------------");
            throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
        }
        OnlineOrderQueryDTO onlineOrderQueryDTO = new OnlineOrderQueryDTO();
        onlineOrderQueryDTO.setInstitutionId(onlineOrderQueryRSADTO.getInstitutionId());
        onlineOrderQueryDTO.setOrderNo(onlineOrderQueryRSADTO.getOrderNo());
        return onlineOrderQuery(onlineOrderQueryDTO);
    }

    /******************************************************************【【模拟界面使用】】******************************************************************/

    /**
     * 模拟界面用
     *
     * @param placeOrdersDTO
     * @return
     */
    @Override
    public BaseResponse imitateGateway(PlaceOrdersDTO placeOrdersDTO) {
        //判断
        if (!StringUtils.isEmpty(placeOrdersDTO.getIssuerId())) {
            //直连
            return imitateDirectConnection(placeOrdersDTO);
        }
        //间连
        return indirectConnection(placeOrdersDTO);
    }

    /**
     * 【模拟界面用】
     * 线上直连下单
     *
     * @param placeOrdersDTO 下单实体
     * @return baseResponse
     */
    private BaseResponse imitateDirectConnection(PlaceOrdersDTO placeOrdersDTO) {
        log.info("----------【模拟界面用 线上直连】下单信息记录---------【请求参数】 placeOrdersDTO:{}", JSON.toJSON(placeOrdersDTO));
        if (!commonService.checkOrderCurrency(placeOrdersDTO)) {
            log.info("----------------订单金额不符合的当前币种默认值----------------");
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getProductName())) {
            //商品名称
            throw new BusinessException(EResultEnum.PRODUCT_NAME_DOES_NOT_EXIST.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getServerUrl())) {
            //服务器回调地址
            throw new BusinessException(EResultEnum.SERVER_URL_CANNOT_BE_EMPTY.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getSignType())) {
            //签名方式
            throw new BusinessException(EResultEnum.SIGN_TYPE_IS_NULL.getCode());
        }
        //重复请求check
        if (!commonService.repeatedRequests(placeOrdersDTO.getInstitutionId(), placeOrdersDTO.getOrderNo())) {
            log.info("-----------------【线上直连】下单信息记录--------------【重复请求】");
            throw new BusinessException(EResultEnum.REPEAT_ORDER_REQUEST.getCode());
        }
        //验签
        if (placeOrdersDTO.getSignType().equals(TradeConstant.RSA)) {
            //RSA验签
            if (!commonService.checkOnlineSignMsg(placeOrdersDTO)) {
                log.info("-----------------【线上直连】下单信息记录--------------【RSA签名不匹配】");
                throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
            }
        } else if (placeOrdersDTO.getSignType().equals(TradeConstant.MD5)) {
            //MD5验签
            if (!commonService.checkOnlineSignMsgUseMD5(placeOrdersDTO)) {
                log.info("-----------------【线上直连】下单信息记录--------------【MD5签名不匹配】");
                throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
            }
        }
        //根据机构订单号查询订单
        Orders order = ordersMapper.selectOrderByInstitutionOrderId(placeOrdersDTO.getOrderNo());
        log.info("-----------------【线上直连】下单信息记录-----------------根据【机构订单号】查询数据库订单信息 order:{}", JSON.toJSONString(order));
        //判断订单状态,不为支付中直接返回
        if (order != null && !TradeConstant.ORDER_PAYING.equals(order.getTradeStatus())) {
            log.info("-----------------【线上直连】下单信息记录-----------------订单状态不是【交易中】");
            throw new BusinessException(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        //查询机构配置信息
        BasicsInfoVO basicsInfo = commonService.getBasicsInfoByIssuerId(placeOrdersDTO.getIssuerId(), placeOrdersDTO, TradeConstant.TRADE_ONLINE);
        log.info("-----------------【线上直连】下单信息记录-----------------【机构配置信息记录】 basicsInfo:{}", JSON.toJSONString(basicsInfo));
        //校验订单
        commonService.checkOnlineOrder(placeOrdersDTO, basicsInfo);
        //设置订单属性
        Orders orders = commonService.setAttributes(placeOrdersDTO, basicsInfo);
        //上报通道的流水号,不为空时使用上一次订单的reportNumber
        if (order != null && !StringUtils.isEmpty(order.getReportNumber())) {
            //上报通道的流水号
            orders.setReportNumber(order.getReportNumber());
        }
        //交易方向--线上
        orders.setTradeDirection(TradeConstant.TRADE_ONLINE);
        //订单类型-直连
        orders.setClearStatus(TradeConstant.DIRECTCONNECTION);
        //响应实体
        BaseResponse baseResponse = new BaseResponse();
        //换汇与手续费计算,下单业务校验
        if (calculationDCMethod(orders, baseResponse, basicsInfo)) {
            return baseResponse;
        }
        //交易状态--支付中
        orders.setTradeStatus(TradeConstant.ORDER_PAYING);
        //上报通道时间
        orders.setReportChannelTime(new Date());
        //订单落地
        ordersMapper.insert(orders);
        //判断通道及上传通道
        if (channelMethod(orders, basicsInfo.getChannel(), baseResponse)) {
            if (!StringUtils.isEmpty(baseResponse.getData())) {
                AD3OnlineVO ad3OnlineVO = (AD3OnlineVO) baseResponse.getData();
                if (!StringUtils.isEmpty(ad3OnlineVO.getRespCode())) {
                    AD3OnlineScanVO ad3OnlineScanVO = new AD3OnlineScanVO();
                    BeanUtils.copyProperties(ad3OnlineVO, ad3OnlineScanVO);
                    ad3OnlineScanVO.setTradeAmount(orders.getTradeAmount());
                    ad3OnlineScanVO.setTradeCurrency(orders.getTradeCurrency());
                    baseResponse.setData(ad3OnlineScanVO);
                }
            }
            return baseResponse;
        }
        //判断data是否有数据
        if (baseResponse.getData() == null) {
            log.info("----------【线上直连】下单信息记录----------【通道响应结果】为空");
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        return baseResponse;
    }
}
