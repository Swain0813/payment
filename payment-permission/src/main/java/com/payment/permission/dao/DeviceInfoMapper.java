package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.DeviceInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 设备信息
 */
@Repository
public interface DeviceInfoMapper extends BaseMapper<DeviceInfo> {

    /**
     * 通过IMEL查询型号数
     *
     * @param imei
     * @return
     */
    int selectByIMEL(@Param("imei") Object imei);

    /**
     * 通过SN 查询型号数
     *
     * @param name
     * @return
     */
    int selectBySN(@Param("name") Object name);
}
