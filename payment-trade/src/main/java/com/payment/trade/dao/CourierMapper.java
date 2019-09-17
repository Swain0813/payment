package com.payment.trade.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.Courier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 运输商简码表的数据层
 */
@Repository
public interface CourierMapper extends BaseMapper<Courier> {
    /**
     * 获取所有的运输商
     * @return
     */
    List<Courier> getCouriers();
}
