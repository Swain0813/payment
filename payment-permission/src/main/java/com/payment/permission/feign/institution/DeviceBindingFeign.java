package com.payment.permission.feign.institution;

import com.payment.common.dto.DeviceBindingDTO;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.DeviceBindingFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 设备绑定Feign
 */
@FeignClient(value = "payment-institution", fallback = DeviceBindingFeignImpl.class)
public interface DeviceBindingFeign {

    /**
     * 添加设备绑定
     *
     * @param deviceBindingDTO
     * @return
     */
    @PostMapping("/devicebinding/addDeviceBinding")
    BaseResponse addDeviceBinding(@RequestBody @ApiParam DeviceBindingDTO deviceBindingDTO);

    /**
     * 接除设备绑定
     *
     * @param deviceBindingDTO
     * @return
     */
    @PostMapping("/devicebinding/banDeviceBinding")
    BaseResponse banDeviceBinding(@RequestBody @ApiParam DeviceBindingDTO deviceBindingDTO);

    /**
     * 查询设备绑定
     *
     * @param deviceBindingDTO
     * @return
     */
    @PostMapping("/devicebinding/pageDeviceBinding")
    BaseResponse pageDeviceBinding(@RequestBody @ApiParam DeviceBindingDTO deviceBindingDTO);
}
