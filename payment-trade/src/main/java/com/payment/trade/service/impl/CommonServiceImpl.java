package com.payment.trade.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.LogisticsBachDTO;
import com.payment.common.dto.OrderLogisticsBachDTO;
import com.payment.common.dto.PayOutDTO;
import com.payment.common.dto.PayOutRequestDTO;
import com.payment.common.entity.*;
import com.payment.common.enums.Status;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.response.HttpResponse;
import com.payment.common.utils.*;
import com.payment.common.vo.DirectConnectionVO;
import com.payment.common.vo.PayOutNoticeVO;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.*;
import com.payment.trade.dto.CashierDTO;
import com.payment.trade.dto.MegaPayIDRServerCallbackDTO;
import com.payment.trade.dto.MegaPayServerCallbackDTO;
import com.payment.trade.dto.PlaceOrdersDTO;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.CommonService;
import com.payment.trade.utils.SettleDateUtil;
import com.payment.trade.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @Author shenxinran
 * @Date 2019/3/18 15:26
 * @Descripate 交易通用业务接口实现类
 */
@Service
@Slf4j
@EnableAsync
public class CommonServiceImpl implements CommonService {

    @Autowired
    private InstitutionProductMapper institutionProductMapper;

    @Autowired
    private AttestationMapper attestationMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ExchangeRateMapper exchangeRateMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private AuditorProvider auditorProvider;

    @Value("${custom.developer.mobile}")
    private String developerMobile;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    @Qualifier(value = "ad3ParamsConfig")
    private AD3ParamsConfig ad3ParamsConfig;

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private InstitutionChannelMapper institutionChannelMapper;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Autowired
    private BankIssueridMapper bankIssueridMapper;

    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    @Autowired
    private ChannelBankMapper channelBankMapper;

    @Autowired
    private BankMapper bankMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private SettleControlMapper settleControlMapper;

    /**
     * 重复请求判断
     *
     * @param institutionCode    机构code
     * @param institutionOrderId 机构订单号
     * @return true
     */
    @Override

    public boolean repeatedRequests(String institutionCode, String institutionOrderId) {
        String redisKey = TradeConstant.REPEATED_REQUEST.concat(institutionCode.concat("_").concat(institutionOrderId));
        try {
            //重复请求判断
            if (redisService.get(redisKey) != null && redisService.get(redisKey).equals(institutionOrderId)) {
                return false;
            }
            redisService.set(redisKey, institutionOrderId, 2);
        } catch (Exception e) {
            log.error("重复请求判断发生异常:", e.getMessage());
        }
        return true;
    }


    /**
     * 判断签名是否正确，当传递的机构code或者签名信息错误时，会返回 签名错误 消息
     *
     * @param institutionCode 机构code
     * @param signMsg         签名信息
     * @return true
     */
    @Override
    public boolean checkSignMsg(String institutionCode, String signMsg) {
        try {
            Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode)), Attestation.class);
            if (attestation == null) {
                attestation = attestationMapper.selectByInstitutionCode(institutionCode);
                if (attestation == null) {
                    return false;
                }
            }
            RSA rsa = new RSA(null, attestation.getPubkey());
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decryptBytes = rsa.decrypt(decoder.decode(signMsg), KeyType.PublicKey);
            byte[] mdkBytes = attestation.getMd5key().getBytes(StandardCharsets.UTF_8);
            return Arrays.equals(mdkBytes, decryptBytes);
        } catch (Exception e) {
            log.info("----------- 签名解密发生错误----------机构code:{},签名signMsg:{}", institutionCode, signMsg);
            return false;
        }
    }

    /**
     * 线下校验订单
     *
     * @param placeOrdersDTO 订单实体
     * @param basicsInfo     机构关联产品通道实体
     */
    @Override
    public void checkOrder(PlaceOrdersDTO placeOrdersDTO, BasicsInfoVO basicsInfo) {
        //判断订单币种是否位于基础表中
        if (getCurrency(placeOrdersDTO.getOrderCurrency()) == null) {
            log.info("==================【下单校验订单信息】==================【订单币种不合法】");
            throw new BusinessException(EResultEnum.ORDER_CURRENCY_IS_NOT_AVAILABLE.getCode());
        }
        if (placeOrdersDTO.getOrderAmount().compareTo(BigDecimal.ZERO) != 1) {//小于等于0
            log.info("==================【下单校验订单信息】==================【订单金额不合法】");
            //订单金额不合法
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        if (ordersMapper.selectByInstitutionOrderId(placeOrdersDTO.getOrderNo()) != null) {
            log.info("==================【下单校验订单信息】==================【机构订单号已存在】");
            //机构订单号已存在
            throw new BusinessException(EResultEnum.INSTITUTION_ORDER_ID_EXIST.getCode());
        }
        if (!TradeConstant.AUDIT_SUCCESS.equals(basicsInfo.getInstitution().getAuditStatus())) {
            log.info("==================【下单校验订单信息】==================【机构审核状态异常】");
            //机构审核状态异常
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }
        if (!basicsInfo.getInstitution().getEnabled()) {
            log.info("==================【下单校验订单信息】==================【机构已禁用】");
            //机构状态异常
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }
        if (!basicsInfo.getProduct().getEnabled()) {
            log.info("==================【下单校验订单信息】==================【产品已禁用】");
            //产品状态异常
            throw new BusinessException(EResultEnum.PRODUCT_STATUS_ABNORMAL.getCode());
        }
        if (!basicsInfo.getInstitutionProduct().getEnabled()) {
            log.info("==================【下单校验订单信息】==================【机构产品已禁用】");
            //机构产品状态异常
            throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_STATUS_ABNORMAL.getCode());
        }
        if (!basicsInfo.getChannel().getEnabled()) {
            log.info("==================【下单校验订单信息】==================【通道已禁用】");
            //通道状态异常
            throw new BusinessException(EResultEnum.CHANNEL_STATUS_ABNORMAL.getCode());
        }
    }

    /**
     * 校验订单，无重复订单号校验
     *
     * @param placeOrdersDTO
     * @param basicsInfo
     */
    @Override
    public void checkOnlineOrder(PlaceOrdersDTO placeOrdersDTO, BasicsInfoVO basicsInfo) {
        //校验域名
        checkDomain(placeOrdersDTO.getServerUrl(), basicsInfo.getInstitution().getInstitutionWebUrl());
        //判断订单币种是否位于基础表中
        if (getCurrency(placeOrdersDTO.getOrderCurrency()) == null) {
            log.info("----------------- 线上下单校验订单信息--------------订单币种【不合法】");
            throw new BusinessException(EResultEnum.ORDER_CURRENCY_IS_NOT_AVAILABLE.getCode());
        }
        if (placeOrdersDTO.getOrderAmount().compareTo(BigDecimal.ZERO) != 1) {//小于等于0
            log.info("----------------- 线上下单校验订单信息--------------订单金额不合法");
            //订单金额不合法
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        //机构状态check-审核状态
        if (!TradeConstant.AUDIT_SUCCESS.equals(basicsInfo.getInstitution().getAuditStatus())) {
            log.info("----------------- 线上下单校验订单信息--------------机构审核状态异常");
            //机构审核状态异常
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }
        if (!basicsInfo.getInstitution().getEnabled()) {
            log.info("----------------- 线上下单校验订单信息--------------机构是禁用状态");
            //机构状态异常
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }
        //该机构不支持直连
        if (!basicsInfo.getInstitution().getConnectLimit()) {
            log.info("----------------- 线上下单校验订单信息--------------机构不支持直连");
            throw new BusinessException(EResultEnum.CONNECT_LIMIT_ERROR.getCode());
        }
        if (!basicsInfo.getProduct().getEnabled()) {
            log.info("----------------- 线上下单校验订单信息--------------产品是禁用状态");
            //产品信息异常
            throw new BusinessException(EResultEnum.PRODUCT_STATUS_ABNORMAL.getCode());
        }
        if (!basicsInfo.getInstitutionProduct().getEnabled()) {
            log.info("----------------- 线上下单校验订单信息--------------机构产品是禁用状态");
            //机构产品信息异常
            throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_STATUS_ABNORMAL.getCode());
        }
        if (!basicsInfo.getChannel().getEnabled()) {
            log.info("----------------- 线上下单校验订单信息--------------通道是禁用状态");
            //通道状态异常
            throw new BusinessException(EResultEnum.CHANNEL_STATUS_ABNORMAL.getCode());
        }
    }

    /**
     * 校验线上订单
     */
    @Override
    public void checkICOnlineOrder(PlaceOrdersDTO placeOrdersDTO, InstitutionVO institutionVO) {
        if (institutionVO == null) {
            log.info("==================【下单校验订单信息】==================获取机构关联信息异常");
            //获取机构关联信息异常
            throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_CHANNEL_NOT_EXISTS.getCode());
        }
        //校验域名
        checkDomain(placeOrdersDTO.getServerUrl(), institutionVO.getInstitutionWebUrl());
        //判断订单币种是否位于基础表中
        if (getCurrency(placeOrdersDTO.getOrderCurrency()) == null) {
            log.info("==================【下单校验订单信息】==================订单币种【不合法】");
            throw new BusinessException(EResultEnum.ORDER_CURRENCY_IS_NOT_AVAILABLE.getCode());
        }
        String institutionCode = placeOrdersDTO.getInstitutionId();//机构编号
        String institutionOrderId = placeOrdersDTO.getOrderNo();//机构订单号
        if (placeOrdersDTO.getOrderAmount().compareTo(BigDecimal.ZERO) != 1) {//小于等于0
            log.info("==================【下单校验订单信息】==================订单金额不合法**订单金额:{},机构号:{},机构订单号:{}", placeOrdersDTO.getOrderAmount(), institutionCode, institutionOrderId);
            //订单金额不合法
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        if (!institutionVO.getEnabled()) {
            log.info("==================【下单校验订单信息】==================机构状态异常 机构编号:{},机构订单号:{}", institutionCode, institutionOrderId);
            //机构状态异常
            throw new BusinessException(EResultEnum.INSTITUTION_STATUS_ABNORMAL.getCode());
        }
        if (institutionVO.getProductList() == null || institutionVO.getProductList().size() == 0) {
            log.info("==================【下单校验订单信息】==================获取产品信息异常 机构订单号:{}", institutionOrderId);
            //获取产品信息异常
            throw new BusinessException(EResultEnum.GET_PRODUCT_INFO_ERROR.getCode());
        }
        if (institutionVO.getProductList().get(0).getChannelList() == null || institutionVO.getProductList().get(0).getChannelList().size() == 0) {
            log.info("==================【下单校验订单信息】==================获取通道信息失败 机构订单号:{}", institutionOrderId);
            //获取通道信息异常
            throw new BusinessException(EResultEnum.GET_CHANNEL_INFO_ERROR.getCode());
        }
    }

    /**
     * * 下单业务信息校验
     * * 线上和线下都用
     * * 校验交易金额
     *
     * @param orders       订单
     * @param basicsInfoVO 基础配置信息
     * @param baseResponse 响应实体
     * @return
     */
    @Override
    public BaseResponse checkPlaceOrder(Orders orders, BasicsInfoVO basicsInfoVO, BaseResponse baseResponse) {
        Product product = basicsInfoVO.getProduct();
        InstitutionProduct institutionProduct = basicsInfoVO.getInstitutionProduct();
        Channel channel = basicsInfoVO.getChannel();
        //校验机构产品限额
        if (institutionProduct.getAuditLimitStatus() != null && institutionProduct.getAuditLimitStatus().equals(TradeConstant.AUDIT_SUCCESS)) {
            if (orders.getTradeAmount().compareTo(institutionProduct.getLimitAmount()) == 1) {
                //交易金额大于单笔限额
                log.info("==================【校验订单信息】==================【单笔交易金额不合法】");
                orders.setRemark("单笔金额不合法");
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                ordersMapper.insert(orders);
                baseResponse.setCode(EResultEnum.LIMIT_AMOUNT_ERROR.getCode());
                return baseResponse;
            }
            //获取今天日期
            String todayDate = DateToolUtils.getReqDateE();
            //日交易限额key
            String DAILY_TOTAL_AMOUNT_KEY = orders.getInstitutionCode().concat("_").concat(product.getProductCode().toString()).concat("_").concat(todayDate).concat("_").concat(TradeConstant.DAILY_TOTAL_AMOUNT);
            //日交易笔数key
            String DAILY_TRADING_COUNT_KEY = orders.getInstitutionCode().concat("_").concat(product.getProductCode().toString()).concat("_").concat(todayDate).concat("_").concat(TradeConstant.DAILY_TRADING_COUNT);
            String dailyAmount = redisService.get(DAILY_TOTAL_AMOUNT_KEY);
            String dailyCount = redisService.get(DAILY_TRADING_COUNT_KEY);
            if (StringUtils.isEmpty(dailyAmount) || StringUtils.isEmpty(dailyCount)) {
                //用户第一次下单的时候
                redisService.set(DAILY_TOTAL_AMOUNT_KEY, "0", 24 * 60 * 60);
                redisService.set(DAILY_TRADING_COUNT_KEY, "0", 24 * 60 * 60);
            } else {
                //日交易笔数
                Integer dailyTradingCount = Integer.parseInt(dailyCount);
                if (dailyTradingCount >= institutionProduct.getDailyTradingCount()) {
                    log.info("-==================【校验订单信息】==================【日交易笔数不合法】 dailyTradingCount: {}", dailyTradingCount);
                    orders.setRemark("日交易笔数不合法");
                    orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                    ordersMapper.insert(orders);
                    baseResponse.setCode(EResultEnum.TRADE_COUNT_ERROR.getCode());
                    return baseResponse;
                }
                //日交易限额
                BigDecimal dailyTotalAmount = new BigDecimal(dailyAmount);
                if (dailyTotalAmount.compareTo(institutionProduct.getDailyTotalAmount()) >= 0) {
                    log.info("==================【校验订单信息】==================【日交易金额不合法】 dailyTotalAmount: {}", dailyTotalAmount);
                    orders.setRemark("日交易金额不合法");
                    orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                    ordersMapper.insert(orders);
                    baseResponse.setCode(EResultEnum.TRADE_AMOUNT_ERROR.getCode());
                    return baseResponse;
                }
            }
        }
        //校验通道限额
        if ((!StringUtils.isEmpty(channel.getLimitMinAmount()) && !StringUtils.isEmpty(channel.getLimitMaxAmount())) &&
                (channel.getLimitMinAmount().compareTo(BigDecimal.ZERO) != 0 && channel.getLimitMaxAmount().compareTo(BigDecimal.ZERO) != 0)) {
            if (orders.getTradeAmount().compareTo(channel.getLimitMinAmount()) == -1 || orders.getTradeAmount().compareTo(channel.getLimitMaxAmount()) == 1) {
                log.info("==================【校验订单信息】==================【通道限额不合法】 tradeAmount: {},LimitMinAmount: {},LimitMaxAmount: {}",
                        orders.getTradeAmount(), channel.getLimitMinAmount(), channel.getLimitMaxAmount());
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                orders.setRemark("交易金额不在通道限额范围内");
                ordersMapper.insert(orders);
                baseResponse.setCode(EResultEnum.LIMIT_AMOUNT_ERROR.getCode());
                return baseResponse;
            }
        }
        //截取币种默认值
        this.interceptDigit(orders, baseResponse);
        return baseResponse;
    }

    /**
     * 校验通道限值
     *
     * @param tradeAmount
     * @param channel
     * @return
     */
    @Override
    public boolean verifyChannelLimits(BigDecimal tradeAmount, Channel channel) {
        if (StringUtils.isEmpty(channel.getLimitMinAmount()) || StringUtils.isEmpty(channel.getLimitMaxAmount())) {
            return true;
        }
        if (channel.getLimitMinAmount().compareTo(BigDecimal.ZERO) == 0 && channel.getLimitMaxAmount().compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        if (tradeAmount.compareTo(channel.getLimitMinAmount()) == -1 || tradeAmount.compareTo(channel.getLimitMaxAmount()) == 1) {
            return false;
        }
        return true;
    }

    /**
     * 收银台下单信息校验
     *
     * @param cd           订单
     * @param basicsInfoVO 产品信息
     */
    @Override
    public void checkCashierOrder(CashierDTO cd, BasicsInfoVO basicsInfoVO, Orders orders) {
        Product product = basicsInfoVO.getProduct();
        InstitutionProduct institutionProduct = basicsInfoVO.getInstitutionProduct();
        //审核限额状态通过
        if (institutionProduct.getAuditLimitStatus() != null && institutionProduct.getAuditLimitStatus().equals(TradeConstant.AUDIT_SUCCESS)) {
            //获取收银台的交易金额
            BigDecimal amount = new BigDecimal(cd.getTradeAmount());
            if (amount.compareTo(institutionProduct.getLimitAmount()) == 1) {
                //单笔交易限额
                throw new BusinessException(EResultEnum.LIMIT_AMOUNT_ERROR.getCode());
            }
            //校检日累计交易笔数，日累计交易限额
            String todayDate = DateToolUtils.getReqDateE();//获取今天日期
            String DAILY_TOTAL_AMOUNT_KEY = cd.getInstitutionCode().concat("_").concat(product.getProductCode().toString()).concat("_").concat(todayDate).concat("_").concat(TradeConstant.DAILY_TOTAL_AMOUNT);//日交易限额key
            String DAILY_TRADING_COUNT_KEY = cd.getInstitutionCode().concat("_").concat(product.getProductCode().toString()).concat("_").concat(todayDate).concat("_").concat(TradeConstant.DAILY_TRADING_COUNT);//日交易笔数key
            String dailyAmount = redisService.get(DAILY_TOTAL_AMOUNT_KEY);//redis存储的交易额
            String dailyCount = redisService.get(DAILY_TRADING_COUNT_KEY);//redis存储的交易笔数
            if (StringUtils.isEmpty(dailyAmount) || StringUtils.isEmpty(dailyCount)) {
                redisService.set(DAILY_TOTAL_AMOUNT_KEY, "0", 24 * 60 * 60);
                redisService.set(DAILY_TRADING_COUNT_KEY, "0", 24 * 60 * 60);
                return;
            }
            Integer dailyTradingCount = Integer.parseInt(dailyCount);//日交易笔数
            BigDecimal dailyTotalAmount = new BigDecimal(dailyAmount);//日交易限额
            if (dailyTradingCount >= institutionProduct.getDailyTradingCount()) {
                log.info("==================【下单校验订单信息】==================日交易笔数不合法 productVO:{}", JSON.toJSONString(product));
                //日交易笔数不合法
                throw new BusinessException(EResultEnum.TRADE_COUNT_ERROR.getCode());
            }
            if (dailyTotalAmount.compareTo(institutionProduct.getDailyTotalAmount()) >= 0) {
                log.info("==================【下单校验订单信息】==================日交易限额不合法 productVO:{}", JSON.toJSONString(product));
                //日交易限额不合法
                throw new BusinessException(EResultEnum.TRADE_AMOUNT_ERROR.getCode());
            }
        }
    }

    /**
     * 校验域名
     *
     * @param serverUrl         订单上送服务回调url
     * @param institutionWebUrl 机构基础信息配置url
     */
    @Override
    public void checkDomain(String serverUrl, String institutionWebUrl) {
        //校验一级域名
        if (!StringUtils.isEmpty(serverUrl) && !StringUtils.isEmpty(institutionWebUrl)) {
            log.info("=====================【校验一级域名】=====================【参数记录】 serverUrl: {}; institutionWebUrl: {}", serverUrl, institutionWebUrl);
            int startIndex = serverUrl.indexOf(".");
            if (startIndex == -1) {
                log.info("=====================【校验一级域名】=====================【服务器回调地址格式错误】 serverUrl: {}", serverUrl);
                throw new BusinessException(EResultEnum.SERVER_CALLBACK_FORMAT_ERROR.getCode());
            }
            int endIndex = serverUrl.indexOf("/", startIndex);
            String ordersDomain = "";
            if (endIndex == -1) {
                //不带/时,截取到字符串最后
                ordersDomain = serverUrl.substring(startIndex + 1);
            } else {
                //带/时,截取到/之前
                ordersDomain = serverUrl.substring(startIndex + 1, endIndex);
            }
            log.info("=====================【校验一级域名】=====================【订单上送的回调地址】的一级域名 ordersDomain: {}", ordersDomain);
            String[] institutionWebUrlArrays = institutionWebUrl.split(",");
            log.info("=====================【校验一级域名】=====================【运营后台报备的机构URL】 institutionWebUrlArrays: {}", Arrays.toString(institutionWebUrlArrays));
            boolean flag = false;
            for (String institutionUrl : institutionWebUrlArrays) {
                int beginIndex = institutionUrl.indexOf(".");
                if (beginIndex == -1) {
                    log.info("=====================【校验一级域名】=====================【运营后台报备的机构URL格式错误】 institutionUrl: {}", institutionUrl);
                    throw new BusinessException(EResultEnum.INSTITUTION_CONFIGURATION_URL_FORMAT_ERROR.getCode());
                }
                int finallyIndex = institutionUrl.indexOf("/", beginIndex);
                String institutionDomain = "";
                if (finallyIndex == -1) {
                    //不带/时,截取到字符串最后
                    institutionDomain = institutionUrl.substring(beginIndex + 1);
                } else {
                    //带/时,截取到/之前
                    institutionDomain = institutionUrl.substring(beginIndex + 1, finallyIndex);
                }
                log.info("=====================【校验一级域名】=====================【运营后台报备的机构URL】的一级域名 institutionDomain: {}", institutionDomain);
                if (!StringUtils.isEmpty(ordersDomain) && !StringUtils.isEmpty(institutionDomain) && institutionDomain.equalsIgnoreCase(ordersDomain)) {
                    flag = true;
                    log.info("=====================【校验一级域名】=====================【合法的一级域名】");
                    break;
                }
            }
            if (!flag) {
                log.info("=====================【校验一级域名】=====================【一级域名不匹配】 serverUrl: {}", serverUrl);
                //一级域名不匹配
                throw new BusinessException(EResultEnum.DOMAIN_NOT_MATCH.getCode());
            }
        }
    }

    /**
     * 汇率计算
     *
     * @param orderCurrency 订单币种
     * @param tradeCurrency 交易币种
     * @param floatRate     浮动率
     * @param amount        订单金额
     * @return 汇率计算输出实体
     */
    @Override
    public CalcRateVO calcExchangeRate(String orderCurrency, String tradeCurrency, BigDecimal floatRate, BigDecimal amount) {
        //汇率计算输出实体
        CalcRateVO calcRateVO = new CalcRateVO();
        try {
            //换汇时间
            calcRateVO.setExchangeTime(new Date());
            //先从redis获取
            ExchangeRate exchangeRate = JSON.parseObject(redisService.get(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(orderCurrency) + "_" + tradeCurrency), ExchangeRate.class);
            if (exchangeRate == null || !exchangeRate.getEnabled()) {
                //数据库查询汇率
                exchangeRate = exchangeRateMapper.selectRateByOrderCurrencyAndTradeCurrency(orderCurrency, tradeCurrency);
            }
            if (exchangeRate == null || exchangeRate.getBuyRate() == null) {
                log.info("==================【换汇计算】==================【汇率查询异常】 本位币种: {} 目标币种: {}", orderCurrency, tradeCurrency);
                messageFeign.sendSimple(developerMobile, "换汇计算:查询汇率异常!本位币种:" + orderCurrency + " 目标币种:" + tradeCurrency);
                messageFeign.sendSimpleMail(developerEmail, "换汇计算:查询汇率异常!", "换汇计算:查询汇率异常!本位币种:" + orderCurrency + " 目标币种:" + tradeCurrency);
                calcRateVO.setExchangeStatus(TradeConstant.SWAP_FALID);
                return calcRateVO;
            }
            redisService.set(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(orderCurrency).concat("_").concat(tradeCurrency), JSON.toJSONString(exchangeRate));
            //浮动率为空,默认为0
            if (floatRate == null) {
                floatRate = new BigDecimal(0);
            }
            //换汇汇率 = 汇率 * (1 + 浮动率)
            BigDecimal swapRate = exchangeRate.getBuyRate().multiply(floatRate.add(new BigDecimal(1)));
            //交易金额 = 订单金额 * 换汇汇率
            BigDecimal tradeAmount = amount.multiply(swapRate);
            //换汇信息记录
            log.info("==================【换汇计算】==================买入汇率: {}, 浮动率: {}, 交易金额: {}", exchangeRate.getBuyRate(), floatRate, tradeAmount);
            //四舍五入保留2位
            calcRateVO.setTradeAmount(tradeAmount.setScale(2, BigDecimal.ROUND_HALF_UP));
            //换汇汇率
            calcRateVO.setExchangeRate(swapRate);
            //换汇成功
            calcRateVO.setExchangeStatus(TradeConstant.SWAP_SUCCESS);
            //原始汇率
            calcRateVO.setOriginalRate(exchangeRate.getBuyRate());
        } catch (Exception e) {
            log.info("==================【换汇计算】==================", e);
            calcRateVO.setExchangeStatus(TradeConstant.SWAP_FALID);
            return calcRateVO;
        }
        return calcRateVO;
    }

    /**
     * 计算手续费
     *
     * @param orders       订单金额
     * @param basicsInfoVO 机构产品
     * @return CalcFeeVO  手续费输出实体
     */
    @Override
    public CalcFeeVO calcPoundage(Orders orders, BasicsInfoVO basicsInfoVO) {
        BigDecimal poundage = new BigDecimal(0);
        CalcFeeVO calcFeeVO = new CalcFeeVO();//返回结果
        //产品币种
        String productCurrency = basicsInfoVO.getProduct().getCurrency();
        //机构产品
        InstitutionProduct institutionProduct = basicsInfoVO.getInstitutionProduct();
        //查询出商户对应产品的费率信息
        if (institutionProduct.getRate() == null || institutionProduct.getRateType() == null || institutionProduct.getAddValue() == null) {
            log.info("----------------- 计费信息记录 ----------------费率:{},费率类型:{},机构产品信息:{}", institutionProduct.getRate(), institutionProduct.getRateType(), JSON.toJSONString(institutionProduct));
            calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
            return calcFeeVO;
        }
        //币种不一致时
        if (!orders.getOrderCurrency().equals(productCurrency)) {
            //换汇后的机构产品
            institutionProduct = CalcFeeExchange(productCurrency, institutionProduct, null, orders.getOrderCurrency());
            if (StringUtils.isEmpty(institutionProduct)) {
                //换汇失败
                log.info("----------------- %换汇失败% 产品币种-->订单币种汇率不存在 计费信息记录 ----------------订单:{},机构产品信息:{}", JSON.toJSONString(orders), JSON.toJSONString(institutionProduct));
                calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_OTHER);
                return calcFeeVO;
            }
        }
        //单笔费率
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_RATE)) {
            //手续费=订单金额*单笔费率+附加值
            poundage = orders.getAmount().multiply(institutionProduct.getRate()).add(institutionProduct.getAddValue());
            //判断手续费是否小于最小值，大于最大值
            if (institutionProduct.getMinTate() != null && poundage.compareTo(institutionProduct.getMinTate()) == -1) {
                poundage = institutionProduct.getMinTate();
            }
            if (institutionProduct.getMaxTate() != null && poundage.compareTo(institutionProduct.getMaxTate()) == 1) {
                poundage = institutionProduct.getMaxTate();
            }
        }
        //单笔定额
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_QUOTA)) {
            //手续费=单笔定额值+附加值
            poundage = institutionProduct.getRate().add(institutionProduct.getAddValue());
        }
        //返回计算手续费
        calcFeeVO.setFee(poundage);
        //计费状态
        calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_SUCCESS);
        //计费时间
        calcFeeVO.setChargeTime(new Date());
        return calcFeeVO;
    }

    /**
     * 计算手续费时的换汇计算
     *
     * @param productCurrency
     * @param institutionProduct
     * @param existRate          原先的汇率
     * @param orderCurrency
     * @return
     */
    @Override
    public InstitutionProduct CalcFeeExchange(String productCurrency, InstitutionProduct institutionProduct, BigDecimal existRate, String orderCurrency) {
        //汇率
        //先从redis获取
        ExchangeRate exchangeRate = new ExchangeRate();
        if (existRate == null) {
            exchangeRate = JSON.parseObject(redisService.get(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(productCurrency).concat("_").concat(orderCurrency)), ExchangeRate.class);
        } else {
            exchangeRate.setBuyRate(existRate);
            exchangeRate.setEnabled(true);
        }
        if (exchangeRate == null || !exchangeRate.getEnabled()) {
            //查询订单买入汇率
            exchangeRate = exchangeRateMapper.selectRateByOrderCurrencyAndTradeCurrency(productCurrency, orderCurrency);
        }
        if (exchangeRate == null || exchangeRate.getBuyRate() == null) {
            log.info("----------------- 计算手续费 换汇计算信息记录 ----------------未查询到币种对应汇率,本位币种:{},目标币种:{}", productCurrency, orderCurrency);
            messageFeign.sendSimple(developerMobile, "计算手续费换汇计算:查询汇率异常!本位币种：" + productCurrency + " 目标币种：" + orderCurrency);
            messageFeign.sendSimpleMail(developerEmail, "计算手续费换汇计算:查询汇率异常!", "计算手续费换汇计算:查询汇率异常!本位币种：" + productCurrency + " 目标币种：" + orderCurrency);
            return null;
        }
        redisService.set(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(productCurrency).concat("_").concat(orderCurrency), JSON.toJSONString(exchangeRate));
        //附加值
        institutionProduct.setAddValue(institutionProduct.getAddValue().multiply(exchangeRate.getBuyRate()).setScale(2, BigDecimal.ROUND_HALF_UP));

        //单笔费率 最大值
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_RATE) && !StringUtils.isEmpty(institutionProduct.getMaxTate())) {
            institutionProduct.setMaxTate(institutionProduct.getMaxTate().multiply(exchangeRate.getBuyRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        //单笔费率 最小值
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_RATE) && !StringUtils.isEmpty(institutionProduct.getMinTate())) {
            institutionProduct.setMinTate(institutionProduct.getMinTate().multiply(exchangeRate.getBuyRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        //单笔定额时的换汇
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_QUOTA)) {
            institutionProduct.setRate(institutionProduct.getRate().multiply(exchangeRate.getBuyRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        return institutionProduct;
    }

    /**
     * 计算通道手续费
     *
     * @param amount 订单金额
     * @return CalcFeeVO  通道费用输出实体
     */
    @Override
    public CalcFeeVO calcChannelPoundage(BigDecimal amount, Channel channel) {
        BigDecimal poundage = BigDecimal.ZERO;
        CalcFeeVO calcFeeVO = new CalcFeeVO();
        //单笔费率
        if (channel.getChannelFeeType().equals(TradeConstant.FEE_TYPE_RATE)) {
            if (channel.getChannelRate() == null || channel.getChannelMinRate() == null
                    || channel.getChannelMaxRate() == null) {
                calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
                return calcFeeVO;
            }
            //手续费=订单金额*费率
            poundage = amount.multiply(channel.getChannelRate());
            //判断手续费是否小于最小值，大于最大值
            if (channel.getChannelMinRate() != null && poundage.compareTo(channel.getChannelMinRate()) == -1) {
                poundage = channel.getChannelMinRate();
            }
            if (channel.getChannelMaxRate() != null && poundage.compareTo(channel.getChannelMaxRate()) == 1) {
                poundage = channel.getChannelMaxRate();
            }
        } else if (channel.getChannelFeeType().equals(TradeConstant.FEE_TYPE_QUOTA)) {
            //单笔定额
            if (channel.getChannelRate() == null) {
                calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
                return calcFeeVO;
            }
            //手续费=通道手续费
            poundage = channel.getChannelRate();
        } else {
            calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
            return calcFeeVO;
        }
        //通道手续费
        calcFeeVO.setFee(poundage);
        //计费状态
        calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_SUCCESS);
        //计费时间
        calcFeeVO.setChargeTime(new Date());
        return calcFeeVO;
    }

    /**
     * 计算通道网关手续费
     *
     * @param amount 订单金额
     * @return CalcFeeVO  通道费用输出实体
     */
    @Override
    public CalcFeeVO calcChannelGatewayPoundage(BigDecimal amount, Channel channel) {
        BigDecimal poundage = new BigDecimal(0);
        CalcFeeVO calcFeeVO = new CalcFeeVO();
        //单笔费率
        if (channel.getChannelGatewayFeeType().equals(TradeConstant.FEE_TYPE_RATE)) {
            if (channel.getChannelGatewayRate() == null || channel.getChannelGatewayMinRate() == null ||
                    channel.getChannelGatewayMaxRate() == null) {
                calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
                return calcFeeVO;
            }
            //手续费=订单金额*费率
            poundage = amount.multiply(channel.getChannelGatewayRate());
            //判断手续费是否小于最小值，大于最大值
            if (channel.getChannelGatewayMinRate() != null && poundage.compareTo(channel.getChannelGatewayMinRate()) == -1) {
                poundage = channel.getChannelGatewayMinRate();
            }
            if (channel.getChannelGatewayMaxRate() != null && poundage.compareTo(channel.getChannelGatewayMaxRate()) == 1) {
                poundage = channel.getChannelGatewayMaxRate();
            }
        }
        //单笔定额
        if (channel.getChannelGatewayFeeType().equals(TradeConstant.FEE_TYPE_QUOTA)) {
            if (channel.getChannelGatewayRate() == null) {
                calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
                return calcFeeVO;
            }
            //手续费=通道网关手续费
            poundage = channel.getChannelGatewayRate();
        }
        //通道网关手续费
        calcFeeVO.setFee(poundage);
        calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_SUCCESS);
        calcFeeVO.setChargeTime(new Date());
        return calcFeeVO;
    }

    /**
     * 回调时计算通道网关手续费(交易成功时收取)
     *
     * @param orders orders
     */
    @Override
    public void calcCallBackGatewayFeeSuccess(Orders orders) {
        try {
            //获取通道信息
            Channel channel = this.getChannelByChannelCode(orders.getChannelCode());
            if (channel.getChannelGatewayRate() != null && TradeConstant.CHANNEL_GATEWAY_CHARGE_YES.equals(channel.getChannelGatewayCharge())
                    && TradeConstant.CHANNEL_GATEWAY_CHARGE_SUCCESS_STATUS.equals(channel.getChannelGatewayStatus())) {
                CalcFeeVO channelGatewayPoundage = calcChannelGatewayPoundage(orders.getAmount(), channel);
                //通道网关手续费
                orders.setChannelGatewayFee(channelGatewayPoundage.getFee());
                int i = ordersMapper.updateByPrimaryKeySelective(orders);
                if (i == 1) {
                    log.info("===============计算支付成功时的通道网关手续费【成功】===============");
                }
            }
        } catch (Exception e) {
            log.error("===============计算支付成功时的通道网关手续费发生异常===============", e);
        }
    }

    /**
     * 回调时计算通道网关手续费(交易失败时收取)
     *
     * @param orders orders
     */
    @Override
    public void calcCallBackGatewayFeeFailed(Orders orders) {
        try {
            //获取通道信息
            Channel channel = this.getChannelByChannelCode(orders.getChannelCode());
            if (channel.getChannelGatewayRate() != null && TradeConstant.CHANNEL_GATEWAY_CHARGE_YES.equals(channel.getChannelGatewayCharge())
                    && TradeConstant.CHANNEL_GATEWAY_CHARGE_FAILURE_STATUS.equals(channel.getChannelGatewayStatus())) {
                CalcFeeVO channelGatewayPoundage = calcChannelGatewayPoundage(orders.getAmount(), channel);
                //通道网关手续费
                orders.setChannelGatewayFee(channelGatewayPoundage.getFee());
                int i = ordersMapper.updateByPrimaryKeySelective(orders);
                if (i == 1) {
                    log.info("===============计算支付失败时的通道网关手续费【成功】===============");
                }
            }
        } catch (Exception e) {
            log.error("===============计算支付失败时的通道网关手续费发生异常===============", e);
        }
    }

    /**
     * 计算通道网关手续费 线上下单用
     *
     * @param channel
     * @param orders
     * @param baseResponse
     * @return
     */
    @Override
    @Transactional
    public boolean calcGatewayFee(Channel channel, Orders orders, BaseResponse baseResponse) {
        CalcFeeVO channelGatewayPoundage = calcChannelGatewayPoundage(orders.getAmount(), channel);
        //通道网关收取状态 1-成功时收取 2-失败时收取 3-全收
        orders.setChannelGatewayStatus(channel.getChannelGatewayStatus());
        //全收取状态
        if (channel.getChannelGatewayStatus().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_ALL_STATUS)) {
            if (channelGatewayPoundage.getChargeStatus().equals(TradeConstant.CHARGE_STATUS_FALID)) {
                orders.setRemark("通道网关手续费计费失败");//备注
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                ordersMapper.insert(orders);
                baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
                return true;
            }
        }
        //通道网关-全收取场合的手续费
        orders.setChannelGatewayFee(channelGatewayPoundage.getFee());
        //通道网关是否收取 1-收 2-不收
        orders.setChannelGatewayCharge(TradeConstant.CHANNEL_GATEWAY_CHARGE_YES);
        return false;
    }

    /**
     * 设置订单属性
     * 线上和线下共用
     *
     * @param placeOrdersDTO 订单输入实体
     * @param basicsInfoVO   机构关联产品通道
     * @return 订单实体
     */
    @Override
    public Orders setAttributes(PlaceOrdersDTO placeOrdersDTO, BasicsInfoVO basicsInfoVO) {
        Institution institution = basicsInfoVO.getInstitution();//机构
        Product product = basicsInfoVO.getProduct();//产品
        InstitutionProduct institutionProduct = basicsInfoVO.getInstitutionProduct();//机构产品
        Channel channel = basicsInfoVO.getChannel();//通道
        Orders orders = new Orders();
        //订单信息
        orders.setId("O" + IDS.uniqueID().toString());//id
        orders.setReportNumber("Q" + IDS.uniqueID().toString());//上报的流水号
        orders.setTradeType(TradeConstant.GATHER_TYPE);//交易类型
        orders.setInstitutionOrderId(placeOrdersDTO.getOrderNo());//机构订单号
        orders.setAmount(placeOrdersDTO.getOrderAmount());//订单金额
        orders.setOrderCurrency(placeOrdersDTO.getOrderCurrency());//订单币种
        orders.setInstitutionOrderTime(DateUtil.parse(placeOrdersDTO.getOrderTime(), "yyyy-MM-dd HH:mm:ss"));//机构订单时间
        orders.setDeviceCode(placeOrdersDTO.getTerminalId());//设备编号
        orders.setDeviceOperator(placeOrdersDTO.getOperatorId());//设备操作员id
        orders.setGoodsDescription(placeOrdersDTO.getProductDescription());//商品描述
        orders.setProductName(StringUtils.isEmpty(placeOrdersDTO.getProductName()) ? "商品" : placeOrdersDTO.getProductName());//商品名称
        orders.setDraweeAccount(placeOrdersDTO.getPayerAccount());//付款人账户
        orders.setDraweeBank(placeOrdersDTO.getPayerBank());//付款人银行
        orders.setDraweeEmail(StringUtils.isEmpty(placeOrdersDTO.getPayerEmail()) ? ad3ParamsConfig.getDraweeEmail() : placeOrdersDTO.getPayerEmail());//付款人邮箱
        orders.setDraweeName(StringUtils.isEmpty(placeOrdersDTO.getPayerName()) ? ad3ParamsConfig.getDraweeName() : placeOrdersDTO.getPayerName());//付款人姓名
        orders.setDraweePhone(placeOrdersDTO.getPayerPhone());//付款人手机
        orders.setLanguage(StringUtils.isEmpty(placeOrdersDTO.getLanguage()) ? auditorProvider.getLanguage() : placeOrdersDTO.getLanguage());//语言
        this.getUrl(placeOrdersDTO, orders);//IP地址
        orders.setJumpUrl(placeOrdersDTO.getBrowserUrl());//浏览器回调地址
        orders.setReturnUrl(placeOrdersDTO.getServerUrl());//服务器回调地址
        orders.setCreateTime(new Date());//创建时间
        orders.setUpdateTime(new Date());//修改时间
        orders.setCreator(institution.getCnName());//创建人
        //机构信息
        orders.setInstitutionCode(placeOrdersDTO.getInstitutionId());//机构code
        orders.setSecondInstitutionName(placeOrdersDTO.getSubInstitutionName());//子机构名称
        orders.setSecondInstitutionCode(placeOrdersDTO.getSubInstitutionCode());//子机构code
        orders.setInstitutionName(institution.getCnName());//机构名称
        orders.setAgencyCode(institution.getAgencyCode());//代理机构编号
        //产品信息
        orders.setProductCode(product.getProductCode());//产品编号
        orders.setPayMethod(product.getPayType());//支付方式
        //机构产品信息
        orders.setRateType(institutionProduct.getRateType());//费率类型
        orders.setRate(institutionProduct.getRate());//费率
        orders.setFloatRate(institutionProduct.getFloatRate());//浮动率
        orders.setAddValue(institutionProduct.getAddValue());//附加值
        orders.setFeePayer(institutionProduct.getFeePayer());//手续费付款方
        //判断结算周期类型
        if (TradeConstant.DELIVERED.equals(institutionProduct.getSettleCycle())) {
            //妥投结算
            orders.setProductSettleCycle(TradeConstant.FUTURE_TIME);
        } else {
            orders.setProductSettleCycle(SettleDateUtil.getSettleDate(institutionProduct.getSettleCycle()));//产品结算周期
        }
        //通道信息
        orders.setIssuerId(channel.getIssuerId());//设置银行机构代码
        orders.setChannelName(channel.getChannelCnName());//通道名称
        orders.setChannelCode(channel.getChannelCode());//通道编号
        orders.setChannelRate(channel.getChannelRate());//通道费率
        orders.setChannelFeeType(channel.getChannelFeeType());//通道费率类型
        orders.setChannelGatewayFeeType(channel.getChannelGatewayFeeType());//通道网关手续费类型
        orders.setChannelGatewayCharge(channel.getChannelGatewayCharge());//是否收取通道网关手续费
        orders.setChannelGatewayStatus(channel.getChannelGatewayStatus());//通道网关手续费收取状态
        //不存签名到数据库
        orders.setSign(null);
        //银行名称
        orders.setBankName(basicsInfoVO.getBankName());
        return orders;
    }

    /**
     * 截取Url
     *
     * @param placeOrdersDTO
     * @param orders
     */
    @Override
    public void getUrl(PlaceOrdersDTO placeOrdersDTO, Orders orders) {
        if (!StringUtils.isEmpty(placeOrdersDTO.getServerUrl())) {
            String[] split = placeOrdersDTO.getServerUrl().split("/");
            StringBuffer sb = new StringBuffer();
            if (placeOrdersDTO.getServerUrl().contains("http")) {
                for (int i = 0; i < split.length; i++) {
                    if (i == 2) {
                        sb.append(split[i]);
                        break;
                    } else {
                        sb.append(split[i]).append("/");
                    }
                }
            } else {
                sb.append(split[0]);
            }
            orders.setReqIp(String.valueOf(sb));//请求ip
        } else {
            orders.setReqIp(auditorProvider.getReqIp());//请求ip
        }
    }


    /**
     * 收银台收单设置订单属性
     *
     * @return 订单实体
     */
    @Override
    public Orders setCashierAttributes(CashierDTO cd, BasicsInfoVO basicsInfoVO, Orders orders) {
        Product product = basicsInfoVO.getProduct();
        Channel channel = basicsInfoVO.getChannel();
        Institution institution = basicsInfoVO.getInstitution();
        InstitutionProduct institutionProduct = basicsInfoVO.getInstitutionProduct();
        orders.setInstitutionCode(cd.getInstitutionCode());
        orders.setAmount(new BigDecimal(cd.getOrderAmount()));
        orders.setOrderCurrency(cd.getOrderCurrency());
        orders.setTradeAmount(new BigDecimal(cd.getTradeAmount()));
        orders.setTradeCurrency(cd.getTradeCurrency());
        orders.setExchangeRate(new BigDecimal(cd.getExchangeRate()));
        orders.setExchangeTime(DateUtil.parse(cd.getExchangeTime(), "yyyy-MM-dd HH:mm:ss"));
        orders.setInstitutionOrderId(cd.getInstitutionOrderId());
        orders.setPayMethod(cd.getPayType());
        orders.setCommodityName(cd.getOriginalRate());//原始汇率
        orders.setChannelCode(channel.getChannelCode());
        orders.setChannelName(channel.getChannelCnName());
        orders.setChannelRate(channel.getChannelRate());
        orders.setChannelFeeType(channel.getChannelFeeType());
        orders.setChannelGatewayRate(channel.getChannelGatewayRate());
        orders.setChannelGatewayFeeType(channel.getChannelGatewayFeeType());
        orders.setChannelGatewayCharge(channel.getChannelGatewayCharge());
        orders.setChannelGatewayStatus(channel.getChannelGatewayStatus());
        orders.setTradeType(TradeConstant.GATHER_TYPE);//交易类型
        orders.setInstitutionName(institution.getCnName());//机构名称
        orders.setAgencyCode(institution.getAgencyCode());//代理机构编号
        orders.setExchangeRate(new BigDecimal(cd.getExchangeRate()));//换汇汇率
        orders.setTradeCurrency(cd.getTradeCurrency());//交易币种
        orders.setProductCode(product.getProductCode());
        orders.setRate(institutionProduct.getRate());//费率
        orders.setTradeAmount(new BigDecimal(cd.getTradeAmount()));//交易金额
        orders.setExchangeStatus(TradeConstant.SWAP_SUCCESS);//换汇状态
        orders.setExchangeTime(DateUtil.parse(cd.getExchangeTime(), "yyyy-MM-dd HH:mm:ss"));
        orders.setRateType(institutionProduct.getRateType());//费率类型
        orders.setChannelName(channel.getChannelCnName());//通道名称
        orders.setChannelCode(channel.getChannelCode());//机构编号
        orders.setFloatRate(institutionProduct.getFloatRate());//浮动率
        orders.setAddValue(institutionProduct.getAddValue());//附加值
        orders.setFeePayer(institutionProduct.getFeePayer());//手续费付款方
        orders.setPayMethod(product.getPayType());//支付方式
        //判断结算周期类型
        if (TradeConstant.DELIVERED.equals(institutionProduct.getSettleCycle())) {
            //妥投结算
            orders.setProductSettleCycle(TradeConstant.FUTURE_TIME);
        } else {
            orders.setProductSettleCycle(SettleDateUtil.getSettleDate(institutionProduct.getSettleCycle()));//产品结算周期
        }
        orders.setCreateTime(new Date());//创建时间
        orders.setCreator(institution.getCnName());//创建人
        return orders;
    }


    /**
     * 校验线上签名
     *
     * @param o
     * @return
     */
    @Override
    public boolean checkOnlineSignMsg(Object o) {
        Map<String, Object> map = ReflexClazzUtils.getFieldNames(o);
        String sign = String.valueOf(map.get("sign"));
        String institutionCode = String.valueOf(map.get("institutionId"));
        if (sign == null || "".equals(sign)) {
            throw new BusinessException(EResultEnum.SIGNATURE_CANNOT_BE_EMPTY.getCode());
        }
        if (map.get("serialVersionUID") != null) {
            map.put("serialVersionUID", null);
        }
        if (map.get("reqIp") != null) {
            map.put("reqIp", null);
        }
        if (map.get("sign") != null) {
            map.put("sign", null);
        }
        if (map.get("sort") != null) {
            map.put("sort", null);
        }
        if (map.get("order") != null) {
            map.put("order", null);
        }
        Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode)), Attestation.class);
        if (attestation == null) {
            attestation = attestationMapper.selectByInstitutionCode(institutionCode);
            if (attestation == null) {
                return false;
            }
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode), JSON.toJSONString(attestation));
        }
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] signMsg = decoder.decode(sign);
        map.put("sign", null);
        byte[] data = SignTools.getSignStr(objectMapToStringMap(map)).getBytes();
        try {
            return RSAUtils.verify(data, signMsg, attestation.getPubkey());
        } catch (Exception e) {
            log.info("----------- 签名校验发生错误----------机构code:{},签名signMsg:{}", institutionCode, signMsg);
        }
        return false;
    }

    /**
     * 校验RSA与MD5签名的一体方法
     *
     * @param o
     * @return
     */
    @Override
    public boolean checkSignMsgWithRSAMD5(Object o) {
        Map<String, Object> map = ReflexClazzUtils.getFieldNames(o);
        String sign = String.valueOf(map.get("sign"));
        String signType = String.valueOf(map.get("signType"));
        String institutionCode = String.valueOf(map.get("institutionId"));
        if (sign == null || "".equals(sign)) {
            throw new BusinessException(EResultEnum.SIGNATURE_CANNOT_BE_EMPTY.getCode());
        }
        if (map.get("serialVersionUID") != null) {
            map.put("serialVersionUID", null);
        }
        if (map.get("reqIp") != null) {
            map.put("reqIp", null);
        }
        if (map.get("sign") != null) {
            map.put("sign", null);
        }
        if (map.get("sort") != null) {
            map.put("sort", null);
        }
        if (map.get("order") != null) {
            map.put("order", null);
        }
        if (signType.equals(TradeConstant.RSA)) {
            Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode)), Attestation.class);
            if (attestation == null) {
                attestation = attestationMapper.selectByInstitutionCode(institutionCode);
                if (attestation == null) {
                    return false;
                }
                redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode), JSON.toJSONString(attestation));
            }
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] signMsg = decoder.decode(sign);
            map.put("sign", null);
            byte[] data = SignTools.getSignStr(objectMapToStringMap(map)).getBytes();
            try {
                return RSAUtils.verify(data, signMsg, attestation.getPubkey());
            } catch (Exception e) {
                log.info("----------- checkSignMsgWithRSAMD5 签名校验发生错误----------机构code:{},签名signMsg:{}", institutionCode, signMsg);
            }
        } else if (signType.equals(TradeConstant.MD5)) {
            Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode)), Attestation.class);
            if (attestation == null) {
                attestation = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
                if (attestation == null) {
                    return false;
                }
                redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode), JSON.toJSONString(attestation));
            }
            String str = SignTools.getSignStr(objectMapToStringMap(map)) + attestation.getMd5key();
            log.info("----------checkSignMsgWithRSAMD5 MD5加密前明文----------str:{}", str);
            String decryptSign = MD5Util.getMD5String(str);
            if (sign.equalsIgnoreCase(decryptSign)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验线上签名
     * 亚洲钱包 MD5验签用
     *
     * @param o
     * @return
     */
    @Override
    public boolean checkOnlineSignMsgUseMD5(Object o) {
        Map<String, Object> map = ReflexClazzUtils.getFieldNames(o);
        String sign = String.valueOf(map.get("sign"));
        String institutionCode = String.valueOf(map.get("institutionId"));
        if (StringUtils.isEmpty(sign)) {
            throw new BusinessException(EResultEnum.SIGNATURE_CANNOT_BE_EMPTY.getCode());
        }
        if (map.get("serialVersionUID") != null) {
            map.put("serialVersionUID", null);
        }
        if (map.get("reqIp") != null) {
            map.put("reqIp", null);
        }
        if (map.get("sign") != null) {
            map.put("sign", null);
        }
        if (map.get("sort") != null) {
            map.put("sort", null);
        }
        if (map.get("order") != null) {
            map.put("order", null);
        }
        Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode)), Attestation.class);
        if (attestation == null) {
            attestation = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
            if (attestation == null) {
                return false;
            }
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode), JSON.toJSONString(attestation));
        }
        String str = SignTools.getSignStr(objectMapToStringMap(map)) + attestation.getMd5key();
        log.info("----------checkOnlineSignMsgUseMD5 MD5加密前明文----------str:{}", str);
        String decryptSign = MD5Util.getMD5String(str);
        if (sign.equalsIgnoreCase(decryptSign)) {
            return true;
        }
        return false;
    }


    /**
     * 校验线下参数的签名
     *
     * @param o
     * @return
     */
    @Override
    public boolean checkOfflineSignMsg(Object o) {
        Map<String, Object> map = ReflexClazzUtils.getFieldNames(o);
        String sign = String.valueOf(map.get("sign"));
        if (sign == null || "".equals(sign)) {
            throw new BusinessException(EResultEnum.SIGNATURE_CANNOT_BE_EMPTY.getCode());
        }
        if (map.get("reqIp") != null) {
            map.put("reqIp", null);
        }
        map.put("sort", null);
        map.put("serialVersionUID", null);
        map.put("order", null);
        map.put("sign", null);
        String decryptSign = MD5Util.getMD5String(SignTools.getSignStr(objectMapToStringMap(map)));
        if (sign.equalsIgnoreCase(decryptSign)) {
            return true;
        }
        return false;
    }

    /**
     * 收银台参数解密
     *
     * @param cd
     * @return
     */
    @Override
    public CashierDTO decryptCashierSignMsg(CashierDTO cd) throws Exception {
        HashMap<String, Object> originalMap = BeanToMapUtil.beanToMap(cd);
        Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_PLATFORM_KEY), Attestation.class);
        if (attestation == null) {
            attestation = attestationMapper.selectPlatformInfo();
            if (attestation == null) {
                log.info("************收银台下单时对应的机构在平台没有对应的公钥信息*************");
                throw new BusinessException(EResultEnum.SIGNATURE_ERROR.getCode());
            }
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_PLATFORM_KEY, JSON.toJSONString(attestation));
        }
        HashMap<String, String> map = new HashMap<>();
        Set<String> originalSet = originalMap.keySet();
        for (String key : originalSet) {
            String originalValue = String.valueOf(originalMap.get(key));
            if (StringUtils.isEmpty(originalValue)) {
                log.info("---------------收银台参数含空---------------key:{},CashierDTO:{}", key, JSON.toJSON(cd));
                throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
            }
            String value = RSAUtils.decryptByPriKey(originalValue, attestation.getPrikey());
            map.put(key, value);
        }
        return JSON.parseObject(JSON.toJSONString(map), CashierDTO.class);
    }

    /**
     * 使用机构对应平台的私钥生成签名
     *
     * @param o
     * @return
     */
    @Override
    public String generateSignatureUsePlatRSA(Object o) {
        Map<String, Object> fieldMaps = ReflexClazzUtils.getFieldNames(o);
        String institutionCode = String.valueOf(fieldMaps.get("institutionId"));
        Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode)), Attestation.class);
        if (attestation == null) {
            attestation = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
            if (attestation == null) {
                return null;
            }
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode), JSON.toJSONString(attestation));
        }
        HashMap<String, Object> dtoMap = BeanToMapUtil.beanToMap(o);
        HashMap<String, String> map = new HashMap<>();
        if (dtoMap.get("reqIp") != null) {
            dtoMap.put("reqIp", null);
        }
        Set<String> keySet = dtoMap.keySet();
        for (String dtoKey : keySet) {
            map.put(dtoKey, String.valueOf(dtoMap.get(dtoKey)));
        }
        String signStr = SignTools.getSignStr(map);
        log.info("-------------generateSignatureUsePlatRSA 签名前原文--------------sign:{}", JSON.toJSON(signStr));
        byte[] msg = signStr.getBytes();
        String signMsg = null;
        try {
            signMsg = RSAUtils.sign(msg, attestation.getPrikey());
        } catch (Exception e) {
            log.info("----------------- generateSignatureUsePlatRSA 线上签名错误信息记录 ----------------签名时间:{},签名原始明文:{},签名:{}", DateUtil.formatDateTime(new Date()), msg, signMsg);
            throw new BusinessException(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        return signMsg;
    }

    /**
     * 使用机构对应平台的MD5生成签名
     *
     * @param o
     * @return
     */
    @Override
    public String generateSignatureUsePlatMD5(Object o) {
        Map<String, Object> fieldMaps = ReflexClazzUtils.getFieldNames(o);
        String institutionCode = String.valueOf(fieldMaps.get("institutionId"));
        Attestation attestationMD5 = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode)), Attestation.class);
        if (StringUtils.isEmpty(attestationMD5)) {
            attestationMD5 = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode), JSON.toJSONString(attestationMD5));
        }
        HashMap<String, Object> dtoMap = BeanToMapUtil.beanToMap(o);
        HashMap<String, String> map = new HashMap<>();
        if (dtoMap.get("sign") != null) {
            dtoMap.put("sign", null);
        }
        Set<String> keySet = dtoMap.keySet();
        for (String dtoKey : keySet) {
            map.put(dtoKey, String.valueOf(dtoMap.get(dtoKey)));
        }
        String signStr = SignTools.getSignStr(map);
        String md5Str = signStr + attestationMD5.getMd5key();
        log.info("----------generateSignatureUsePlatMD5 线下回调MD5原文----------MD5:{}", md5Str);
        String md5String = MD5Util.getMD5String(md5Str);
        log.info("----------generateSignatureUsePlatMD5 线下回调MD5密文----------MD5:{}", md5String);
        return md5String;
    }


    /**
     * 使用商户私钥生成RSA签名与MD5签名
     *
     * @param o
     * @return
     */
    @Override
    public Map generateSignatureUseInst(Object o) {
        Map<String, Object> fieldMaps = ReflexClazzUtils.getFieldNames(o);
        String institutionCode = String.valueOf(fieldMaps.get("institutionId"));
        Attestation attestationMD5 = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PFM_").concat(institutionCode)), Attestation.class);
        Attestation attestationRSA = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PFR_").concat(institutionCode)), Attestation.class);
        if (attestationRSA == null || attestationMD5 == null) {
            attestationMD5 = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
            attestationRSA = attestationMapper.selectByInstitutionCode(institutionCode);
            if (attestationRSA == null) {
                return null;
            }
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PFR_").concat(institutionCode), JSON.toJSONString(attestationRSA));
            redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PFM_").concat(institutionCode), JSON.toJSONString(attestationMD5));
        }
        HashMap<String, Object> dtoMap = BeanToMapUtil.beanToMap(o);
        HashMap<String, String> map = new HashMap<>();
        if (dtoMap.get("reqIp") != null) {
            dtoMap.put("reqIp", null);
        }
        if (dtoMap.get("sign") != null) {
            dtoMap.put("sign", null);
        }
        Set<String> keySet = dtoMap.keySet();
        for (String dtoKey : keySet) {
            map.put(dtoKey, String.valueOf(dtoMap.get(dtoKey)));
        }
        String signStr = SignTools.getSignStr(map);
        byte[] msg = signStr.getBytes();
        String md5Str = signStr + attestationMD5.getMd5key();
        String rsaString = null;
        try {
            rsaString = RSAUtils.sign(msg, attestationRSA.getPrikey());
        } catch (Exception e) {
            log.info("----------------- generateSignatureUseInst 线上签名错误信息记录 ----------------签名时间:{},签名原始明文:{},签名:{}", DateUtil.formatDateTime(new Date()), msg, rsaString);
            throw new BusinessException(EResultEnum.SIGNATURE_ERROR.getCode());
        }
        HashMap<String, String> hashMap = new HashMap<>();
        String md5String = MD5Util.getMD5String(md5Str);
        log.info("----------generateSignatureUseInst MD5----------MD5:{}", md5String);
        log.info("----------generateSignatureUseInst RSA----------RSA:{}", rsaString);
        hashMap.put("MD5", md5String);
        hashMap.put("RSA", rsaString);
        return hashMap;
    }


    /**
     * 对OrderLogisticsBachDTO生成签名
     *
     * @param dto
     * @return
     */
    @Override
    public String generateListSignatureLog(OrderLogisticsBachDTO dto) {
        List<LogisticsBachDTO> logisticsBachDTOs = dto.getLogisticsBachDTOs();
        StringBuilder sb = new StringBuilder();
        for (Object o : logisticsBachDTOs) {
            HashMap<String, Object> dtoMap = BeanToMapUtil.beanToMap(o);
            HashMap<String, String> map = new HashMap<>();
            Set<String> keySet = dtoMap.keySet();
            for (String dtoKey : keySet) {
                map.put(dtoKey, String.valueOf(dtoMap.get(dtoKey)));
            }
            sb.append(SignTools.getSignStr(map));
        }
        String institutionCode = dto.getInstitutionId();
        Attestation attestationRSA = null;
        Attestation attestationMD5 = null;
        attestationRSA = attestationMapper.selectByInstitutionCode(institutionCode);
        attestationMD5 = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
        byte[] msg = sb.toString().getBytes();
        String signMsg = null;
        String s = sb.toString() + attestationMD5.getMd5key();
        log.info("-------------MD5加密前原文-------------s:{}", s);
        String md5String = MD5Util.getMD5String(s);
        try {
            signMsg = RSAUtils.sign(msg, attestationRSA.getPrikey());
        } catch (Exception e) {
            log.info("----------------- 线上签名错误信息记录 ----------------签名时间:{},签名原始明文:{},签名:{}", DateUtil.formatDateTime(new Date()), msg, signMsg);
            throw new BusinessException(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        log.info("-------------RSA-------------rsa:{}", signMsg);
        log.info("-------------MD5-------------md5String:{}", md5String);
        return signMsg;
    }

    /**
     * 对PayOutDTO生成签名 付款
     *
     * @param dto
     * @return
     */
    @Override
    public String generateListSignaturePay(PayOutDTO dto) {
        List<PayOutRequestDTO> payOutRequestDTOs = dto.getPayOutRequestDTOs();
        StringBuilder sb = new StringBuilder();
        for (Object o : payOutRequestDTOs) {
            HashMap<String, Object> dtoMap = BeanToMapUtil.beanToMap(o);
            HashMap<String, String> map = new HashMap<>();
            Set<String> keySet = dtoMap.keySet();
            for (String dtoKey : keySet) {
                map.put(dtoKey, String.valueOf(dtoMap.get(dtoKey)));
            }
            sb.append(SignTools.getSignStr(map));
        }
        String institutionCode = dto.getPayOutRequestDTOs().get(0).getInstitutionId();
        Attestation attestationRSA = null;
        Attestation attestationMD5 = null;
        attestationRSA = attestationMapper.selectByInstitutionCode(institutionCode);
        attestationMD5 = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
        byte[] msg = sb.toString().getBytes();
        String signMsg = null;
        String s = sb.toString() + attestationMD5.getMd5key();
        log.info("-------------MD5加密前原文-------------s:{}", s);
        String md5String = MD5Util.getMD5String(s);
        try {
            signMsg = RSAUtils.sign(msg, attestationRSA.getPrikey());
        } catch (Exception e) {
            log.info("----------------- PayOutDTO签名错误信息记录 ----------------签名时间:{},签名原始明文:{},签名:{}", DateUtil.formatDateTime(new Date()), msg, signMsg);
            throw new BusinessException(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        log.info("-------------RSA-------------rsa:{}", signMsg);
        log.info("-------------MD5-------------md5String:{}", md5String);
        return signMsg;
    }

    /**
     * 配置限额限次信息
     *
     * @param institutionCode 机构编号
     * @param productCode     产品编号
     * @param amount          交易金额
     */
    @Override
    public void quota(String institutionCode, Integer productCode, BigDecimal amount) {
        log.info("=============【添加限额限次】=============START");
        //今日日期
        String todayDate = DateToolUtils.getReqDateE();
        //添加分布式锁
        String lockQuota = AsianWalletConstant.QUOTA + "_" + institutionCode.concat("_").concat(productCode.toString()).concat("_").concat(todayDate);
        try {
            //日交易限额key
            String DAILY_TOTAL_AMOUNT_KEY = institutionCode.concat("_").concat(productCode.toString()).concat("_").concat(todayDate).concat("_").concat(TradeConstant.DAILY_TOTAL_AMOUNT);
            //日交易笔数key
            String DAILY_TRADING_COUNT_KEY = institutionCode.concat("_").concat(productCode.toString()).concat("_").concat(todayDate).concat("_").concat(TradeConstant.DAILY_TRADING_COUNT);
            if (redisService.lock(lockQuota, 30 * 1000)) {
                String dailyAmount = redisService.get(DAILY_TOTAL_AMOUNT_KEY);
                String dailyCount = redisService.get(DAILY_TRADING_COUNT_KEY);
                if (StringUtils.isEmpty(dailyAmount) || StringUtils.isEmpty(dailyCount)) {
                    dailyAmount = "0";
                    dailyCount = "0";
                }
                //日交易限额
                BigDecimal dailyTotalAmount = new BigDecimal(dailyAmount);
                //日交易笔数
                int dailyTradingCount = Integer.parseInt(dailyCount);
                //加上交易金额
                dailyTotalAmount = dailyTotalAmount.add(amount);
                //加上交易笔数
                dailyTradingCount = dailyTradingCount + 1;
                redisService.set(DAILY_TOTAL_AMOUNT_KEY, String.valueOf(dailyTotalAmount), 24 * 60 * 60);
                redisService.set(DAILY_TRADING_COUNT_KEY, String.valueOf(dailyTradingCount), 24 * 60 * 60);
                log.info("=============【添加限额限次】=============【添加限额限次信息成功】");
            } else {
                log.info("=============【添加限额限次】=============【获取分布式锁异常】");
            }
        } catch (NumberFormatException e) {
            log.error("=============【添加限额限次信息异常】=============", e);
        } finally {
            //释放锁
            redisService.releaseLock(lockQuota);
            log.info("=============【添加限额限次】=============END");
        }
    }

    /**
     * 将对象转换为Map<String,String>类型
     *
     * @param oldMap
     * @return
     */
    private Map<String, String> objectMapToStringMap(Map<String, Object> oldMap) {
        HashMap<String, String> newMap = new HashMap<>();
        Set<String> set = oldMap.keySet();
        for (String s : set) {
            newMap.put(s, String.valueOf(oldMap.get(s)));
        }
        return newMap;
    }

    /**
     * 退款用创建调账单
     *
     * @param orderRefund
     * @return
     */
    @Override
    public Reconciliation createReconciliation(OrderRefund orderRefund, String remmark) {
        Reconciliation reconciliation = new Reconciliation();
        //调账订单id
        String reconciliationId = "T" + IDS.uniqueID();
        orderRefund.setRemark3(null);
        BeanUtils.copyProperties(orderRefund, reconciliation);
        reconciliation.setInstitutionCode(orderRefund.getInstitutionCode());
        reconciliation.setId(reconciliationId);
        reconciliation.setRefundOrderId(orderRefund.getId());
        //调账
        reconciliation.setChangeType(TradeConstant.TRANSFER);
        //调入
        reconciliation.setReconciliationType(AsianWalletConstant.RECONCILIATION_IN);
        //待调账
        reconciliation.setStatus(TradeConstant.RECONCILIATION_WAIT);
        reconciliation.setRemark(remmark);
        return reconciliation;
    }

    /**
     * 校验回调地址是否合法
     *
     * @param
     * @param str
     * @return
     */
    @Override
    public boolean checkUrl(String str) {
        String regex = "^((https|http)?:\\/\\/)[^\\s]+";
        if (!StringUtils.isEmpty(str) && !Pattern.matches(regex, str)) {
            return true;
        } else if (!StringUtils.isEmpty(str) && !Pattern.matches(regex, str)) {
            return true;
        }
        return false;
    }

    /**
     * 请求商户的回调地址
     * 服务器地址
     *
     * @param orders
     */
    @Override
    public void replyReturnUrl(Orders orders) {
        log.info("==================【回调商户服务器】==================【回调订单信息记录】 orders: {}", JSON.toJSONString(orders));
        if (StringUtils.isEmpty(orders.getReturnUrl())) {
            log.info("==================【回调商户服务器】==================【商户回调地址为空】");
            throw new BusinessException(EResultEnum.CALLBACK_ADDRESS_IS_NULL.getCode());
        }
        OnlineCallbackURLVO onlineCallbackURLVO = new OnlineCallbackURLVO(orders);
        OnlineCallbackVO onlineCallbackVO = onlineCallbackURLVO.getOnlineCallbackVO();
        if (orders.getTradeDirection().equals(TradeConstant.TRADE_ONLINE)) {
            //线上签名
            onlineCallbackVO.setSign(this.generateSignatureUsePlatRSA(onlineCallbackVO));
        } else {
            //线下签名
            onlineCallbackVO.setSign(this.generateSignatureUsePlatMD5(onlineCallbackVO));
        }
        log.info("==================【回调商户服务器】==================【商户回调接口URL记录】  serverUrl: {}", orders.getReturnUrl());
        log.info("==================【回调商户服务器】==================【回调参数记录】  onlineCallbackVO: {}", JSON.toJSON(onlineCallbackURLVO));
        //30s超时
        cn.hutool.http.HttpResponse execute = null;
        try {
            execute = HttpRequest.post(orders.getReturnUrl())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(JSON.toJSONString(onlineCallbackVO))
                    .timeout(30000)
                    .execute();
        } catch (Exception e) {
            log.info("==================【回调商户服务器】==================【httpException异常,上报回调商户队列】 【MQ_AW_CALLBACK_URL_FAIL】", e.getMessage());
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(onlineCallbackURLVO));
            rabbitMQSender.send(AD3MQConstant.E_MQ_AW_CALLBACK_URL_FAIL, JSON.toJSONString(rabbitMassage));
            return;
        }
        String body = execute.body();
        log.info("==================【回调商户服务器】==================【响应结果记录】 body: {}", body);
        if (StringUtils.isEmpty(body) || !body.equalsIgnoreCase(AsianWalletConstant.CALLBACK_SUCCESS)) {
            log.info("==================【回调商户服务器】==================【商户响应结果不正确,上报回调商户队列】 【MQ_AW_CALLBACK_URL_FAIL】");
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(onlineCallbackURLVO));
            rabbitMQSender.send(AD3MQConstant.E_MQ_AW_CALLBACK_URL_FAIL, JSON.toJSONString(rabbitMassage));
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/14
     * @Descripate 汇款回调商户
     **/
    @Override
    public void payOutCallBack(OrderPayment orderPayment) {
        log.info("==================【汇款回调商户服务器】==================【回调订单信息记录】 orders: {}", JSON.toJSONString(orderPayment));
        if (StringUtils.isEmpty(orderPayment.getServerUrl())) {
            log.info("==================【汇款回调商户服务器】==================【商户回调地址为空】");
            throw new BusinessException(EResultEnum.CALLBACK_ADDRESS_IS_NULL.getCode());
        }
        PayOutNoticeVO payOutNoticeVO = new PayOutNoticeVO(orderPayment);
        payOutNoticeVO.setSign(this.generateSignatureUsePlatMD5(payOutNoticeVO));
        log.info("==================【汇款回调商户服务器】==================【商户回调接口URL记录】  serverUrl: {}", orderPayment.getServerUrl());
        log.info("==================【汇款回调商户服务器】==================【回调参数记录】  payOutNoticeVO: {}", JSON.toJSON(payOutNoticeVO));
        HttpResponse httpResponse = HttpClientUtils.reqPost(orderPayment.getServerUrl(), BeanToMapUtil.beanToMap(payOutNoticeVO), null);
        log.info("==================【汇款回调商户服务器】==================【响应结果记录】 httpResponse: {}", JSON.toJSONString(httpResponse));
        if (httpResponse.getHttpStatus() != AsianWalletConstant.HTTP_SUCCESS_STATUS
                || !httpResponse.getJsonObject().toString().equalsIgnoreCase(AsianWalletConstant.CALLBACK_SUCCESS)) {
            log.info("==================【汇款回调商户服务器】==================【商户响应结果不正确,上报回调商户队列】 【MQ_AW_CALLBACK_URL_FAIL】");
            RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(payOutNoticeVO));
            rabbitMQSender.send(AD3MQConstant.E_MQ_PAYMENT_CALLBACK_URL_FAIL, JSON.toJSONString(rabbitMassage));
        }
    }

    /**
     * 截取币种位数
     *
     * @param orders
     * @return
     */
    @Override
    public boolean interceptDigit(Orders orders, BaseResponse baseResponse) {
        String defaultValue = this.getCurrencyDefaultValue(orders.getTradeCurrency());
        if (defaultValue == null) {
            orders.setRemark("币种默认值不存在");
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            ordersMapper.insert(orders);
            baseResponse.setCode(EResultEnum.DICINFO_CURRENCY_DEFAULT_IS_NULL.getCode());
            return false;
        }
        int bitPos = defaultValue.indexOf(".");
        int numOfBits = 0;
        if (bitPos != -1) {
            numOfBits = defaultValue.length() - bitPos - 1;
        }
        //交易金额
        orders.setTradeAmount((orders.getTradeAmount().setScale(numOfBits, BigDecimal.ROUND_HALF_UP)));
        return true;
    }

    /**
     * 请求用户的浏览器返回地址
     *
     * @param orders
     * @param response
     */
    @Override
    public void replyJumpUrl(Orders orders, HttpServletResponse response) {
        log.info("----------【**请求商户的jumpUrl地址开始**】---------- orders:{}", orders);
        if (StringUtils.isEmpty(orders.getJumpUrl())) {
            log.info("---------【商户的jumpUrl为空】----------- orders:{}", JSON.toJSON(orders));
            throw new BusinessException(EResultEnum.CALLBACK_ADDRESS_IS_NULL.getCode());
        }
        OnlineCallbackVO onlineCallbackVO = new OnlineCallbackVO(orders);
        HashMap<String, Object> map = BeanToMapUtil.beanToMap(onlineCallbackVO);
        StringBuilder sb = new StringBuilder();
        if (checkUrl(orders.getJumpUrl())) {
            sb.append("http://");
        }
        sb.append(orders.getJumpUrl().concat("?"));
        Set<String> key = map.keySet();
        int i = 1;
        for (String value : key) {
            i++;
            sb.append(value).append("=").append(map.get(value));
            if (key.size() >= i) {
                sb.append("&");
            }
        }
        String url = sb.toString();
        log.info("------------【跳转商户jumpUrl地址】------------URL:{}", JSON.toJSON(url));
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            log.info("--------------【跳转jumpUrl失败】--------------");
        }
    }

    /**
     * 从redis获取基础配置信息
     * 线下下单用
     *
     * @param placeOrdersDTO 下单实体
     * @param tradeDirection
     * @return
     */
    @Override
    public BasicsInfoVO getBasicsInfo(PlaceOrdersDTO placeOrdersDTO, Byte tradeDirection) {
        String institutionCode = placeOrdersDTO.getInstitutionId();//机构code
        //获取机构信息
        Institution institution = getInstitutionInfo(institutionCode);
        log.info("-----------------【机构】信息记录 -----------------institution: {}", JSON.toJSONString(institution));
        //查询产品信息,先从redis获取
        Product product = JSON.parseObject(redisService.get(AsianWalletConstant.PRODUCT_CACHE_CODE_KEY.concat("_").concat(String.valueOf(placeOrdersDTO.getProductCode()))), Product.class);
        if (product == null) {
            //redis不存在,从数据库获取
            product = productMapper.selectByProductCode(placeOrdersDTO.getProductCode());
            if (product == null) {
                log.info("-----------------【产品】信息不存在 -----------------placeOrdersDTO :{}", JSON.toJSONString(placeOrdersDTO));
                //产品信息不存在
                throw new BusinessException(EResultEnum.GET_PRODUCT_INFO_ERROR.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.PRODUCT_CACHE_CODE_KEY.concat("_").concat(String.valueOf(placeOrdersDTO.getProductCode())), JSON.toJSONString(product));
        }
        log.info("-----------------【产品】信息记录 -----------------product: {}", JSON.toJSONString(product));
        //查询机构产品信息,先从redis获取
        InstitutionProduct institutionProduct = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institution.getId().concat("_").concat(product.getId()))), InstitutionProduct.class);
        if (institutionProduct == null) {
            //redis不存在,从数据库获取
            institutionProduct = institutionProductMapper.selectByInstitutionIdAndProductId(institution.getId(), product.getId());
            if (institutionProduct == null) {
                log.info("-----------------【机构产品】信息不存在 -----------------placeOrdersDTO :{}", JSON.toJSONString(placeOrdersDTO));
                //机构产品通道信息不存在
                throw new BusinessException(EResultEnum.INSTITUTIONAL_PRODUCTS_DO_NOT_EXIST.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institution.getId().concat("_").concat(product.getId())), JSON.toJSONString(institutionProduct));
        }
        log.info("-----------------【机构产品】信息记录 -----------------institutionProduct: {}", JSON.toJSONString(institutionProduct));
        //按权重查询通道银行中间表ID
        List<String> channels = getChannels(institutionProduct);
        //通道银行List
        List<ChannelBank> channelBanks = new ArrayList<>();
        for (String channelBankId : channels) {
            //根据通道银行中间表ID,查询通道银行
            ChannelBank channelBank = channelBankMapper.selectById(channelBankId);
            if (channelBank == null) {
                continue;
            }
            channelBanks.add(channelBank);
        }
        log.info("-----------------【通道银行List】信息记录 -----------------channelBanks: {}", JSON.toJSONString(channelBanks));
        //通道
        Channel channel = null;
        //银行映射
        BankIssuerid bankIssuerid = null;
        //查询通道以及对应的银行映射
        flag:
        for (ChannelBank cb : channelBanks) {
            channel = this.getChannelById(cb.getChannelId());
            if (!(StringUtils.isEmpty(channel)) && channel.getEnabled()) {
                List<BankIssuerid> bis = bankIssueridMapper.selectByChannelCode(channel.getChannelCode());
                for (BankIssuerid bi : bis) {
                    if (!StringUtils.isEmpty(bi)) {
                        bankIssuerid = bi;
                        log.info("-----------------【银行映射】信息记录 -----------------bankIssuerid: {}", JSON.toJSONString(bankIssuerid));
                        log.info("-----------------【通道】信息记录 -----------------channel: {}", JSON.toJSONString(channel));
                        break flag;
                    }
                }
            }
        }
        if (StringUtils.isEmpty(channel)) {
            log.info("-----------------【通道信息】不存在 -----------------placeOrdersDTO :{}", JSON.toJSONString(placeOrdersDTO));
            //通道信息不存在
            throw new BusinessException(EResultEnum.CHANNEL_IS_NOT_EXISTS.getCode());
        }
        if (StringUtils.isEmpty(bankIssuerid)) {
            log.info("-----------------【银行映射信息】不存在 -----------------placeOrdersDTO :{}", JSON.toJSONString(placeOrdersDTO));
            //银行映射不存在
            throw new BusinessException(EResultEnum.BANK_MAPPING_NO_EXIST.getCode());
        }
        //银行机构号
        channel.setIssuerId(bankIssuerid.getIssuerId());
        //机构配置信息VO
        BasicsInfoVO basicsInfoVO = new BasicsInfoVO();
        //银行名称
        basicsInfoVO.setBankName(bankIssuerid.getBankName());
        //机构信息
        basicsInfoVO.setInstitution(institution);
        //产品信息
        basicsInfoVO.setProduct(product);
        //通道信息
        basicsInfoVO.setChannel(channel);
        //机构产品信息
        basicsInfoVO.setInstitutionProduct(institutionProduct);
        log.info("================== CommonService getBasicsInfo =================== basicsInfoVO: {}", JSON.toJSONString(basicsInfoVO));
        return basicsInfoVO;
    }


    /**
     * 获取通道id List
     *
     * @param institutionProduct
     * @return
     */
    @Override
    public List<String> getChannels(InstitutionProduct institutionProduct) {
        //查询机构通道信息,先从redis获取
        List<String> channelIds = JSON.parseArray(redisService.get(AsianWalletConstant.INSTITUTIONCHANNEL_CACHE_KEY.concat("_").concat(institutionProduct.getId())), String.class);
        if (channelIds == null || channelIds.size() == 0) {
            //redis不存在,从数据库获取
            channelIds = institutionChannelMapper.selectByInsProId(institutionProduct.getId());
            if (channelIds == null || channelIds.size() == 0) {
                log.info("====================【机构通道中间表】中【通道银行中间表id】不存在====================");
                //机构产品通道信息不存在
                throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_CHANNEL_NOT_EXISTS.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTIONCHANNEL_CACHE_KEY.concat("_").concat(institutionProduct.getId()), JSON.toJSONString(channelIds));
        }
        return channelIds;
    }


    /**
     * 从redis获取基础配置信息,通道通过issuerId匹配
     * 线上下单用
     *
     * @param issuerId
     * @param placeOrdersDTO
     * @param tradeDirection
     * @return
     */
    @Override
    public BasicsInfoVO getBasicsInfoByIssuerId(String issuerId, PlaceOrdersDTO placeOrdersDTO, Byte tradeDirection) {
        //获取机构信息
        Institution institution = getInstitutionInfo(placeOrdersDTO.getInstitutionId());
        //查询产品信息,先从redis获取
        Product product = null;
        //返回结果
        BasicsInfoVO basicsInfoVO = new BasicsInfoVO();
        if (placeOrdersDTO.getProductCode() == null) {
            product = JSON.parseObject(redisService.get(AsianWalletConstant.PRODUCT_CACHE_TYPE_KEY.concat("_").concat(placeOrdersDTO.getPayMethod().concat("_").concat(placeOrdersDTO.getTradeCurrency()).concat("_").concat(String.valueOf(tradeDirection)))), Product.class);
            if (product == null) {
                //redis不存在,从数据库获取
                product = productMapper.selectByPayTypeAndCurrencyAndTradeDirection(placeOrdersDTO.getPayMethod(), placeOrdersDTO.getTradeCurrency(), tradeDirection);
                if (product == null) {
                    //产品信息不存在
                    log.info("----------------- 产品信息不存在 -----------------  placeOrdersDTO :{}", JSON.toJSONString(placeOrdersDTO));
                    throw new BusinessException(EResultEnum.GET_PRODUCT_INFO_ERROR.getCode());
                }
                //同步redis
                redisService.set(AsianWalletConstant.PRODUCT_CACHE_TYPE_KEY.concat("_").concat(placeOrdersDTO.getPayMethod()).concat("_").concat(placeOrdersDTO.getTradeCurrency()).concat("_").concat(String.valueOf(tradeDirection)), JSON.toJSONString(product));
            }
        } else {
            //直连
            List<DirectConnectionVO> directConnectionVOList = institutionMapper.selectByIssuerIdAndInstitutionId(institution.getId(), issuerId);
            if (directConnectionVOList == null || directConnectionVOList.size() == 0) {
                log.info("------------------  机构产品通道信息不存在 直连从数据库查询关系信息为空  ------------------");
                throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_CHANNEL_NOT_EXISTS.getCode());
            }
            for (DirectConnectionVO directConnectionVO : directConnectionVOList) {
                //通道
                Channel channel = JSON.parseObject(redisService.get(AsianWalletConstant.CHANNEL_CACHE_CODE_KEY.concat("_").concat(directConnectionVO.getChannelCode())), Channel.class);
                if (channel == null) {
                    //redis不存在,从数据库获取
                    channel = channelMapper.selectByChannelCode(directConnectionVO.getChannelCode());
                    if (channel == null) {
                        //通道信息不存在
                        log.info("----------------- 通道信息不存在 -----------------  placeOrdersDTO :{}", JSON.toJSONString(placeOrdersDTO));
                        throw new BusinessException(EResultEnum.CHANNEL_IS_NOT_EXISTS.getCode());
                    }
                    //同步redis
                    redisService.set(AsianWalletConstant.CHANNEL_CACHE_CODE_KEY.concat("_").concat(directConnectionVO.getChannelCode()), JSON.toJSONString(channel));
                }
                //映射表
                BankIssuerid bankIssuerid = bankIssueridMapper.selectBankAndIssuerId(channel.getCurrency(), directConnectionVO.getBankName(), channel.getChannelCode());
                if (bankIssuerid != null) {
                    channel.setIssuerId(bankIssuerid.getIssuerId());
                } else {
                    break;
                }
                //产品
                product = JSON.parseObject(redisService.get(AsianWalletConstant.PRODUCT_CACHE_CODE_KEY.concat("_").concat(directConnectionVO.getProductCode())), Product.class);
                if (product == null) {
                    //redis不存在,从数据库获取
                    product = productMapper.selectByProductCode(Integer.valueOf(directConnectionVO.getProductCode()));
                    if (product == null) {
                        //产品信息不存在
                        log.info("----------------- 产品信息不存在 -----------------productCode:{}", directConnectionVO.getProductCode());
                        throw new BusinessException(EResultEnum.GET_PRODUCT_INFO_ERROR.getCode());
                    }
                    //同步redis
                    redisService.set(AsianWalletConstant.PRODUCT_CACHE_CODE_KEY.concat("_").concat(String.valueOf(directConnectionVO.getProductCode())), JSON.toJSONString(product));
                }
                //查询机构产品信息,先从redis获取
                InstitutionProduct institutionProduct = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institution.getId().concat("_").concat(product.getId()))), InstitutionProduct.class);
                if (institutionProduct == null) {
                    //redis不存在,从数据库获取
                    institutionProduct = institutionProductMapper.selectByInstitutionIdAndProductId(institution.getId(), product.getId());
                    if (institutionProduct == null) {
                        //机构产品通道信息不存在
                        log.info("----------------- 机构产品信息不存在 -----------------  placeOrdersDTO :{}", JSON.toJSONString(placeOrdersDTO));
                        throw new BusinessException(EResultEnum.INSTITUTIONAL_PRODUCTS_DO_NOT_EXIST.getCode());
                    }
                    //同步redis
                    redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institution.getId().concat("_").concat(product.getId())), JSON.toJSONString(institutionProduct));
                }
                basicsInfoVO.setBankName(directConnectionVO.getBankName());//银行名称
                basicsInfoVO.setInstitution(institution);//机构
                basicsInfoVO.setProduct(product);//产品
                basicsInfoVO.setChannel(channel);//通道
                basicsInfoVO.setInstitutionProduct(institutionProduct);//机构产品
                return basicsInfoVO;
            }
            if (basicsInfoVO.getChannel() == null) {
                log.info("------------------  机构产品通道信息不存在  【映射表信息】不存在  ------------------");
                throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_CHANNEL_NOT_EXISTS.getCode());
            }
        }
        //查询机构产品信息,先从redis获取
        InstitutionProduct institutionProduct = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institution.getId().concat("_").concat(product.getId()))), InstitutionProduct.class);
        if (institutionProduct == null) {
            //redis不存在,从数据库获取
            institutionProduct = institutionProductMapper.selectByInstitutionIdAndProductId(institution.getId(), product.getId());
            if (institutionProduct == null) {
                //机构产品通道信息不存在
                log.info("----------------- 机构产品信息不存在 ----------------- ");
                throw new BusinessException(EResultEnum.INSTITUTIONAL_PRODUCTS_DO_NOT_EXIST.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institution.getId().concat("_").concat(product.getId())), JSON.toJSONString(institutionProduct));
        }
        //查询通道
        String bankName = bankMapper.selectByIssuerId(issuerId);
        if (StringUtils.isEmpty(bankName)) {
            log.info("---------------------对应IssuerID的银行未找到---------------------issuerId:{}", issuerId);
            throw new BusinessException(EResultEnum.BANK_INSTITUTION_NUMBER_NOT_EXIST.getCode());
        }
        Channel channel = null;
        BankIssuerid bankIssuerid = null;
        List<String> channels = getChannels(institutionProduct);//通道银行中间表ID
        List<ChannelBank> channelBanks = new ArrayList<>();
        for (String s : channels) {
            ChannelBank channelBank = channelBankMapper.selectById(s);
            if (channelBank == null) {
                continue;
            }
            channelBanks.add(channelBank);
        }
        log.info("----------------通道银行list----------------channelBanks:{}", JSON.toJSONString(channelBanks));
        for (ChannelBank cb : channelBanks) {
            channel = this.getChannelById(cb.getChannelId());
            Bank bank = bankMapper.selectById(cb.getBankId());
            if (!(StringUtils.isEmpty(channel) && StringUtils.isEmpty(bank)) && bank.getBankName().equals(bankName) && channel.getEnabled()) {
                bankIssuerid = bankIssueridMapper.selectBankAndIssuerId(channel.getCurrency(), bank.getBankName(), channel.getChannelCode());
                if (!StringUtils.isEmpty(bankIssuerid)) {
                    channel.setIssuerId(bankIssuerid.getIssuerId());
                    log.info("-----------------匹配到的通道信息-----------------channel:{}", JSON.toJSONString(channel));
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(bankIssuerid)) {
            //通道信息不存在
            log.info("----------------- 通道信息不存在 -----------------  placeOrdersDTO :{}", JSON.toJSONString(placeOrdersDTO));
            throw new BusinessException(EResultEnum.CHANNEL_IS_NOT_EXISTS.getCode());
        }
        if (StringUtils.isEmpty(channel.getCurrency())) {
            log.info("----------------- 通道币种不存在----------------- 通道信息:{}", JSON.toJSON(channel));
            throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_CHANNEL_NOT_EXISTS.getCode());
        }
        basicsInfoVO.setBankName(bankIssuerid.getBankName());//银行名称
        basicsInfoVO.setInstitution(institution);//机构
        basicsInfoVO.setProduct(product);//产品
        basicsInfoVO.setChannel(channel);//通道
        basicsInfoVO.setInstitutionProduct(institutionProduct);//机构产品
        log.info("================== CommonService getBasicsInfoByIssuerId =================== basicsInfoVO: {}", JSON.toJSONString(basicsInfoVO));
        return basicsInfoVO;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 创建付款单
     **/
    public OrderPayment createOrderPayment(PayOutRequestDTO payOutRequestDTO, String reqIp, BasicsInfoVO
            basicsInfoVO) {
        OrderPayment orderPayment = new OrderPayment();
        //付款单id
        orderPayment.setId("PO" + IDS.uniqueID());
        //机构code
        orderPayment.setInstitutionCode(payOutRequestDTO.getInstitutionId());
        //机构名称
        orderPayment.setInstitutionName(basicsInfoVO == null ? null : basicsInfoVO.getInstitution().getCnName());
        //机构上报时间
        if (StringUtils.isEmpty(payOutRequestDTO.getOrderTime())) {
            orderPayment.setInstitutionOrderTime(new Date());
        } else {
            orderPayment.setInstitutionOrderTime(DateUtil.parse(payOutRequestDTO.getOrderTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        //机构上报付款流水号
        orderPayment.setInstitutionOrderId(payOutRequestDTO.getOrderNo());
        //机构上报付款批次号
        orderPayment.setInstitutionBatchNo(payOutRequestDTO.getInstitutionBatchNo());
        //订单币种
        orderPayment.setTradeCurrency(payOutRequestDTO.getOrderCurrency());
        //汇款币种
        orderPayment.setPaymentCurrency(payOutRequestDTO.getPaymentCurrency());
        //汇款银行名称
        orderPayment.setBankAccountName(basicsInfoVO == null ? null : basicsInfoVO.getBankName());
        //汇款银行卡号
        orderPayment.setBankAccountNumber(payOutRequestDTO.getBankAccountNumber());
        //汇款国家
        orderPayment.setReceiverCountry(payOutRequestDTO.getCountry());
        //汇款地址
        orderPayment.setReceiverAdress(payOutRequestDTO.getAdress());
        //银行code
        orderPayment.setBankCode(basicsInfoVO == null ? null : basicsInfoVO.getChannel().getIssuerId());
        //产品编号
        orderPayment.setProductCode(basicsInfoVO == null ? null : basicsInfoVO.getProduct().getProductCode());
        //通道编号
        orderPayment.setChannelCode(basicsInfoVO == null ? null : basicsInfoVO.getChannel().getChannelCode());
        //通道名称
        orderPayment.setChannelName(basicsInfoVO == null ? null : basicsInfoVO.getChannel().getChannelCnName());
        //付款方式
        orderPayment.setPayMethod(basicsInfoVO == null ? null : basicsInfoVO.getProduct().getPayType());
        //请求ip
        orderPayment.setReqIp(reqIp);
        //服务器回调地址
        orderPayment.setServerUrl(payOutRequestDTO.getServerUrl());
        //浏览器返回地址
        orderPayment.setBrowserUrl(payOutRequestDTO.getBrowserUrl());
        //创建时间
        orderPayment.setCreateTime(new Date());
        //创建人
        orderPayment.setCreator(basicsInfoVO.getInstitution().getCnName());
        //应结算日期
        orderPayment.setExtend1(SettleDateUtil.getSettleDate(basicsInfoVO.getInstitutionProduct().getSettleCycle()));
        //持卡人
        orderPayment.setExtend2(payOutRequestDTO.getCardholder());
        //通道服务名
        orderPayment.setExtend3(basicsInfoVO.getChannel().getChannelEnName());
        //默认为不是人工退款
        orderPayment.setExtend4(false);
        //代理商户号
        orderPayment.setExtend5(basicsInfoVO.getInstitution().getAgencyCode());
        return orderPayment;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 付款计算手续费
     **/
    public CalcFeeVO calcPoundageOrderPayment(OrderPayment orderPayment, BasicsInfoVO basicsInfoVO) {
        CalcFeeVO calcFeeVO = new CalcFeeVO();//返回结果
        try {
            BigDecimal poundage = new BigDecimal(0);
            //产品币种
            String productCurrency = basicsInfoVO.getProduct().getCurrency();
            //机构产品
            InstitutionProduct institutionProduct = basicsInfoVO.getInstitutionProduct();
            //查询出商户对应产品的费率信息
            if (institutionProduct.getRate() == null || institutionProduct.getRateType() == null || institutionProduct.getAddValue() == null) {
                log.info("----------------- 计费信息记录 ----------------费率:{},费率类型:{},机构产品信息:{}", institutionProduct.getRate(), institutionProduct.getRateType(), JSON.toJSONString(institutionProduct));
                calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
                return calcFeeVO;
            }
            //币种不一致时
            if (!orderPayment.getTradeCurrency().equals(productCurrency)) {
                //换汇后的机构产品
                institutionProduct = CalcFeeExchangeOrderPayment(productCurrency, institutionProduct, null, orderPayment.getTradeCurrency());
                if (StringUtils.isEmpty(institutionProduct)) {
                    //换汇失败
                    log.info("----------------- 换汇失败 计费信息记录 ----------------订单:{},机构产品信息:{}", JSON.toJSONString(orderPayment), JSON.toJSONString(institutionProduct));
                    calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
                    return calcFeeVO;
                }
            }
            //单笔费率
            if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_RATE)) {
                //手续费=订单金额*单笔费率+附加值
                poundage = orderPayment.getPaymentAmount().multiply(institutionProduct.getRate());
                //判断手续费是否小于最小值，大于最大值
                if (institutionProduct.getMinTate() != null && poundage.compareTo(institutionProduct.getMinTate()) == -1) {
                    poundage = institutionProduct.getMinTate();
                }
                if (institutionProduct.getMaxTate() != null && poundage.compareTo(institutionProduct.getMaxTate()) == 1) {
                    poundage = institutionProduct.getMaxTate();
                }
            }
            //单笔定额
            if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_QUOTA)) {
                //手续费=单笔定额值+附加值
                poundage = institutionProduct.getRate().add(institutionProduct.getAddValue());
            }
            //返回计算手续费
            calcFeeVO.setFee(poundage);
            //计费状态
            calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_SUCCESS);
            //计费时间
            calcFeeVO.setChargeTime(new Date());
        } catch (Exception e) {
            log.info("---------------- 付款计算手续费异常 --------------------Exception ：{}", e);
            calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
            return calcFeeVO;
        }
        log.info("================== CommonService calcPoundageOrderPayment =================== calcFeeVO: {}", JSON.toJSONString(calcFeeVO));
        return calcFeeVO;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 付费计算换汇之后的机构产品
     **/
    public InstitutionProduct CalcFeeExchangeOrderPayment(String productCurrency, InstitutionProduct
            institutionProduct, BigDecimal existRate, String orderCurrency) {
        //汇率
        //先从redis获取
        ExchangeRate exchangeRate = new ExchangeRate();
        if (StringUtils.isEmpty(existRate)) {
            exchangeRate = JSON.parseObject(redisService.get(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(productCurrency).concat("_").concat(orderCurrency)), ExchangeRate.class);
        } else {
            exchangeRate.setBuyRate(existRate);
        }
        if (exchangeRate == null || !exchangeRate.getEnabled()) {
            //查询订单买入汇率
            exchangeRate = exchangeRateMapper.selectRateByOrderCurrencyAndTradeCurrency(productCurrency, orderCurrency);
        }
        if (exchangeRate == null || exchangeRate.getBuyRate() == null) {
            log.info("----------------- 计算手续费 换汇计算信息记录 ----------------未查询到币种对应汇率,本位币种:{},目标币种:{}", productCurrency, orderCurrency);
            messageFeign.sendSimple(developerMobile, "计算手续费换汇计算:查询汇率异常!");
            messageFeign.sendSimpleMail(developerEmail, "计算手续费换汇计算:查询汇率异常!", "计算手续费换汇计算:查询汇率异常!");
            return null;
        }
        redisService.set(AsianWalletConstant.EXCHANGERATE_CACHE_KEY.concat("_").concat(productCurrency).concat("_").concat(orderCurrency), JSON.toJSONString(exchangeRate));
        //单笔费率 最大值
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_RATE) && !StringUtils.isEmpty(institutionProduct.getMaxTate())) {
            institutionProduct.setMaxTate(institutionProduct.getMaxTate().multiply(exchangeRate.getBuyRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        //单笔费率 最小值
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_RATE) && !StringUtils.isEmpty(institutionProduct.getMinTate())) {
            institutionProduct.setMinTate(institutionProduct.getMinTate().multiply(exchangeRate.getBuyRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        //单笔定额时的换汇
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_QUOTA)) {
            institutionProduct.setRate(institutionProduct.getRate().multiply(exchangeRate.getBuyRate()).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        log.info("================== CommonService CalcFeeExchangeOrderPayment =================== institutionProduct: {}", JSON.toJSONString(institutionProduct));
        return institutionProduct;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 付款获取基础信息
     **/
    public BasicsInfoVO getBasicsInfo(PayOutRequestDTO payOutRequestDTO) {
        //查询机构
        Institution institution = this.getInstitutionInfo(payOutRequestDTO.getInstitutionId());
        //查询产品
        Product product = productMapper.selectByCurrencyAndInstitutionId(payOutRequestDTO.getPaymentCurrency(), institution.getId());
        if (product == null) {
            log.info("-----------------付款获取基础信息 产品信息不存在 -----------------  payOutRequestDTO :{}", JSON.toJSONString(payOutRequestDTO));
            throw new BusinessException(EResultEnum.GET_PRODUCT_INFO_ERROR.getCode());
        }
        //查询机构产品信息,先从redis获取
        InstitutionProduct institutionProduct = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institution.getId().concat("_").concat(product.getId()))), InstitutionProduct.class);
        if (institutionProduct == null) {
            //redis不存在,从数据库获取
            institutionProduct = institutionProductMapper.selectByInstitutionIdAndProductId(institution.getId(), product.getId());
            if (institutionProduct == null || institutionProduct.getEnabled() == false) {
                //机构产品通道信息不存在
                log.info("----------------- 付款获取基础信息 机构产品信息不存在 -----------------  payOutRequestDTO :{}", JSON.toJSONString(payOutRequestDTO));
                throw new BusinessException(EResultEnum.INSTITUTIONAL_PRODUCTS_DO_NOT_EXIST.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(institution.getId().concat("_").concat(product.getId())), JSON.toJSONString(institutionProduct));
        }

        //查询通道
        String bankName = bankMapper.selectByIssuerId(payOutRequestDTO.getIssuerId());
        if (StringUtils.isEmpty(bankName)) {
            log.info("---------------------付款获取基础信息 对应IssuerID的银行未找到---------------------issuerId:{}", payOutRequestDTO.getIssuerId());
            throw new BusinessException(EResultEnum.BANK_INSTITUTION_NUMBER_NOT_EXIST.getCode());
        }
        Channel channel = null;
        BankIssuerid bankIssuerid = null;
        List<String> channels = this.getChannels(institutionProduct);//通道银行中间表ID
        List<ChannelBank> channelBanks = new ArrayList<>();
        for (String cs : channels) {
            ChannelBank channelBank = channelBankMapper.selectById(cs);
            if (channelBank == null) {
                continue;
            }
            channelBanks.add(channelBank);
        }
        log.info("----------------付款获取基础信息 通道银行list----------------channelBanks:{}", JSON.toJSONString(channelBanks));
        for (ChannelBank cb : channelBanks) {
            channel = this.getChannelById(cb.getChannelId());
            Bank bank = bankMapper.selectById(cb.getBankId());
            if (!(StringUtils.isEmpty(channel) && StringUtils.isEmpty(bank)) && bank.getBankName().equals(bankName) && channel.getEnabled()) {
                bankIssuerid = bankIssueridMapper.selectBankAndIssuerId(channel.getCurrency(), bank.getBankName(), channel.getChannelCode());
                if (!StringUtils.isEmpty(bankIssuerid)) {
                    channel.setIssuerId(bankIssuerid.getIssuerId());
                    log.info("-----------------付款获取基础信息 匹配到的通道信息-----------------channel:{}", JSON.toJSONString(channel));
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(bankIssuerid)) {
            //通道信息不存在
            log.info("-----------------付款获取基础信息 通道信息不存在 -----------------  payOutRequestDTO :{}", JSON.toJSONString(payOutRequestDTO));
            throw new BusinessException(EResultEnum.CHANNEL_IS_NOT_EXISTS.getCode());
        }
        if (StringUtils.isEmpty(channel.getCurrency())) {
            log.info("----------------- 付款获取基础信息 通道币种不存在----------------- 通道信息:{}", JSON.toJSON(channel));
            throw new BusinessException(EResultEnum.INSTITUTION_PRODUCT_CHANNEL_NOT_EXISTS.getCode());
        }
        //返回结果
        BasicsInfoVO basicsInfoVO = new BasicsInfoVO();
        basicsInfoVO.setBankName(bankIssuerid.getBankName());//银行名称
        basicsInfoVO.setInstitution(institution);//机构
        basicsInfoVO.setProduct(product);//产品
        basicsInfoVO.setChannel(channel);//通道
        basicsInfoVO.setInstitutionProduct(institutionProduct);//机构产品
        log.info("================== CommonService getBasicsInfo =================== basicsInfoVO: {}", JSON.toJSONString(basicsInfoVO));
        return basicsInfoVO;
    }

    /**
     * 根据通道id从redis里获取通道信息
     *
     * @param channelId
     * @return
     */
    @Override
    public Channel getChannelById(String channelId) {
        //从redis获取
        Channel channel = JSON.parseObject(redisService.get(AsianWalletConstant.CHANNEL_CACHE_KEY.concat("_").concat(channelId)), Channel.class);
        if (channel == null) {
            //redis为空从数据库获取
            channel = channelMapper.selectById(channelId);
            if (channel == null) {
                log.info("-----------------通道信息不存在 ----------------- channelId:{}", channelId);
                //通道信息不存在
                throw new BusinessException(EResultEnum.GET_CHANNEL_INFO_ERROR.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.CHANNEL_CACHE_KEY.concat("_").concat(channelId), JSON.toJSONString(channel));
        }
        log.info("================== CommonService getChannelById =================== channel: {}", JSON.toJSONString(channel));
        return channel;
    }

    /**
     * 根据通道code从redis获取通道信息
     *
     * @param channelCode 通道code
     */
    @Override
    public Channel getChannelByChannelCode(String channelCode) {
        //从redis获取
        Channel channel = JSON.parseObject(redisService.get(AsianWalletConstant.CHANNEL_CACHE_CODE_KEY.concat("_").concat(channelCode)), Channel.class);
        if (channel == null) {
            //redis为空从数据库获取
            channel = channelMapper.selectByChannelCode(channelCode);
            if (channel == null) {
                log.info("-----------------通道信息不存在 ----------------- channelCode:{}", channelCode);
                //通道信息不存在
                throw new BusinessException(EResultEnum.GET_CHANNEL_INFO_ERROR.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.CHANNEL_CACHE_CODE_KEY.concat("_").concat(channelCode), JSON.toJSONString(channel));
        }
        if (!channel.getEnabled()) {
            log.info("-----------------通道禁用 ----------------- channelCode:{}", channelCode);
            throw new BusinessException(EResultEnum.CHANNEL_STATUS_ABNORMAL.getCode());
        }
        log.info("================== CommonService getChannelByChannelCode =================== channel: {}", JSON.toJSONString(channel));
        return channel;
    }

    /**
     * 获得机构的信息
     *
     * @param institutionCode
     * @return
     */
    @Override
    public Institution getInstitutionInfo(String institutionCode) {
        //查询机构信息,先从redis获取
        Institution institution = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institutionCode)), Institution.class);
        if (institution == null) {
            //redis不存在,从数据库获取
            institution = institutionMapper.selectByInstitutionCode(institutionCode);
            if (institution == null || institution.getEnabled() == false) {
                log.info("-----------------机构信息不存在 -----------------  institutionCode :{}", institutionCode);
                //机构信息不存在
                throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTION_CACHE_KEY.concat("_").concat(institution.getInstitutionCode()), JSON.toJSONString(institution));
        }
        log.info("================== CommonService getInstitutionInfo =================== institution: {}", JSON.toJSONString(institution));
        return institution;
    }

    /**
     * 支付成功新增物流信息
     *
     * @param orders
     */
    @Override
    public void insertOrderLogistics(Orders orders) {
        try {
            //创建订单物流信息对象
            OrderLogistics orderLogistics = new OrderLogistics();
            orderLogistics.setId(IDS.uniqueID().toString());//订单物流id
            orderLogistics.setReferenceNo(orders.getId());//系统流水号
            orderLogistics.setInstitutionCode(orders.getInstitutionCode());//机构code
            orderLogistics.setInstitutionName(orders.getInstitutionName());//机构名称
            orderLogistics.setInstitutionOrderId(orders.getInstitutionOrderId());//机构订单号
            orderLogistics.setOrderCurrency(orders.getOrderCurrency());//订单币种
            orderLogistics.setAmount(orders.getAmount());//订单金额
            orderLogistics.setReqIp(orders.getReqIp());//请求ip或者请求网站url
            orderLogistics.setChannelCallbackTime(orders.getChannelCallbackTime());//订单支付完成时间
            orderLogistics.setIssuerId(orders.getIssuerId());//银行机构代码
            orderLogistics.setLanguage(orders.getLanguage());//语言
            orderLogistics.setProductDescription(orders.getProductName());//商品名称
            orderLogistics.setPayerName(orders.getDraweeName());//收货人姓名
            orderLogistics.setPayerEmail(orders.getDraweeEmail());//收货人邮箱
            orderLogistics.setDeliveryStatus(TradeConstant.UNSHIPPED);//未发货
            orderLogistics.setCreateTime(new Date());//创建时间
            orderLogistics.setCreator(orders.getInstitutionName());//创建人
            orderLogisticsMapper.insertSelective(orderLogistics);
        } catch (Exception e) {
            log.error("===============支付成功新增物流信息发生异常===============", e);
        }
    }


    /**
     * 支付成功发送邮件给付款人
     *
     * @param email
     * @param language
     * @param emailNum
     * @param
     */
    @Override
    @Async
    public void sendEmail(String email, String language, Status emailNum, Orders orders) {
        log.info("*********************支付成功发送支付通知邮件 Start*************************************");
        try {
            if (!StringUtils.isEmpty(email)) {
                log.info("*******************支付成功订单对应的付款人邮箱是：*******************" + email);
                Map<String, Object> map = new HashMap<>();
//                map.put("draweeName", orders.getDraweeName());//付款人姓名
                map.put("orderCurrency", orders.getOrderCurrency());//订单币种
                map.put("amount", orders.getAmount());//订单金额
                map.put("reqIp", orders.getReqIp());//请求的网站url
                SimpleDateFormat sf = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);//外国时间格式
                map.put("channelCallbackTime", sf.format(orders.getChannelCallbackTime()));//支付完成时间
                map.put("institutionOrderId", orders.getInstitutionOrderId());//机构订单号
                map.put("goodsDescription", orders.getProductName());//商品名称
                map.put("issuerId", orders.getBankName());//付款银行
                map.put("referenceNo", orders.getId());//AW交易流水号
                messageFeign.sendTemplateMail(email, language, emailNum, map);
            }
        } catch (Exception e) {
            messageFeign.sendSimple("18800330943", "支付成功发送支付通知邮件失败:" + email);
            log.error("支付成功发送支付通知邮件失败：{}==={}", email, e.getMessage());
        }
        log.info("*********************支付成功发送支付通知邮件 End*************************************");
    }

    /**
     * 校验批量更新物流订单签名
     *
     * @param orderLogisticsBachDTO
     */
    @Override
    public boolean checkOrderLogistics(OrderLogisticsBachDTO orderLogisticsBachDTO) {
        String sign = orderLogisticsBachDTO.getSign();
        String institutionCode = orderLogisticsBachDTO.getInstitutionId();
        List<LogisticsBachDTO> logisticsBachDTOs = orderLogisticsBachDTO.getLogisticsBachDTOs();
        StringBuilder sb = new StringBuilder();
        for (LogisticsBachDTO logisticsBachDTO : logisticsBachDTOs) {
            Map<String, Object> map = ReflexClazzUtils.getFieldNames(logisticsBachDTO);
            sb.append(SignTools.getSignStr(objectMapToStringMap(map)));
        }
        if (sign == null || "".equals(sign)) {
            throw new BusinessException(EResultEnum.SIGNATURE_CANNOT_BE_EMPTY.getCode());
        }
        if (orderLogisticsBachDTO.getSignType().equals("1")) {
            //RSA 验签
            Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode)), Attestation.class);
            if (attestation == null) {
                attestation = attestationMapper.selectByInstitutionCode(institutionCode);
                if (attestation == null) {
                    return false;
                }
                redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode), JSON.toJSONString(attestation));
            }
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] signMsg = decoder.decode(sign);
            byte[] data = sb.toString().getBytes();
            try {
                return RSAUtils.verify(data, signMsg, attestation.getPubkey());
            } catch (Exception e) {
                log.info("----------- checkOrderLogistics 签名校验发生错误----------机构code:{},签名signMsg:{}", institutionCode, signMsg);
                return false;
            }
        } else if (orderLogisticsBachDTO.getSignType().equals("2")) {
            //MD5 验签
            Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode)), Attestation.class);
            if (attestation == null) {
                attestation = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
                if (attestation == null) {
                    return false;
                }
                redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode), JSON.toJSONString(attestation));
            }
            String str = sb.toString() + attestation.getMd5key();
            log.info("----------checkOrderLogistics MD5加密前明文----------str:{}", str);
            String decryptSign = MD5Util.getMD5String(str);
            if (sign.equalsIgnoreCase(decryptSign)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验付款签名
     *
     * @param payOutDTO
     */
    @Override
    public boolean checkPayment(PayOutDTO payOutDTO) {
        String sign = payOutDTO.getSign();
        List<PayOutRequestDTO> pay = payOutDTO.getPayOutRequestDTOs();
        StringBuilder sb = new StringBuilder();
        String institutionCode = pay.get(0).getInstitutionId();
        for (PayOutRequestDTO logisticsBachDTO : pay) {
            Map<String, Object> map = ReflexClazzUtils.getFieldNames(logisticsBachDTO);
            sb.append(SignTools.getSignStr(objectMapToStringMap(map)));
        }
        if (sign == null || "".equals(sign)) {
            throw new BusinessException(EResultEnum.SIGNATURE_CANNOT_BE_EMPTY.getCode());
        }
        if (payOutDTO.getSignType().equals("1")) {
            //RSA 验签
            Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode)), Attestation.class);
            if (attestation == null) {
                attestation = attestationMapper.selectByInstitutionCode(institutionCode);
                if (attestation == null) {
                    return false;
                }
                redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_").concat(institutionCode), JSON.toJSONString(attestation));
            }
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] signMsg = decoder.decode(sign);
            byte[] data = sb.toString().getBytes();
            try {
                return RSAUtils.verify(data, signMsg, attestation.getPubkey());
            } catch (Exception e) {
                log.info("----------- PayOutDTO 签名校验发生错误----------机构code:{},签名signMsg:{}", institutionCode, signMsg);
                return false;
            }
        } else if (payOutDTO.getSignType().equals("2")) {
            //MD5 验签
            Attestation attestation = JSON.parseObject(redisService.get(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode)), Attestation.class);
            if (attestation == null) {
                attestation = attestationMapper.selectByInstitutionCode("PF_" + institutionCode);
                if (attestation == null) {
                    return false;
                }
                redisService.set(AsianWalletConstant.ATTESTATION_CACHE_KEY.concat("_PF_").concat(institutionCode), JSON.toJSONString(attestation));
            }
            String str = sb.toString() + attestation.getMd5key();
            log.info("----------PayOutDTO MD5加密前明文----------str:{}", str);
            String decryptSign = MD5Util.getMD5String(str);
            if (sign.equalsIgnoreCase(decryptSign)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 物流信息更新后发送发货通知邮件
     *
     * @param email
     * @param language
     * @param emailNum
     * @param orderLogistics
     */
    @Override
    @Async
    public void sendDeliveryEmail(String email, String language, Status emailNum, OrderLogistics orderLogistics) {
        log.info("*********************物流信息更新成功后发送发货通知邮件 Start*************************************");
        try {
            if (!StringUtils.isEmpty(email)) {
                log.info("*******************发送物流信息通知的邮箱是：*******************" + email);
                Map<String, Object> map = new HashMap<>();
//                map.put("draweeName", orderLogistics.getPayerName());//收货人姓名
                map.put("reqIp", orderLogistics.getReqIp());//请求的网站url
                SimpleDateFormat sf = new SimpleDateFormat("MMM dd,yyyy", Locale.ENGLISH);//外国时间格式
                map.put("channelCallbackTime", sf.format(orderLogistics.getChannelCallbackTime()));//支付完成时间
                map.put("institutionOrderId", orderLogistics.getInstitutionOrderId());//机构订单号
                map.put("orderCurrency", orderLogistics.getOrderCurrency());//订单币种
                map.put("amount", orderLogistics.getAmount());//订单金额
                map.put("goodsDescription", orderLogistics.getProductDescription());//商品名称
                map.put("issuerId", orderLogistics.getIssuerId());//付款银行
                map.put("invoiceNo", orderLogistics.getInvoiceNo());//发货单号
                messageFeign.sendTemplateMail(email, language, emailNum, map);
            }
        } catch (Exception e) {
            messageFeign.sendSimple("18800330943", "物流信息更新成功后发送发货通知邮件失败:" + email);
            log.error("物流信息更新成功后发送发货通知邮件失败：{}==={}", email, e.getMessage());
        }
        log.info("*********************物流信息更新成功后发送发货通知邮件 End*************************************");
    }

    /**
     * megaPay-THB通道分发AD3
     *
     * @param megaPayServerCallbackDTO 参数
     * @param url                      url
     */
    @Override
    public String megaTHBCallbackAD3(MegaPayServerCallbackDTO megaPayServerCallbackDTO, String url) {
        String ad3Url = ad3ParamsConfig.getAd3ItsUrl().concat(url);
        log.info("----------------------megaPay-THB通道回调AD3信息记录----------------------分发AD3URL:{}", ad3Url);
        Map<String, Object> map = new HashMap<>();
        map.put("inv", megaPayServerCallbackDTO.getInv());
        map.put("amt", megaPayServerCallbackDTO.getAmt());
        map.put("merID", megaPayServerCallbackDTO.getMerID());
        map.put("refCode", megaPayServerCallbackDTO.getRefCode());
        map.put("mark", megaPayServerCallbackDTO.getMark());
        map.put(megaPayServerCallbackDTO.getMd5KeyStr(), megaPayServerCallbackDTO.getResult());
        log.info("----------------------megaPay-THB通道回调AD3信息记录----------------------分发AD3参数:{}", JSON.toJSONString(map));
        //分发给AD3
        cn.hutool.http.HttpResponse execute = HttpRequest.post(ad3Url)
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(map)
                .timeout(20000)
                .execute();
        String body = execute.body();
        log.info("----------------------megaPay-THB通道回调AD3信息记录----------------------回调返回 body:{}", body);
        log.info("----------------------megaPay-THB通道回调AD3信息记录----------------------http状态码:{}", execute.getStatus());
        return body;
    }

    /**
     * megaPay-IDR通道分发AD3
     *
     * @param megaPayIDRServerCallbackDTO 参数
     * @param url                         url
     */
    @Override
    public String megaIDRCallbackAD3(MegaPayIDRServerCallbackDTO megaPayIDRServerCallbackDTO, String url) {
        String ad3Url = ad3ParamsConfig.getAd3ItsUrl().concat(url);
        log.info("----------------------megaPay-IDR通道回调AD3信息记录----------------------分发AD3URL:{}", ad3Url);
        Map<String, Object> map = new HashMap<>();
        map.put("np_inv", megaPayIDRServerCallbackDTO.getNp_inv());
        map.put("np_amt", megaPayIDRServerCallbackDTO.getNp_amt());
        map.put("np_merID", megaPayIDRServerCallbackDTO.getNp_merID());
        map.put("np_refCode", megaPayIDRServerCallbackDTO.getNp_refCode());
        map.put("np_mark", megaPayIDRServerCallbackDTO.getNp_mark());
        map.put(megaPayIDRServerCallbackDTO.getMd5KeyStr(), megaPayIDRServerCallbackDTO.getResult());
        log.info("----------------------megaPay-IDR通道回调AD3信息记录----------------------分发AD3参数:{}", JSON.toJSONString(map));
        //分发给AD3
        cn.hutool.http.HttpResponse execute = HttpRequest.post(ad3Url)
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(map)
                .timeout(20000)
                .execute();
        String body = execute.body();
        log.info("----------------------megaPay-IDR通道回调AD3信息记录----------------------回调返回 body:{}", body);
        log.info("----------------------megaPay-IDR通道回调AD3信息记录----------------------http状态码:{}", execute.getStatus());
        return body;
    }

    /**
     * megaPay-NextPos通道分发AD3
     *
     * @param map 参数
     */
    @Override
    public String nextPosCallbackAD3(Map<String, Object> map) {
        String ad3Url = ad3ParamsConfig.getNextPosUrl();
        log.info("----------------------megaPay-NexPos通道回调AD3信息记录----------------------分发AD3URL:{}", ad3Url);
        log.info("----------------------megaPay-NexPos通道回调AD3信息记录----------------------分发AD3参数:{}", JSON.toJSONString(map));
        //分发给AD3
        cn.hutool.http.HttpResponse execute = HttpRequest.get(ad3Url)
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(map)
                .timeout(20000)
                .execute();
        String body = execute.body();
        log.info("----------------------megaPay-NexPos通道回调AD3信息记录----------------------回调返回 body:{}", body);
        log.info("----------------------megaPay-NexPos通道回调AD3信息记录----------------------http状态码:{}", execute.getStatus());
        return body;
    }

    /**
     * 分发AD3
     *
     * @param obj 参数
     * @param url url
     */
    @Override
    public String callbackAD3(Object obj, String url) {
        String ad3Url = ad3ParamsConfig.getAd3ItsUrl().concat(url);
        log.info("----------------------回调信息分发AD3方法记录----------------------分发AD3URL:{}", ad3Url);
        log.info("----------------------回调信息分发AD3方法记录----------------------分发AD3参数:{}", JSON.toJSONString(obj));
        //分发给AD3
        cn.hutool.http.HttpResponse execute = HttpRequest.post(ad3Url)
                .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .form(BeanToMapUtil.beanToMap(obj))
                .timeout(20000)
                .execute();
        String body = execute.body();
        log.info("----------------------回调信息分发AD3方法记录----------------------回调返回 body:{}", body);
        log.info("----------------------回调信息分发AD3方法记录----------------------http状态码:{}", execute.getStatus());
        return body;
    }

    /**
     * 判断通道网关手续费收取状态
     *
     * @param channel 通道
     */
    @Override
    public Byte judgeChannelGatewayFee(Channel channel) {
        //check
        if (channel.getChannelGatewayRate() != null && channel.getChannelGatewayCharge() != null && channel.getChannelGatewayStatus() != null) {
            //通道网关手续费是否收取状态
            if (channel.getChannelGatewayCharge().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_YES)) {
                if (channel.getChannelGatewayStatus().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_ALL_STATUS)) {
                    //全收取
                    return TradeConstant.CHANNEL_GATEWAY_CHARGE_ALL_STATUS;
                } else if (channel.getChannelGatewayStatus().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_SUCCESS_STATUS)) {
                    //成功时收取
                    return TradeConstant.CHANNEL_GATEWAY_CHARGE_SUCCESS_STATUS;
                } else if (channel.getChannelGatewayStatus().equals(TradeConstant.CHANNEL_GATEWAY_CHARGE_FAILURE_STATUS)) {
                    //失败时收取
                    return TradeConstant.CHANNEL_GATEWAY_CHARGE_FAILURE_STATUS;
                }
            }
        }
        //不收取
        return TradeConstant.CHANNEL_GATEWAY_CHARGE_NOT_STATUS;
    }

    /**
     * 根据机构编号和币种从redis里获取账户信息
     *
     * @param institutionCode
     * @param currency
     * @return
     */
    @Override
    public Account getAccount(String institutionCode, String currency) {
        //根据机构code和币种获取账户信息
        Account account = JSON.parseObject(redisService.get(AsianWalletConstant.ACCOUNT_CACHE_KEY.concat("_").concat(institutionCode).concat("_").concat(currency)), Account.class);
        try {
            if (account == null) {
                //redis不存在,从数据库获取
                account = accountMapper.getAccount(institutionCode, currency);
                if (account == null) {
                    return null;
                }
                //同步redis
                redisService.set(AsianWalletConstant.ACCOUNT_CACHE_KEY.concat("_").concat(institutionCode).concat("_").concat(currency), JSON.toJSONString(account));
            }
        } catch (Exception e) {
            log.error("同步账户信息到redis里发生异常:", e.getMessage());
        }
        log.info("================== CommonService getAccount =================== account: {}", JSON.toJSONString(account));
        return account;
    }

    /**
     * 获取基础数据中的币种信息
     *
     * @param currency
     * @return
     */
    @Override
    public String getCurrency(String currency) {
        //币种code转大写
        String upCurrency = currency.toUpperCase();
        //从redis获取币种信息
        String dbCurrency = JSON.parseObject(redisService.get(AsianWalletConstant.CURRENCY_CACHE_KEY.concat("_").concat(upCurrency)), String.class);
        try {
            if (StringUtils.isEmpty(dbCurrency)) {
                //redis不存在,从数据库获取
                dbCurrency = dictionaryMapper.getCurrency(upCurrency);
                if (StringUtils.isEmpty(dbCurrency)) {
                    return null;
                }
                //同步redis
                redisService.set(AsianWalletConstant.CURRENCY_CACHE_KEY.concat("_").concat(upCurrency), JSON.toJSONString(dbCurrency));
            }
        } catch (Exception e) {
            log.error("获取基础数据中的币种信息同步到redis里发生异常:", e.getMessage());
        }
        return dbCurrency;
    }

    /**
     * 创建机构对应币种的账户
     *
     * @param institutionCode
     * @param currency
     */
    @Override
    public void createAccount(String institutionCode, String currency) {
        try {
            //添加账户
            SettleControl settleControl = new SettleControl();
            Account account = new Account();
            //账户id
            account.setAccountCode(IDS.uniqueID().toString());
            account.setInstitutionId(institutionCode);//机构code
            account.setInstitutionName(this.getInstitutionInfo(institutionCode).getCnName());//机构名称
            account.setCurrency(currency);//币种
            account.setId(IDS.uuid2());
            account.setSettleBalance(BigDecimal.ZERO);//默认结算金额为0
            account.setClearBalance(BigDecimal.ZERO);//默认清算金额为0
            account.setFreezeBalance(BigDecimal.ZERO);//默认冻结金额为0
            account.setEnabled(true);//默认是启用
            account.setCreateTime(new Date());//创建时间
            account.setCreator("sys");//创建人
            account.setRemark("下单时系统自动创建的对应订单币种的账户");
            settleControl.setAccountId(account.getId());
            settleControl.setId(IDS.uuid2());
            settleControl.setMinSettleAmount(BigDecimal.ZERO);
            settleControl.setSettleSwitch(false);
            settleControl.setCreateTime(new Date());
            settleControl.setEnabled(true);
            settleControl.setCreator("sys");
            settleControl.setRemark("下单时系统自动创建自动创建币种的结算控制信息");
            //账户创建成功,把账户信息存到reids里面
            if (accountMapper.insertSelective(account) > 0 && settleControlMapper.insertSelective(settleControl) > 0) {
                redisService.set(AsianWalletConstant.ACCOUNT_CACHE_KEY.concat("_").concat(institutionCode).concat("_").concat(currency), JSON.toJSONString(account));
            }
        } catch (Exception e) {
            log.error("下单时创建机构对应币种的账户发生异常:", e.getMessage());
        }
    }

    /**
     * 校验订单的币种与金额 是否符合币种默认值
     *
     * @param placeOrdersDTO
     * @return
     */
    @Override
    public boolean checkOrderCurrency(PlaceOrdersDTO placeOrdersDTO) {
        //当前币种的默认值
        String defaultValue = this.getCurrencyDefaultValue(placeOrdersDTO.getOrderCurrency());
        if (defaultValue == null) {
            throw new BusinessException(EResultEnum.PRODUCT_CURRENCY_NO_SUPPORT.getCode());
        }
        String orderValue = String.valueOf(placeOrdersDTO.getOrderAmount());
        int a = new StringBuilder(defaultValue).reverse().indexOf(".");
        int b = new StringBuilder(orderValue).reverse().indexOf(".");
        if (a >= b) {
            return true;
        }
        return false;
    }

    /**
     * 获取币种默认值
     *
     * @param currency
     * @return
     */
    @Override
    public String getCurrencyDefaultValue(String currency) {
        //当前币种的默认值
        String defaultValue = redisService.get(AsianWalletConstant.CURRENCY_DEFAULT + "_" + currency);
        try {
            if (StringUtils.isEmpty(defaultValue)) {
                defaultValue = dictionaryMapper.selectByCurrency(currency);
                if (StringUtils.isEmpty(defaultValue)) {
                    //币种默认值不存在
                    log.info("==================【币种默认值】获取失败================== tradeCurrency:{}", currency);
                    return defaultValue;
                }
                redisService.set(AsianWalletConstant.CURRENCY_DEFAULT + "_" + currency, defaultValue);
            }
        } catch (Exception e) {
            log.error("同步币种默认值到redis里发生异常:", e.getMessage());
        }
        log.info("================== CommonService getCurrencyDefaultValue =================== defaultValue: {}", defaultValue);
        return defaultValue;
    }
}

