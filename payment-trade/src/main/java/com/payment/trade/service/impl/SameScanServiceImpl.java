package com.payment.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.payment.common.config.AuditorProvider;
import com.payment.common.constant.AD3Constant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.*;
import com.payment.common.exception.BusinessException;
import com.payment.common.redis.RedisService;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.common.utils.ArrayUtil;
import com.payment.common.utils.ReflexClazzUtils;
import com.payment.common.vo.OfflineOrdersVO;
import com.payment.common.vo.PosOrdersVO;
import com.payment.common.vo.SysUserVO;
import com.payment.trade.channels.ad3Offline.AD3Service;
import com.payment.trade.dao.*;
import com.payment.trade.dto.PlaceOrdersDTO;
import com.payment.trade.dto.PosGetOrdersDTO;
import com.payment.trade.dto.TerminalQueryOrdersDTO;
import com.payment.trade.dto.TerminalQueryRelevantDTO;
import com.payment.trade.service.CommonService;
import com.payment.trade.service.SameScanService;
import com.payment.trade.utils.AbstractHandler;
import com.payment.trade.utils.HandlerContext;
import com.payment.trade.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;


/***
 *
 *
 *                                                    __----~~~~~~~~~~~------___
 *                                   .  .   ~~//====......          __--~ ~~
 *                   -.            \_|//     |||\\  ~~~~~~::::... /~
 *                ___-==_       _-~o~  \/    |||  \\            _/~~-
 *        __---~~~.==~||\=_    -_--~/_-~|-   |\\   \\        _/~
 *    _-~~     .=~    |  \\-_    '-~7  /-   /  ||    \      /
 *  .~       .~       |   \\ -_    /  /-   /   ||      \   /
 * /  ____  /         |     \\ ~-_/  /|- _/   .||       \ /
 * |~~    ~~|--~~~~--_ \     ~==-/   | \~--===~~        .\
 *          '         ~-|      /|    |-~\~~       __--~~
 *                      |-~~-_/ |    |   ~\_   _-~            /\
 *                           /  \     \__   \/~                \__
 *                       _--~ _/ | .-~~____--~-/                  ~~==.
 *                      ((->/~   '.|||' -_|    ~~-/ ,              . _||
 *                                 -_     ~\      ~~---l__i__i__i--~~_/
 *                                 _-~-__   ~)  \--______________--~~
 *                               //.-~~~-~_--~- |-------~~~~~~~~
 *                                      //.-~~~--\
 *                               神兽保佑
 *                               永无BUG!
 */


/**
 * @Author XuWenQi
 * @Date 2019/2/12 15:26
 * @Descripate 线下同机构动态扫码业务接口实现类
 */


@Service
@Slf4j
public class SameScanServiceImpl implements SameScanService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private AD3Service ad3Service;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private InstitutionMapper institutionMapper;

    @Autowired
    private DeviceBindingMapper deviceBindingMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AuditorProvider auditorProvider;

    @Autowired
    private HandlerContext handlerContext;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    /**
     * 线下同机构CSB动态扫码
     *
     * @param placeOrdersDTO 下单实体
     * @return 通用响应实体
     */
    @Override
    @Transactional
    public BaseResponse csbScan(PlaceOrdersDTO placeOrdersDTO) {
        log.info("==================【线下CSB】下单信息记录==================【请求参数】 placeOrdersDTO: {}", JSON.toJSONString(placeOrdersDTO));
        //非空check
        if (StringUtils.isEmpty(placeOrdersDTO.getTerminalId())) {
            //设备编号
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getOperatorId())) {
            //设备操作员
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getProductCode())) {
            //产品编号
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getToken())) {
            //token
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //机构编号
        String institutionCode = placeOrdersDTO.getInstitutionId();
        //订单币种
        String orderCurrency = placeOrdersDTO.getOrderCurrency();
        //订单金额
        BigDecimal amount = placeOrdersDTO.getOrderAmount();
        //重复请求check
        if (!commonService.repeatedRequests(institutionCode, placeOrdersDTO.getOrderNo())) {
            log.info("==================【线下CSB】下单信息记录==================【重复请求】");
            throw new BusinessException(EResultEnum.REPEAT_ORDER_REQUEST.getCode());
        }
        //校验订单金额
        if (!commonService.checkOrderCurrency(placeOrdersDTO)) {
            log.info("==================【线下CSB】下单信息记录==================【订单金额不符合的当前币种默认值】");
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        //校验设备,签名信息
        checkDeviceAndSign(placeOrdersDTO);
        //校验token信息
        SysUserVO sysUserVO = JSON.parseObject(redisService.get(placeOrdersDTO.getToken()), SysUserVO.class);
        if (sysUserVO == null || !sysUserVO.getUsername().equals(institutionCode.concat(placeOrdersDTO.getOperatorId()))) {
            log.info("==================【线下CSB】下单信息记录==================【Token不合法】");
            //token不合法
            throw new BusinessException(EResultEnum.TOKEN_IS_INVALID.getCode());
        }
        //查询机构产品通道信息
        BasicsInfoVO basicsInfo = commonService.getBasicsInfo(placeOrdersDTO, TradeConstant.TRADE_UPLINE);
        //机构产品信息
        InstitutionProduct institutionProduct = basicsInfo.getInstitutionProduct();
        //通道信息
        Channel channel = basicsInfo.getChannel();
        //校验订单
        commonService.checkOrder(placeOrdersDTO, basicsInfo);
        //设置订单属性
        Orders orders = commonService.setAttributes(placeOrdersDTO, basicsInfo);
        //线下
        orders.setTradeDirection(TradeConstant.TRADE_UPLINE);
        //通道币种
        String channelCurrency = channel.getCurrency();
        //响应实体
        BaseResponse baseResponse = new BaseResponse();
        //判断订单币种,在对应通道中是否支持
        if (!orderCurrency.equals(channelCurrency)) {
            //换汇
            CalcRateVO calcRateVO = commonService.calcExchangeRate(orderCurrency, channelCurrency, institutionProduct.getFloatRate(), amount);
            log.info("==================【线下CSB】下单信息记录==================【换汇信息】记录 calcRateVO:{}", JSON.toJSONString(calcRateVO));
            //换汇时间
            orders.setExchangeTime(calcRateVO.getExchangeTime());
            //换汇状态
            orders.setExchangeStatus(calcRateVO.getExchangeStatus());
            //判断换汇状态
            if (TradeConstant.SWAP_FALID.equals(calcRateVO.getExchangeStatus())) {
                log.info("==================【线下CSB】下单信息记录==================【换汇失败】");
                //交易币种
                orders.setTradeCurrency(channelCurrency);
                //支付失败
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                //备注
                orders.setRemark("未查询到币种汇率，本位币种：" + orderCurrency + " 目标币种：" + channelCurrency);
                //订单落地
                ordersMapper.insert(orders);
                //换汇失败
                baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
                return baseResponse;
            }
            //换汇汇率
            orders.setExchangeRate(calcRateVO.getExchangeRate());
            //交易币种
            orders.setTradeCurrency(channelCurrency);
            //交易金额
            orders.setTradeAmount(calcRateVO.getTradeAmount());
            //原始汇率
            orders.setCommodityName(String.valueOf(calcRateVO.getOriginalRate()));
        } else {
            //未换汇
            log.info("==================【线下CSB】下单信息记录==================【订单不需要换汇】");
            //换汇汇率
            orders.setExchangeRate(new BigDecimal(1));
            //交易币种
            orders.setTradeCurrency(orderCurrency);
            //交易金额
            orders.setTradeAmount(amount.setScale(2, BigDecimal.ROUND_HALF_UP));
            //原始汇率
            orders.setCommodityName("1");
        }
        //下单业务信息校验
        if (!StringUtils.isEmpty(commonService.checkPlaceOrder(orders, basicsInfo, baseResponse).getCode())) {
            //错误信息不为空则返回
            return baseResponse;
        }
        //计算手续费
        CalcFeeVO calcFeeVO = commonService.calcPoundage(orders, basicsInfo);
        log.info("==================【线下CSB】下单信息记录==================【计算手续费】 calcFeeVO:{}", JSON.toJSONString(calcFeeVO));
        //计费时间
        orders.setChargeTime(calcFeeVO.getChargeTime());
        //计费状态
        orders.setChargeStatus(TradeConstant.CHARGE_STATUS_FALID);
        //判断计费状态
        if (TradeConstant.CHARGE_STATUS_FALID.equals(calcFeeVO.getChargeStatus())) {
            log.info("==================【线下CSB】下单信息记录==================【计算手续费失败】");
            //支付失败
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //备注
            orders.setRemark("计费失败");
            ordersMapper.insert(orders);
            baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
            return baseResponse;
        }
        //手续费
        orders.setFee(calcFeeVO.getFee());
        //计算通道手续费用
        if (channel.getChannelRate() != null) {
            CalcFeeVO channelPoundage = commonService.calcChannelPoundage(amount, channel);
            log.info("==================【线下CSB】下单信息记录==================【计算通道手续费】 channelPoundage:{}", JSON.toJSONString(channelPoundage));
            if (channelPoundage.getChargeStatus().equals(TradeConstant.CHARGE_STATUS_FALID)) {
                log.info("==================【线下CSB】下单信息记录==================【计算通道手续费失败】");
                //支付失败
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                //备注
                orders.setRemark("通道手续费计费失败");
                ordersMapper.insert(orders);
                baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
                return baseResponse;
            }
            //通道手续费
            orders.setChannelFee(channelPoundage.getFee());
        }
        //判断通道网关手续费收取状态
        if (TradeConstant.CHANNEL_GATEWAY_CHARGE_ALL_STATUS.equals(commonService.judgeChannelGatewayFee(channel))) {
            //全收取
            if (commonService.calcGatewayFee(channel, orders, baseResponse)) {
                log.info("==================【线下CSB】下单信息记录==================【计算通道网关手续费失败】");
                return baseResponse;
            }
        }
        //上报通道时间
        orders.setReportChannelTime(new Date());
        //交易状态--付款中
        orders.setTradeStatus(TradeConstant.ORDER_PAYING);
        //创建订单
        log.info("==================【线下CSB】下单信息记录==================【落地订单信息】 orders:{}", JSON.toJSONString(orders));
        ordersMapper.insert(orders);
        //判断通道
        AbstractHandler handler;
        try {
            handler = handlerContext.getInstance(channel.getChannelEnName());
        } catch (Exception e) {
            log.info("==================【线下CSB】下单信息记录==================【通道服务名称】不匹配 channelEnName: {}", channel.getChannelEnName());
            baseResponse.setCode(EResultEnum.CHANNEL_SERVICE_NAME_NO_MATCH.getCode());
            return baseResponse;
        }
        try {
            baseResponse = handler.offlineCSB(orders, channel, baseResponse);
        } catch (Exception e) {
            log.info("==================【线下CSB】下单信息记录==================【调用CSB方法异常】", e);
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        //CODE不为空,返回错误信息
        if (!StringUtils.isEmpty(baseResponse.getCode())) {
            return baseResponse;
        }
        //返回结果的数据实体
        OrdersPosCSBVO ordersVO = new OrdersPosCSBVO();
        //eNets标记解码
        if (AD3Constant.ENETS.equalsIgnoreCase(channel.getIssuerId())) {
            //eNets需要pos机解码的标志
            ordersVO.setDecodeFlag("1");
        }
        //机构订单号
        ordersVO.setInstitutionOrderId(placeOrdersDTO.getOrderNo());
        //二维码URL
        ordersVO.setUrl(String.valueOf(baseResponse.getData()));
        baseResponse.setData(ordersVO);
        log.info("==================【线下CSB】下单信息记录==================【下单结束】");
        return baseResponse;
    }


    /**
     * 线下同机构BSC动态扫码
     *
     * @param placeOrdersDTO 下单实体
     * @return 通用响应实体
     */
    @Override
    @Transactional
    public BaseResponse bscScan(PlaceOrdersDTO placeOrdersDTO) {
        log.info("==================【线下BSC】下单信息记录==================【请求参数】 placeOrdersDTO: {}", JSON.toJSONString(placeOrdersDTO));
        //非空check
        if (StringUtils.isEmpty(placeOrdersDTO.getProductCode())) {
            //产品编号
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getAuthCode())) {
            //付款码
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getTerminalId())) {
            //设备编号
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getOperatorId())) {
            //设备操作员
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (StringUtils.isEmpty(placeOrdersDTO.getToken())) {
            //token
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //机构编号
        String institutionCode = placeOrdersDTO.getInstitutionId();
        //订单金额
        BigDecimal amount = placeOrdersDTO.getOrderAmount();
        //订单币种
        String orderCurrency = placeOrdersDTO.getOrderCurrency();
        //校验订单金额
        if (!commonService.checkOrderCurrency(placeOrdersDTO)) {
            log.info("==================【线下BSC】下单信息记录==================【订单金额不符合的当前币种默认值】");
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        //重复请求check
        if (!commonService.repeatedRequests(institutionCode, placeOrdersDTO.getOrderNo())) {
            log.info("==================【线下BSC】下单信息记录==================【重复请求】");
            throw new BusinessException(EResultEnum.REPEAT_ORDER_REQUEST.getCode());
        }
        //校验设备,签名信息
        checkDeviceAndSign(placeOrdersDTO);
        //校验token信息
        SysUserVO sysUserVO = JSON.parseObject(redisService.get(placeOrdersDTO.getToken()), SysUserVO.class);
        if (sysUserVO == null || !sysUserVO.getUsername().equals(institutionCode.concat(placeOrdersDTO.getOperatorId()))) {
            log.info("==================【线下BSC】下单信息记录==================【Token】不合法");
            throw new BusinessException(EResultEnum.TOKEN_IS_INVALID.getCode());
        }
        //查询机构产品通道信息
        BasicsInfoVO basicsInfo = commonService.getBasicsInfo(placeOrdersDTO, TradeConstant.TRADE_UPLINE);
        //机构产品信息
        InstitutionProduct institutionProduct = basicsInfo.getInstitutionProduct();
        //通道信息
        Channel channel = basicsInfo.getChannel();
        log.info("==================【线下BSC】下单信息记录==================JSON格式化后的【通道信息】记录 channel: {}", JSON.toJSONString(channel));
        //校验订单
        commonService.checkOrder(placeOrdersDTO, basicsInfo);
        //设置订单属性
        Orders orders = commonService.setAttributes(placeOrdersDTO, basicsInfo);
        //线下
        orders.setTradeDirection(TradeConstant.TRADE_UPLINE);
        //通道币种
        String channelCurrency = channel.getCurrency();
        //响应实体
        BaseResponse baseResponse = new BaseResponse();
        //判断订单币种,在对应通道中是否支持
        if (!orderCurrency.equals(channelCurrency)) {
            //换汇
            CalcRateVO calcRateVO = commonService.calcExchangeRate(orderCurrency, channelCurrency, institutionProduct.getFloatRate(), amount);
            log.info("==================【线下BSC】下单信息记录==================【换汇信息】记录 calcRateVO:{}", JSON.toJSONString(calcRateVO));
            //换汇时间
            orders.setExchangeTime(calcRateVO.getExchangeTime());
            //换汇状态
            orders.setExchangeStatus(calcRateVO.getExchangeStatus());
            //判断换汇状态
            if (TradeConstant.SWAP_FALID.equals(calcRateVO.getExchangeStatus())) {
                log.info("==================【线下BSC】下单信息记录==================【换汇失败】");
                //交易币种
                orders.setTradeCurrency(channelCurrency);
                //支付失败
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                //备注
                orders.setRemark("未查询到币种汇率，本位币种：" + orderCurrency + " 目标币种：" + channelCurrency);
                //订单落地
                ordersMapper.insert(orders);
                //换汇失败
                baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
                return baseResponse;
            }
            //换汇汇率
            orders.setExchangeRate(calcRateVO.getExchangeRate());
            //交易币种
            orders.setTradeCurrency(channelCurrency);
            //交易金额
            orders.setTradeAmount(calcRateVO.getTradeAmount());
            //原始汇率
            orders.setCommodityName(calcRateVO.getOriginalRate().toString());
        } else {
            log.info("==================【线下BSC】下单信息记录==================【订单不需要换汇】");
            //换汇汇率
            orders.setExchangeRate(new BigDecimal(1));
            //交易币种
            orders.setTradeCurrency(orderCurrency);
            //交易金额
            orders.setTradeAmount(amount.setScale(2, BigDecimal.ROUND_HALF_UP));
            //原始汇率
            orders.setCommodityName("1");
        }
        //下单业务信息校验
        if (!StringUtils.isEmpty(commonService.checkPlaceOrder(orders, basicsInfo, baseResponse).getCode())) {
            //错误信息不为空则返回
            return baseResponse;
        }
        //计算手续费
        CalcFeeVO calcFeeVO = commonService.calcPoundage(orders, basicsInfo);
        log.info("==================【线下BSC】下单信息记录==================【计算手续费】 CalcFeeVO:{}", JSON.toJSONString(calcFeeVO));
        //计费时间
        orders.setChargeTime(calcFeeVO.getChargeTime());
        //计费状态
        orders.setChargeStatus(calcFeeVO.getChargeStatus());
        //判断计费状态
        if (TradeConstant.CHARGE_STATUS_FALID.equals(calcFeeVO.getChargeStatus())) {
            log.info("==================【线下BSC】下单信息记录==================【计算手续费失败】");
            //支付失败
            orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
            //备注
            orders.setRemark("计费失败");
            ordersMapper.insert(orders);
            baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
            return baseResponse;
        }
        //手续费
        orders.setFee(calcFeeVO.getFee());
        //计算通道手续费用
        if (channel.getChannelRate() != null) {
            CalcFeeVO channelPoundage = commonService.calcChannelPoundage(amount, channel);
            log.info("==================【线下BSC】下单信息记录==================【计算通道手续费】 CalcFeeVO:{}", JSON.toJSONString(channelPoundage));
            if (channelPoundage.getChargeStatus().equals(TradeConstant.CHARGE_STATUS_FALID)) {
                log.info("==================【线下BSC】下单信息记录==================【计算通道手续费失败】");
                //支付失败
                orders.setTradeStatus(TradeConstant.ORDER_PAY_FAILD);
                //备注
                orders.setRemark("通道手续费计费失败");
                ordersMapper.insert(orders);
                baseResponse.setCode(EResultEnum.SYS_ERROR_CREATE_ORDER_FAIL.getCode());
                return baseResponse;
            }
            //通道手续费
            orders.setChannelFee(channelPoundage.getFee());
        }
        //判断通道网关手续费收取状态
        if (TradeConstant.CHANNEL_GATEWAY_CHARGE_ALL_STATUS.equals(commonService.judgeChannelGatewayFee(channel))) {
            //全收取时
            if (commonService.calcGatewayFee(channel, orders, baseResponse)) {
                log.info("==================【线下BSC】下单信息记录==================【计算通道网关手续费失败】");
                return baseResponse;
            }
        }
        //上报通道时间
        orders.setReportChannelTime(new Date());
        //交易状态--付款中
        orders.setTradeStatus(TradeConstant.ORDER_PAYING);
        //创建订单
        log.info("==================【线下BSC】下单信息记录==================【落地订单信息】 orders:{}", JSON.toJSONString(orders));
        ordersMapper.insert(orders);
        //判断通道
        AbstractHandler handler;
        try {
            handler = handlerContext.getInstance(channel.getChannelEnName());
        } catch (Exception e) {
            log.info("==================【线下BSC】下单信息记录==================【通道服务名称】不匹配 channelEnName: {}", channel.getChannelEnName());
            baseResponse.setCode(EResultEnum.CHANNEL_SERVICE_NAME_NO_MATCH.getCode());
            return baseResponse;
        }
        try {
            baseResponse = handler.offlineBSC(orders, channel, baseResponse, placeOrdersDTO.getAuthCode());
        } catch (Exception e) {
            log.info("==================【线下BSC】下单信息记录==================【调用BSC方法异常】", e);
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        //CODE不为空,返回错误信息
        if (!StringUtils.isEmpty(baseResponse.getCode())) {
            return baseResponse;
        }
        OrdersPosBSCVO ordersVO = new OrdersPosBSCVO();
        //机构编号
        ordersVO.setInstitutionId(placeOrdersDTO.getInstitutionId());
        //机构订单号
        ordersVO.setOrderNo(placeOrdersDTO.getOrderNo());
        //系统流水号
        ordersVO.setReferenceNo(orders.getId());
        //订单币种
        ordersVO.setOrderCurrency(placeOrdersDTO.getOrderCurrency());
        //订单金额
        ordersVO.setOrderAmount(placeOrdersDTO.getOrderAmount());
        //机构订单时间
        ordersVO.setOrderTime(placeOrdersDTO.getOrderTime());
        //订单状态
        ordersVO.setTxnstatus(orders.getTradeStatus());
        //设备编号
        ordersVO.setTerminalId(placeOrdersDTO.getTerminalId());
        //操作员ID
        ordersVO.setOperatorId(placeOrdersDTO.getOperatorId());
        //响应实体
        baseResponse.setData(ordersVO);
        log.info("==================【线下BSC】下单信息记录==================【下单结束】");
        return baseResponse;
    }


    /**
     * 线下分页查询订单列表
     *
     * @param posGetOrdersDTO 查询订单输入实体
     * @return 订单输出实体集合
     */
    @Override
    public List<OfflineOrdersVO> terminalQueryOrderList(PosGetOrdersDTO posGetOrdersDTO) {
        log.info("==================【线下查询订单】==================【请求参数】 posGetOrdersDTO: {}", JSON.toJSONString(posGetOrdersDTO));
        //校验设备,签名信息
        checkDeviceAndSign(posGetOrdersDTO);
        //页码默认为1
        if (posGetOrdersDTO.getPageNum() == null) {
            posGetOrdersDTO.setPageNum(1);
        }
        //每页默认30
        if (posGetOrdersDTO.getPageSize() == null) {
            posGetOrdersDTO.setPageSize(30);
        }
        //分页查询订单
        List<OfflineOrdersVO> offlineOrdersVOS = ordersMapper.pageOfflineOrdersInfo(posGetOrdersDTO);
        log.info("==================【线下查询订单】==================【响应参数】 offlineOrdersVOS: {}", JSON.toJSONString(offlineOrdersVOS));
        return offlineOrdersVOS;
    }

    /**
     * 校验设备,签名信息
     *
     * @param obj 对象
     * @return
     */
    private void checkDeviceAndSign(Object obj) {
        //获取属性名,属性值
        Map<String, Object> map = ReflexClazzUtils.getFieldNames(obj);
        String institutionCode = String.valueOf(map.get("institutionId"));
        String deviceCode = String.valueOf(map.get("terminalId"));
        String deviceOperator = String.valueOf(map.get("operatorId"));
        //查询机构绑定设备
        DeviceBinding deviceBinding = deviceBindingMapper.selectByInstitutionCodeAndImei(institutionCode, deviceCode);
        if (deviceBinding == null) {
            log.info("-----------------【线下业务接口】信息记录--------------【设备编号不合法】");
            //设备编号不合法
            throw new BusinessException(EResultEnum.DEVICE_CODE_INVALID.getCode());
        }
        //查询设备操作员
        SysUser sysUser = sysUserMapper.selectByInstitutionCodeAndUserName(institutionCode.concat(deviceOperator));
        if (sysUser == null) {
            log.info("-----------------【线下业务接口】信息记录--------------【设备操作员不合法】");
            //设备操作员不合法
            throw new BusinessException(EResultEnum.DEVICE_OPERATOR_INVALID.getCode());
        }
        //验签
        if (!commonService.checkOnlineSignMsgUseMD5(obj)) {
            log.info("-----------------【线下业务接口】信息记录--------------【签名不匹配】");
            throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
        }
    }

    /**
     * 【内部接口-POS机】查询订单详情
     *
     * @param terminalQueryDTO 订单输入实体
     * @return 订单输出实体
     */
    @Override
    public PosOrdersVO terminalQueryOrderDetail(TerminalQueryOrdersDTO terminalQueryDTO) {
        log.info("===================【POS机查询订单详情】信息记录===================【参数记录】 terminalQueryDTO: {}", JSON.toJSONString(terminalQueryDTO));
        //校验设备,签名信息
        checkDeviceAndSign(terminalQueryDTO);
        if (StringUtils.isEmpty(terminalQueryDTO.getLanguage())) {
            terminalQueryDTO.setLanguage(auditorProvider.getLanguage());//语言
        }
        PosOrdersVO posOrdersVO = ordersMapper.terminalQueryOrderDetail(terminalQueryDTO);
        if (posOrdersVO == null) {
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        //根据币种默认值设置订单金额
        String defaultValue = posOrdersVO.getDefaultValue();
        //得到.对应的索引值
        int bitPos = defaultValue.indexOf(".");
        int numOfBits;
        if (bitPos == -1) {
            //币种默认值不包含. 默认为0
            numOfBits = 0;
        } else {
            numOfBits = defaultValue.length() - bitPos - 1;
        }
        posOrdersVO.setOrderAmount(String.valueOf(posOrdersVO.getAmount().setScale(numOfBits, BigDecimal.ROUND_DOWN)));
        if (posOrdersVO.getPayTypeName().contains("-")) {
            posOrdersVO.setPayTypeName(posOrdersVO.getPayTypeName().substring(0, posOrdersVO.getPayTypeName().indexOf("-")));
        } else if (posOrdersVO.getPayTypeName().contains("CSB")) {
            posOrdersVO.setPayTypeName(posOrdersVO.getPayTypeName().substring(0, posOrdersVO.getPayTypeName().indexOf("C")));
        } else if (posOrdersVO.getPayTypeName().contains("BSC")) {
            posOrdersVO.setPayTypeName(posOrdersVO.getPayTypeName().substring(0, posOrdersVO.getPayTypeName().indexOf("B")));
        }
        return posOrdersVO;
    }

    /**
     * 【内部接口-POS机】查询机构产品信息
     *
     * @param terminalQueryDTO 输入实体
     * @return 订单实体
     */
    @Override
    public OfflineRelevantInfoVO terminalQueryRelevantInfo(TerminalQueryRelevantDTO terminalQueryDTO) {
        log.info("===================【POS机查询机构产品】信息记录===================【参数记录】 terminalQueryDTO: {}", JSON.toJSONString(terminalQueryDTO));
        //校验设备,签名信息
        checkDeviceAndSign(terminalQueryDTO);
        //查询AW支持币种
        List<CurrencyVO> currencies = dictionaryMapper.selectDictionaryLists(AsianWalletConstant.CURRENCY_CODE);//币种
        if (ArrayUtil.isEmpty(currencies)) {
            log.info("===================【POS机查询机构产品】信息记录===================【获取币种信息异常】");
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        //查询机构产品信息
        List<OfflineProductVO> offlineProductVOS = institutionMapper.selectProductInfo(terminalQueryDTO.getInstitutionId(), TradeConstant.PRODUCT_UPLINE_MOVE, auditorProvider.getLanguage(), terminalQueryDTO.getDealType());
        if (ArrayUtil.isEmpty(offlineProductVOS)) {
            log.info("===================【POS机查询机构产品】信息记录===================【获取机构关联产品信息异常】");
            throw new BusinessException(EResultEnum.INSTITUTIONAL_PRODUCTS_DO_NOT_EXIST.getCode());
        }
        //排序机构产品集合
        LinkedList<OfflineProductVO> sortProductList = new LinkedList<>();
        for (OfflineProductVO productVO : offlineProductVOS) {
            //截取不包含带(-,CSB,BSC)的支付方式名称
            if (productVO.getPayTypeName().contains("-")) {
                productVO.setPayTypeName(productVO.getPayTypeName().substring(0, productVO.getPayTypeName().indexOf("-")));
            } else if (productVO.getPayTypeName().contains("BSC")) {
                productVO.setPayTypeName(productVO.getPayTypeName().substring(0, productVO.getPayTypeName().indexOf("B")));
            } else if (productVO.getPayTypeName().contains("CSB")) {
                productVO.setPayTypeName(productVO.getPayTypeName().substring(0, productVO.getPayTypeName().indexOf("C")));
            }
            //将BSC的产品放在集合前面
            if ("BSC".equals(productVO.getFlag())) {
                sortProductList.addFirst(productVO);
            } else {
                //将CSB的产品放在集合后面
                sortProductList.addLast(productVO);
            }
        }
        //响应实体
        OfflineRelevantInfoVO offlineRelevantInfoVO = new OfflineRelevantInfoVO();
        //支持币种
        offlineRelevantInfoVO.setCurrencys(currencies);
        //机构产品信息
        offlineRelevantInfoVO.setOfflineProductVOS(sortProductList);
        return offlineRelevantInfoVO;
    }


    /**
     * 【内部接口-POS机】分页查询订单列表
     *
     * @param posGetOrdersDTO 订单输入实体
     * @return
     */
    @Override
    public List<PosOrdersVO> posQueryOrderList(PosGetOrdersDTO posGetOrdersDTO) {
        log.info("===================【POS机分页查询订单列表】信息记录===================【参数记录】 posGetOrdersDTO: {}", JSON.toJSONString(posGetOrdersDTO));
        //校验设备,签名信息
        checkDeviceAndSign(posGetOrdersDTO);
        //页码默认为1
        if (posGetOrdersDTO.getPageNum() == null) {
            posGetOrdersDTO.setPageNum(1);
        }
        //每页默认30
        if (posGetOrdersDTO.getPageSize() == null) {
            posGetOrdersDTO.setPageSize(30);
        }
        if (StringUtils.isEmpty(posGetOrdersDTO.getLanguage())) {
            //语言
            posGetOrdersDTO.setLanguage(auditorProvider.getLanguage());
        }
        List<PosOrdersVO> posOrdersVOS = ordersMapper.pagePosGetOrdersInfo(posGetOrdersDTO);
        for (PosOrdersVO posOrdersVO : posOrdersVOS) {
            if (posOrdersVO.getDefaultValue() != null) {
                //根据币种默认值设置订单金额
                String defaultValue = posOrdersVO.getDefaultValue();
                //得到.对应的索引值
                int bitPos = defaultValue.indexOf(".");
                int numOfBits;
                if (bitPos == -1) {
                    //币种默认值不包含. 默认为0
                    numOfBits = 0;
                } else {
                    numOfBits = defaultValue.length() - bitPos - 1;
                }
                posOrdersVO.setOrderAmount(String.valueOf(posOrdersVO.getAmount().setScale(numOfBits, BigDecimal.ROUND_DOWN)));
            }
            //截取支付方式名称
            if (StringUtils.isEmpty(posOrdersVO.getPayTypeName())) {
                posOrdersVO.setPayTypeName("");
            } else {
                if (posOrdersVO.getPayTypeName().contains("-")) {
                    posOrdersVO.setPayTypeName(posOrdersVO.getPayTypeName().substring(0, posOrdersVO.getPayTypeName().indexOf("-")));
                } else if (posOrdersVO.getPayTypeName().contains("CSB")) {
                    posOrdersVO.setPayTypeName(posOrdersVO.getPayTypeName().substring(0, posOrdersVO.getPayTypeName().indexOf("C")));
                } else if (posOrdersVO.getPayTypeName().contains("BSC")) {
                    posOrdersVO.setPayTypeName(posOrdersVO.getPayTypeName().substring(0, posOrdersVO.getPayTypeName().indexOf("B")));
                }
            }
        }
        return posOrdersVOS;
    }

    /**
     * 【内部接口-POS机】查询订单状态接口
     *
     * @param terminalQueryDTO 查询订单输入实体
     * @return 终端订单输出实体
     */
    @Override
    public TerminalOrderVO terminalQueryOrderStatus(TerminalQueryOrdersDTO terminalQueryDTO) {
        log.info("===================【POS机查询订单状态】信息记录===================【参数记录】 terminalQueryDTO: {}", JSON.toJSONString(terminalQueryDTO));
        //校验设备,签名信息
        checkDeviceAndSign(terminalQueryDTO);
        //根据机构订单号查询订单
        Orders orders = ordersMapper.selectByInstitutionOrderId(terminalQueryDTO.getOrderNo());
        if (orders == null) {
            log.info("===================【POS机查询订单状态】信息记录===================【原始订单信息不存在】");
            //订单不存在
            throw new BusinessException(EResultEnum.ORDER_NOT_EXIST.getCode());
        }
        TerminalOrderVO terminalOrderVO = new TerminalOrderVO();//终端查询返回实体
        terminalOrderVO.setOrderNo(orders.getInstitutionOrderId());//机构订单号
        terminalOrderVO.setTxnstatus(orders.getTradeStatus());//交易状态
        //不是付款中的订单直接返回
        if (!orders.getTradeStatus().equals(TradeConstant.ORDER_PAYING)) {
            return terminalOrderVO;
        }
        //获取通道信息
        Channel channel = commonService.getChannelByChannelCode(orders.getChannelCode());
        //判断哪条通道
        if (channel.getChannelEnName().equals(AD3Constant.AD3_OFFLINE)) {
            //调用ad3单笔订单查询接口
            terminalOrderVO = ad3Service.ad3TerminalQueryOrder(terminalOrderVO, orders);
        }
        return terminalOrderVO;
    }


    /**
     * 机构分配通道查询关联关系
     *
     * @param institutionCode 机构code
     * @return 订单实体
     */
    @Override
    public List<InstitutionRelevantVO> getRelevantInfo(String institutionCode) {
        log.info("===================【收银台查询机构通道】信息记录===================【参数记录】 institutionCode: {}", institutionCode);
        Institution institution = commonService.getInstitutionInfo(institutionCode);
        //判断机构是否已经禁用
        if (!institution.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//机构已经禁用
        }
        //查询机构关联产品,产品关联通道,机构关联通道信息
        InstitutionRelevantVO institutionVO1 = institutionMapper.getRelevantByInstitutionCode(institutionCode, auditorProvider.getLanguage());
        //查询机构关联产品,产品关联通道信息
        InstitutionRelevantVO institutionVO2 = institutionMapper.getNoRelevantByInstitutionCode(institutionCode, auditorProvider.getLanguage());
        List<InstitutionRelevantVO> list = new ArrayList<>();
        list.add(institutionVO1);
        list.add(institutionVO2);
        return list;
    }


    /**
     * 【内部接口-收银台】根据机构code查询机构产品,产品通道,机构通道信息
     *
     * @param institutionCode 机构code
     * @return 订单实体
     */
    @Override
    public List<InstitutionRelevantVO> getRelevantInfoSy(String institutionCode) {
        log.info("===================【收银台查询机构通道】信息记录===================【参数记录】 institutionCode: {}", institutionCode);
        Institution institution = commonService.getInstitutionInfo(institutionCode);
        //判断机构是否已经禁用
        if (!institution.getEnabled()) {
            //机构已经禁用
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());
        }
        //查询机构关联产品,产品关联通道,机构关联通道信息
        InstitutionRelevantVO institutionVO1 = institutionMapper.getRelevantByInstitutionCodeSy(institutionCode, auditorProvider.getLanguage());
        //查询机构关联产品,产品关联通道信息
        InstitutionRelevantVO institutionVO2 = institutionMapper.getNoRelevantByInstitutionCode(institutionCode, auditorProvider.getLanguage());
        List<InstitutionRelevantVO> list = new ArrayList<>();
        list.add(institutionVO1);
        list.add(institutionVO2);
        return list;
    }
}

/***
 * ┌───┐   ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│   │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│  ┌┐    ┌┐    ┌┐
 * └───┘   └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘  └┘    └┘    └┘
 * ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐ ┌───┬───┬───┐ ┌───┬───┬───┬───┐
 * │~ `│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp │ │Ins│Hom│PUp│ │N L│ / │ * │ - │
 * ├───┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤ ├───┼───┼───┤ ├───┼───┼───┼───┤
 * │ Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ │ │Del│End│PDn│ │ 7 │ 8 │ 9 │   │
 * ├─────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤ └───┴───┴───┘ ├───┼───┼───┤ + │
 * │ Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter  │               │ 4 │ 5 │ 6 │   │
 * ├──────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤     ┌───┐     ├───┼───┼───┼───┤
 * │ Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │     │ ↑ │     │ 1 │ 2 │ 3 │   │
 * ├─────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤ ┌───┼───┼───┐ ├───┴───┼───┤ E││
 * │ Ctrl│    │Alt │         Space         │ Alt│    │    │Ctrl│ │ ← │ ↓ │ → │ │   0   │ . │←─┘│
 * └─────┴────┴────┴───────────────────────┴────┴────┴────┴────┘ └───┴───┴───┘ └───────┴───┴───┘
 */
