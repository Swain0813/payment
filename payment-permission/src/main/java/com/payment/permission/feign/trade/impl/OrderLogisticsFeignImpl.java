package com.payment.permission.feign.trade.impl;

import com.payment.common.dto.OrderLogisticsDTO;
import com.payment.common.dto.OrderLogisticsQueryDTO;
import com.payment.common.entity.OrderLogistics;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.trade.OrderLogisticsFeign;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 物流
 */
@Component
public class OrderLogisticsFeignImpl implements OrderLogisticsFeign {

    @Override
    public BaseResponse getOrderLogisticsInfo(OrderLogisticsQueryDTO orderLogisticsQueryDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }


    @Override
    public BaseResponse updateOrderLogistics(OrderLogisticsDTO orderLogisticsDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    @Override
    public BaseResponse uploadOrderLogistics(List<OrderLogistics> uploadFiles) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
