package com.payment.trade.service.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.*;
import com.payment.common.redis.RedisService;
import com.payment.common.utils.IDS;
import com.payment.trade.dao.*;
import com.payment.trade.feign.MessageFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.service.AgentCalculateService;
import com.payment.trade.service.CommonService;
import com.payment.trade.vo.BasicsInfoVO;
import com.payment.trade.vo.CalcFeeVO;
import com.payment.trade.vo.CalcRateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.Date;


/**
 * @description: 分润计算服务
 * @author: YangXu
 * @create: 2019-08-22 16:25
 **/
@Service
@Transactional
@Slf4j
public class AgentCalculateServiceImpl implements AgentCalculateService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderPaymentMapper orderPaymentMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private ShareBenefitLogsMapper shareBenefitLogsMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InstitutionProductMapper institutionProductMapper;

    @Autowired
    private RedisService redisService;

    @Value("${custom.developer.email}")
    private String developerEmail;

    @Autowired
    private MessageFeign messageFeign;

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 插入分润流水
     **/
    @Override
    public void insertShareBenefitLogs(String orderId) {
        try {
            log.info("================== 插入分润流水 START =================== orderId: {}", orderId);
            // 查询代理商基础信息----1是收款单 2--付款单
            Byte type = 1;//默认是收款单
            if (orderId.startsWith("PO")) {//PO开头付款产品
                type = 2;
            }
            String agencyCode = null; //代理商户号
            String institutionCode = null; //机构号
            String institutionName = null; //机构名称
            Integer productCode = null; //产品code
            String orderCurrency = null; //订单币种
            String payoutCurrency = null; //汇款币种
            BigDecimal orderAmount = null; //订单金额
            BigDecimal payoutAmount = null; //汇款金额
            BigDecimal orderFee = null; //订单手续费
            if (type == 1) {
                //收款单的场合
                Orders orders = ordersMapper.selectById(orderId);
                agencyCode = orders.getAgencyCode();
                institutionCode = orders.getInstitutionCode();
                institutionName = orders.getInstitutionName();
                productCode = orders.getProductCode();
                orderCurrency = orders.getOrderCurrency();
                orderAmount = orders.getAmount();
                orderFee = orders.getFee();

            } else if (type == 2) {
                //付款单的场合
                OrderPayment orderPayment = orderPaymentMapper.selectByPrimaryKey(orderId);
                agencyCode = orderPayment.getExtend5();
                institutionCode = orderPayment.getInstitutionCode();
                institutionName = orderPayment.getInstitutionName();
                productCode = orderPayment.getProductCode();
                orderCurrency = orderPayment.getTradeCurrency();
                payoutCurrency = orderPayment.getPaymentCurrency();
                orderAmount = orderPayment.getTradeAmount();
                payoutAmount = orderPayment.getPaymentAmount();
                orderFee = orderPayment.getFee();
            }
            Institution agent = commonService.getInstitutionInfo(agencyCode);
            if (StringUtils.isEmpty(agent) || agent.getEnabled() == false) {
                log.info("================== 插入分润流水 订单对应的代理商异常 =================== agencyCode ：{} ", agencyCode);
                return;
            }
            //查询分润流水是否存在当前订单信息
            int count = shareBenefitLogsMapper.selectCountByOrderId(orderId);
            if (count > 0) {
                log.info("================== 插入分润流水 流水记录已存在 =================== orderId: {}", orderId);
                return;
            }
            BasicsInfoVO basicsInfoVO = this.getBaseInfo(agencyCode, productCode, type);
            if (basicsInfoVO.getInstitution() == null || basicsInfoVO.getProduct() == null || basicsInfoVO.getInstitutionProduct() == null) {
                log.info("================== 插入分润流水 代理商产品信息不全 =================== orderId: {}，basicsInfoVO：{}", orderId, JSON.toJSONString(basicsInfoVO));
                rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orderId);
                messageFeign.sendSimpleMail(developerEmail, "代理商产品信息不全 预警", "代理商商户号 ：{ " + agencyCode + " } 产品信息不全");//邮件通知
                return;
            }
            //创建流水对象
            ShareBenefitLogs shareBenefitLogs = this.creatShareBenefitLogs(orderId, institutionCode, institutionName, orderCurrency, orderAmount, basicsInfoVO, type);
            //算费模块
            InstitutionProduct institutionProduct = basicsInfoVO.getInstitutionProduct();
            CalcFeeVO calcFeeVO = this.calculateShareBenefit(orderAmount, payoutAmount, orderCurrency, payoutCurrency, orderFee, institutionProduct, shareBenefitLogs, type);
            if (calcFeeVO.getChargeStatus().equals(TradeConstant.CHARGE_STATUS_FALID)) {
                rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orderId);
                messageFeign.sendSimpleMail(developerEmail, "代理商产品算费失败 预警", "代理商商户号 ：{ " + agencyCode + " } 代理商产品算费失败");//邮件通知
                return;
            }
            //分润金额
            shareBenefitLogs.setShareBenefit(calcFeeVO.getFee().doubleValue());
            shareBenefitLogsMapper.insert(shareBenefitLogs);
        } catch (Exception e) {
            log.error("================== 插入分润流水异常 =================== orderId: {},Exception :{}", orderId, e);
            rabbitMQSender.send(AD3MQConstant.MQ_FR_DL, orderId);
            //回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 创建分润流水对象
     **/
    private ShareBenefitLogs creatShareBenefitLogs(String orderId, String institutionCode, String institutionName, String orderCurrency, BigDecimal orderAmount, BasicsInfoVO basicsInfoVO, Byte type) {
        ShareBenefitLogs shareBenefitLogs = new ShareBenefitLogs();
        shareBenefitLogs.setId("SL" + IDS.uniqueID());
        shareBenefitLogs.setOrderId(orderId);
        shareBenefitLogs.setInstitutionCode(institutionCode);
        shareBenefitLogs.setInstitutionName(institutionName);
        shareBenefitLogs.setAgentCode(basicsInfoVO.getInstitution().getInstitutionCode());
        shareBenefitLogs.setAgentName(basicsInfoVO.getInstitution().getCnName());
        //订单币种
        shareBenefitLogs.setTradeCurrency(orderCurrency);
        //订单金额
        shareBenefitLogs.setTradeAmount(orderAmount.doubleValue());
        //分润状态 1:待分润，2：已分润
        shareBenefitLogs.setIsShare(TradeConstant.SHARE_BENEFIT_WAIT);
        //分润模式 1-分成 2-费用差
        shareBenefitLogs.setDividedMode(basicsInfoVO.getInstitutionProduct().getDividedMode());
        //分润比例
        shareBenefitLogs.setDividedRatio(basicsInfoVO.getInstitutionProduct().getDividedRatio());
        //创建时间
        shareBenefitLogs.setCreateTime(new Date());
        //订单类型 1-收单 2-付款
        shareBenefitLogs.setExtend1(String.valueOf(type));
        return shareBenefitLogs;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 计算代理商分润
     **/
    private CalcFeeVO calculateShareBenefit(BigDecimal orderAmount, BigDecimal payoutAmount, String orderCurrency, String payoutCurrency, BigDecimal orderFee, InstitutionProduct institutionProduct, ShareBenefitLogs shareBenefitLogs, Byte type) {
        CalcFeeVO calcFeeVO = new CalcFeeVO();
        calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
        //单笔费率
        BigDecimal poundage = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        if (type == 1) {
            amount = orderAmount;
        } else if (type == 2) {
            amount = payoutAmount;
        }
        if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_RATE)) {
            //手续费=订单金额*单笔费率+附加值
            poundage = amount.multiply(institutionProduct.getRate());
            //判断手续费是否小于最小值，大于最大值
            if (institutionProduct.getMinTate() != null && poundage.compareTo(institutionProduct.getMinTate()) == -1) {
                poundage = institutionProduct.getMinTate();
            }
            if (institutionProduct.getMaxTate() != null && poundage.compareTo(institutionProduct.getMaxTate()) == 1) {
                poundage = institutionProduct.getMaxTate();
            }
        } else if (institutionProduct.getRateType().equals(TradeConstant.FEE_TYPE_QUOTA)) {//单笔定额
            //手续费=单笔定额值+附加值
            poundage = institutionProduct.getRate().add(institutionProduct.getAddValue());
        } else {
            log.info("================== 计算代理商分润 手续费模式异常 =================== calcFeeVO: {} ", JSON.toJSONString(calcFeeVO));
            return calcFeeVO;
        }
        //手续费
        shareBenefitLogs.setFee(poundage.doubleValue());

        //若是汇款单需要把代理商手续费换汇
        if (type == 2 && !payoutCurrency.equals(orderCurrency)) {
            CalcRateVO calcRateVO = commonService.calcExchangeRate(payoutCurrency, orderCurrency, BigDecimal.ZERO, poundage);
            if (calcFeeVO.getChargeStatus().equals(TradeConstant.SWAP_FALID)) {
                log.info("================== 计算代理商分润 汇款代理商手续费换费失败 =================== calcRateVO: {} ", JSON.toJSONString(calcRateVO));
            }
            poundage = calcRateVO.getTradeAmount();
        }
        //计算分润
        BigDecimal benefit = BigDecimal.ZERO;
        if (!StringUtils.isEmpty(institutionProduct.getDividedRatio())) {
            benefit = orderFee.subtract(poundage).multiply(institutionProduct.getDividedRatio());
        } else {
            log.info("================== 计算代理商分润 分润模式异常 =================== calcFeeVO: {} ", JSON.toJSONString(calcFeeVO));
            return calcFeeVO;
        }
        //算费成功
        calcFeeVO.setChargeStatus(TradeConstant.CHARGE_STATUS_SUCCESS);
        calcFeeVO.setFee(benefit);
        calcFeeVO.setChargeTime(new Date());
        log.info("================== 计算代理商分润 =================== calcFeeVO: {} ", JSON.toJSONString(calcFeeVO));
        return calcFeeVO;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/23
     * @Descripate 查询代理商产品信息
     **/
    private BasicsInfoVO getBaseInfo(String agencyCode, Integer productCode, Byte type) {
        BasicsInfoVO basicsInfoVO = new BasicsInfoVO();
        Institution agent = null;
        try {
            agent = commonService.getInstitutionInfo(agencyCode);
        } catch (Exception e) {
            log.info("================== 查询代理商信息为空 =================== agencyCode: {}", agencyCode);
            return basicsInfoVO;
        }
        basicsInfoVO.setInstitution(agent);
        //根据订单币种，机构id以及订单收付类型查询产品信息
        Product product = productMapper.selectByCurrencyAndCodeAndType(productCode, agent.getId(), type);
        if (product == null) {
            log.info("================== 查询代理商产品信息为空 =================== agencyCode: {}，productCode ：{}", agencyCode, productCode);
            return basicsInfoVO;
        }
        basicsInfoVO.setProduct(product);
        //查询机构产品信息,先从redis获取
        InstitutionProduct institutionProduct = JSON.parseObject(redisService.get(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(agent.getId().concat("_").concat(product.getId()))), InstitutionProduct.class);
        if (institutionProduct == null) {
            //redis不存在,从数据库获取
            institutionProduct = institutionProductMapper.selectByInstitutionIdAndProductId(agent.getId(), product.getId());
            if (institutionProduct == null || !institutionProduct.getEnabled()) {
                //机构产品通道信息不存在
                log.info("================== 查询代理商机构产品信息不存在或者已禁用 ==================   agencyCode: {}，productCode ：{}", agencyCode, productCode);
                return basicsInfoVO;
            }
            //同步redis
            redisService.set(AsianWalletConstant.INSTITUTIONPRODUCT_CACHE_KEY.concat("_").concat(agent.getId().concat("_").concat(product.getId())), JSON.toJSONString(institutionProduct));
        }
        basicsInfoVO.setInstitutionProduct(institutionProduct);
        log.info("================== 查询代理商产品信息 ==================   basicsInfoVO: {}", JSON.toJSONString(basicsInfoVO));
        return basicsInfoVO;
    }
}
