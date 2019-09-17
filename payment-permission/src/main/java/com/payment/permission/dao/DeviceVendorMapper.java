package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.DeviceVendor;
import org.springframework.stereotype.Repository;

/**
 * 设备厂商
 */

@Repository
public interface DeviceVendorMapper extends BaseMapper<DeviceVendor> {


    /**
     * 通过厂商名称查询ID
     *
     * @param name
     * @return
     */
    String selectByVendorName(String name);
}
