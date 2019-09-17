package com.payment.task.dao;
import com.payment.common.base.BaseMapper;
import com.payment.common.entity.TcsStFlow;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 结算表的数据层
 */
@Repository
public interface TcsStFlowMapper extends BaseMapper<TcsStFlow> {

    /**
     * 查询未结算的妥投结算记录
     * @return
     */
    List<TcsStFlow> getTcsStFlows(String shouldSTtime);
}
