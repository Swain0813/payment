package com.payment.permission.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.entity.DeviceModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 设备型号
 */
@Repository
public interface DeviceModelMapper extends BaseMapper<DeviceModel> {


    /**
     * 通过型号名字查询型号格式
     *
     * @param name
     * @return
     */
    String selectByModelName(@Param("name") String name);
}
