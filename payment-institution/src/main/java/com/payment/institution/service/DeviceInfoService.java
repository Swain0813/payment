package com.payment.institution.service;

import com.payment.common.base.BaseService;
import com.payment.common.dto.DeviceInfoDTO;
import com.payment.common.entity.DeviceInfo;
import com.payment.common.vo.DeviceInfoVO;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 设备信息管理 Service
 */
public interface DeviceInfoService extends BaseService<DeviceInfo> {
    /**
     * 新增设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    int addDeviceInfo(DeviceInfoDTO deviceInfoDTO);

    /**
     * 启用禁用设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    int banDeviceInfo(DeviceInfoDTO deviceInfoDTO);

    /**
     * 修改设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    int updateDeviceInfo(DeviceInfoDTO deviceInfoDTO);

    /**
     * 查询设备信息
     *
     * @param deviceInfoDTO
     * @return
     */
    PageInfo<DeviceInfoVO> pageDeviceInfo(DeviceInfoDTO deviceInfoDTO);

    /**
     * 导入设备信息
     *
     * @param fileList
     * @return
     */
    int uploadFiles(List<DeviceInfo> fileList);
}
