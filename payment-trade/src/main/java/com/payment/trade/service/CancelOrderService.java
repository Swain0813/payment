package com.payment.trade.service;
import com.payment.common.entity.Orders;
import com.payment.common.response.BaseResponse;
import com.payment.trade.dto.UndoDTO;

/**
 * 撤销订单业务
 */
public interface CancelOrderService {
    /**
     * 撤销指定的订单
     * @param undoDTO
     * @return
     */
    BaseResponse undo(UndoDTO undoDTO);

    /**
     * 撤销
     * @param order
     */
    void cancelOrder(Orders order);

    /**
     * 退款
     * @param order
     * @param
     */
    void  refund(Orders order);
}
