package com.payment.trade.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.OrderLogisticsDTO;
import com.payment.common.dto.OrderLogisticsQueryDTO;
import com.payment.common.entity.OrderLogistics;
import com.payment.trade.dto.OrderLogisticsBatchQueryDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLogisticsMapper extends BaseMapper<OrderLogistics> {

    /**
     * 查询订单物流信息
     * @param orderLogisticsQueryDTO
     * @return
     */
    List<OrderLogistics> getOrderLogisticsInfo(OrderLogisticsQueryDTO orderLogisticsQueryDTO);

    /**
     * 根据查询条件查询单个订单物流信息
     * @param orderLogisticsDTO
     * @return
     */
    OrderLogistics  getOrderLogisticsInfoById(OrderLogisticsDTO orderLogisticsDTO);

    /**
     * 查找
     *
     * @param orderLogistics
     * @return
     */
    OrderLogistics selectByinstitutionOrderIdAndInstitutionCode(OrderLogistics orderLogistics);

    /**
     * 机构批量查询订单物流信息
     * @param orderLogisticsBatchQueryDTO
     * @return
     */
    List<OrderLogistics> getOrderLogisticsInfos(OrderLogisticsBatchQueryDTO orderLogisticsBatchQueryDTO);

}
