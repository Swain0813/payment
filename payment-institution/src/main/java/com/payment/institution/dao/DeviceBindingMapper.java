package com.payment.institution.dao;

import com.payment.common.base.BaseMapper;
import com.payment.common.dto.DeviceBindingDTO;
import com.payment.common.vo.DeviceBindingVO;
import com.payment.common.entity.DeviceBinding;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 设备绑定
 */
@Repository
public interface DeviceBindingMapper extends BaseMapper<DeviceBinding> {


    /**
     * 查询设备信息
     *
     * @param deviceBindingDTO
     * @return
     */
    List<DeviceBindingVO> pageDeviceBinding(DeviceBindingDTO deviceBindingDTO);
}
