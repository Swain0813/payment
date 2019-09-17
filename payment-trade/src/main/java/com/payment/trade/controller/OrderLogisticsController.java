package com.payment.trade.controller;
import com.payment.common.base.BaseController;
import com.payment.common.dto.OrderLogisticsBachDTO;
import com.payment.common.dto.OrderLogisticsDTO;
import com.payment.common.dto.OrderLogisticsQueryDTO;
import com.payment.common.entity.OrderLogistics;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.ResultUtil;
import com.payment.trade.dto.OrderLogisticsBatchQueryDTO;
import com.payment.trade.service.OrderLogisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 订单物流信息服务
 */
@RestController
@Api(description = "订单物流信息调接口")
@RequestMapping("/logistics")
@Slf4j
public class OrderLogisticsController extends BaseController{

    @Autowired
    private OrderLogisticsService orderLogisticsService;

    /**
     * 查询订单物流信息
     * @param orderLogisticsQueryDTO
     * @return
     */
    @ApiOperation(value = "查询订单物流信息")
    @PostMapping("/getOrderLogisticsInfo")
    public BaseResponse getOrderLogisticsInfo(@RequestBody @ApiParam @Valid OrderLogisticsQueryDTO orderLogisticsQueryDTO) {
        return ResultUtil.success(orderLogisticsService.getOrderLogisticsInfo(orderLogisticsQueryDTO));
    }

    /**
     * 单条更新订单物流信息
     * @param orderLogisticsDTO
     * @return
     */
    @ApiOperation(value = "修改订单物流信息")
    @PostMapping("/updateOrderLogistics")
    public BaseResponse updateOrderLogistics(@RequestBody @ApiParam OrderLogisticsDTO orderLogisticsDTO) {
        return ResultUtil.success(orderLogisticsService.updateOrderLogistics(this.getSysUserVO().getUsername(), orderLogisticsDTO));
    }

    /**
     * 导入物流信息
     * @param fileList
     * @return
     */
    @ApiOperation(value = "导入物流信息")
    @PostMapping("/uploadOrderLogistics")
    public BaseResponse uploadFiles(@RequestBody @ApiParam List<OrderLogistics> fileList) {
        return ResultUtil.success(orderLogisticsService.uploadFiles(fileList));
    }


    /**
     * 机构批量查询的订单物流信息--对外的api
     * @param orderLogisticsBatchQueryDTO
     * @return
     */
    @ApiOperation(value = "订单物流信息批量查询")
    @PostMapping("/getOrderLogisticsInfos")
    public BaseResponse getOrderLogisticsInfos(@RequestBody @ApiParam @Valid OrderLogisticsBatchQueryDTO orderLogisticsBatchQueryDTO) {
        return ResultUtil.success(orderLogisticsService.getOrderLogisticsInfos(orderLogisticsBatchQueryDTO));
    }


    /**
     * 对外提供的批量修改订单物流信息API接口
     * @param orderLogisticsDTO
     * @return
     */
    @ApiOperation(value = "批量修改订单物流信息接口")
    @PostMapping("/updateOrderLogisticsBatch")
    @CrossOrigin
    public BaseResponse updateOrderLogisticsBatch(@RequestBody @ApiParam @Valid OrderLogisticsBachDTO orderLogisticsDTO) {
        return ResultUtil.success(orderLogisticsService.updateOrderLogisticsBatch(orderLogisticsDTO));
    }
}
