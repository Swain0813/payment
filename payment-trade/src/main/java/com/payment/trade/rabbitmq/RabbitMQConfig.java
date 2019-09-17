package com.payment.trade.rabbitmq;

import com.payment.common.constant.AD3MQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Wu, Hua-Zheng
 * @version v1.0.0
 * @classDesc: 功能描述: RabbitMQ配置
 * @createTime 2018年8月9日 下午9:52:13
 */
@Configuration
public class RabbitMQConfig {

    //操作记录
    //—————————————————————————下单相关队列———————————————————————————————————————————————————
    public final static String MQ_PLACE_ORDER_FUND_CHANGE_FAIL = AD3MQConstant.MQ_PLACE_ORDER_FUND_CHANGE_FAIL;//线下支付成功上报清结算失败队列


    @Bean
    public Queue operateRecordPlaceOrderFundChangeFail() {
        return new Queue(RabbitMQConfig.MQ_PLACE_ORDER_FUND_CHANGE_FAIL);
    }

    //————————————————————————退款接口相关队列————————————————————————————————————————————————————
    public final static String MQ_TK_XX_QQSB_DL = AD3MQConstant.MQ_TK_XX_QQSB_DL;//退款请求失败（线下）
    public final static String MQ_TK_XS_QQSB_DL = AD3MQConstant.MQ_TK_XS_QQSB_DL;//退款请求失败（线上）
    public final static String MQ_QJS_TZSB_DL = AD3MQConstant.MQ_QJS_TZSB_DL;//上报清结算调账失败
    public final static String MQ_CX_XX_QQSB_DL = AD3MQConstant.MQ_CX_XX_QQSB_DL;//撤销请求失败对列(线下)
    public final static String MQ_CX_XS_QQSB_DL = AD3MQConstant.MQ_CX_XS_QQSB_DL;//撤销请求失败对列(线上)
    public final static String MQ_TK_SBQJSSB_DL = AD3MQConstant.MQ_TK_SBQJSSB_DL;//退款上报清结算失败
    public final static String MQ_CX_SBQJSSB_DL = AD3MQConstant.MQ_CX_SBQJSSB_DL;//撤销上报清结算失败

    public final static String MQ_TK_APLIPAY_QQSB_DL = AD3MQConstant.MQ_TK_APLIPAY_QQSB_DL;//alipay退款请求失败
    public final static String MQ_TK_WECHAT_QQSB_DL = AD3MQConstant.MQ_TK_WECHAT_QQSB_DL;//WECHAT退款请求失败
    public final static String MQ_TK_NEXTPOS_QQSB_DL = AD3MQConstant.MQ_TK_NEXTPOS_QQSB_DL;//NextPos退款请求失败


    public static final String MQ_CX_TDTKSB_DL = AD3MQConstant.MQ_CX_TDTKSB_DL;//撤销通道退款失败
    public static final String E_MQ_CX_TDTKSB_DL = AD3MQConstant.E_MQ_CX_TDTKSB_DL;//撤销通道退款失败死信队列
    public static final String MQ_CX_TDTKSB_DL_KEY = AD3MQConstant.MQ_CX_TDTKSB_DL_KEY;//撤销通道退款失败
    public static final String MQ_CX_TDTKSB_DL_EXCHANGE = AD3MQConstant.MQ_CX_TDTKSB_DL_EXCHANGE;//撤销通道退款失败死信路由

    //用于延时消费的队列
    @Bean
    public Queue operateRecordQueueCXTDTKSB() {
        return new Queue(RabbitMQConfig.MQ_CX_TDTKSB_DL, true, false, false);
    }

    //死信路由
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(MQ_CX_TDTKSB_DL_EXCHANGE);
    }

    //绑定exchange 到出队队列
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(operateRecordQueueCXTDTKSB()).to(exchange()).with(MQ_CX_TDTKSB_DL_KEY);
    }

    //配置死信队列，即入队队列
    @Bean
    public Queue deadLetterQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 1000 * 60 * 5);
        args.put("x-dead-letter-exchange", MQ_CX_TDTKSB_DL_EXCHANGE);
        args.put("x-dead-letter-routing-key", MQ_CX_TDTKSB_DL_KEY);
        return new Queue(E_MQ_CX_TDTKSB_DL, true, false, false, args);
    }


    @Bean
    public Queue operateRecordQueueXXQQSB() {
        return new Queue(RabbitMQConfig.MQ_TK_XX_QQSB_DL);
    }

    @Bean
    public Queue operateRecordQueueXSQQSB() {
        return new Queue(RabbitMQConfig.MQ_TK_XS_QQSB_DL);
    }

    @Bean
    public Queue operateRecordQueueTKSBJDSB() {
        return new Queue(RabbitMQConfig.MQ_QJS_TZSB_DL);
    }

    @Bean
    public Queue operateRecordQueueCXXXQQSB() {
        return new Queue(RabbitMQConfig.MQ_CX_XX_QQSB_DL);
    }

    @Bean
    public Queue operateRecordQueueCXXSQQSB() {
        return new Queue(RabbitMQConfig.MQ_CX_XS_QQSB_DL);
    }

    @Bean
    public Queue operateRecordQueueTKSBQJSSB() {
        return new Queue(RabbitMQConfig.MQ_TK_SBQJSSB_DL);
    }

    @Bean
    public Queue operateRecordQueueCXSBQJSSB() {
        return new Queue(RabbitMQConfig.MQ_CX_SBQJSSB_DL);
    }

    @Bean
    public Queue operateRecordQueueALIPAYTKSBJDSB() {
        return new Queue(RabbitMQConfig.MQ_TK_APLIPAY_QQSB_DL);
    }

    @Bean
    public Queue operateRecordQueueWECHATTKSBJDSB() {
        return new Queue(RabbitMQConfig.MQ_TK_WECHAT_QQSB_DL);
    }

    @Bean
    public Queue operateRecordQueueNEXTPOSTKSBJDSB() {
        return new Queue(RabbitMQConfig.MQ_TK_NEXTPOS_QQSB_DL);
    }

    //———————————————————————撤销接口相关队列—————————————————————————————————————————————————————
    public static final String MQ_AD3_ORDER_QUERY = AD3MQConstant.MQ_AD3_ORDER_QUERY;//撤销付款中订单查询AD3订单信息队列
    public static final String E_MQ_AD3_ORDER_QUERY = AD3MQConstant.E_MQ_AD3_ORDER_QUERY;//撤销付款中订单查询AD3订单信息死信队列
    public static final String MQ_AD3_ORDER_QUERY_KEY = AD3MQConstant.MQ_AD3_ORDER_QUERY_KEY;//撤销付款中订单查询AD3订单信息key
    public static final String MQ_AD3_ORDER_QUERY_EXCHANGE = AD3MQConstant.MQ_AD3_ORDER_QUERY_EXCHANGE;//撤销付款中订单查询AD3订单信息死信路由

    //用于延时撤销付款中订单查询AD3订单信息消费的队列
    @Bean
    public Queue operateRecordQueueAd3OrderQuery() {
        return new Queue(RabbitMQConfig.MQ_AD3_ORDER_QUERY, true, false, false);
    }

    //用于撤销ad3订单查询队列的死信路由
    @Bean
    public DirectExchange exchangeAd3Query() {
        return new DirectExchange(MQ_AD3_ORDER_QUERY_EXCHANGE);
    }

    //撤销AD3订单查询的绑定exchange 到出队队列
    @Bean
    public Binding deadLetterBindingAd3Query() {
        return BindingBuilder.bind(operateRecordQueueAd3OrderQuery()).to(exchangeAd3Query()).with(MQ_AD3_ORDER_QUERY_KEY);
    }

    //撤销AD3订单查询的配置死信队列，即入队队列
    @Bean
    public Queue deadLetterQueueAd3Query() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 1000 * 60 * 5);//延迟队列5分钟
        args.put("x-dead-letter-exchange", MQ_AD3_ORDER_QUERY_EXCHANGE);
        args.put("x-dead-letter-routing-key", MQ_AD3_ORDER_QUERY_KEY);
        return new Queue(E_MQ_AD3_ORDER_QUERY, true, false, false, args);
    }

    public static final String MQ_AD3_REFUND = AD3MQConstant.MQ_AD3_REFUND;//AD3退款队列
    public final static String MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL = AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL;//撤销时调用清结算资金变动时发生失败时的队列
    public static final String MQ_CANCEL_ORDER_REQUEST_FAIL = AD3MQConstant.MQ_CANCEL_ORDER_REQUEST_FAIL;//撤销时撤销请求失败队列
    public static final String MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL = AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL;//撤销时退款调用清结算资金变动RF发生失败时队列
    public static final String MQ_CANCEL_ORDER_FUND_CHANGE_FAIL = AD3MQConstant.MQ_CANCEL_ORDER_FUND_CHANGE_FAIL;//撤销时退款调用清结算资金变动失败队列
    public static final String MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL = AD3MQConstant.MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL;//撤销时退款请求上游通道失败队列
    public static final String MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL = AD3MQConstant.MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL;//撤销时撤销通道退款失败

    @Bean
    public Queue operateRecordQueueAd3Refund() {
        return new Queue(RabbitMQConfig.MQ_AD3_REFUND);
    }

    @Bean
    public Queue operateRecordQueueOrderFundChangeRvFail() {
        return new Queue(RabbitMQConfig.MQ_CANCEL_ORDER_FUND_CHANGE_RV_FAIL);
    }

    @Bean
    public Queue operateRecordQueueOrderCancelRequestFail() {
        return new Queue(RabbitMQConfig.MQ_CANCEL_ORDER_REQUEST_FAIL);
    }

    @Bean
    public Queue operateRecordQueueFundChangeRfFail() {
        return new Queue(RabbitMQConfig.MQ_CANCEL_ORDER_FUND_CHANGE_RF_FAIL);
    }

    @Bean
    public Queue operateRecordQueueOrderCancelFundChangeFail() {
        return new Queue(RabbitMQConfig.MQ_CANCEL_ORDER_FUND_CHANGE_FAIL);
    }

    @Bean
    public Queue operateRecordQueueOrderCancelChancelAcceptFail() {
        return new Queue(RabbitMQConfig.MQ_CANCEL_ORDER_CHANNEL_ACCEPT_FAIL);
    }

    @Bean
    public Queue operateRecordQueueOrderCancelChancelRefundFail() {
        return new Queue(RabbitMQConfig.MQ_CANCEL_ORDER_CHANNEL_REFUND_FAIL);
    }

    /* ===========================================      撤销接口相关队列 =============================================== */
    public final static String TC_MQ_CANCEL_ORDER = AD3MQConstant.TC_MQ_CANCEL_ORDER;//撤销队列-付款中订单
    @Bean
    public Queue operateRecordQueueOrderWaitPay() {
        return new Queue(RabbitMQConfig.TC_MQ_CANCEL_ORDER);
    }

    /* ===========================================      NganLuong查询队列       =============================================== */
    public static final String MQ_NGANLUONG_CHECK_ORDER_DL = AD3MQConstant.MQ_NGANLUONG_CHECK_ORDER_DL;//NganLuong查询订单状态队列
    public static final String E_MQ_NGANLUONG_CHECK_ORDER_DL = AD3MQConstant.E_MQ_NGANLUONG_CHECK_ORDER_DL;//NganLuong查询订单死信队列
    public static final String MQ_NGANLUONG_CHECK_ORDER_DL_KEY = AD3MQConstant.MQ_NGANLUONG_CHECK_ORDER_DL_KEY;//NganLuong查询订单信息key
    public static final String MQ_NGANLUONG_CHECK_ORDER_DL_EXCHANGE = AD3MQConstant.MQ_NGANLUONG_CHECK_ORDER_DL_EXCHANGE;//NganLuong查询订单死信路由

    @Bean
    public Queue operateNGANLUONGQuery() {
        return new Queue(RabbitMQConfig.MQ_NGANLUONG_CHECK_ORDER_DL, true, false, false);
    }

    //用于NL通道查询队列的死信路由
    @Bean
    public DirectExchange exchangeNGANLUONGQuery() {
        return new DirectExchange(MQ_NGANLUONG_CHECK_ORDER_DL_EXCHANGE);
    }

    //NL通道查询队列的绑定exchange 到出队队列
    @Bean
    public Binding deadLetterBindingNGANLUONGuery() {
        return BindingBuilder.bind(operateNGANLUONGQuery()).to(exchangeNGANLUONGQuery()).with(MQ_NGANLUONG_CHECK_ORDER_DL_KEY);
    }

    //NL通道查询队列的配置死信队列,即入队队列
    @Bean
    public Queue deadLetterQueueNGANLUONGuery() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 1000 * 60 * 5);//延迟队列5分钟
        args.put("x-dead-letter-exchange", MQ_NGANLUONG_CHECK_ORDER_DL_EXCHANGE);
        args.put("x-dead-letter-routing-key", MQ_NGANLUONG_CHECK_ORDER_DL_KEY);
        return new Queue(E_MQ_NGANLUONG_CHECK_ORDER_DL, true, false, false, args);
    }

    /* =========================================== 回调队列 =============================================== */
    public static final String MQ_AW_CALLBACK_URL_FAIL = AD3MQConstant.MQ_AW_CALLBACK_URL_FAIL;//回调失败队列
    public static final String E_MQ_AW_CALLBACK_URL_FAIL = AD3MQConstant.E_MQ_AW_CALLBACK_URL_FAIL;//N回调失败队列死信队列
    public static final String MQ_AW_CALLBACK_URL_FAIL_KEY = AD3MQConstant.MQ_AW_CALLBACK_URL_FAIL_KEY;//回调失败队列key
    public static final String MQ_AW_CALLBACK_URL_FAIL_EXCHANGE = AD3MQConstant.MQ_AW_CALLBACK_URL_FAIL_EXCHANGE;//回调失败队列死信路由

    @Bean
    public Queue operateCallBack() {
        return new Queue(RabbitMQConfig.MQ_AW_CALLBACK_URL_FAIL, true, false, false);
    }

    //用于回调失败队列的死信路由
    @Bean
    public DirectExchange exchangeCallBack() {
        return new DirectExchange(MQ_AW_CALLBACK_URL_FAIL_EXCHANGE);
    }

    //回调失败队列的绑定exchange 到出队队列
    @Bean
    public Binding deadLetterBindingCallBack() {
        return BindingBuilder.bind(operateCallBack()).to(exchangeCallBack()).with(MQ_AW_CALLBACK_URL_FAIL_KEY);
    }

    //回调失败队列的配置死信队列,即入队队列
    @Bean
    public Queue deadLetterCallBack() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 1000 * 60 * 2);//延迟队列2分钟
        args.put("x-dead-letter-exchange", MQ_AW_CALLBACK_URL_FAIL_EXCHANGE);
        args.put("x-dead-letter-routing-key", MQ_AW_CALLBACK_URL_FAIL_KEY);
        return new Queue(E_MQ_AW_CALLBACK_URL_FAIL, true, false, false, args);
    }

    /* =========================================== 汇款回调队列 =============================================== */
    public static final String MQ_PAYMENT_CALLBACK_URL_FAIL = AD3MQConstant.MQ_PAYMENT_CALLBACK_URL_FAIL;//回调失败队列
    public static final String E_MQ_PAYMENT_CALLBACK_URL_FAIL = AD3MQConstant.E_MQ_PAYMENT_CALLBACK_URL_FAIL;//N回调失败队列死信队列
    public static final String MQ_PAYMENT_CALLBACK_URL_FAIL_KEY = AD3MQConstant.MQ_PAYMENT_CALLBACK_URL_FAIL_KEY;//回调失败队列key
    public static final String MQ_PAYMENT_CALLBACK_URL_FAIL_EXCHANGE = AD3MQConstant.MQ_PAYMENT_CALLBACK_URL_FAIL_EXCHANGE;//回调失败队列死信路由

    @Bean
    public Queue operatePAYMENTCallBack() {
        return new Queue(RabbitMQConfig.MQ_PAYMENT_CALLBACK_URL_FAIL, true, false, false);
    }

    //用于回调失败队列的死信路由
    @Bean
    public DirectExchange exchangePAYMENTCallBack() {
        return new DirectExchange(MQ_PAYMENT_CALLBACK_URL_FAIL_EXCHANGE);
    }

    //回调失败队列的绑定exchange 到出队队列
    @Bean
    public Binding deadLetterBindingPAYMENTCallBack() {
        return BindingBuilder.bind(operatePAYMENTCallBack()).to(exchangePAYMENTCallBack()).with(MQ_PAYMENT_CALLBACK_URL_FAIL_KEY);
    }

    //回调失败队列的配置死信队列,即入队队列
    @Bean
    public Queue deadLetterPAYMENTCallBack() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 1000 * 60 * 3);//延迟队列3分钟
        args.put("x-dead-letter-exchange", MQ_PAYMENT_CALLBACK_URL_FAIL_EXCHANGE);
        args.put("x-dead-letter-routing-key", MQ_PAYMENT_CALLBACK_URL_FAIL_KEY);
        return new Queue(E_MQ_PAYMENT_CALLBACK_URL_FAIL, true, false, false, args);
    }
    /* ===========================================      分润队列       =============================================== */
    public static final String MQ_FR_DL = AD3MQConstant.MQ_FR_DL;//分润处理队列
    @Bean
    public Queue operateRecordQueueFR() {
        return new Queue(RabbitMQConfig.MQ_FR_DL);
    }


}
