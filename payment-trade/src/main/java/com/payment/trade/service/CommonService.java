package com.payment.trade.service;

import com.payment.common.dto.OrderLogisticsBachDTO;
import com.payment.common.dto.PayOutDTO;
import com.payment.common.dto.PayOutRequestDTO;
import com.payment.common.entity.*;
import com.payment.common.enums.Status;
import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.CashierDTO;
import com.payment.trade.dto.MegaPayIDRServerCallbackDTO;
import com.payment.trade.dto.MegaPayServerCallbackDTO;
import com.payment.trade.dto.PlaceOrdersDTO;
import com.payment.trade.vo.BasicsInfoVO;
import com.payment.trade.vo.CalcFeeVO;
import com.payment.trade.vo.CalcRateVO;
import com.payment.trade.vo.InstitutionVO;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * @Author XuWenQi
 * @Date 2019/3/18 15:26
 * @Descripate 交易通用业务接口
 */
public interface CommonService {

    /**
     * 重复请求判断
     *
     * @param institutionCode 机构code
     * @param signMsg         签名信息
     * @return true
     */
    boolean repeatedRequests(String institutionCode, String signMsg);


    /**
     * 判断签名是否正确
     *
     * @param institutionCode 机构code
     * @param signMsg         签名信息
     * @return true
     */
    boolean checkSignMsg(String institutionCode, String signMsg);

    /**
     * 校验线上签名
     * 亚洲钱包 rsa验签用
     *
     * @param o
     * @return
     */
    boolean checkOnlineSignMsg(Object o);

    /**
     * 校验RSA与MD5签名的一体方法
     *
     * @param o
     * @return
     */
    boolean checkSignMsgWithRSAMD5(Object o);

    /**
     * 校验线上签名
     * 亚洲钱包 MD5验签用
     *
     * @param o
     * @return
     */
    boolean checkOnlineSignMsgUseMD5(Object o);

    /**
     * 校验线下参数的签名
     *
     * @param o
     * @return
     */
    boolean checkOfflineSignMsg(Object o);

    /**
     * 解密收银台收单参数
     *
     * @param cd
     * @return
     */
    CashierDTO decryptCashierSignMsg(CashierDTO cd) throws Exception;


    /**
     * 使用机构对应平台的私钥生成签名
     *
     * @param o
     * @return
     */
    String generateSignatureUsePlatRSA(Object o);

    /**
     * 使用机构对应平台的MD5生成签名
     *
     * @param o
     * @return
     */
    String generateSignatureUsePlatMD5(Object o);

    /**
     * 使用商户私钥生成RSA签名与MD5签名
     *
     * @param o
     * @return
     */
    Map generateSignatureUseInst(Object o);

    /**
     * 对OrderLogisticsBachDTO生成签名
     *
     * @param dto
     * @return
     */
    String generateListSignatureLog(OrderLogisticsBachDTO dto);

    /**
     * 对PayOutDTO生成签名 付款
     *
     * @param dto
     * @return
     */
    String generateListSignaturePay(PayOutDTO dto);

    /**
     * 校验订单
     *
     * @param placeOrdersDTO 订单
     * @param basicsInfo     机构关联产品通道
     */
    void checkOrder(PlaceOrdersDTO placeOrdersDTO, BasicsInfoVO basicsInfo);

    /**
     * 校验订单，无重复订单号校验
     *
     * @param placeOrdersDTO
     * @param basicsInfo
     */
    void checkOnlineOrder(PlaceOrdersDTO placeOrdersDTO, BasicsInfoVO basicsInfo);

    /**
     * 校验间连订单
     *
     * @param placeOrdersDTO 订单
     * @param institutionVO
     */
    void checkICOnlineOrder(PlaceOrdersDTO placeOrdersDTO, InstitutionVO institutionVO);

    /**
     * 下单业务信息校验
     * 线上和线下都用
     *
     * @param orders       订单
     * @param basicsInfoVO 基础配置信息
     * @param baseResponse 响应实体
     */
    BaseResponse checkPlaceOrder(Orders orders, BasicsInfoVO basicsInfoVO, BaseResponse baseResponse);

    /**
     * 校验通道限值
     *
     * @param tradeAmount
     * @param channel
     * @return
     */
    boolean verifyChannelLimits(BigDecimal tradeAmount, Channel channel);

    /**
     * 收银台下单信息校验
     *
     * @param cd           订单
     * @param basicsInfoVO 产品信息
     */
    void checkCashierOrder(CashierDTO cd, BasicsInfoVO basicsInfoVO, Orders orders);

    /**
     * 回调时计算通道网关手续费(交易成功时收取)
     *
     * @param orders orders
     * @return
     */
    void calcCallBackGatewayFeeSuccess(Orders orders);

    /**
     * 回调时计算通道网关手续费(交易失败时收取)
     *
     * @param orders orders
     * @return
     */
    void calcCallBackGatewayFeeFailed(Orders orders);

    /**
     * 下单计算通道网关手续费
     *
     * @param channel
     * @param orders
     * @param baseResponse
     * @return
     */
    boolean calcGatewayFee(Channel channel, Orders orders, BaseResponse baseResponse);


    /**
     * 设置订单属性
     *
     * @param placeOrdersDTO 订单输入实体
     * @param basicsInfoVO   机构关联产品通道
     * @return 订单实体
     */
    Orders setAttributes(PlaceOrdersDTO placeOrdersDTO, BasicsInfoVO basicsInfoVO);

    /**
     * 收银台收单设置订单属性
     *
     * @return 订单实体
     */
    Orders setCashierAttributes(CashierDTO cd, BasicsInfoVO basicsInfoVO, Orders orders);


    /**
     * 汇率计算
     *
     * @param orderCurrency 订单币种
     * @param tradeCurrency 交易币种
     * @param floatRate     浮动率
     * @param amount        订单金额
     * @return 交易金额与换汇汇率
     */
    CalcRateVO calcExchangeRate(String orderCurrency, String tradeCurrency, BigDecimal floatRate, BigDecimal amount);

    /**
     * 计算手续费
     * 线上和线下
     *
     * @param orders
     * @param basicsInfoVO
     * @return
     */
    CalcFeeVO calcPoundage(Orders orders, BasicsInfoVO basicsInfoVO);

    /**
     * 计算手续费时的换汇计算
     *
     * @param productCurrency
     * @param institutionProduct
     * @param existRate          原先的汇率
     * @param orderCurrency
     * @return
     */
    InstitutionProduct CalcFeeExchange(String productCurrency, InstitutionProduct institutionProduct, BigDecimal existRate, String orderCurrency);

    /**
     * 计算通道手续费
     *
     * @param amount
     * @param channel
     * @return
     */
    CalcFeeVO calcChannelPoundage(BigDecimal amount, Channel channel);

    /**
     * 计算通道网关手续费
     *
     * @param amount 订单金额
     * @return CalcFeeVO  通道费用输出实体
     */
    CalcFeeVO calcChannelGatewayPoundage(BigDecimal amount, Channel channel);

    /**
     * 配置限额限次信息
     *
     * @param institutionCode 机构编号
     * @param productCode     产品编号
     */
    void quota(String institutionCode, Integer productCode, BigDecimal amount);

    /**
     * 退款用创建调账单
     *
     * @param orderRefund
     * @return
     */
    Reconciliation createReconciliation(OrderRefund orderRefund, String remark);

    /**
     * 校验商户的回调地址是否合法
     * 规则：以http:// 与 https:// 开头
     *
     * @param str@return
     */
    boolean checkUrl(String str);

    /**
     * 回调商户
     *
     * @param orders
     */
    void replyReturnUrl(Orders orders);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/8/14
     * @Descripate 汇款回调商户
     **/
    void payOutCallBack(OrderPayment orderPayment);

    /**
     * 截取币种默认值
     *
     * @param orders
     * @return
     */
    boolean interceptDigit(Orders orders, BaseResponse baseResponse);

    /**
     * 重定向用户jumpUrl
     *
     * @param orders
     * @param response
     */
    void replyJumpUrl(Orders orders, HttpServletResponse response);

    /**
     * 从redis获取基础配置信息
     *
     * @param placeOrdersDTO 下单实体
     * @param tradeDirection
     */
    BasicsInfoVO getBasicsInfo(PlaceOrdersDTO placeOrdersDTO, Byte tradeDirection);

    /**
     * 根据通道code从redis获取通道信息
     *
     * @param channelCode 通道code
     */
    Channel getChannelByChannelCode(String channelCode);

    /**
     * 获得机构信息
     *
     * @param institutionCode
     * @return
     */
    Institution getInstitutionInfo(String institutionCode);

    /**
     * 线上下单从redis获取基础配置信息,通道通过issuerId匹配
     *
     * @param issuerId
     * @param placeOrdersDTO
     * @param tradDirection
     * @return
     */
    BasicsInfoVO getBasicsInfoByIssuerId(String issuerId, PlaceOrdersDTO placeOrdersDTO, Byte tradDirection);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 付款获取基础信息
     **/
    BasicsInfoVO getBasicsInfo(PayOutRequestDTO payOutRequestDTO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 创建付款单
     **/
    OrderPayment createOrderPayment(PayOutRequestDTO payOutRequestDTO, String reqIp, BasicsInfoVO
            basicsInfoVO);

    /**
     * @return
     * @Author YangXu
     * @Date 2019/7/23
     * @Descripate 付款计算手续费
     **/
    CalcFeeVO calcPoundageOrderPayment(OrderPayment orderPayment, BasicsInfoVO basicsInfoVO);

    /**
     * 获取通道id List
     *
     * @param institutionProduct
     * @return
     */
    List<String> getChannels(InstitutionProduct institutionProduct);

    /**
     * 支付成功新增物流信息
     *
     * @param orders
     */
    void insertOrderLogistics(Orders orders);

    /**
     * 支付成功发送邮件给付款人
     *
     * @param email
     * @param language
     * @param emailNum
     * @param orders
     */
    void sendEmail(String email, String language, Status emailNum, Orders orders);

    /**
     * 校验批量更新物流订单签名
     *
     * @param orderLogisticsBachDTO
     */
    boolean checkOrderLogistics(OrderLogisticsBachDTO orderLogisticsBachDTO);

    /**
     * 校验付款签名
     *
     * @param payOutDTO
     */
    boolean checkPayment(PayOutDTO payOutDTO);

    /**
     * 校验域名
     *
     * @param serverUrl         订单上送服务回调url
     * @param institutionWebUrl 机构基础信息配置url
     */
    void checkDomain(String serverUrl, String institutionWebUrl);

    /**
     * 截取Url
     *
     * @param placeOrdersDTO
     * @param orders
     */
    void getUrl(PlaceOrdersDTO placeOrdersDTO, Orders orders);


    /**
     * 物流信息更新后发送发货通知邮件
     *
     * @param email
     * @param language
     * @param emailNum
     * @param orderLogistics
     */
    void sendDeliveryEmail(String email, String language, Status emailNum, OrderLogistics orderLogistics);

    /**
     * megaPay-THB通道分发AD3
     *
     * @param megaPayServerCallbackDTO 参数
     * @param url                      url
     */
    String megaTHBCallbackAD3(MegaPayServerCallbackDTO megaPayServerCallbackDTO, String url);

    /**
     * megaPay-IDR通道分发AD3
     *
     * @param megaPayIDRServerCallbackDTO 参数
     * @param url                         url
     */
    String megaIDRCallbackAD3(MegaPayIDRServerCallbackDTO megaPayIDRServerCallbackDTO, String url);

    /**
     * megaPay-NextPos通道分发AD3
     *
     * @param map 参数
     */
    String nextPosCallbackAD3(Map<String, Object> map);

    /**
     * 分发AD3
     *
     * @param obj 参数
     * @param url url
     */
    String callbackAD3(Object obj, String url);

    /**
     * 判断通道网关手续费收取状态
     *
     * @param channel 通道
     */
    Byte judgeChannelGatewayFee(Channel channel);

    /**
     * 根据通道id从redis里获取通道信息
     *
     * @param id
     * @return
     */
    Channel getChannelById(String id);

    /**
     * 根据机构编号和币种从redis里获取账户信息
     *
     * @param institutionCode
     * @param currency
     * @return
     */
    Account getAccount(String institutionCode, String currency);

    /**
     * 创建机构对应币种的账户
     */
    void createAccount(String institutionCode, String currency);

    /**
     * 获取基础数据中的币种信息
     *
     * @param currency
     * @return
     */
    String getCurrency(String currency);

    /**
     * 校验订单的币种与金额 是否符合币种默认值
     *
     * @return
     */
    boolean checkOrderCurrency(PlaceOrdersDTO placeOrdersDTO);

    /**
     * 根据币种获取币种默认值
     * @param currency
     * @return
     */
    String getCurrencyDefaultValue(String currency);
}
