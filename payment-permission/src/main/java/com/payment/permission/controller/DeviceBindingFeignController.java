package com.payment.permission.controller;

import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.DeviceBindingDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.DeviceBindingFeign;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备绑定管理FeignController管理接口
 */
@RestController
@Api(description = "设备绑定管理接口")
@RequestMapping("/devicebinding")
public class DeviceBindingFeignController extends BaseController {
    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private DeviceBindingFeign deviceBindingFeign;

    @ApiOperation(value = "新增设备绑定")
    @PostMapping("/addDeviceBinding")
    public BaseResponse addDeviceBinding(@RequestBody @ApiParam DeviceBindingDTO deviceBindingDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(deviceBindingDTO),
                "新增设备绑定"));
        return deviceBindingFeign.addDeviceBinding(deviceBindingDTO);
    }

    @ApiOperation(value = "解绑设备")
    @PostMapping("/banDeviceBinding")
    public BaseResponse banDeviceBinding(@RequestBody @ApiParam DeviceBindingDTO deviceBindingDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(deviceBindingDTO),
                "解绑设备"));
        return deviceBindingFeign.banDeviceBinding(deviceBindingDTO);
    }

    @ApiOperation(value = "查询设备绑定信息")
    @PostMapping("/pageDeviceBinding")
    public BaseResponse pageDeviceBinding(@RequestBody @ApiParam DeviceBindingDTO deviceBindingDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(deviceBindingDTO),
                "查询设备绑定信息"));
        return deviceBindingFeign.pageDeviceBinding(deviceBindingDTO);
    }
}
