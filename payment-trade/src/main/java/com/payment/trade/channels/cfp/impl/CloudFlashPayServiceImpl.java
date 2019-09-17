package com.payment.trade.channels.cfp.impl;
import com.payment.common.constant.TradeConstant;
import com.payment.common.entity.Channel;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;
import com.payment.trade.channels.cfp.CloudFlashPayService;
import com.payment.trade.dao.OrdersMapper;
import com.payment.trade.dto.CloudFlashCallbackDTO;
import com.payment.trade.utils.AbstractHandlerAdapter;
import com.payment.trade.utils.HandlerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@Slf4j
@HandlerType(TradeConstant.CLOUD_PAY_OFFLINE)
public class CloudFlashPayServiceImpl extends AbstractHandlerAdapter implements CloudFlashPayService {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 云闪付线下
     *
     * @param orders       订单
     * @param channel      通道
     * @param baseResponse 通用响应实体
     * @return
     */
    @Override
    public BaseResponse offlineCSB(Orders orders, Channel channel, BaseResponse baseResponse) {
        baseResponse.setData("http://psw.payment.com/checkstandweb/#/wangYing?aoumt=" + orders.getTradeAmount() + "&currency=" +
                orders.getTradeCurrency() + "&orderNo=" + orders.getInstitutionOrderId() + "&productName=" + orders.getInstitutionName());
        return baseResponse;
    }

    /**
     * 云闪付前端回调
     *
     * @param cloudFlashCallbackDTO 云闪付回调实体
     */
    @Override
    public BaseResponse cloudFlashPayCallback(CloudFlashCallbackDTO cloudFlashCallbackDTO) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setCode("200");
        Orders orders = ordersMapper.selectOrderByInstitutionOrderId(cloudFlashCallbackDTO.getId());
        if (orders == null) {
            log.info("===================【云闪付前端回调】===================【回调订单不存在】 机构订单号:{}", cloudFlashCallbackDTO.getId());
            baseResponse.setMsg("The origin order no exist");
            return baseResponse;
        }
        if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS) || orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_FAILD)) {
            log.info("===================【云闪付前端回调】===================【订单状态 已修改】 status:{}", orders.getTradeStatus());
            baseResponse.setData(orders.getTradeStatus());
            baseResponse.setMsg("The order status already changed");
            return baseResponse;
        }
        orders.setUpdateTime(new Date());
        orders.setChannelCallbackTime(new Date());
        orders.setTradeStatus(cloudFlashCallbackDTO.getTradeStatus());
        int modifier = ordersMapper.updateByPrimaryKeySelective(orders);
        if (modifier > 0) {
            if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_SUCCESS)) {
                log.info("===================【云闪付前端回调】===================【订单已支付成功】");
                baseResponse.setData(TradeConstant.ORDER_PAY_SUCCESS);
                baseResponse.setMsg("PAY SUCCESS");
            } else if (orders.getTradeStatus().equals(TradeConstant.ORDER_PAY_FAILD)) {
                log.info("===================【云闪付前端回调】===================【订单已支付失败】");
                baseResponse.setData(TradeConstant.ORDER_PAY_FAILD);
                baseResponse.setMsg("PAY FAILED");
            }
            return baseResponse;
        }
        log.info("===================【云闪付前端回调】===================【订单更新状态失败,系统错误】");
        baseResponse.setMsg("SYSTEM ERROR");
        return baseResponse;
    }
}
