package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.DeviceInfoDTO;
import com.payment.common.entity.DeviceInfo;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.DeviceInfoFeign;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author shenxinran
 * @Date: 2019/3/6 18:04
 * @Description: 设备信息 Feign
 */
@Component
public class DeviceInfoFeignImpl implements DeviceInfoFeign {
    /**
     * 添加设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    @Override
    public BaseResponse addDeviceInfo(DeviceInfoDTO deviceInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 启用禁用设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    @Override
    public BaseResponse banDeviceInfo(DeviceInfoDTO deviceInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 更新设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    @Override
    public BaseResponse updateDeviceInfo(DeviceInfoDTO deviceInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    @Override
    public BaseResponse pageDeviceInfo(DeviceInfoDTO deviceInfoDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 上传设备信息
     *
     * @param fileList
     * @return
     */
    @Override
    public BaseResponse uploadDeviceInfo(List<DeviceInfo> fileList) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
