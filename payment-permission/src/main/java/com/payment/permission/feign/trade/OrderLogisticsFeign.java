package com.payment.permission.feign.trade;

import com.payment.common.dto.OrderLogisticsDTO;
import com.payment.common.dto.OrderLogisticsQueryDTO;
import com.payment.common.entity.OrderLogistics;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.trade.impl.OrderLogisticsFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 调用订单物流信息服务
 */
@FeignClient(value = "payment-trade", fallback = OrderLogisticsFeignImpl.class)
public interface OrderLogisticsFeign {

    /**
     * 查询订单物流信息
     * @param orderLogisticsQueryDTO
     * @return
     */
    @PostMapping(value = "/logistics/getOrderLogisticsInfo")
    BaseResponse getOrderLogisticsInfo(@RequestBody @ApiParam OrderLogisticsQueryDTO orderLogisticsQueryDTO);

    /**
     * 修改订单物流信息(单条)
     * @param orderLogisticsDTO
     * @return
     */
    @PostMapping(value = "/logistics/updateOrderLogistics")
    BaseResponse updateOrderLogistics(@RequestBody @ApiParam OrderLogisticsDTO orderLogisticsDTO);

    /**
     * 上传文件
     *
     * @param uploadFiles
     * @return
     */
    @PostMapping(value = "/logistics/uploadOrderLogistics")
    BaseResponse uploadOrderLogistics(List<OrderLogistics> uploadFiles);
}
