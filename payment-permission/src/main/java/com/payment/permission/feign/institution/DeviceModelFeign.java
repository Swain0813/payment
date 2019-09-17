package com.payment.permission.feign.institution;

import com.payment.common.dto.DeviceModelDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.DeviceModelFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author shenxinran
 * @Date: 2019/3/6 14:23
 * @Description: 设备型号管理 Feign
 */

@FeignClient(value = "payment-institution", fallback = DeviceModelFeignImpl.class)
public interface DeviceModelFeign {

    /**
     * 添加设备型号
     *
     * @param deviceModelDTO
     * @return
     */
    @PostMapping("/devicemodel/addDeviceModel")
    BaseResponse addDeviceModel(@RequestBody @ApiParam DeviceModelDTO deviceModelDTO);

    /**
     * 启用警用禁用设备型号
     *
     * @param deviceModelDTO
     * @return
     */
    @PostMapping("/devicemodel/banDeviceModel")
    BaseResponse banDeviceModel(@RequestBody @ApiParam DeviceModelDTO deviceModelDTO);

    /**
     * 更新设备型号
     *
     * @param deviceModelDTO
     * @return
     */
    @PostMapping("/devicemodel/updateDeviceModel")
    BaseResponse updateDeviceModel(@RequestBody @ApiParam DeviceModelDTO deviceModelDTO);

    /**
     * 查询设备型号
     *
     * @param deviceModelDTO
     * @return
     */
    @PostMapping("/devicemodel/pageDeviceModel")
    BaseResponse pageDeviceModel(@RequestBody @ApiParam DeviceModelDTO deviceModelDTO);

    /**
     * 查询设备类别
     *
     * @return
     */
    @GetMapping("/devicemodel/queryModelCategory")
    BaseResponse queryModelCategory();
}
