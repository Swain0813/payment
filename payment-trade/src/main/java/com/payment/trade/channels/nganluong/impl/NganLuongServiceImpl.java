package com.payment.trade.channels.nganluong.impl;
import com.alibaba.fastjson.JSON;
import com.payment.common.constant.AD3MQConstant;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.constant.TradeConstant;
import com.payment.common.dto.nganluong.NganLuongDTO;
import com.payment.common.dto.nganluong.NganLuongRequestDTO;
import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.entity.RabbitMassage;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.trade.config.AD3ParamsConfig;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.feign.ChannelsFeign;
import com.payment.trade.rabbitmq.RabbitMQSender;
import com.payment.trade.channels.nganluong.NganLuongService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
public class NganLuongServiceImpl implements NganLuongService {

    @Autowired
    private AD3ParamsConfig ad3ParamsConfig;

    @Autowired
    private ChannelsFeign channelsFeign;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * nganLuong网银收单方法
     *
     * @param orders  订单
     * @param channel 通道
     * @return
     */
    @Override
    public BaseResponse nganLuongPay(Orders orders, Channel channel, BaseResponse baseResponse) {
        NganLuongRequestDTO nganLuongRequestDTO = new NganLuongRequestDTO(channel, orders, ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_SUCCESS, ad3ParamsConfig.getPaySuccessUrl() + "?page=" + TradeConstant.PAGE_PROCESSING);
        NganLuongDTO nganLuongDTO = new NganLuongDTO(nganLuongRequestDTO, orders.getInstitutionOrderId(), orders.getReqIp());
        log.info("----------------- nganLuong网银收单方法 ----------------- nganLuongDTO: {}", JSON.toJSONString(nganLuongDTO));
        BaseResponse response = channelsFeign.nganLuongPay(nganLuongDTO);
        //判断baseResponse code
        if (!StringUtils.isEmpty(response.getCode())) {
            baseResponse.setCode(response.getCode());
            return baseResponse;
        }
        Map<String, String> map = (Map<String, String>) response.getData();
        //判断error_code
        String error_code = map.get("error_code");
        if (!error_code.equals("00")) {
            baseResponse.setCode(EResultEnum.ORDER_CREATION_FAILED.getCode());
            return baseResponse;
        }
        //将查询token放入订单
        orders.setSign(map.get("token"));
        ordersMapper.updateByPrimaryKeySelective(orders);
        log.info("-----------------nganLuong网银收单接口信息记录-------------上报查询订单队列 E_MQ_NGANLUONG_CHECK_ORDER_DL -------------- token:{},ordersId:{}", map.get("token"), orders.getId());
        RabbitMassage rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, map.get("token"));
        rabbitMQSender.send(AD3MQConstant.E_MQ_NGANLUONG_CHECK_ORDER_DL, JSON.toJSONString(rabbitMassage));
        baseResponse.setData(response.getData());
        log.info("----------------- nganLuong网银收单方法 返回----------------- baseResponse: {}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }

}
