package com.payment.permission.feign.institution.impl;

import com.payment.common.dto.DeviceVendorDTO;
import com.payment.common.exception.BusinessException;
import com.payment.common.response.BaseResponse;
import com.payment.common.response.EResultEnum;
import com.payment.permission.feign.institution.DeviceVendorFeign;
import org.springframework.stereotype.Component;

/**
 * @author shenxinran
 * @Date: 2019/3/6 10:06
 * @Description: 设备厂商 Feign
 */
@Component
public class DeviceVendorFeignImpl implements DeviceVendorFeign {
    /**
     * 添加
     *
     * @param deviceVendorDTO
     * @return
     */
    @Override
    public BaseResponse addDeviceVendor(DeviceVendorDTO deviceVendorDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 启用禁用
     *
     * @param deviceVendorDTO
     * @return
     */
    @Override
    public BaseResponse banDeviceVendor(DeviceVendorDTO deviceVendorDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询
     *
     * @param deviceVendorDTO
     * @return
     */
    @Override
    public BaseResponse pageDeviceVendor(DeviceVendorDTO deviceVendorDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 更新
     *
     * @param deviceVendorDTO
     * @return
     */
    @Override
    public BaseResponse updateDeviceVendor(DeviceVendorDTO deviceVendorDTO) {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }

    /**
     * 查询设备型号类型
     *
     * @return
     */
    @Override
    public BaseResponse queryVendorCategory() {
        throw new BusinessException(EResultEnum.ERROR.getCode());
    }
}
