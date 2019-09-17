package com.payment.permission.controller;
import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.OrderLogisticsDTO;
import com.payment.common.dto.OrderLogisticsQueryDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.trade.OrderLogisticsFeign;
import com.payment.permission.service.OperationLogService;
import com.payment.permission.service.OrderLogisticsFeignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *  订单物流信息相关服务
 */
@RestController
@Api(description = "订单物流信息接口")
@RequestMapping("/logistics")
public class OrderLogisticsFeignController extends BaseController {

    @Autowired
    private OrderLogisticsFeign orderLogisticsFeign;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private OrderLogisticsFeignService orderLogisticsFeignService;

    @Value("${file.tmpfile}")
    private String tmpfile;//springboot启动的临时文件存放

    @ApiOperation(value = "查询订单物流信息")
    @PostMapping("getOrderLogisticsInfo")
    public BaseResponse getOrderLogisticsInfo(@RequestBody @ApiParam OrderLogisticsQueryDTO orderLogisticsQueryDTO) {
        //添加操作日志
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(orderLogisticsQueryDTO),
                "查询订单物流信息"));
        return orderLogisticsFeign.getOrderLogisticsInfo(orderLogisticsQueryDTO);
    }

    @ApiOperation(value = "修改订单物流信息")
    @PostMapping("updateOrderLogistics")
    public BaseResponse updateOrderLogistics(@RequestBody @ApiParam OrderLogisticsDTO orderLogisticsDTO) {
        //添加操作日志
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(orderLogisticsDTO),
                "修改订单物流信息"));
        return orderLogisticsFeign.updateOrderLogistics(orderLogisticsDTO);
    }

    @ApiOperation(value = "导入订单物流信息")
    @PostMapping("/uploadOrderLogistics")
    public BaseResponse uploadOrderLogistics(@RequestParam("file") @ApiParam MultipartFile file) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, null,
                "导入订单物流信息"));
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(tmpfile);//指定临时文件路径，这个路径可以随便写
        factory.createMultipartConfig();
        return orderLogisticsFeign.uploadOrderLogistics(orderLogisticsFeignService.uploadFiles(file, this.getSysUserVO().getUsername()));
    }

}
