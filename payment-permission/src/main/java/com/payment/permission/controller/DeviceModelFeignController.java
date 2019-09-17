package com.payment.permission.controller;

import com.alibaba.fastjson.JSON;
import com.payment.common.base.BaseController;
import com.payment.common.constant.AsianWalletConstant;
import com.payment.common.dto.DeviceModelDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.DeviceModelFeign;
import com.payment.permission.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author shenxinran
 * @Date: 2019/3/6 11:03
 * @Description: 设备型号管理
 */
@RestController
@Api(description = "设备型号管理接口")
@RequestMapping("/devicemodel")
public class DeviceModelFeignController extends BaseController {

    @Autowired
    private DeviceModelFeign deviceModelFeign;

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation(value = "新增设备型号")
    @PostMapping("/addDeviceModel")
    public BaseResponse addDeviceModel(@RequestBody @ApiParam DeviceModelDTO deviceModelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.ADD, JSON.toJSONString(deviceModelDTO),
                "新增设备型号"));
        return deviceModelFeign.addDeviceModel(deviceModelDTO);
    }

    @ApiOperation(value = "启用禁用设备型号")
    @PostMapping("/banDeviceModel")
    public BaseResponse banDeviceModel(@RequestBody @ApiParam DeviceModelDTO deviceModelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(deviceModelDTO),
                "启用禁用设备型号"));
        return deviceModelFeign.banDeviceModel(deviceModelDTO);
    }

    @ApiOperation(value = "修改设备型号信息")
    @PostMapping("/updateDeviceModel")
    public BaseResponse updateDeviceModel(@RequestBody @ApiParam DeviceModelDTO deviceModelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.UPDATE, JSON.toJSONString(deviceModelDTO),
                "修改设备型号信息"));
        return deviceModelFeign.updateDeviceModel(deviceModelDTO);
    }

    @ApiOperation(value = "查询设备型号信息")
    @PostMapping("/pageDeviceModel")
    public BaseResponse pageDeviceModel(@RequestBody @ApiParam DeviceModelDTO deviceModelDTO) {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, JSON.toJSONString(deviceModelDTO),
                "查询设备型号信息"));
        return deviceModelFeign.pageDeviceModel(deviceModelDTO);
    }

    @ApiOperation(value = "查询厂商类别")
    @GetMapping("/queryModelCategory")
    public BaseResponse queryModelCategory() {
        operationLogService.addOperationLog(this.setOperationLog(this.getSysUserVO().getUsername(), AsianWalletConstant.SELECT, null,
                "查询厂商类别"));
        return deviceModelFeign.queryModelCategory();
    }

}
