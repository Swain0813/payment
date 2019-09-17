package com.payment.trade.service;
import com.payment.common.dto.OrderLogisticsBachDTO;
import com.payment.common.dto.OrderLogisticsDTO;
import com.payment.common.dto.OrderLogisticsQueryDTO;
import com.payment.common.entity.OrderLogistics;
import com.payment.trade.dto.OrderLogisticsBatchQueryDTO;

import java.util.List;

/**
 * 订单物流信息服务
 */
public interface OrderLogisticsService {

    /**
     *查询订单物流信息
     * @param orderLogisticsQueryDTO
     * @return
     */
    List<OrderLogistics> getOrderLogisticsInfo(OrderLogisticsQueryDTO orderLogisticsQueryDTO);

    /**
     * 修改订单物流信息
     * @param name
     * @param orderLogisticsDTO
     * @return
     */
    int updateOrderLogistics(String name, OrderLogisticsDTO orderLogisticsDTO);

    /**
     * 对外提供api接口
     * 批量修改订单物流信息
     * @param orderLogisticsDTO
     * @return
     */
    int  updateOrderLogisticsBatch(OrderLogisticsBachDTO orderLogisticsDTO);

    /**
     * 机构物流信息批量导入
     *
     * @param fileList
     */
    int uploadFiles(List<OrderLogistics> fileList);

    /**
     * 机构批量查询的订单物流信息
     * @param orderLogisticsBatchQueryDTO
     * @return
     */
    List<OrderLogistics> getOrderLogisticsInfos(OrderLogisticsBatchQueryDTO orderLogisticsBatchQueryDTO);
}
