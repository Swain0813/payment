package com.payment.permission.feign.institution;

import com.payment.common.dto.DeviceInfoDTO;
import com.payment.common.entity.DeviceInfo;
import com.payment.common.response.BaseResponse;
import com.payment.permission.feign.institution.impl.DeviceInfoFeignImpl;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author shenxinran
 * @Date: 2019/3/6 18:04
 * @Description: 设备信息Feign
 */
@FeignClient(value = "payment-institution", fallback = DeviceInfoFeignImpl.class)
public interface DeviceInfoFeign {
    /**
     * 添加设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    @PostMapping("/deviceinfo/addDeviceInfo")
    BaseResponse addDeviceInfo(@RequestBody @ApiParam DeviceInfoDTO deviceInfoDTO);

    /**
     * 启用禁用设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    @PostMapping("/deviceinfo/banDeviceInfo")
    BaseResponse banDeviceInfo(@RequestBody @ApiParam DeviceInfoDTO deviceInfoDTO);

    /**
     * 更新设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    @PostMapping("/deviceinfo/updateDeviceInfo")
    BaseResponse updateDeviceInfo(@RequestBody @ApiParam DeviceInfoDTO deviceInfoDTO);

    /**
     * 查询设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    @PostMapping("/deviceinfo/pageDeviceInfo")
    BaseResponse pageDeviceInfo(@RequestBody @ApiParam DeviceInfoDTO deviceInfoDTO);

    /**
     * 上传设备信息
     *
     * @param fileList
     * @return
     */
    @PostMapping("/deviceinfo/uploadDeviceInfo")
    BaseResponse uploadDeviceInfo(@RequestBody @ApiParam List<DeviceInfo> fileList);
}
