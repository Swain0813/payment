package com.payment.common.constant;

/**
 * @classDesc: 功能描述: AD3接口参数常量类
 * @createTime 2019年3月4日 上午19:33:11
 * @copyright: 上海众哈网络技术有限公司
 */
public class AD3Constant {

    //--------------字符集
    public static final String CHARSET_UTF_8 = "1"; //UTF-8
    public static final String CHARSET_GBK = "2"; //GBK

    //--------------语言
    public static final String LANGUAGE_CN = "1"; //中文
    public static final String LANGUAGE_EN = "2"; //英文

    //------------登陆类型
    public static final String LOGIN_IN = "1"; //登陆
    public static final String LOGIN_OUT = "2"; //登出

    //------------业务类型
    public static final String BUSINESS_IN = "1";  //人民币业务
    public static final String BUSINESS_OUT = "2"; //跨境业务

    //------------查询类型
    public static final Integer TRADE_ORDER = 1; //交易订单
    public static final Integer REFUND_ORDER = 2; //退款订单

    //------------交易订单状态
    public static final String ORDER_IN_TRADING = "1"; //交易中
    public static final String ORDER_FAILED = "2"; //支付失败
    public static final String ORDER_SUCCESS = "3"; //支付成功

    //------------退款订单状态
    public static final String REFUND_ORDER_ACCEPTED_SUCCESS = "1"; //受理成功
    public static final String REFUND_ORDER_ACCEPTED_FAILED = "2"; //受理失败
    public static final String REFUND_ORDER_SUCCESS = "3"; //退款成功
    public static final String REFUND_ORDER_FAILED = "4"; //退款失败

    //------------银行机构代码
    public static final String WECHAT = "wechat"; //微信
    public static final String ALIPAY = "alipay"; //支付宝
    public static final String ENETS = "enets"; //enets钱包

    //------------AD3登陆接口redis key
    public static final String AD3_LOGIN_TOKEN = "AD3_LOGIN_TOKEN"; //ad3登陆key
    public static final String AD3_LOGIN_TERMINAL = "AD3_LOGIN_TERMINAL"; //ad3登陆key

    //------------AD3接口回调标记
    public static final String NEED_CALLBACK = "1"; //需要回调
    public static final String NO_NEED_CALLBACK = "2"; //不需要回调

    //AD3响应码
    public static final String AD3_OFFLINE_SUCCESS = "10000";//线下成功返回码
    public static final String AD3_ONLINE_SUCCESS = "T000";//线上成功返回码
    public static final String AMOUNT_IS_ILLEGAL = "GW0034";//金额不合法

    //AD3线上和线下通道的类型
    public static final String AD3_ONLINE = "AD3_ONLINE"; //AD3线上通道
    public static final String AD3_OFFLINE = "AD3_OFFLINE"; //AD3线下通道



}
